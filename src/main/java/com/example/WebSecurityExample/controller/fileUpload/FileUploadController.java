package com.example.WebSecurityExample.controller.fileUpload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/Files")
@CrossOrigin(origins = {"https://codeforchallenge.online", "http://localhost:5173"})
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        // Ensure the upload directory exists
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Failed to create directory: {}", uploadDir);
                return "Failed to create directory";
            }
            logger.info("Directory created: {}", uploadDir);
        }

        // Save the file
        try {
            Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            logger.info("File uploaded successfully: {}", path.toString());
            return "File uploaded successfully: " + path.toString();
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            return "Failed to upload file";
        }
    }
}
