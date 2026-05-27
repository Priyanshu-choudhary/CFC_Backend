package com.cfc.platform.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * goboxd (self-hosted nsjail sandbox) implementation of
 * {@link CodeExecutionService}.
 *
 * This is the ONLY implementation now — Judge0 was removed in favour of the
 * self-hosted backend.  Kept the {@link CodeExecutionService} interface in
 * place so future swaps (sandbox engines, etc.) remain a one-bean change.
 *
 * goboxd exposes a single synchronous {@code POST /run} endpoint that both
 * compiles and runs the code against optional test cases, returning the full
 * result in one call. This class translates CFC's request shape into goboxd's
 * and maps goboxd's response back into the same Map shape the frontend already
 * consumes, so the controller and UI are unaware of the engine swap.
 */
@Service
public class GoboxdService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(GoboxdService.class);

    @Value("${goboxd.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String providerName() {
        return "goboxd";
    }

    /** CFC language alias -> goboxd registered language name. */
    private static final Map<String, String> LANGUAGE_MAP = Map.ofEntries(
            Map.entry("java",       "java"),
            Map.entry("python",     "python"),
            Map.entry("python3",    "python"),
            Map.entry("python2",    "python"),
            Map.entry("c",          "c"),
            Map.entry("cpp",        "cpp"),
            Map.entry("c++",        "cpp"),
            Map.entry("javascript", "node"),
            Map.entry("nodejs",     "node"),
            Map.entry("node",       "node"),
            Map.entry("bash",       "bash"),
            Map.entry("sh",         "bash"),
            Map.entry("rust",       "rust"),
            Map.entry("go",         "go"),
            Map.entry("verilog",    "verilog")
    );

    private String mapLanguage(String language) {
        if (language == null) return null;
        return LANGUAGE_MAP.get(language.toLowerCase());
    }

    // ---------------------------------------------------------------------
    // Playground run
    // ---------------------------------------------------------------------

    @Override
    public Map<String, Object> runCode(String sourceCode, String language, String stdin) {
        String lang = mapLanguage(language);
        if (lang == null) {
            return Map.of("error", "Unsupported language for goboxd: " + language);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("language", lang);
        body.put("source", sourceCode);
        if (stdin != null && !stdin.isBlank()) {
            // No expected output -> goboxd does a single smoke run with this stdin.
            List<Map<String, Object>> tcs = new ArrayList<>();
            Map<String, Object> tc = new HashMap<>();
            tc.put("input", stdin);
            tc.put("expected_output", ""); // ignored on the smoke path
            tcs.add(tc);
            body.put("test_cases", tcs);
        }

        try {
            Map<String, Object> resp = postRun(body);
            return mapPlaygroundResult(resp);
        } catch (Exception e) {
            log.error("goboxd runCode error: {}", e.getMessage());
            return Map.of("error", "Execution failed: " + e.getMessage());
        }
    }

    private Map<String, Object> mapPlaygroundResult(Map<String, Object> resp) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> build = asMap(resp.get("build"));
        List<Map<String, Object>> tests = asList(resp.get("tests"));
        String topStatus = (String) resp.getOrDefault("status", "internal_error");

        // Compilation failure: surface the compiler stderr like Judge0's
        // compile_output field.
        if (build != null && !"ok".equals(build.get("status"))) {
            out.put("stdout", "");
            out.put("stderr", "");
            out.put("compile_output", build.getOrDefault("stderr", ""));
            JudgeStatus js = mapStatus("build_failed");
            out.put("statusDescription", js.description);
            out.put("statusId", js.id);
            out.put("time", null);
            out.put("memory", null);
            return out;
        }

        Map<String, Object> t = (tests != null && !tests.isEmpty()) ? tests.get(0) : Map.of();
        String testStatus = (String) t.getOrDefault("status", topStatus);

        // Playground mode has no "expected answer". We pass stdin to goboxd as a
        // single test case with an empty expected_output, so goboxd reports
        // wrong_output / output_whitespace_mismatch whenever the program prints
        // anything. For a playground run that is irrelevant: if the code merely
        // executed, report Accepted (this mirrors Judge0's playground behaviour).
        // Genuine failures (runtime/time/memory/compile) still surface.
        String effective = switch (testStatus) {
            case "wrong_output", "output_whitespace_mismatch" -> "accepted";
            default -> testStatus;
        };
        JudgeStatus js = mapStatus(effective);

        out.put("stdout", t.getOrDefault("stdout", ""));
        out.put("stderr", t.getOrDefault("stderr", ""));
        out.put("compile_output", null);
        out.put("statusDescription", js.description);
        out.put("statusId", js.id);
        out.put("time", msToSeconds(t.get("duration_ms")));
        out.put("memory", t.get("memory_kb"));
        return out;
    }

    // ---------------------------------------------------------------------
    // Submit against test cases
    // ---------------------------------------------------------------------

    @Override
    public Map<String, Object> submitWithTestCases(
            String sourceCode,
            String language,
            Map<String, String> testCases,
            Double timeLimitSeconds,
            Integer memoryLimitKb) {

        String lang = mapLanguage(language);
        if (lang == null) {
            return Map.of("error", "Unsupported language for goboxd: " + language);
        }
        if (testCases == null || testCases.isEmpty()) {
            return Map.of("error", "No test cases supplied");
        }

        List<String> inputOrder = new ArrayList<>(testCases.keySet());

        List<Map<String, Object>> tcList = new ArrayList<>();
        for (String input : inputOrder) {
            Map<String, Object> tc = new HashMap<>();
            tc.put("input", input);
            tc.put("expected_output", testCases.get(input));
            if (timeLimitSeconds != null) {
                tc.put("time_limit_s", (int) Math.ceil(timeLimitSeconds));
            }
            if (memoryLimitKb != null) {
                tc.put("memory_kb", memoryLimitKb);
            }
            tcList.add(tc);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("language", lang);
        body.put("source", sourceCode);
        body.put("test_cases", tcList);

        try {
            Map<String, Object> resp;
            try {
                resp = postRun(body);
            } catch (RestClientResponseException e) {
                // goboxd rejects per-test overrides that exceed a language's
                // configured maximum (HTTP 400 override_too_big). Retry once
                // using the language defaults so a too-aggressive problem limit
                // never hard-fails a submission.
                if (e.getStatusCode().value() == 400) {
                    log.warn("goboxd 400 ({}), retrying without per-test overrides", e.getMessage());
                    for (Map<String, Object> tc : tcList) {
                        tc.remove("time_limit_s");
                        tc.remove("memory_kb");
                    }
                    resp = postRun(body);
                } else {
                    throw e;
                }
            }
            return mapBatchResult(resp, inputOrder, testCases);
        } catch (Exception e) {
            log.error("goboxd submit error: {}", e.getMessage());
            return Map.of("error", "Batch execution failed: " + e.getMessage());
        }
    }

    private Map<String, Object> mapBatchResult(
            Map<String, Object> resp,
            List<String> inputOrder,
            Map<String, String> testCases) {

        Map<String, Object> build = asMap(resp.get("build"));
        List<Map<String, Object>> tests = asList(resp.get("tests"));

        // A build failure means no test actually ran. Mirror Judge0 by marking
        // every test as a Compilation Error and attaching the compiler output.
        boolean buildFailed = build != null && !"ok".equals(build.get("status"));
        String compileError = buildFailed ? String.valueOf(build.getOrDefault("stderr", "")) : null;

        boolean allPassed = true;
        List<Map<String, Object>> tcResults = new ArrayList<>();

        for (int i = 0; i < inputOrder.size(); i++) {
            String input = inputOrder.get(i);
            Map<String, Object> t = (tests != null && i < tests.size()) ? tests.get(i) : Map.of();
            String status = (String) t.getOrDefault("status", buildFailed ? "build_failed" : "internal_error");

            JudgeStatus js = mapStatus(status);
            boolean passed = "accepted".equals(status);
            if (!passed) allPassed = false;

            Map<String, Object> tc = new LinkedHashMap<>();
            tc.put("testCaseNumber", i + 1);
            tc.put("input", input);
            tc.put("expectedOutput", testCases.get(input));
            tc.put("actualOutput", t.get("stdout") != null ? String.valueOf(t.get("stdout")).trim() : "");
            tc.put("status", js.description);
            tc.put("statusId", js.id);
            tc.put("passed", passed);
            tc.put("time", msToSeconds(t.get("duration_ms")));
            tc.put("memory", t.get("memory_kb"));
            if (compileError != null && !compileError.isBlank()) {
                tc.put("compileError", compileError);
            }
            Object stderr = t.get("stderr");
            if (stderr != null && !String.valueOf(stderr).isBlank()) {
                tc.put("stderr", stderr);
            }
            tcResults.add(tc);
        }

        long passedCount = tcResults.stream().filter(t -> (Boolean) t.get("passed")).count();
        return Map.of(
                "allPassed", allPassed,
                "totalTestCases", tcResults.size(),
                "passedCount", passedCount,
                "results", tcResults
        );
    }

    // ---------------------------------------------------------------------
    // Languages
    // ---------------------------------------------------------------------

    @Override
    public List<Map<String, Object>> getLanguages() {
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(baseUrl + "/info", Map.class);
            Object langs = resp.getBody() != null ? resp.getBody().get("languages") : null;
            if (!(langs instanceof List<?> list)) return List.of();

            List<Map<String, Object>> out = new ArrayList<>();
            for (Object name : list) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", name);
                entry.put("name", name);
                out.add(entry);
            }
            return out;
        } catch (Exception e) {
            log.warn("goboxd /info languages error: {}", e.getMessage());
            return List.of();
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> postRun(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/run", HttpMethod.POST, entity, Map.class);
        return resp.getBody() != null ? resp.getBody() : Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object o) {
        return (o instanceof List) ? (List<Map<String, Object>>) o : null;
    }

    /** goboxd duration_ms (Number) -> Judge0-style seconds string e.g. "0.012". */
    private String msToSeconds(Object durationMs) {
        if (!(durationMs instanceof Number n)) return null;
        return String.format(Locale.ROOT, "%.3f", n.doubleValue() / 1000.0);
    }

    /**
     * Map a goboxd per-test status to a Judge0-compatible (id, description)
     * pair so any frontend that keys off Judge0 status IDs keeps working.
     * Judge0 IDs: 3=Accepted, 4=Wrong Answer, 5=TLE, 6=Compilation Error,
     * 7=Runtime(SIGSEGV), 11=Runtime(Other), 13=Internal Error.
     */
    private JudgeStatus mapStatus(String goboxdStatus) {
        if (goboxdStatus == null) return new JudgeStatus(13, "Internal Error");
        return switch (goboxdStatus) {
            case "accepted"                   -> new JudgeStatus(3,  "Accepted");
            case "wrong_output"               -> new JudgeStatus(4,  "Wrong Answer");
            case "output_whitespace_mismatch" -> new JudgeStatus(4,  "Wrong Answer (Presentation)");
            case "time_exceeded"              -> new JudgeStatus(5,  "Time Limit Exceeded");
            case "memory_exceeded"            -> new JudgeStatus(7,  "Memory Limit Exceeded");
            case "runtime_error"              -> new JudgeStatus(11, "Runtime Error");
            case "build_failed"               -> new JudgeStatus(6,  "Compilation Error");
            case "not_executed"               -> new JudgeStatus(13, "Not Executed");
            default                            -> new JudgeStatus(13, "Internal Error");
        };
    }

    private record JudgeStatus(int id, String description) {}
}
