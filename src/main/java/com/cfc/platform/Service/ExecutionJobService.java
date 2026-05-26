package com.cfc.platform.Service;

import com.cfc.platform.Pojo.execution.ExecutionJob;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Duration;
import java.util.*;

/**
 * Core service for the async code-execution pipeline.
 *
 * ── Submit flow ──────────────────────────────────────────────────────────────
 *   1. Generate a UUID jobId
 *   2. Write   job:{jobId}  →  { status:QUEUED, jobId, queuedAt }  in Redis (TTL 1h)
 *      Why write Redis BEFORE SQS? If the client polls immediately after getting
 *      the jobId back, it gets QUEUED instead of NOT_FOUND.
 *   3. Serialize the ExecutionJob to JSON and publish to SQS
 *   4. Return jobId  — entire method returns in < 50ms, never blocks on execution
 *
 * ── Result flow ──────────────────────────────────────────────────────────────
 *   Client polls GET /judge/result/{jobId}
 *   → HGETALL job:{jobId} from Redis
 *   → goboxd worker wrote RUNNING → DONE/ERROR/TIMEOUT to same key
 *   → return current state to client
 *
 * ── Redis key schema ─────────────────────────────────────────────────────────
 *   Key    : job:{jobId}           (Hash, TTL = 3600s)
 *   Fields :
 *     jobId     — echoed back so client doesn't need to track it separately
 *     status    — QUEUED | RUNNING | DONE | ERROR | TIMEOUT
 *     result    — JSON string of the execution result (stdout, stderr, etc.)
 *     error     — error message if status=ERROR
 *     updatedAt — epoch millis of last write
 *
 *   NOTE: both this class (Java) and the goboxd worker (Go) write to the SAME
 *   key schema. They must stay in sync. The Go struct is in goboxd-worker/models/job.go.
 */
@Service
public class ExecutionJobService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionJobService.class);

    private static final String KEY_PREFIX      = "job:";
    private static final long   JOB_TTL_SECONDS = 3600; // 1 hour — jobs auto-expire from Redis

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;   // Spring Boot auto-configures Jackson

    @Value("${aws.sqs.jobs-queue-url:}")
    private String jobsQueueUrl;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Enqueues a code execution job.
     *
     * @return jobId the client should use to poll for the result
     * @throws RuntimeException if SQS publish fails (Redis status will be ERROR)
     */
    public String submitJob(ExecutionJob job) {
        String jobId = UUID.randomUUID().toString();
        job.setJobId(jobId);
        job.setQueuedAt(System.currentTimeMillis());

        // Step 1: Pre-create the Redis key so the first poll returns QUEUED,
        // not NOT_FOUND.
        writeRedis(jobId, "QUEUED", null, null);

        // Step 2: Push job to SQS.
        try {
            String messageBody = objectMapper.writeValueAsString(job);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(jobsQueueUrl)
                    .messageBody(messageBody)
                    // Message attributes let the worker skip JSON parsing for routing.
                    .messageAttributes(Map.of(
                            "jobId", strAttr(jobId),
                            "type",  strAttr(job.getType()),
                            "lang",  strAttr(job.getLanguage())
                    ))
                    .build());

            log.info("Job queued → SQS  jobId={} lang={} type={}", jobId, job.getLanguage(), job.getType());

        } catch (Exception e) {
            // Mark the job as ERROR in Redis so polling clients get a meaningful
            // response rather than waiting forever.
            writeRedis(jobId, "ERROR", null, "Failed to queue: " + e.getMessage());
            throw new RuntimeException("SQS publish failed for job " + jobId, e);
        }

        return jobId;
    }

    /**
     * Fetches the current state of a job from Redis.
     *
     * The "result" field is stored as a JSON string by the goboxd worker.
     * We deserialize it here so the HTTP response contains a proper nested
     * object (not a string), matching the shape of the existing sync endpoints.
     *
     * Possible return shapes:
     *   { status: "QUEUED",  jobId, updatedAt }
     *   { status: "RUNNING", jobId, updatedAt }
     *   { status: "DONE",    jobId, updatedAt, result: { stdout, stderr, ... } }
     *   { status: "ERROR",   jobId, updatedAt, error: "..." }
     *   { status: "TIMEOUT", jobId, updatedAt }
     *   { status: "NOT_FOUND", jobId }   ← expired or never created
     */
    public Map<String, Object> getResult(String jobId) {
        String key = KEY_PREFIX + jobId;
        Map<Object, Object> raw = redis.opsForHash().entries(key);

        if (raw.isEmpty()) {
            return Map.of("status", "NOT_FOUND", "jobId", jobId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        raw.forEach((k, v) -> response.put(k.toString(), v));

        // Deserialize the nested result JSON string into a Map so the HTTP
        // response is clean JSON, not a JSON-encoded string inside JSON.
        if (response.containsKey("result")) {
            try {
                String resultJson = response.get("result").toString();
                response.put("result", objectMapper.readValue(
                        resultJson,
                        new TypeReference<Map<String, Object>>() {}
                ));
            } catch (Exception e) {
                log.warn("Could not parse result JSON for job {} — returning as string", jobId);
                // Leave as string rather than crashing the poll endpoint.
            }
        }

        return response;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Writes (or updates) the job:{jobId} hash in Redis.
     * Uses HSET (not SET) so individual fields can be updated without
     * overwriting the whole key — e.g. status QUEUED→RUNNING only touches
     * the "status" and "updatedAt" fields, leaving "jobId" intact.
     */
    private void writeRedis(String jobId, String status,
                             Map<String, Object> result, String error) {
        String key = KEY_PREFIX + jobId;
        Map<String, String> fields = new LinkedHashMap<>();

        fields.put("jobId",     jobId);
        fields.put("status",    status);
        fields.put("updatedAt", String.valueOf(System.currentTimeMillis()));

        if (error != null && !error.isBlank()) {
            fields.put("error", error);
        }
        if (result != null) {
            try {
                fields.put("result", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                log.error("Failed to serialize result for job {}", jobId, e);
            }
        }

        // HSET all fields atomically, then reset TTL.
        // Always resetting TTL on every write means a long-running job
        // won't expire mid-execution.
        redis.opsForHash().putAll(key, fields);
        redis.expire(key, Duration.ofSeconds(JOB_TTL_SECONDS));
    }

    private MessageAttributeValue strAttr(String value) {
        return MessageAttributeValue.builder()
                .stringValue(value != null ? value : "")
                .dataType("String")
                .build();
    }
}
