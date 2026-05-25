package com.cfc.platform.controller.fileUpload;

import com.cfc.platform.Service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/Files")
@CrossOrigin(origins = {"https://codeforchallenge.online", "https://www.codeforchallenge.online", "http://localhost:5173"})
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final S3Service s3Service;

    public FileUploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * Upload an image/file to S3.
     * Returns: { "location": "<CloudFront or S3 URL>" }
     * The frontend stores this URL and uses it directly — no more serving
     * files through this Spring Boot container.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String fileUrl = s3Service.uploadFile(file);
            log.info("File uploaded successfully: {}", fileUrl);
            return ResponseEntity.ok(Map.of("location", fileUrl));
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
