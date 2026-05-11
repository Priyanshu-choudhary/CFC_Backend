package com.cfc.platform.controller.TempRegisteration;

public class RegisterRequest {
    private String username; // Changed from username to name
    private String email;
    private String password;

    // No-args constructor
    public RegisterRequest() {
    }

    // All-args constructor
    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RegisterRequest that = (RegisterRequest) o;
        return java.util.Objects.equals(username, that.username) &&
                java.util.Objects.equals(email, that.email) &&
                java.util.Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(username, email, password);
    }
}
