package com.cfc.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cfc.platform.Pojo.Posts.Submission.Submission;
import com.cfc.platform.Service.SubmissionService;

import java.util.List;

@RestController
@RequestMapping("/problemSubmission")
@CrossOrigin
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // Save Submission
    @PostMapping
    public ResponseEntity<Submission> saveSubmission(@RequestBody Submission submission) {

        Submission saved = submissionService.saveSubmission(submission);

        return ResponseEntity.ok(saved);
    }

    // Get all submissions
    @GetMapping
    public List<Submission> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }

    // Get submission by id
    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmission(@PathVariable String id) {

        return submissionService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all submissions of a user
    @GetMapping("/user/{userId}")
    public List<Submission> getUserSubmissions(@PathVariable String userId) {

        return submissionService.getUserSubmissions(userId);
    }

    // Get all submissions of a problem
    @GetMapping("/problem/{problemId}")
    public List<Submission> getProblemSubmissions(@PathVariable String problemId) {

        return submissionService.getProblemSubmissions(problemId);
    }

    // Get user's submissions for one problem
    @GetMapping("/user/{userId}/problem/{problemId}")
    public List<Submission> getUserProblemSubmissions(
            @PathVariable String userId,
            @PathVariable String problemId) {

        return submissionService.getUserProblemSubmissions(userId, problemId);
    }

    // Get contest submissions
    @GetMapping("/contest/{contestId}")
    public List<Submission> getContestSubmissions(@PathVariable Integer contestId) {

        return submissionService.getContestSubmissions(contestId);
    }

    // Delete submission
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable String id) {

        if (!submissionService.exists(id))
            return ResponseEntity.notFound().build();

        submissionService.deleteSubmission(id);

        return ResponseEntity.noContent().build();
    }
}