package com.cfc.platform.Service;

import org.springframework.stereotype.Service;

import com.cfc.platform.MongoRepo.SubmissionRepository;
import com.cfc.platform.Pojo.Posts.Submission.Submission;

import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    private final SubmissionRepository   submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public Submission saveSubmission(Submission submission) {
        return submissionRepository.save(submission);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public Optional<Submission> getSubmissionById(String id) {
        return submissionRepository.findById(id);
    }

    public List<Submission> getUserSubmissions(String userId) {
        return submissionRepository.findByUserId(userId);
    }

    public List<Submission> getProblemSubmissions(String problemId) {
        return submissionRepository.findByProblemId(problemId);
    }

    public List<Submission> getUserProblemSubmissions(String userId, String problemId) {
        return submissionRepository.findByUserIdAndProblemId(userId, problemId);
    }

    public List<Submission> getContestSubmissions(Integer contestId) {
        return submissionRepository.findByContestId(contestId);
    }

    public void deleteSubmission(String id) {
        submissionRepository.deleteById(id);
    }

    public boolean exists(String id) {
        return submissionRepository.existsById(id);
    }
}
