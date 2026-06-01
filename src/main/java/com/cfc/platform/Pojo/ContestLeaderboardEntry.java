package com.cfc.platform.Pojo;

import java.util.Map;

/**
 * Leaderboard row — returned from GET /Contest/{id}/leaderboard.
 *
 * Sorting (applied by ContestService before caching):
 *   1. totalScore      DESC  — more points = better
 *   2. penaltyMinutes  ASC   — fewer wrong attempts = better
 *   3. timeToFinishSec ASC   — faster = better (ICPC-style tiebreaker)
 */
public class ContestLeaderboardEntry {

    private int    rank;
    private String username;
    private int    totalScore;
    private int    penaltyMinutes;
    private long   timeToFinishSec;  // seconds from contest start to last AC; MAX_VALUE if not finished
    private int    solvedCount;
    private Map<String, String> submissions;  // problemId → AC/WA/TLE/RE/CE

    public ContestLeaderboardEntry() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getPenaltyMinutes() { return penaltyMinutes; }
    public void setPenaltyMinutes(int penaltyMinutes) { this.penaltyMinutes = penaltyMinutes; }

    public long getTimeToFinishSec() { return timeToFinishSec; }
    public void setTimeToFinishSec(long timeToFinishSec) { this.timeToFinishSec = timeToFinishSec; }

    public int getSolvedCount() { return solvedCount; }
    public void setSolvedCount(int solvedCount) { this.solvedCount = solvedCount; }

    public Map<String, String> getSubmissions() { return submissions; }
    public void setSubmissions(Map<String, String> submissions) { this.submissions = submissions; }
}
