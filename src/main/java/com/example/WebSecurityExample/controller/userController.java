package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/users")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})

public class userController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @GetMapping("getUser")
    public User getAllUsersbyUserName() {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        return  userService.findByName(username);
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
    public ResponseEntity<?>updateUserName(@RequestBody User updatedUser){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        User userIndb= userService.findByName(username);
        if(userIndb!=null){
            userIndb.setName(updatedUser.getName());
            userIndb.setPassword(updatedUser.getPassword());
            userIndb.setEmail(updatedUser.getEmail());
            userIndb.setCollage(updatedUser.getCollage());
            userIndb.setBranch(updatedUser.getBranch());
            userIndb.setYear(updatedUser.getYear());
            userIndb.setBadges(updatedUser.getBadges());
            userIndb.setNumber(updatedUser.getNumber());
            userIndb.setSkills(updatedUser.getSkills());
            userIndb.setRoles(updatedUser.getRoles());

            // Save the updated user back to the database
            userService.createNewUser(userIndb);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Return appropriate response if user not found
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById() {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        userService.deleteByName(auth.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
