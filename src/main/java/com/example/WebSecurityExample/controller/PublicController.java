package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.PostService;
import com.example.WebSecurityExample.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Public")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", " http://localhost:5173"})

public class PublicController {

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;

    @GetMapping("HealthCheck")
    public String sayHello(){
        return "Ok!";
    }

    @GetMapping("getUser")
    public List getAllUsersbyUserName() {
        return  userService.getAllUsers();
    }

    @CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
    @PostMapping("/Create-User")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            if (userService.existsByName(user.getName())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT); // Return 409 Conflict if user already exists
            }
            userService.createNewUser(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> users= userService.getUserById(id);
        if (users.isPresent()) {
            return new ResponseEntity<>(users.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>( HttpStatus.NOT_FOUND);
    }
}
