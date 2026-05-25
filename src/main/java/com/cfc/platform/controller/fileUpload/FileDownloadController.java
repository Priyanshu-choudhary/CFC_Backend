package com.cfc.platform.controller.fileUpload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Files are now stored in S3 and served directly via CloudFront.
 * This controller is kept as a legacy stub so any old URLs pointing to
 * /FileManager/files/{filename} get a clear 410 Gone response instead
 * of a 404, making it obvious during debugging that the endpoint moved.
 *
 * You can delete this class once you're sure no clients call it.
 */
@RestController
@RequestMapping("/FileManager")
@CrossOrigin(origins = {"https://codeforchallenge.online", "https://www.codeforchallenge.online", "http://localhost:5173"})
public class FileDownloadController {

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<String> downloadFile(@PathVariable String filename) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Files are now served via CloudFront. " +
                      "Use the URL returned by the /Files/upload endpoint.");
    }
}
