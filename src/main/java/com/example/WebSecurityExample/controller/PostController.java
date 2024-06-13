package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
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
@RequestMapping("/Posts")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", "http://localhost:5173"})
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getUserByUserName() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User users = userService.findByName(username);
            List<Posts> all = users.getPosts();
            if (all != null) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching posts by username", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getQuestionsByExactTags(@RequestParam List<String> tags, @RequestParam boolean exactMatch) {
        try {
            // Fetch the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByName(username);

            logger.info("Filtering posts for user: {}", username);
            logger.info("Request tags: {}", tags);

            // Convert the list of tags from the request to a Set for comparison
            Set<String> requestTags = new HashSet<>(tags);

            // Filter the user's posts by whether they contain all the requested tags
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
            User users = userService.findByName(username);
            List<Posts> collect = users.getPosts().stream().filter(x -> x.getId().equals(myid)).collect(Collectors.toList());

            if (!collect.isEmpty()) {
                Optional<Posts> userById = postService.getUserById(myid);
                if (userById.isPresent()) {
                    return new ResponseEntity<>(userById.get(), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching post by ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Posts> createUser(@RequestBody Posts user) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            postService.createUser(user, username);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            postService.deleteUserById(id, username);
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

            Posts updatedPost = postService.updatePost(myId, newPost, username);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating post by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
