package com.example.WebSecurityExample.controller.TempRegisteration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Generates getters, setters, toString, hashCode, equals
@NoArgsConstructor  // Generates a no-args constructor
@AllArgsConstructor  // Generates a constructor with all fields
public class RegisterRequest {
    private String username;  // Changed from username to name
    private String email;
    private String password;
}
