package com.cfc.platform.controller;

import com.cfc.platform.Service.CodeExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/judge")
public class Judge0Controller {

    private static final Logger log = LoggerFactory.getLogger(Judge0Controller.class);

    /**
     * Injected by interface — Spring wires whichever provider is active
     * (judge0 or goboxd) based on the {@code code.execution.provider} property.
     */
    @Autowired
    private CodeExecutionService codeExecutionService;

    /**
     * POST /judge/run
     * Playground run â€” no expected output check, no login required.
     * Body: { "sourceCode": "...", "language": "java", "stdin": "5\n3" }
     */
    @PostMapping("/run")
    public ResponseEntity<?> runCode(@RequestBody Map<String, String> request) {
        log.info("Run request: language={}", request.get("language"));
        Map<String, Object> result = codeExecutionService.runCode(
                request.get("sourceCode"),
                request.get("language"),
                request.get("stdin")
        );
        return ResponseEntity.ok(result);
    }

    /**
     * POST /judge/submit
     * Submit against test cases. Requires JWT auth.
     * Body: {
     *   "sourceCode": "...",
     *   "language": "java",
     *   "testCases": { "input1": "expected1", "input2": "expected2" },
     *   "timeLimitSeconds": 2.0,   // optional
     *   "memoryLimitKb": 262144    // optional
     * }
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitCode(@RequestBody Map<String, Object> request) {
        log.info("Submit request: language={}", request.get("language"));

        Double timeLimit = request.get("timeLimitSeconds") != null
                ? ((Number) request.get("timeLimitSeconds")).doubleValue() : null;
        Integer memLimit = request.get("memoryLimitKb") != null
                ? ((Number) request.get("memoryLimitKb")).intValue() : null;

        Map<String, Object> result = codeExecutionService.submitWithTestCases(
                (String) request.get("sourceCode"),
                (String) request.get("language"),
                (Map<String, String>) request.get("testCases"),
                timeLimit,
                memLimit
        );
        return ResponseEntity.ok(result);
    }

    /**
     * GET /judge/languages
     * Returns all Judge0-supported languages with their IDs.
     */
    @GetMapping("/languages")
    public ResponseEntity<List<Map<String, Object>>> getLanguages() {
        return ResponseEntity.ok(codeExecutionService.getLanguages());
    }
}
