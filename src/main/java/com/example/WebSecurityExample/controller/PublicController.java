package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.CourseService;
import com.example.WebSecurityExample.Service.PostService;
import com.example.WebSecurityExample.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/Public")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", " http://localhost:5173"})

public class PublicController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;

    @Autowired
    private CourseService courseService;
    @GetMapping("HealthCheck")
    public String sayHello(){
        return "Ok!";
    }

    @GetMapping("getAllUser")
    public List getAllUsersbyUserName() {
        return  userService.getAllUsers();
    }

    @GetMapping("showUser/{username}")
    public User getUsersbyUserName(@PathVariable String username) {
        return  userService.findByName(username);
    }


    @CrossOrigin(origins = {"https://code-for-challenge.vercel.app", "http://localhost:5173"})
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

    @DeleteMapping("/user/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            userService.deleteUserById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
