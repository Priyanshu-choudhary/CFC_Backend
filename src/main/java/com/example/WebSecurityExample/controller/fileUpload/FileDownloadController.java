package com.example.WebSecurityExample.controller.fileUpload;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;

@RestController
@RequestMapping("/FileManager")
@CrossOrigin(origins = {"https://codeforchallenge.online", "http://localhost:5173"})
public class FileDownloadController {
    @Value("${upload.dir}")
    private String uploadDir;

    @GetMapping("/files/{filename:.+}")
    public Resource downloadFile(@PathVariable String filename) {
        File file = new File(uploadDir + File.separator + filename);
        return new FileSystemResource(file);
    }
}
