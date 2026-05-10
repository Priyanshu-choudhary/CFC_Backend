package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.ConfigSecurity.JwtUtil;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userService.findByName(req.username());
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getName());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "roles", user.getRoles() != null ? user.getRoles() : java.util.List.of()
        ));
    }

    public record LoginRequest(String username, String password) {}
}
