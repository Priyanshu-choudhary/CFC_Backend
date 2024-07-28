package com.example.WebSecurityExample.controller.fileUpload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/Files")
@CrossOrigin(origins = {"https://codeforchallenge.online", "http://localhost:5173"})
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${server.port:8080}") // Default port is 8080 if not specified
    private String serverPort;

    @Value("${server.servlet.context-path:/}") // Default context path is root if not specified
    private String contextPath;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // Ensure the upload directory exists
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Failed to create directory: {}", uploadDir);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponse("Failed to create directory", null));
            }
            logger.info("Directory created: {}", uploadDir);
        }

        // Save the file
        try {
            Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            String fileName = file.getOriginalFilename();
            String fileUrl = "https://hytechlabs.online:" + serverPort + contextPath + "images/" + fileName;
            logger.info("File uploaded successfully: {}", path.toString());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(createResponse("File uploaded successfully", fileUrl));
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse("Failed to upload file", null));
        }
    }

    private Map<String, String> createResponse(String message, String fileUrl) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("fileUrl", fileUrl);
        return response;
    }
}
