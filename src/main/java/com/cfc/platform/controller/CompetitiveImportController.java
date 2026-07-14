package com.cfc.platform.controller;

import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Service.AtCoderToolsService;
import com.cfc.platform.Service.CFToolsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/imports")
public class CompetitiveImportController {

    private final CFToolsService codeforcesTools;
    private final AtCoderToolsService atCoderTools;

    public CompetitiveImportController(CFToolsService codeforcesTools, AtCoderToolsService atCoderTools) {
        this.codeforcesTools = codeforcesTools;
        this.atCoderTools = atCoderTools;
    }

    public record ImportRequest(String url) {}

    @PostMapping("/problem")
    public ResponseEntity<?> importProblem(@RequestBody ImportRequest request) {
        try {
            String url = request.url() == null ? "" : request.url().trim();
            if (url.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Problem URL is required."));

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String normalized = url.toLowerCase(Locale.ROOT);
            Posts imported;
            String platform;
            if (normalized.contains("atcoder.jp/")) {
                imported = atCoderTools.importAndSaveProblem(url, username, true);
                platform = "atcoder";
            } else if (normalized.contains("codeforces.com/")) {
                imported = codeforcesTools.importAndSaveProblem(url, username, true);
                platform = "codeforces";
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Use a Codeforces or AtCoder problem URL."));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", imported.getId(),
                    "title", imported.getTitle(),
                    "platform", platform,
                    "message", "Problem imported successfully."
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to import problem: " + exception.getMessage()));
        }
    }
}
