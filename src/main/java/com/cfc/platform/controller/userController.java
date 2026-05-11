package com.cfc.platform.controller;

import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.User;
import com.cfc.platform.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/users")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})

public class userController {

    private static final Logger log = LoggerFactory.getLogger(userController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @GetMapping("getUser")
    public ResponseEntity<User> getOneUsersbyUserName() {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        log.info("getUser: {}", username);
        try {
            User user = userService.findByName(username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }catch (Exception e){
            log.error("getUser error for {}: {}", username, e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        User userIndb = userService.findByName(user.getName());
        if (userIndb != null && passwordEncoder.matches(user.getPassword(), userIndb.getPassword())) {
            return new ResponseEntity<>(userIndb, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PutMapping
    public ResponseEntity<?> updateUserName(@RequestBody User updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User userIndb = userService.findByName(username);
        if (userIndb != null) {
            userIndb.setName(updatedUser.getName());
            // Only re-encode if caller sent a new plaintext password
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                userIndb.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            userIndb.setEmail(updatedUser.getEmail());
            userIndb.setCollage(updatedUser.getCollage());
            userIndb.setBranch(updatedUser.getBranch());
            userIndb.setYear(updatedUser.getYear());
            userIndb.setBadges(updatedUser.getBadges());
            userIndb.setNumber(updatedUser.getNumber());
            userIndb.setSkills(updatedUser.getSkills());
            // Do NOT allow role changes via this endpoint â€” roles are admin-managed
            userIndb.setProfileImg(updatedUser.getProfileImg());
            userIndb.setLastModifiedUser(new Date());
            userService.createUser(userIndb); // direct save â€” no re-encoding
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



}
