package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.MongoRepo.CourseRepo;
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

import java.util.*;
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

    @Autowired
    private CourseRepo courseRepo;

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
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            String id = courseService.createCourse(course, username);

            // Wrap the ID in a JSON object
            Map<String, String> response = new HashMap<>();
            response.put("courseId", id);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating Course", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Map<String, String>> deleteUserById(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Assuming courseService.deleteUserById(id, username) returns a boolean indicating success
            boolean deleted = courseService.deleteUserById(id, username);

            if (deleted) {
                response.put("message", "Course deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Course not found or you do not have permission to delete it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("Error deleting course by ID", e);
            response.put("message", "Error deleting course");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateCourseById(@PathVariable String myId, @RequestBody Course newdata) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.error("Try to updating course by ID");
            Course updatedCourse = courseService.updateCourse(myId, newdata, username);
            return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating post by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
