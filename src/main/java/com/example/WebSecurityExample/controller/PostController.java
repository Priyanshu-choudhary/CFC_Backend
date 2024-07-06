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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/Posts")
@CrossOrigin(origins = {"https://code-for-challeng-fuodp780u.vercel.app", "http://localhost:5173"})
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;


    @GetMapping("/username/{username}")
    public ResponseEntity<?> getAllPosts(@PathVariable String username,@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        try {

//            User users = userService.findByName(username);
            logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^findByName^^^^^^^^^^^^^^^^^^^");
            List<Posts> all = userService.findByName(username).getPosts();
            return new ResponseEntity<>(all, HttpStatus.OK);

        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @GetMapping
//    public ResponseEntity<?> getPostByUsername(@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
//        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            String username = auth.getName();
//            logger.info("Fetching posts for user: {}", username);
//            logger.info("ifModifiedSince: {}", ifModifiedSince);
//
//            User users = userService.findByName(username);
//            if (users == null) {
//                logger.error("User not found: {}", username);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            List<Posts> all = users.getPosts();
//            if (all == null || all.isEmpty()) {
//                logger.info("No posts found for user: {}", username);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//            logger.info("try to fetch lastModified ");
//            Date lastModified = postService.getLastModifiedForUser(username);
//            logger.info("lastModifyed : {}", lastModified);
//
//            if (ifModifiedSince != null) {
//                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//                Date ifModifiedSinceDate = dateFormat.parse(ifModifiedSince);
//                if (!lastModified.after(ifModifiedSinceDate)) {
//                    logger.info("Posts not modified since: {}", ifModifiedSince);
//                    return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
//                }
//            }
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setLastModified(lastModified.getTime());
//            logger.info("Returning posts for user: {}", username);
//            return new ResponseEntity<>(all, headers, HttpStatus.OK);
//        } catch (ParseException e) {
//            logger.error("Error parsing If-Modified-Since header", e);
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            logger.error("Error fetching posts by username", e);
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/filter")
    public ResponseEntity<?> getQuestionsByExactTags(@RequestParam List<String> tags, @RequestParam boolean exactMatch) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Filtering posts for user: {}", username);
            logger.info("Request tags: {}", tags);

            User user = userService.findByName(username);
            if (user == null) {
                logger.error("User not found: {}", username);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Set<String> requestTags = new HashSet<>(tags);
            List<Posts> filteredPosts = user.getPosts().stream()
                    .filter(post -> {
                        Set<String> postTags = post.getTags() != null ? new HashSet<>(post.getTags()) : new HashSet<>();
                        boolean isMatch = postTags.containsAll(requestTags);
                        if (isMatch) {
                            logger.info("Match found: Post ID = {}", post.getId());
                        }
                        return isMatch;
                    })
                    .collect(Collectors.toList());

            logger.info("Filtered posts count: {}", filteredPosts.size());

            if (filteredPosts.isEmpty()) {
                logger.info("No posts found matching the tags: {}", tags);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            logger.info("Returning filtered posts for user: {}", username);
            return new ResponseEntity<>(filteredPosts, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering posts by tags", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/id/{myid}")
    public ResponseEntity<?> getUserById(@PathVariable String myid) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Fetching post by ID: {} for user: {}", myid, username);

            User users = userService.findByName(username);
            if (users == null) {
                logger.error("User not found: {}", username);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<Posts> collect = users.getPosts().stream().filter(x -> x.getId().equals(myid)).collect(Collectors.toList());
            if (collect.isEmpty()) {
                logger.info("Post not found with ID: {}", myid);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Optional<Posts> userById = postService.getUserById(myid);
            if (userById.isPresent()) {
                logger.info("Returning post with ID: {}", myid);
                return new ResponseEntity<>(userById.get(), HttpStatus.OK);
            } else {
                logger.info("Post not found in database with ID: {}", myid);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error fetching post by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Course/{courseName}/username/{username}")
    public ResponseEntity<?> getPostsByCourse(@PathVariable String courseName ,@PathVariable String username)   {
        try {

            logger.info("Fetching posts for course '{}' for user: {}", courseName, username);

            // Find the user
            logger.info("+++++++++++++++++++++++++Find by name +++++++++++++++++++++");
            User user = userService.findByName(username);
            logger.info("+++++++++++++++++++++++++Find by name +++++++++++++++++++++");
            if (user == null) {
                logger.error("User not found: {}", username);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Find the course by name for this user
            Optional<Course> courseOpt = user.getCourses().stream()
                    .filter(c -> c.getTitle().equalsIgnoreCase(courseName))
                    .findFirst();

            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                List<Posts> posts = course.getPosts();

                if (posts == null || posts.isEmpty()) {
                    logger.info("No posts found for course: {}", courseName);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                logger.info("Returning posts for course '{}' for user: {}", courseName, username);
                return new ResponseEntity<>(posts, HttpStatus.OK);
            } else {
                logger.error("Course not found: {}", courseName);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            logger.error("Error fetching posts by course", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/username/{username}")
    public ResponseEntity<Posts> createPost(@PathVariable String username,@RequestBody Posts post) {
        try {

            logger.info("Creating new post for user: {}", username);

            User user = userService.findByName(username);
            logger.info(" user: {}", user);
            List<Posts> allPosts = user.getPosts();

            if (allPosts != null) {
                for (Posts existingPost : allPosts) {
                    if (existingPost.getTitle().equals(post.getTitle())) {
                        logger.warn("Duplicate post creation attempt for user: {}", username);
                        return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 Conflict
                    }
                }
            }

            // Set the lastModified field to the current date and time
            post.setLastModified(new Date());
            userService.setLastdate(username);
            postService.createPost(post, username,user);
            logger.info("Post created successfully for user: {}", username);
            return new ResponseEntity<>(post, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/Course/{courseName}/username/{username}")
    public ResponseEntity<?> createPostRefCourse(@PathVariable String username,@PathVariable String courseName, @RequestBody Posts post) {
        try {

            logger.info("Creating new post ref to course for user: {}", username);

            // Fetch the user
            User user = userService.findByName(username);

            // Find the course by name for this user
            Optional<Course> courseOpt = user.getCourses().stream()
                    .filter(c -> c.getTitle().equalsIgnoreCase(courseName))
                    .findFirst();

            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();

                // Set the lastModified field to the current date and time
                post.setLastModified(new Date());

                // Reference the course in the post
                post.setCourse(course);

                // Create the post
                postService.createPostWithRefCourse(post, user,username);
                logger.info("Post ref to course created successfully for user: {}", username);

                return new ResponseEntity<>(post, HttpStatus.CREATED);
            } else {
                logger.error("Course not found: {}", courseName);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            logger.error("Error creating post ref to course", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }





    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Deleting post with ID: {} for user: {}", id, username);

            postService.deleteUserById(id, username);

            userService.setLastdate(username);
            logger.info("Post deleted successfully with ID: {} for user: {}", id, username);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting post by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updatePostById(@PathVariable String myId, @RequestBody Posts newPost) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.info("Updating post with ID: {} for user: {}", myId, username);

            Posts updatedPost = postService.updatePost(myId, newPost, username);
            userService.setLastdate(username);
            logger.info("Post updated successfully with ID: {} for user: {}", myId, username);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating post by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


}
