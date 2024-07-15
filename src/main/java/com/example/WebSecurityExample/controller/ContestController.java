package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.MongoRepo.ContestRepo;
import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.Pojo.Contest;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Service.ContestService;
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
@RequestMapping("/Contest")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class ContestController {
    private static final Logger logger = LoggerFactory.getLogger(ContestController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ContestService contestService;

    @GetMapping
    public ResponseEntity<?> getAllContestController() {
        try {
            List<Contest> all = contestService.getAllContest();
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching Contest by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getContestByUserNameController(@PathVariable String username) {
        try {
            List<Contest> all = contestService.getUserContest(username);
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching Contest by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable String id) {
        try {

            Optional<Contest> all = contestService.getUserContestByID(id);
            if (all.isPresent()) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching Contest by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PostMapping
    public ResponseEntity<?> createContest(@RequestBody Contest contest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.error("creating course for username {}", username);
            String id = contestService.createContest(contest, username);
            logger.error(" Contest id {}", id);
            // Wrap the ID in a JSON object
            Map<String, String> response = new HashMap<>();
            response.put("ContestID", id);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating Course", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Map<String, String>> deleteContestById(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Assuming courseService.deleteUserById(id, username) returns a boolean indicating success
            boolean deleted = contestService.deleteContestById(id, username);

            if (deleted) {
                response.put("message", "Contest deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Contest not found or you do not have permission to delete it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("Error deleting Contest by ID", e);
            response.put("message", "Error deleting Contest");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateCourseById(@PathVariable String myId, @RequestBody Contest newContest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.error("Try to updating contest by ID");
            Contest updatedContest = contestService.updateContest(myId, newContest, username);
            return new ResponseEntity<>(updatedContest, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating contest by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
