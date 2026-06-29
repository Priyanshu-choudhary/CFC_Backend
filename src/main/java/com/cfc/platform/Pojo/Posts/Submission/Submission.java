package com.cfc.platform.Pojo.Posts.Submission;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "submissions")
@CompoundIndexes({
        @CompoundIndex(name = "user_problem_idx", def = "{'userId':1,'problemId':1}"),
        @CompoundIndex(name = "problem_time_idx", def = "{'problemId':1,'submittedAt':-1}"),
        @CompoundIndex(name = "user_time_idx", def = "{'userId':1,'submittedAt':-1}")
})
public class Submission {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String username;

    @Indexed
    private String problemId;

    private Integer contestId;

    private String problemIndex;

    private Integer languageId;

    private String language;

    // Source code submitted by user
    private String sourceCode;

    // ACCEPTED, WRONG_ANSWER, etc.
    private Verdict verdict;

    // Total execution time (seconds)
    private Double executionTime;

    // Peak memory usage (KB)
    private Integer memoryUsed;

    private Integer passedTestCases;

    private Integer totalTestCases;

    // Optional score (0-100 or contest score)
    private Double score;

    // True if practice submission
    private Boolean practiceSubmission = true;

    // Queue job id used by sandbox
    private String jobId;

    private Date submittedAt = new Date();

    // Optional compiler output
    private String compileOutput;

    // Optional runtime error
    private String stderr;

    // Optional program output
    private String stdout;

    private List<TestCaseResult> testResults = new ArrayList<>();

    public Submission() {
    }

    // -------------------- Getters & Setters --------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public String getProblemIndex() {
        return problemIndex;
    }

    public void setProblemIndex(String problemIndex) {
        this.problemIndex = problemIndex;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Double executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Integer memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(Integer passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(Integer totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getPracticeSubmission() {
        return practiceSubmission;
    }

    public void setPracticeSubmission(Boolean practiceSubmission) {
        this.practiceSubmission = practiceSubmission;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public void setCompileOutput(String compileOutput) {
        this.compileOutput = compileOutput;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public List<TestCaseResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<TestCaseResult> testResults) {
        this.testResults = testResults;
    }
}
