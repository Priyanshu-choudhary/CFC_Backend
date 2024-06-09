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
import java.util.stream.Collectors;


@RestController
@RequestMapping("/Posts")
@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})

public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getUserByUserName() {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        User users = userService.findByName(username);
        List<Posts> all=users.getPosts();
        if (all != null) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>( HttpStatus.NOT_FOUND);
    }
    @GetMapping("/filter")
    public List<Posts> getQuestionsByTags(@RequestParam List<String> tags) {
        return postService.getQuestionsByTags(tags);
    }
    @GetMapping("/id/{myid}")
    public ResponseEntity<?> getUserById(@PathVariable String myid) {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        User users = userService.findByName(username);
        List<Posts> collect = users.getPosts().stream().filter(x -> x.getId().equals(myid)).collect(Collectors.toList());

        if (!collect.isEmpty()) {
            Optional<Posts> userById = postService.getUserById(myid);
            if (userById.isPresent()) {
                return new ResponseEntity<>(userById.get(), HttpStatus.OK);
            }

        }
        return new ResponseEntity<>( HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<Posts> createUser(@RequestBody Posts user) {
       try {
           Authentication auth= SecurityContextHolder.getContext().getAuthentication();
           String username= auth.getName();
           postService.createUser(user,username);
           return new ResponseEntity<>(user,HttpStatus.CREATED);
       }catch(Exception e){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username= auth.getName();
        postService.deleteUserById(id,username);
        return new ResponseEntity<>(HttpStatus.OK);


    }


    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updatePostById(@PathVariable String myId, @RequestBody Posts newPost) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            Posts updatedPost = postService.updatePost(myId, newPost, username);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }




}
