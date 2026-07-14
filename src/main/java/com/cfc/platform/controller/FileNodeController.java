package com.cfc.platform.controller;

import com.cfc.platform.DTO.CreateFileNodeRequest;
import com.cfc.platform.DTO.FileNodeResponse;
import com.cfc.platform.DTO.MoveFileNodeRequest;
import com.cfc.platform.DTO.UpdateFileNodeRequest;
import com.cfc.platform.Service.FileNodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/files")
public class FileNodeController {

    private final FileNodeService fileNodeService;

    public FileNodeController(FileNodeService fileNodeService) {
        this.fileNodeService = fileNodeService;
    }

    @PostMapping
    public ResponseEntity<?> createNode(@RequestBody CreateFileNodeRequest request) {
        try {
            FileNodeResponse response = fileNodeService.createNode(getAuthenticatedUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create file node.");
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        try {
            List<FileNodeResponse> tree = fileNodeService.getTree(getAuthenticatedUsername());
            return ResponseEntity.ok(tree);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch file tree.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNodes(@RequestParam("q") String query) {
        try {
            List<FileNodeResponse> matches = fileNodeService.searchByName(getAuthenticatedUsername(), query);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to search file nodes.");
        }
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<?> getNode(@PathVariable String nodeId) {
        try {
            FileNodeResponse response = fileNodeService.getNode(getAuthenticatedUsername(), nodeId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch file node.");
        }
    }

    @PutMapping("/{nodeId}")
    public ResponseEntity<?> updateNode(@PathVariable String nodeId, @RequestBody UpdateFileNodeRequest request) {
        try {
            FileNodeResponse response = fileNodeService.updateNode(getAuthenticatedUsername(), nodeId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update file node.");
        }
    }

    @PutMapping("/{nodeId}/move")
    public ResponseEntity<?> moveNode(@PathVariable String nodeId, @RequestBody MoveFileNodeRequest request) {
        try {
            FileNodeResponse response = fileNodeService.moveNode(getAuthenticatedUsername(), nodeId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to move file node.");
        }
    }

    @DeleteMapping("/{nodeId}")
    public ResponseEntity<?> deleteNode(@PathVariable String nodeId) {
        try {
            fileNodeService.deleteNode(getAuthenticatedUsername(), nodeId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "File node deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file node.");
        }
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
