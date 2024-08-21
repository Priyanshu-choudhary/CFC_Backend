package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.MongoRepo.ContestRepo;
import com.example.WebSecurityExample.Pojo.Contest;
import com.example.WebSecurityExample.Pojo.Lecture.Lecture;
import com.example.WebSecurityExample.Pojo.Posts.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.ContestService;
import com.example.WebSecurityExample.Service.LeactureService;
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

@RestController
@RequestMapping("/Lecture")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class LectureController {
    private static final Logger logger = LoggerFactory.getLogger(LectureController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ContestService contestService;

    @Autowired
    private LeactureService lectureService;

    @GetMapping
    public ResponseEntity<?> getAllLectureController() {
        try {
            List<Lecture> all = lectureService.getAllLecture();
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
//            logger.error("Error fetching Lecture by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getLectureByUserNameController(@PathVariable String username) {
        try {
            List<Lecture> all = lectureService.getUserLecture(username);
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
//            logger.error("Error fetching Lecture by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable String id) {
        try {

            Optional<Lecture> all = lectureService.getUserLectureByID(id);
            if (all.isPresent()) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
//            logger.error("Error fetching Lecture by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody Lecture lecture) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
//            logger.error("creating course for username {}", username);
            String id = lectureService.createLecture(lecture, username);
//            logger.error(" Lecture id {}", id);
            // Wrap the ID in a JSON object
            Map<String, String> response = new HashMap<>();
            response.put("LectureID", id);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
//            logger.error("Error creating Course", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

//    @PostMapping("/{lectureName}/username/{username}")
//    public ResponseEntity<?> createPostRefCourse(@PathVariable String username,@PathVariable String lectureName, @RequestBody Posts post) {
//        try {
//
////            logger.info("Creating new post ref to Lecture for user: {}", username);
//
//            // Fetch the user
//            User user = userService.findByName(username);
////            logger.info("user.getContests {}", user.getContests());
//            // Find the course by name for this user
//            Optional<Lecture> lectureOptional = user.getLectures().stream()
//
//                    .filter(c -> c.getTitle().equals(lectureName))
//                    .findFirst();
//
//            if (lectureOptional.isPresent()) {
//
//                Lecture Lecture = lectureOptional.get();
//
//                // Set the lastModified field to the current date and time
//                post.setLastModified(new Date());
//
//                // Reference the course in the post
////                post.setConte(lecture);
//
//                // Create the post
//                postService.createPostWithRefContest(post, user,username);
//
////                logger.info("Post ref to course created successfully for user: {}", username);
//
//                return new ResponseEntity<>(post, HttpStatus.CREATED);
//            } else {
////                logger.error("constest not found: {}", contestName);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//        } catch (Exception e) {
////            logger.error("Error creating post ref to Lecture", e);
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }
//


    @DeleteMapping("/id/{id}")
    public ResponseEntity<Map<String, String>> deleteLectureById(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Assuming courseService.deleteUserById(id, username) returns a boolean indicating success
            boolean deleted = lectureService.deleteLectureById(id, username);

            if (deleted) {
                response.put("message", "Lecture deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Lecture not found or you do not have permission to delete it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
//            logger.error("Error deleting Lecture by ID", e);
            response.put("message", "Error deleting Lecture");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateCourseById(@PathVariable String myId, @RequestBody Lecture newLecture) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
//            logger.error("Try to updating Lecture by ID");
            Lecture updatedLecture = lectureService.updateLecture(myId, newLecture, username);
            return new ResponseEntity<>(updatedLecture, HttpStatus.OK);
        } catch (RuntimeException e) {
//            logger.error("Error updating Lecture by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
