package com.example.WebSecurityExample.controller.TempRegisteration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private TempRegistrationService tempRegistrationService;

    @Autowired
    private JavaMailSender javaMailSender;

    @PostMapping("/temp")
    public void registerTemp(@RequestBody RegisterRequest request) {
        // Save registration data temporarily
        tempRegistrationService.saveTempRegistration(request);

        // Send verification email
        sendVerificationEmail(request.getEmail(), request.getUsername(), request.getPassword());
    }

    private void sendVerificationEmail(String email, String name,String password) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Verify your account ");
        mailMessage.setText("We’re excited you’ve joined CFC. \n As soon as you verify your email to confirm this is you, we can get started. \n Click the link below to verify your email address:\nhttps://code-for-challenge.vercel.app/verify-email?email=" + email+"&name="+name+"&password="+password);

        javaMailSender.send(mailMessage);
    }
}
