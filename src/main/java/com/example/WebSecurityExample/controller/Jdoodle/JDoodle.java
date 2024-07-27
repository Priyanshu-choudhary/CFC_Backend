package com.example.WebSecurityExample.controller.Jdoodle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@RestController
@RequestMapping("/code")
@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class JDoodle {

    private static final Logger logger = LoggerFactory.getLogger(JDoodle.class);

    @Value("${jdoodle.clientId}")
    private String clientId;

    @Value("${jdoodle.clientSecret}")
    private String clientSecret;

    private static final String JDoodle_API_URL = "https://api.jdoodle.com/v1/execute";

    @PostMapping("/execute")
    public ResponseEntity<?> executeCode(@RequestBody Map<String, String> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
//        logger.info("Executing script: {}", requestBody.get("script"));
//        logger.info("Using language: {}", requestBody.get("language"));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("clientId", clientId);
        body.put("clientSecret", clientSecret);
        body.put("script", requestBody.get("script"));
        body.put("language", requestBody.get("language"));
        body.put("versionIndex", "3");
        body.put("stdin", requestBody.get("stdin"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(JDoodle_API_URL, HttpMethod.POST, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error executing code");
        }
    }

    @PostMapping("/executeWithTestcase")
    public ResponseEntity<?> executeCodeWithTestCases(@RequestBody Map<String, Object> requestBody) {
//        logger.info("Received request to execute code with test cases: {}", requestBody);

        String script = (String) requestBody.get("script");
        String language = (String) requestBody.get("language");
        Map<String, String> testCases = (Map<String, String>) requestBody.get("testCases");

        if (testCases == null) {
//            logger.error("Test cases are null");
            return ResponseEntity.badRequest().body("Test cases cannot be null");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("clientId", clientId);
        body.put("clientSecret", clientSecret);
        body.put("script", script);
        body.put("language", language);
        body.put("versionIndex", "3");

        Map<String, Object> results = new HashMap<>();
        boolean allPassed = true;

//        logger.info("Executing script: {}", script);
//        logger.info("Using language: {}", language);
//        logger.info("Number of test cases: {}", testCases.size());

        for (Entry<String, String> testCase : testCases.entrySet()) {
            String input = testCase.getKey();
            String expectedOutput = testCase.getValue();

//            logger.info("Executing test case with input: {}", input);
//            logger.info("Expected output: {}", expectedOutput);

            body.put("stdin", input);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            try {
                ResponseEntity<Map> response = restTemplate.exchange(JDoodle_API_URL, HttpMethod.POST, entity, Map.class);
                Map responseBody = response.getBody();
                if (responseBody != null) {
                    String actualOutput = (String) responseBody.get("output");
//                    logger.info("Actual output: {}", actualOutput);
                    boolean passed = expectedOutput.trim().equals(actualOutput != null ? actualOutput.trim() : "");
                    Map<String, Object> testCaseResult = new HashMap<>();
                    testCaseResult.put("Input", input);
                    testCaseResult.put("ExpectedOutput", expectedOutput);
                    testCaseResult.put("ActualOutput", actualOutput);
                    testCaseResult.put("Status", passed ? "Passed" : "Failed");
                    results.put(input, testCaseResult);
                    if (!passed) {
                        allPassed = false;
                        break;  // Stop executing further test cases if one fails
                    }
                } else {
//                    logger.error("No response received for input: {}", input);
                    Map<String, Object> testCaseResult = new HashMap<>();
                    testCaseResult.put("Input", input);
                    testCaseResult.put("ExpectedOutput", expectedOutput);
                    testCaseResult.put("ActualOutput", "Error: No response");
                    testCaseResult.put("Status", "Failed");
                    results.put(input, testCaseResult);
                    allPassed = false;
                    break;  // Stop executing further test cases if one fails
                }
            } catch (Exception e) {
//                logger.error("Error executing code for input: {}", input, e);
                Map<String, Object> testCaseResult = new HashMap<>();
                testCaseResult.put("Input", input);
                testCaseResult.put("ExpectedOutput", expectedOutput);
                testCaseResult.put("ActualOutput", "Error executing code");
                testCaseResult.put("Status", "Failed");
                results.put(input, testCaseResult);
                allPassed = false;
                break;  // Stop executing further test cases if one fails
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("allPassed", allPassed);
        response.put("results", results);

//        logger.info("All test cases passed: {}", allPassed);
        return ResponseEntity.ok(response);
    }
}
