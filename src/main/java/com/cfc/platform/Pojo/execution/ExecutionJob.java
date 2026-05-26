package com.cfc.platform.Pojo.execution;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Represents a code execution job that travels through the async pipeline:
 *
 *   Spring Boot  →  SQS (this object as JSON)  →  goboxd worker  →  Redis result
 *
 * Both Spring Boot (Java) and goboxd worker (Go) must agree on this JSON shape.
 * Any field change here must be mirrored in the Go models/job.go struct.
 *
 * Fields:
 *   type        "run"    → playground execution (stdin, no expected output)
 *               "submit" → test-case evaluation (testCases map)
 *   sourceCode  the user's code
 *   language    e.g. "java", "python", "cpp" — goboxd maps this to its engine
 *   stdin       only used when type=run
 *   testCases   only used when type=submit  { input → expectedOutput }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)   // omit null fields from JSON → smaller SQS messages
public class ExecutionJob {

    private String jobId;
    private String type;                    // "run" | "submit"
    private String sourceCode;
    private String language;
    private String stdin;                   // type=run only
    private Map<String, String> testCases;  // type=submit only
    private Double timeLimitSeconds;
    private Integer memoryLimitKb;
    private String userId;
    private long queuedAt;                  // epoch millis, set by ExecutionJobService

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getJobId()                        { return jobId; }
    public void   setJobId(String jobId)            { this.jobId = jobId; }

    public String getType()                         { return type; }
    public void   setType(String type)              { this.type = type; }

    public String getSourceCode()                   { return sourceCode; }
    public void   setSourceCode(String sourceCode)  { this.sourceCode = sourceCode; }

    public String getLanguage()                     { return language; }
    public void   setLanguage(String language)      { this.language = language; }

    public String getStdin()                        { return stdin; }
    public void   setStdin(String stdin)            { this.stdin = stdin; }

    public Map<String, String> getTestCases()               { return testCases; }
    public void   setTestCases(Map<String, String> testCases) { this.testCases = testCases; }

    public Double getTimeLimitSeconds()                     { return timeLimitSeconds; }
    public void   setTimeLimitSeconds(Double t)             { this.timeLimitSeconds = t; }

    public Integer getMemoryLimitKb()                       { return memoryLimitKb; }
    public void    setMemoryLimitKb(Integer m)              { this.memoryLimitKb = m; }

    public String getUserId()                       { return userId; }
    public void   setUserId(String userId)          { this.userId = userId; }

    public long getQueuedAt()                       { return queuedAt; }
    public void setQueuedAt(long queuedAt)          { this.queuedAt = queuedAt; }
}
