package com.cfc.platform.MongoRepo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cfc.platform.Pojo.Posts.Submission.Submission;
import com.cfc.platform.Pojo.Posts.Submission.Verdict;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<Submission, String> {

    List<Submission> findByUserId(String userId);

    List<Submission> findByProblemId(String problemId);

    List<Submission> findByUserIdAndProblemId(String userId, String problemId);

    List<Submission> findByContestId(Integer contestId);

    Optional<Submission> findTopByUserIdAndProblemIdOrderBySubmittedAtDesc(
            String userId,
            String problemId);

    List<Submission> findTop20ByUserIdOrderBySubmittedAtDesc(String userId);

    List<Submission> findTop50ByProblemIdOrderBySubmittedAtDesc(String problemId);

    long countByProblemId(String problemId);

    long countByUserId(String userId);

    long countByProblemIdAndVerdict(String problemId, Verdict verdict);

    long countByUserIdAndVerdict(String userId, Verdict verdict);

}