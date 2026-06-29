package com.cfc.platform.controller;

import java.util.Date;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Service.CFToolsService;
import com.cfc.platform.Service.PostService;
import com.cfc.platform.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/codeforces")
public class CFtoolsController {

    private final CFToolsService cfToolsService;
    @Autowired
    private PostRepo postRepo;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot auto-configures this for JSON 
    // conversion


    public CFtoolsController(CFToolsService cfToolsService) {
        this.cfToolsService = cfToolsService;
    }
    public record UrlRequest(String url) {}
    @PostMapping("/import/{username}")
    public ResponseEntity<?> importAndSaveProblem(@PathVariable String username, @RequestBody UrlRequest request) {
        try {
            String url = request.url();
            // 1. Security Check
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!auth.getName().equals(username)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // 2. Parse the problem using your service
            Object parsedResult = cfToolsService.importProblem(url);

            // Check if parsing failed
            if (parsedResult instanceof Map && ((Map<?, ?>) parsedResult).containsKey("success") && !(Boolean) ((Map<?, ?>) parsedResult).get("success")) {
                return new ResponseEntity<>(parsedResult, HttpStatus.BAD_REQUEST);
            }

            // 3. Convert the parsed Map directly into your Posts entity
            Posts post = objectMapper.convertValue(parsedResult, Posts.class);

            // 4. Duplicate Check
            List<Posts> existing = postRepo.findByUserName(username);
            if (existing != null && existing.stream().anyMatch(p -> p.getTitle().equals(post.getTitle()))) {
                return new ResponseEntity<>("Problem already exists in your database", HttpStatus.CONFLICT);
            }

            // 5. Populate metadata and save
            post.setUserName(username);
            post.setLastModified(new Date());
            userService.setLastdate(username);
            
            postService.createPost(post, username);

            // Return the fully saved Post object
            return new ResponseEntity<>(post, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to import and save problem", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}