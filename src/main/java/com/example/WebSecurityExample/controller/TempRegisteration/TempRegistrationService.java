package com.example.WebSecurityExample.controller.TempRegisteration;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TempRegistrationService {

    // In-memory storage example (you may use a temporary database table)
    private Map<String, RegisterRequest> tempStorage = new HashMap<>();

    public void saveTempRegistration(RegisterRequest request) {
        tempStorage.put(request.getEmail(), request);
    }

    public RegisterRequest getTempRegistration(String email) {
        return tempStorage.get(email);
    }

    public void deleteTempRegistration(String email) {
        tempStorage.remove(email);
    }
}
