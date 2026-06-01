package com.cfc.platform.Pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ContestSession — one document per (contest × user).
 *
 * Tracks everything needed to render the live arena, compute the
 * leaderboard, and produce the final results page.
 *
 * Collection is indexed on (contestId, username) to make lookups O(1).
 */
@Document(collection = "ContestSessions")
@CompoundIndex(name = "contest_user_idx", def = "{'contestId': 1, 'username': 1}", unique = true)
public class ContestSession {

    @Id
    private String id;

    @Indexed
    private String contestId;

    @Indexed
    private String username;

    /** When the user clicked "Enter Arena". */
    private Date joinedAt;

    /**
     * Timestamp of the last Accepted submission.
     * Used for tiebreaking: if two users have the same score, the one who
     * finished (got their last AC) earlier ranks higher.
     * Null if the user has not solved any problem yet.
     */
    private Date lastAcAt;

    /**
     * Total points earned so far.
     * = sum of problemWeights for each AC'd problem.
     */
    private int totalScore;

    /**
     * Verdict per problem.
     * Key = Posts.id, Value = "AC" | "WA" | "TLE" | "RE" | "CE"
     * Contains only the *latest* verdict for each problem.
     * A problem is "solved" when its value is "AC".
     */
    private Map<String, String> submissions = new HashMap<>();

    /**
     * Count of WRONG attempts per problem (not counting the AC itself).
     * Used for ICPC-style penalty: penalty += wrongAttempts[pid] * 5 minutes
     * for every problem that was eventually solved.
     */
    private Map<String, Integer> wrongAttempts = new HashMap<>();

    // ────────────────────────────────────────────────────────────────────────────
    // Constructors
    // ────────────────────────────────────────────────────────────────────────────
    public ContestSession() {}

    public ContestSession(String contestId, String username) {
        this.contestId = contestId;
        this.username  = username;
        this.joinedAt  = new Date();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Business helpers
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * ICPC-style penalty in minutes.
     * Only AC'd problems contribute: wrongAttempts[pid] * 5 minutes each.
     */
    public int computePenaltyMinutes() {
        int penalty = 0;
        for (Map.Entry<String, String> entry : submissions.entrySet()) {
            if ("AC".equals(entry.getValue())) {
                penalty += wrongAttempts.getOrDefault(entry.getKey(), 0) * 5;
            }
        }
        return penalty;
    }

    /** How many problems have been solved (AC). */
    public int solvedCount() {
        return (int) submissions.values().stream().filter("AC"::equals).count();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ────────────────────────────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContestId() { return contestId; }
    public void setContestId(String contestId) { this.contestId = contestId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

    public Date getLastAcAt() { return lastAcAt; }
    public void setLastAcAt(Date lastAcAt) { this.lastAcAt = lastAcAt; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public Map<String, String> getSubmissions() { return submissions; }
    public void setSubmissions(Map<String, String> submissions) { this.submissions = submissions; }

    public Map<String, Integer> getWrongAttempts() { return wrongAttempts; }
    public void setWrongAttempts(Map<String, Integer> wrongAttempts) { this.wrongAttempts = wrongAttempts; }
}
