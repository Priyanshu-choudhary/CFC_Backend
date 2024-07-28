package com.example.WebSecurityExample.controller.fileUpload;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
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

        @Value("${upload.dir}")
        private String uploadDir;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
            // Ensure the upload directory exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return "Failed to create directory";
                }
            }

            // Save the file
            try {
                Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
                Files.write(path, file.getBytes());
                return "File uploaded successfully: " + path.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed to upload file";
            }
    }
}

