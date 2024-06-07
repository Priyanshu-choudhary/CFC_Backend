package com.example.WebSecurityExample.controller.Jdoodle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jdoodle")
@CrossOrigin(origins = "https://code-with-challenge.vercel.app")
public class JDoodle {

    @Value("${jdoodle.clientId}")
    private String clientId;

    @Value("${jdoodle.clientSecret}")
    private String clientSecret;

    private static final String JDoodle_API_URL = "https://api.jdoodle.com/v1/execute";

    @PostMapping("/execute")
    public ResponseEntity<?> executeCode(@RequestBody Map<String, String> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
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
}
