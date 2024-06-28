package com.example.WebSecurityExample.controller.TempRegisteration;

import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verify-email")
public class EmailVerificationController {

    @Autowired
    private TempRegistrationService tempRegistrationService;

    @Autowired
    private UserRepo userRepository; // Replace with your actual UserRepository

    @Autowired
    private UserService userService;
    @PostMapping
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        RegisterRequest request = tempRegistrationService.getTempRegistration(email);
        if (request != null) {
            // Save user data to database
            User user = new User(request.getName(), request.getEmail(), request.getPassword());

            userService.createNewUser(user);
//            userRepository.save(user);

            // Optionally, remove temporary data after successful verification
            tempRegistrationService.deleteTempRegistration(email);

            return ResponseEntity.ok("Email verified and user registered successfully!");
        } else {
            return ResponseEntity.badRequest().body("Invalid email verification link or expired.");
        }
    }
}


