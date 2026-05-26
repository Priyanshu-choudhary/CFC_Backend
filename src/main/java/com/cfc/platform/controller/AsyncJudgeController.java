package com.cfc.platform.controller;

import com.cfc.platform.Pojo.execution.ExecutionJob;
import com.cfc.platform.Service.ExecutionJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Async code execution endpoints.
 *
 * These sit alongside the existing sync /judge/run and /judge/submit endpoints.
 * The frontend can use either; async is preferred for production (non-blocking).
 *
 * ── API ───────────────────────────────────────────────────────────────────────
 *
 *   POST /judge/run-async
 *     Body: { sourceCode, language, stdin? }
 *     Returns 202: { jobId, status: "QUEUED", pollUrl }
 *
 *   POST /judge/submit-async
 *     Body: { sourceCode, language, testCases: {input→expected}, timeLimitSeconds?, memoryLimitKb? }
 *     Returns 202: { jobId, status: "QUEUED", pollUrl }
 *
 *   GET /judge/result/{jobId}
 *     Returns 200: { status, jobId, updatedAt, result?: {...} }
 *     Returns 404: if jobId expired (> 1h) or never existed
 *
 * ── Client polling strategy ───────────────────────────────────────────────────
 *   Poll every 1s until status is DONE | ERROR | TIMEOUT.
 *   QUEUED  → job is waiting in SQS queue (no worker picked it up yet)
 *   RUNNING → worker is executing the code right now
 *   DONE    → execution finished, result.stdout / result.stderr are populated
 *   ERROR   → something went wrong, error field explains why
 *   TIMEOUT → hard wall-time kill by nsjail (5s default)
 */
@RestController
@RequestMapping("/judge")
public class AsyncJudgeController {

    private static final Logger log = LoggerFactory.getLogger(AsyncJudgeController.class);

    @Autowired
    private ExecutionJobService jobService;

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Async playground run.
     * Queues the job and returns jobId immediately — never blocks on execution.
     */
    @PostMapping("/run-async")
    public ResponseEntity<?> runAsync(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ExecutionJob job = new ExecutionJob();
        job.setType("run");
        job.setSourceCode(body.get("sourceCode"));
        job.setLanguage(body.get("language"));
        job.setStdin(body.get("stdin"));
        job.setUserId(userId);

        return enqueue(job);
    }

    /**
     * Async submit against test cases.
     * Same contract as /judge/submit but returns jobId immediately.
     */
    @PostMapping("/submit-async")
    public ResponseEntity<?> submitAsync(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ExecutionJob job = new ExecutionJob();
        job.setType("submit");
        job.setSourceCode((String) body.get("sourceCode"));
        job.setLanguage((String) body.get("language"));
        job.setTestCases((Map<String, String>) body.get("testCases"));
        job.setUserId(userId);

        if (body.get("timeLimitSeconds") != null) {
            job.setTimeLimitSeconds(((Number) body.get("timeLimitSeconds")).doubleValue());
        }
        if (body.get("memoryLimitKb") != null) {
            job.setMemoryLimitKb(((Number) body.get("memoryLimitKb")).intValue());
        }

        return enqueue(job);
    }

    /**
     * Poll for result.
     *
     * The client calls this every ~1s after receiving a jobId.
     * Once status = DONE | ERROR | TIMEOUT the client stops polling.
     *
     * HTTP 200 — job exists (any status including QUEUED/RUNNING)
     * HTTP 404 — jobId expired (TTL 1hr) or invalid
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<?> getResult(@PathVariable String jobId) {
        Map<String, Object> result = jobService.getResult(jobId);

        if ("NOT_FOUND".equals(result.get("status"))) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<?> enqueue(ExecutionJob job) {
        if (job.getSourceCode() == null || job.getSourceCode().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "sourceCode is required"));
        }
        if (job.getLanguage() == null || job.getLanguage().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "language is required"));
        }

        try {
            String jobId = jobService.submitJob(job);
            log.info("Accepted async job jobId={} lang={} type={}", jobId, job.getLanguage(), job.getType());
            return ResponseEntity.accepted().body(Map.of(
                    "jobId",   jobId,
                    "status",  "QUEUED",
                    "pollUrl", "/judge/result/" + jobId
            ));
        } catch (Exception e) {
            log.error("Failed to enqueue job lang={} type={}", job.getLanguage(), job.getType(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to queue execution: " + e.getMessage()));
        }
    }
}
