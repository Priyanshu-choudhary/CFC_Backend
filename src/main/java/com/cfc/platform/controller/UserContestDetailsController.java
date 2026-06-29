package com.cfc.platform.controller;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Pojo.User;
import com.cfc.platform.Pojo.UserContestDetails;
import com.cfc.platform.Service.UserDetailsContestService;
import com.cfc.platform.Service.PostService;
import com.cfc.platform.Service.UserService;
import org.bson.Document;
import org.slf4j.Logger;
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

    private static final Logger log = LoggerFactory.getLogger(UserContestDetailsController.class);

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
            log.error("getAllDetailController error: {}", e.getMessage());
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
            log.error("getContestDetails error for user={}, contest={}: {}", username, contestName, e.getMessage());
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
            log.error("getContestDetailsById error for id={}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PostMapping
    public ResponseEntity<?> createUserContestDetails(@RequestBody UserContestDetails userContestDetails) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            log.info("createUserContestDetails: user={}", username);
            Optional<UserContestDetails> detailsResponse= userDetailsContestService.createUserContestDetails(userContestDetails, username);
            return new ResponseEntity<>(detailsResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("createUserContestDetails error: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{contestName}/username/{username}")
    public ResponseEntity<?> createUserDetailsPostRefContest(@PathVariable String username,@PathVariable String contestName, @RequestBody Posts post) {
        try {
            log.info("createUserDetailsPostRefContest: user={}, contest={}", username, contestName);
            User user = userService.findByName(username);
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

                log.info("createUserDetailsPostRefContest: post created for user={}", username);
                return new ResponseEntity<>(post, HttpStatus.CREATED);
            } else {
                log.error("createUserDetailsPostRefContest: contest not found: {}", contestName);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error("createUserDetailsPostRefContest error: {}", e.getMessage());
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
            log.error("deleteContestById error for id={}: {}", id, e.getMessage());
            response.put("message", "Error deleting Contest");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateCourseById(@PathVariable String myId, @RequestBody UserContestDetails newContestDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("updateContestDetails: id={}, user={}", myId, username);
            UserContestDetails updatedContest = userDetailsContestService.updateContestDetails(myId, newContestDetails, username);
            return new ResponseEntity<>(updatedContest, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("updateContestDetails error for id={}: {}", myId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
