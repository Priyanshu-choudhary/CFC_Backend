package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.ContestRepo;
import com.cfc.platform.MongoRepo.ContestSessionRepository;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.ContestLeaderboardEntry;
import com.cfc.platform.Pojo.ContestSession;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Pojo.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ContestService {

    private static final Logger log = LoggerFactory.getLogger(ContestService.class);

    /** Redis key prefix for cached leaderboards. */
    private static final String LB_KEY_PREFIX  = "contest:leaderboard:";
    /** Leaderboard cache TTL — 15 seconds. */
    private static final long   LB_TTL_SECONDS = 15;

    @Autowired private ContestRepo               contestRepo;
    @Autowired private ContestSessionRepository  sessionRepo;
    @Autowired private UserRepo                  userRepo;
    @Autowired private PostRepo                  postRepo;
    @Autowired private UserService               userService;
    @Autowired private CodeExecutionService      codeExecutionService;
    @Autowired private StringRedisTemplate       redis;
    @Autowired private ObjectMapper              objectMapper;

    // ════════════════════════════════════════════════════════════════════════════
    // READ — existing helpers (unchanged)
    // ════════════════════════════════════════════════════════════════════════════

    public List<Contest> getAllContest() {
        return contestRepo.findAll();
    }

    /** All contests with status=SCHEDULED or LIVE that are public. */
    public List<Contest> getPublicContests() {
        List<Contest> result = new ArrayList<>();
        result.addAll(contestRepo.findByStatusAndIsPublic("SCHEDULED", true));
        result.addAll(contestRepo.findByStatusAndIsPublic("LIVE",      true));
        result.addAll(contestRepo.findByStatusAndIsPublic("ENDED",     true));
        return result;
    }

    public List<Contest> getContestsByStatus(String status) {
        return contestRepo.findByStatus(status);
    }

    public List<Contest> getUserContest(String username) {
        User user = userService.findByName(username);
        return user.getContests();
    }

    public Optional<Contest> getUserContestByID(String id) {
        return contestRepo.findById(id);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CREATE — Public contest
    // ════════════════════════════════════════════════════════════════════════════

    public String createContest(Contest contest, String username) {
        try {
            User myUser = userService.findByName(username);
            if (myUser.getName() == null || myUser.getName().isEmpty())
                throw new IllegalArgumentException("User name cannot be null or empty");

            // Idempotency: same title → return existing ID
            Optional<Contest> existing = myUser.getContests().stream()
                    .filter(c -> c.getNameOfContest().equalsIgnoreCase(contest.getNameOfContest()))
                    .findFirst();
            if (existing.isPresent()) return existing.get().getId();

            contest.setHostUsername(username);
            if (contest.getStatus() == null) contest.setStatus("DRAFT");

            Contest saved = contestRepo.save(contest);
            myUser.getContests().add(saved);
            userService.createUser(myUser);
            return saved.getId();
        } catch (Exception e) {
            log.error("createContest error for {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to create contest", e);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CREATE — Custom room (private contest)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Creates a private room.  Generates a unique 6-char alphanumeric code,
     * marks the contest as private (isPublic=false), and starts immediately
     * (status=SCHEDULED, startedAt = now + buffer so host can share the code).
     */
    public Contest createRoom(Contest contest, String username) {
        contest.setHostUsername(username);
        contest.setPublic(false);
        contest.setStatus("SCHEDULED");
        contest.setRoomCode(generateUniqueRoomCode());

        // Default duration if not set
        if (contest.getDurationMinutes() <= 0) contest.setDurationMinutes(60);

        // Schedule start 1 minute from now so host can share the code
        contest.setStartedAt(new Date(System.currentTimeMillis() + 60_000L));

        Contest saved = contestRepo.save(contest);

        User user = userService.findByName(username);
        user.getContests().add(saved);
        userService.createUser(user);
        return saved;
    }

    /** Find a private room by its 6-char code. */
    public Optional<Contest> findByRoomCode(String roomCode) {
        return contestRepo.findByRoomCode(roomCode.toUpperCase());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LIFECYCLE — Start / End
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Host starts the contest manually.
     * Validates the caller is the host, flips status to LIVE,
     * and pre-computes endedAt = startedAt + durationMinutes.
     */
    public Contest startContest(String contestId, String username) {
        Contest contest = requireContest(contestId);
        requireHost(contest, username);

        if ("LIVE".equals(contest.getStatus()))
            throw new IllegalStateException("Contest is already LIVE");
        if ("ENDED".equals(contest.getStatus()))
            throw new IllegalStateException("Contest has already ended");

        Date now = new Date();
        contest.setStatus("LIVE");
        contest.setStartedAt(now);
        contest.setEndedAt(new Date(now.getTime() + contest.getDurationMinutes() * 60_000L));
        return contestRepo.save(contest);
    }

    /**
     * Host ends the contest early (or the scheduler calls this).
     * Computes final ranks and stores them on every ContestSession.
     */
    public Contest endContest(String contestId, String username) {
        Contest contest = requireContest(contestId);
        if (username != null) requireHost(contest, username);

        if ("ENDED".equals(contest.getStatus()))
            throw new IllegalStateException("Contest has already ended");

        contest.setStatus("ENDED");
        contest.setEndedAt(new Date());
        contest = contestRepo.save(contest);

        // Compute final ranks (wraps Redis internally; safe if Redis is offline)
        computeAndCacheLeaderboard(contest);
        // Evict any stale live-leaderboard entry (no-op if Redis is offline)
        evictLeaderboardCache(contestId);
        return contest;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ARENA — Register + Join
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Register a user for a scheduled public contest (before it starts).
     * Idempotent — calling twice is a no-op.
     */
    public void registerForContest(String contestId, String username) {
        Contest contest = requireContest(contestId);
        if ("ENDED".equals(contest.getStatus()))
            throw new IllegalStateException("Contest has already ended");

        if (!contest.getRegisteredUser().contains(username)) {
            contest.getRegisteredUser().add(username);
            contestRepo.save(contest);
        }
    }

    /**
     * Join the arena when the contest goes LIVE.
     * Creates a ContestSession if one doesn't already exist.
     * Returns the existing session if the user is reconnecting.
     */
    public ContestSession joinArena(String contestId, String username) {
        Contest contest = requireContest(contestId);

        if (!"LIVE".equals(contest.getStatus()))
            throw new IllegalStateException("Contest is not live yet. Status: " + contest.getStatus());

        return sessionRepo.findByContestIdAndUsername(contestId, username)
                .orElseGet(() -> {
                    ContestSession session = new ContestSession(contestId, username);
                    return sessionRepo.save(session);
                });
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SUBMISSION
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Core contest submission pipeline:
     *
     *  1. Guard: contest must be LIVE
     *  2. Guard: user must have a ContestSession (called joinArena first)
     *  3. Guard: problem must be in the contest's problem list
     *  4. Guard: problem must not already be AC'd (no re-submit of solved)
     *  5. Execute code against the problem's test cases (via goboxd)
     *  6. Record result in session — update score, wrongAttempts, lastAcAt
     *  7. Persist session
     *  8. Invalidate leaderboard cache
     *  9. Return verdict + updated score + solved count
     *
     * @param contestId  Contest ID
     * @param username   Submitter
     * @param problemId  Posts.id
     * @param sourceCode Submitted source code
     * @param language   Language identifier (python3, java, cpp, …)
     * @return Map with keys: allPassed, totalScore, penaltyMinutes, solvedCount, results
     */
    public Map<String, Object> submitSolution(
            String contestId,
            String username,
            String problemId,
            String sourceCode,
            String language) {

        // ── Guards ──────────────────────────────────────────────────────────────
        Contest contest = requireContest(contestId);

        if (!"LIVE".equals(contest.getStatus()))
            throw new IllegalStateException("Submissions are closed. Contest status: " + contest.getStatus());

        ContestSession session = sessionRepo.findByContestIdAndUsername(contestId, username)
                .orElseThrow(() -> new IllegalStateException(
                        "You have not joined the arena. Call POST /Contest/" + contestId + "/join first."));

        // Problem must be in the contest
        Posts problem = contest.getPosts().stream()
                .filter(p -> p.getId().equals(problemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Problem " + problemId + " is not part of this contest."));

        // Already solved — block re-submission
        if ("AC".equals(session.getSubmissions().get(problemId)))
            throw new IllegalStateException("Problem already accepted. You cannot re-submit a solved problem.");

        // ── Judge ───────────────────────────────────────────────────────────────
        Map<String, String> testCases = problem.getTestcase();
        if (testCases == null || testCases.isEmpty()) {
            log.warn("Problem {} has no test cases — treating as AC", problemId);
            testCases = Collections.emptyMap();
        }

        Map<String, Object> judgeResult;
        try {
            judgeResult = codeExecutionService.submitWithTestCases(
                    sourceCode, language, testCases,
                    problem.getTimeLimitSeconds(),
                    problem.getMemoryLimitKb()
            );
        } catch (Exception e) {
            log.error("Judge error for contest {} problem {} user {}: {}", contestId, problemId, username, e.getMessage());
            throw new RuntimeException("Execution engine error: " + e.getMessage(), e);
        }

        // ── Record result ────────────────────────────────────────────────────────
        boolean allPassed = Boolean.TRUE.equals(judgeResult.get("allPassed"));
        String verdict = allPassed ? "AC" : deriveVerdict(judgeResult);

        session.getSubmissions().put(problemId, verdict);

        if (allPassed) {
            // Award points
            session.setTotalScore(session.getTotalScore() + contest.getWeight(problemId));
            session.setLastAcAt(new Date());
        } else {
            // Increment wrong-attempt counter (drives ICPC penalty)
            session.getWrongAttempts().merge(problemId, 1, Integer::sum);
        }

        sessionRepo.save(session);

        // Invalidate leaderboard cache so next poll reflects this submission
        evictLeaderboardCache(contestId);

        // ── Build response ───────────────────────────────────────────────────────
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("verdict",        verdict);
        response.put("allPassed",      allPassed);
        response.put("totalScore",     session.getTotalScore());
        response.put("penaltyMinutes", session.computePenaltyMinutes());
        response.put("solvedCount",    session.solvedCount());
        response.put("submissions",    session.getSubmissions());
        response.put("results",        judgeResult.get("results"));  // per-test details
        return response;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LEADERBOARD
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Returns the current leaderboard, served from a 15-second Redis cache.
     * If Redis is unavailable, falls back to computing from MongoDB directly.
     */
    public List<ContestLeaderboardEntry> getLeaderboard(String contestId) {
        String key = LB_KEY_PREFIX + contestId;

        // Try cache first
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached,
                        new TypeReference<List<ContestLeaderboardEntry>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for leaderboard {}: {}", contestId, e.getMessage());
        }

        // Compute from DB
        Contest contest = requireContest(contestId);
        return computeAndCacheLeaderboard(contest);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SESSION (my arena state)
    // ════════════════════════════════════════════════════════════════════════════

    public Optional<ContestSession> getMySession(String contestId, String username) {
        return sessionRepo.findByContestIdAndUsername(contestId, username);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DELETE / UPDATE (existing — unchanged)
    // ════════════════════════════════════════════════════════════════════════════

    public boolean deleteContestById(String id, String username) {
        try {
            contestRepo.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting contest {}: {}", id, e.getMessage());
            return false;
        }
    }

    public Contest updateContest(String id, Contest newContest, String username) {
        Contest existing = contestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contest not found: " + id));
        User user = userService.findByName(username);

        if (!user.getContests().contains(existing))
            throw new RuntimeException("You do not own this contest.");

        // Patch non-null fields
        if (str(newContest.getNameOfContest()))     existing.setNameOfContest(newContest.getNameOfContest());
        if (str(newContest.getNameOfOrganization())) existing.setNameOfOrganization(newContest.getNameOfOrganization());
        if (newContest.getDate() != null)            existing.setDate(newContest.getDate());
        if (str(newContest.getDescription()))        existing.setDescription(newContest.getDescription());
        if (str(newContest.getBannerImage()))        existing.setBannerImage(newContest.getBannerImage());
        if (str(newContest.getLogo()))               existing.setLogo(newContest.getLogo());
        if (str(newContest.getType()))               existing.setType(newContest.getType());
        if (str(newContest.getFee()))                existing.setFee(newContest.getFee());
        if (str(newContest.getTimeDuration()))       existing.setTimeDuration(newContest.getTimeDuration());
        if (newContest.getDurationMinutes() > 0)     existing.setDurationMinutes(newContest.getDurationMinutes());
        if (str(newContest.getStatus()))             existing.setStatus(newContest.getStatus());
        if (list(newContest.getTeam()))              existing.setTeam(newContest.getTeam());
        if (list(newContest.getEligibility()))       existing.setEligibility(newContest.getEligibility());
        if (list(newContest.getRounds()))            existing.setRounds(newContest.getRounds());
        if (list(newContest.getRules()))             existing.setRules(newContest.getRules());
        if (list(newContest.getRewards()))           existing.setRewards(newContest.getRewards());
        if (list(newContest.getFaq()))               existing.setFaq(newContest.getFaq());
        if (list(newContest.getFaqAnswer()))         existing.setFaqAnswer(newContest.getFaqAnswer());
        if (list(newContest.getWinners()))           existing.setWinners(newContest.getWinners());
        if (list(newContest.getLanguage()))          existing.setLanguage(newContest.getLanguage());
        if (list(newContest.getRegisteredUser()))    existing.setRegisteredUser(newContest.getRegisteredUser());

        return contestRepo.save(existing);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SCHEDULER HOOKS  (called from ContestScheduler)
    // ════════════════════════════════════════════════════════════════════════════

    /** Called every 30 s by ContestScheduler — flips SCHEDULED → LIVE. */
    public void tickScheduledToLive() {
        Date now = new Date();
        List<Contest> starting = contestRepo.findByStatusAndStartedAtBefore("SCHEDULED", now);
        for (Contest c : starting) {
            log.info("Contest {} going LIVE", c.getId());
            c.setStatus("LIVE");
            // If endedAt not set, compute it from durationMinutes
            if (c.getEndedAt() == null && c.getStartedAt() != null)
                c.setEndedAt(new Date(c.getStartedAt().getTime() + c.getDurationMinutes() * 60_000L));
            contestRepo.save(c);
        }
    }

    /** Called every 30 s by ContestScheduler — flips LIVE → ENDED. */
    public void tickLiveToEnded() {
        Date now = new Date();
        List<Contest> ending = contestRepo.findByStatusAndEndedAtBefore("LIVE", now);
        for (Contest c : ending) {
            log.info("Contest {} ending (auto)", c.getId());
            endContest(c.getId(), null); // null = scheduler, not user
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════════════

    private Contest requireContest(String id) {
        return contestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found: " + id));
    }

    private void requireHost(Contest contest, String username) {
        boolean isHost = username.equals(contest.getHostUsername())
                || contest.getRegisteredUser().contains(username); // legacy fallback
        if (!isHost) {
            // Also allow ADMIN users — we check via user.getRoles()
            User user = userService.findByName(username);
            boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
            if (!isAdmin)
                throw new SecurityException("Only the contest host can perform this action.");
        }
    }

    /**
     * Sorts sessions ICPC-style and writes to both the return value and Redis.
     * Called at contest end and on cache miss.
     */
    private List<ContestLeaderboardEntry> computeAndCacheLeaderboard(Contest contest) {
        List<ContestSession> sessions = sessionRepo.findByContestId(contest.getId());
        long startMs = contest.getStartedAt() != null ? contest.getStartedAt().getTime() : 0L;

        List<ContestLeaderboardEntry> entries = sessions.stream().map(s -> {
            ContestLeaderboardEntry e = new ContestLeaderboardEntry();
            e.setUsername(s.getUsername());
            e.setTotalScore(s.getTotalScore());
            e.setPenaltyMinutes(s.computePenaltyMinutes());
            e.setSolvedCount(s.solvedCount());
            e.setSubmissions(s.getSubmissions());
            long timeToFinish = (s.getLastAcAt() != null && startMs > 0)
                    ? (s.getLastAcAt().getTime() - startMs) / 1000
                    : Long.MAX_VALUE;
            e.setTimeToFinishSec(timeToFinish);
            return e;
        }).sorted(
                Comparator.comparingInt(ContestLeaderboardEntry::getTotalScore).reversed()
                        .thenComparingInt(ContestLeaderboardEntry::getPenaltyMinutes)
                        .thenComparingLong(ContestLeaderboardEntry::getTimeToFinishSec)
        ).collect(Collectors.toList());

        // Assign ranks (handles ties: same score + penalty + time = same rank)
        int rank = 1;
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                ContestLeaderboardEntry prev = entries.get(i - 1);
                ContestLeaderboardEntry curr = entries.get(i);
                boolean tied = prev.getTotalScore()     == curr.getTotalScore()
                            && prev.getPenaltyMinutes() == curr.getPenaltyMinutes()
                            && prev.getTimeToFinishSec()== curr.getTimeToFinishSec();
                if (!tied) rank = i + 1;
            }
            entries.get(i).setRank(rank);
        }

        // Cache in Redis
        try {
            String json = objectMapper.writeValueAsString(entries);
            redis.opsForValue().set(LB_KEY_PREFIX + contest.getId(), json, LB_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Could not cache leaderboard for {}: {}", contest.getId(), e.getMessage());
        }
        return entries;
    }

    private void evictLeaderboardCache(String contestId) {
        try { redis.delete(LB_KEY_PREFIX + contestId); }
        catch (Exception ignored) {}
    }

    /** Extract a human-readable verdict string from a judge result map. */
    private String deriveVerdict(Map<String, Object> judgeResult) {
        // Check first result's status if available
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) judgeResult.get("results");
            if (results != null && !results.isEmpty()) {
                Object status = results.get(0).get("status");
                if (status != null) {
                    String s = status.toString().toUpperCase();
                    if (s.contains("TIME"))    return "TLE";
                    if (s.contains("MEMORY"))  return "MLE";
                    if (s.contains("COMPILE")) return "CE";
                    if (s.contains("RUNTIME")) return "RE";
                }
            }
        } catch (Exception ignored) {}
        return "WA";
    }

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I to avoid confusion
        Random rng   = new Random();
        String code;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
            code = sb.toString();
            attempts++;
            if (attempts > 50) throw new RuntimeException("Could not generate unique room code");
        } while (contestRepo.findByRoomCode(code).isPresent());
        return code;
    }

    // Null-safe helpers for updateContest
    private boolean str(String s)    { return s != null && !s.isBlank(); }
    private boolean list(List<?> l)  { return l != null && !l.isEmpty(); }
}
