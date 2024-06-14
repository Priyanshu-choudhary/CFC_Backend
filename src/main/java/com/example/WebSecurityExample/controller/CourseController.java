package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.Pojo.Course;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Course")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getCourseByUserName() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User users = userService.findByName(username);
            List<Course> all = users.getCourses();
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching posts by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            courseService.createCourse(course,username);
            return new ResponseEntity<>(course, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating Course", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            courseService.deleteUserById(id, username);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting post by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PutMapping("/id/{myId}")
//    public ResponseEntity<?> updatePostById(@PathVariable String myId, @RequestBody Posts newPost) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//
//            Posts updatedPost = postService.updatePost(myId, newPost, username);
//            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
//        } catch (RuntimeException e) {
//            logger.error("Error updating post by ID", e);
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        }
//    }
}
