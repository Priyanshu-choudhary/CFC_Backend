package com.cfc.platform.controller.TempRegisteration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Temporary registration endpoint.
 * Email verification has been removed — registrations are stored in memory
 * until the user completes sign-up via the main AuthController.
 */
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private TempRegistrationService tempRegistrationService;

    @PostMapping("/temp")
    public ResponseEntity<String> registerTemp(@RequestBody RegisterRequest request) {
        tempRegistrationService.saveTempRegistration(request);
        return ResponseEntity.ok("Registration received for: " + request.getEmail());
    }
}
