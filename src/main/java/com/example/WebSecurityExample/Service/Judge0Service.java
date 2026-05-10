package com.example.WebSecurityExample.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);

    @Value("${judge0.api.key}")
    private String apiKey;

    @Value("${judge0.api.host}")
    private String apiHost;

    @Value("${judge0.api.base-url}")
    private String baseUrl;

    private static final Map<String, Integer> LANGUAGE_IDS = Map.ofEntries(
        Map.entry("java",       62),
        Map.entry("python3",    71),
        Map.entry("python2",    70),
        Map.entry("python",     71),
        Map.entry("c",          50),
        Map.entry("cpp",        54),
        Map.entry("c++",        54),
        Map.entry("javascript", 63),
        Map.entry("nodejs",     63),
        Map.entry("typescript", 74),
        Map.entry("kotlin",     78),
        Map.entry("rust",       73),
        Map.entry("php",        68),
        Map.entry("bash",       46),
        Map.entry("sh",         46),
        Map.entry("scala",      81)
    );

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-rapidapi-host", apiHost);
        headers.set("x-rapidapi-key", apiKey);
        return headers;
    }

    /**
     * Free-run playground — no expected output check.
     * Uses ?wait=true for synchronous single-call response.
     */
    public Map<String, Object> runCode(String sourceCode, String language, String stdin) {
        RestTemplate restTemplate = new RestTemplate();
        Integer langId = LANGUAGE_IDS.getOrDefault(language.toLowerCase(), 62);

        Map<String, Object> body = new HashMap<>();
        body.put("source_code", sourceCode);
        body.put("language_id", langId);
        if (stdin != null && !stdin.isBlank()) {
            body.put("stdin", stdin);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        String url = baseUrl + "/submissions?base64_encoded=false&wait=true&fields=stdout,stderr,compile_output,status,time,memory";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return enrichResult(response.getBody());
        } catch (Exception e) {
            log.error("Judge0 runCode error: {}", e.getMessage());
            return Map.of("error", "Execution failed: " + e.getMessage());
        }
    }

    /**
     * Submit code against all test cases using Judge0 batch API.
     * testCases: Map of stdin → expectedOutput pairs.
     * timeLimitSeconds / memoryLimitKb: per-problem limits (nullable → defaults).
     */
    public Map<String, Object> submitWithTestCases(
            String sourceCode,
            String language,
            Map<String, String> testCases,
            Double timeLimitSeconds,
            Integer memoryLimitKb) {

        RestTemplate restTemplate = new RestTemplate();
        Integer langId = LANGUAGE_IDS.getOrDefault(language.toLowerCase(), 62);

        List<Map<String, Object>> submissions = new ArrayList<>();
        List<String> inputOrder = new ArrayList<>(testCases.keySet());

        for (String input : inputOrder) {
            Map<String, Object> sub = new HashMap<>();
            sub.put("source_code", sourceCode);
            sub.put("language_id", langId);
            sub.put("stdin", input);
            sub.put("expected_output", testCases.get(input));
            sub.put("cpu_time_limit", timeLimitSeconds != null ? timeLimitSeconds : 5.0);
            sub.put("memory_limit", memoryLimitKb != null ? memoryLimitKb : 262144);
            submissions.add(sub);
        }

        Map<String, Object> batchBody = Map.of("submissions", submissions);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(batchBody, buildHeaders());
        String batchUrl = baseUrl + "/submissions/batch?base64_encoded=false";

        try {
            ResponseEntity<List> tokenResponse = restTemplate.exchange(
                    batchUrl, HttpMethod.POST, entity, List.class);
            List<Map<String, String>> tokenList = tokenResponse.getBody();

            if (tokenList == null || tokenList.isEmpty()) {
                return Map.of("error", "No tokens returned from Judge0");
            }

            String tokens = tokenList.stream()
                    .map(t -> t.get("token"))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            String resultsUrl = baseUrl + "/submissions/batch?tokens=" + tokens
                    + "&base64_encoded=false&fields=token,stdout,stderr,compile_output,status,time,memory,stdin,expected_output";

            List<Map<String, Object>> results = pollBatchResults(restTemplate, resultsUrl);
            return buildBatchResponse(results, inputOrder, testCases);

        } catch (Exception e) {
            log.error("Judge0 batch submit error: {}", e.getMessage());
            return Map.of("error", "Batch execution failed: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> pollBatchResults(RestTemplate restTemplate, String url)
            throws InterruptedException {
        int maxAttempts = 20;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), Map.class);
            List<Map<String, Object>> subs =
                    (List<Map<String, Object>>) response.getBody().get("submissions");

            boolean allDone = subs.stream().allMatch(s -> {
                Map<String, Object> status = (Map<String, Object>) s.get("status");
                int id = ((Number) status.get("id")).intValue();
                return id != 1 && id != 2; // 1=InQueue 2=Processing
            });

            if (allDone) return subs;
            Thread.sleep(500);
        }
        throw new RuntimeException("Judge0 polling timeout after 10 seconds");
    }

    private Map<String, Object> buildBatchResponse(
            List<Map<String, Object>> results,
            List<String> inputOrder,
            Map<String, String> testCases) {

        boolean allPassed = true;
        List<Map<String, Object>> tcResults = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> r = results.get(i);
            Map<String, Object> status = (Map<String, Object>) r.get("status");
            int statusId = ((Number) status.get("id")).intValue();
            String statusDesc = (String) status.get("description");

            String input = inputOrder.get(i);
            boolean passed = (statusId == 3);
            if (!passed) allPassed = false;

            Map<String, Object> tc = new LinkedHashMap<>();
            tc.put("testCaseNumber", i + 1);
            tc.put("input", input);
            tc.put("expectedOutput", testCases.get(input));
            tc.put("actualOutput", r.get("stdout") != null ? ((String) r.get("stdout")).trim() : "");
            tc.put("status", statusDesc);
            tc.put("statusId", statusId);
            tc.put("passed", passed);
            tc.put("time", r.get("time"));
            tc.put("memory", r.get("memory"));
            if (r.get("compile_output") != null && !((String) r.get("compile_output")).isBlank()) {
                tc.put("compileError", r.get("compile_output"));
            }
            if (r.get("stderr") != null && !((String) r.get("stderr")).isBlank()) {
                tc.put("stderr", r.get("stderr"));
            }
            tcResults.add(tc);
        }

        long passedCount = tcResults.stream().filter(t -> (Boolean) t.get("passed")).count();
        return Map.of(
                "allPassed", allPassed,
                "totalTestCases", results.size(),
                "passedCount", passedCount,
                "results", tcResults
        );
    }

    private Map<String, Object> enrichResult(Map rawResult) {
        if (rawResult == null) return Map.of("error", "No response from Judge0");
        Map<String, Object> result = new LinkedHashMap<>(rawResult);
        if (rawResult.get("status") instanceof Map statusMap) {
            result.put("statusDescription", statusMap.get("description"));
            result.put("statusId", statusMap.get("id"));
        }
        return result;
    }

    public List<Map<String, Object>> getLanguages() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders());
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/languages", HttpMethod.GET, entity, List.class);
        return response.getBody();
    }
}
