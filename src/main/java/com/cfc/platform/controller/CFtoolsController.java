package com.cfc.platform.controller;

import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Service.CFToolsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/codeforces")
public class CFtoolsController {

    private final CFToolsService cfToolsService;

    public CFtoolsController(CFToolsService cfToolsService) {
        this.cfToolsService = cfToolsService;
    }
    public record UrlRequest(String url) {}
    @PostMapping("/import/{username}")
    public ResponseEntity<?> importAndSaveProblem(@PathVariable String username, @RequestBody UrlRequest request) {
        try {
            String url = request.url();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!auth.getName().equals(username)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Posts post = cfToolsService.importAndSaveProblem(url, username, true);
            return new ResponseEntity<>(post, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to import and save problem", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
