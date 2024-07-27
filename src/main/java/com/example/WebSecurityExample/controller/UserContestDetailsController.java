package com.example.WebSecurityExample.controller;
import com.example.WebSecurityExample.Pojo.Posts.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Pojo.UserContestDetails;
import com.example.WebSecurityExample.Service.UserDetailsContestService;
import com.example.WebSecurityExample.Service.PostService;
import com.example.WebSecurityExample.Service.UserService;
import org.bson.Document;
//import org.slf4j.// logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/UserDetailsContest")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class UserContestDetailsController {
//    private static final // logger // logger = LoggerFactory.getLogger(UserContestDetailsController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsContestService userDetailsContestService;

    @GetMapping
    public ResponseEntity<?> getAllDetailController() {
        try {
            List<UserContestDetails> all = userDetailsContestService.getAllUserDetailsContest();
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // logger.error("Error fetching Contest by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{username}/{contestName}")
    public ResponseEntity<?> getContestDetailsByUserNameController(@PathVariable String username,@PathVariable String contestName) {
        try {
            List<UserContestDetails> all = userDetailsContestService.getUserContestDetailsByContestName(username,contestName);
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // logger.error("Error fetching Contest by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/findby/{contestName}")
    public List<Document> getUsersByContestName(@PathVariable String contestName) {
        return userDetailsContestService.getUsersByContestName(contestName);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getContestDetailsById(@PathVariable String id) {
        try {

            Optional<UserContestDetails> all = userDetailsContestService.getUserContestDetailsByID(id);
            if (all.isPresent()) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // logger.error("Error fetching Contest by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PostMapping
    public ResponseEntity<?> createUserContestDetails(@RequestBody UserContestDetails userContestDetails) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            // logger.error("creating course for username {}", username);
            Optional<UserContestDetails> detailsResponse= userDetailsContestService.createUserContestDetails(userContestDetails, username);
            // logger.error(" Contest details {}", detailsResponse);



            return new ResponseEntity<>(detailsResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            // logger.error("Error creating User contest details ", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{contestName}/username/{username}")
    public ResponseEntity<?> createUserDetailsPostRefContest(@PathVariable String username,@PathVariable String contestName, @RequestBody Posts post) {
        try {

            // logger.info("Creating new post ref to contest for user: {}", username);

            // Fetch the user
            User user = userService.findByName(username);
            // logger.info("user.getContests {}", user.getUserContestDetails());
            // Find the course by name for this user
            Optional<UserContestDetails> userContestDetailsOpt = user.getUserContestDetails().stream()

                    .filter(c -> c.getNameOfContest().equals(contestName))
                    .findFirst();

            if (userContestDetailsOpt.isPresent()) {

                UserContestDetails userContestDetails = userContestDetailsOpt.get();

                // Set the lastModified field to the current date and time
                post.setLastModified(new Date());

                // Reference the course in the post
                post.setUserContestDetails(userContestDetails);

                // Create the post
                userDetailsContestService.createPostWithRefUserDetailContest(post, user,username);

                // logger.info("Post ref to User Contest detail created successfully for user: {}", username);

                return new ResponseEntity<>(post, HttpStatus.CREATED);
            } else {
                // logger.error("constest not found: {}", contestName);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            // logger.error("Error creating post ref to contest", e);
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
            boolean deleted = userDetailsContestService.deleteUserContestDetailsById(id, username);

            if (deleted) {
                response.put("message", "Contest deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Contest not found or you do not have permission to delete it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            // logger.error("Error deleting Contest by ID", e);
            response.put("message", "Error deleting Contest");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateCourseById(@PathVariable String myId, @RequestBody UserContestDetails newContestDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            // logger.error("Try to updating user details contest by ID");
            UserContestDetails updatedContest = userDetailsContestService.updateContestDetails(myId, newContestDetails, username);
            return new ResponseEntity<>(updatedContest, HttpStatus.OK);
        } catch (RuntimeException e) {
            // logger.error("Error updating user details contest by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
