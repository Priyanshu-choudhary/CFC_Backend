package com.cfc.platform.controller;

import com.cfc.platform.MongoRepo.ContestRepo;
import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.ContestLeaderboardEntry;
import com.cfc.platform.Pojo.ContestSession;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Pojo.User;
import com.cfc.platform.Service.ContestService;
import com.cfc.platform.Service.PostService;
import com.cfc.platform.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ContestController — REST layer for the CFC contest engine.
 *
 * ─────────────────────────────────────────────────────
 *  Existing (unchanged)
 * ─────────────────────────────────────────────────────
 *  GET    /Contest                  list all (legacy)
 *  GET    /Contest/{username}       user's contests
 *  GET    /Contest/id/{id}          by ID
 *  POST   /Contest                  create public contest
 *  POST   /Contest/{cN}/username/{u}  add problem to contest
 *  DELETE /Contest/id/{id}          delete
 *  PUT    /Contest/id/{id}          update
 *
 * ─────────────────────────────────────────────────────
 *  New Phase-1 endpoints
 * ─────────────────────────────────────────────────────
 *  GET    /Contest/public            public hub (SCHEDULED/LIVE/ENDED)
 *  GET    /Contest/status/{status}   filter by status
 *  POST   /Contest/room              create private room → returns roomCode
 *  GET    /Contest/join/{roomCode}   find room by code (no-auth)
 *  POST   /Contest/{id}/start        host starts contest
 *  POST   /Contest/{id}/end          host ends contest
 *  POST   /Contest/{id}/register     user registers for public contest
 *  POST   /Contest/{id}/join         user enters arena → creates session
 *  GET    /Contest/{id}/session/me   my current session state
 *  POST   /Contest/{id}/submit       submit solution during LIVE window
 *  GET    /Contest/{id}/leaderboard  live/final leaderboard (Redis-cached)
 */
@RestController
@RequestMapping("/Contest")
public class ContestController {

    private static final Logger log = LoggerFactory.getLogger(ContestController.class);

    @Autowired private ContestService contestService;
    @Autowired private ContestRepo    contestRepo;
    @Autowired private PostService    postService;
    @Autowired private UserService    userService;

    // ════════════════════════════════════════════════════════════════════════════
    //  READ
    // ════════════════════════════════════════════════════════════════════════════

    /** Legacy: returns ALL contests (no filter). Kept for backward compatibility. */
    @GetMapping
    public ResponseEntity<?> getAllContests() {
        try {
            List<Contest> all = contestService.getAllContest();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            log.error("getAllContests: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Public contest hub — returns SCHEDULED + LIVE + ENDED public contests.
     * No auth required (permitAll in security config).
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicContests() {
        try {
            return ResponseEntity.ok(contestService.getPublicContests());
        } catch (Exception e) {
            log.error("getPublicContests: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Filter by status: DRAFT | SCHEDULED | LIVE | ENDED */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(contestService.getContestsByStatus(status));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getContestsByUser(@PathVariable String username) {
        try {
            List<Contest> all = contestService.getUserContest(username);
            return all != null ? ResponseEntity.ok(all) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getContestById(@PathVariable String id) {
        try {
            Optional<Contest> c = contestService.getUserContestByID(id);
            return c.isPresent() ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Join a private room by 6-char code.
     * No auth required so anyone can look up a room to see its details.
     */
    @GetMapping("/join/{roomCode}")
    public ResponseEntity<?> findRoomByCode(@PathVariable String roomCode) {
        try {
            return contestService.findByRoomCode(roomCode)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("findRoomByCode {}: {}", roomCode, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  CREATE
    // ════════════════════════════════════════════════════════════════════════════

    /** Create a public scheduled contest. */
    @PostMapping
    public ResponseEntity<?> createContest(@RequestBody Contest contest) {
        try {
            String username = currentUser();
            String id = contestService.createContest(contest, username);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("contestId", id));
        } catch (Exception e) {
            log.error("createContest: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a private custom room.
     * Returns the full Contest object which includes the roomCode.
     */
    @PostMapping("/room")
    public ResponseEntity<?> createRoom(@RequestBody Contest contest) {
        try {
            String username = currentUser();
            Contest saved = contestService.createRoom(contest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("createRoom: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Add an existing problem (by problem ID) to a contest. */
    @PostMapping("/{contestName}/username/{username}")
    public ResponseEntity<?> addProblemToContest(
            @PathVariable String username,
            @PathVariable String contestName,
            @RequestBody Posts post) {
        try {
            User user = userService.findByName(username);
            Optional<Contest> contestOpt = user.getContests().stream()
                    .filter(c -> c.getNameOfContest().equals(contestName))
                    .findFirst();

            if (contestOpt.isEmpty()) return ResponseEntity.notFound().build();

            Contest contest = contestOpt.get();
            post.setLastModified(new Date());
            post.setContest(contest);
            postService.createPostWithRefContest(post, user, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Host starts the contest → SCHEDULED/DRAFT → LIVE.
     * Sets startedAt = now, endedAt = now + durationMinutes.
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startContest(@PathVariable String id) {
        try {
            Contest contest = contestService.startContest(id, currentUser());
            return ResponseEntity.ok(Map.of(
                    "message",   "Contest is now LIVE",
                    "status",    contest.getStatus(),
                    "startedAt", contest.getStartedAt(),
                    "endedAt",   contest.getEndedAt()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("startContest {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Host ends the contest early → LIVE → ENDED.
     * Final ranks are computed and cached.
     */
    @PostMapping("/{id}/end")
    public ResponseEntity<?> endContest(@PathVariable String id) {
        try {
            Contest contest = contestService.endContest(id, currentUser());
            return ResponseEntity.ok(Map.of(
                    "message", "Contest ended",
                    "status",  contest.getStatus(),
                    "endedAt", contest.getEndedAt()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("endContest {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  REGISTRATION & ARENA JOIN
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Register for a public contest (before it starts).
     * Idempotent.
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<?> register(@PathVariable String id) {
        try {
            contestService.registerForContest(id, currentUser());
            return ResponseEntity.ok(Map.of("message", "Registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Enter the arena when the contest is LIVE.
     * Creates a ContestSession and returns it.
     * Idempotent — reconnecting returns the existing session.
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinArena(@PathVariable String id) {
        try {
            ContestSession session = contestService.joinArena(id, currentUser());
            return ResponseEntity.ok(session);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("joinArena {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Get the caller's current ContestSession (score, submissions, solved count). */
    @GetMapping("/{id}/session/me")
    public ResponseEntity<?> getMySession(@PathVariable String id) {
        try {
            return contestService.getMySession(id, currentUser())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  SUBMISSION
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Submit code against a contest problem.
     *
     * Request body:
     * {
     *   "problemId": "6634abc...",
     *   "sourceCode": "def solution...",
     *   "language": "python3"
     * }
     *
     * Response:
     * {
     *   "verdict": "AC" | "WA" | "TLE" | "RE" | "CE",
     *   "allPassed": true,
     *   "totalScore": 200,
     *   "penaltyMinutes": 5,
     *   "solvedCount": 2,
     *   "submissions": {"pid1": "AC", "pid2": "WA"},
     *   "results": [...]    // per-test-case details from goboxd
     * }
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String problemId  = body.get("problemId");
            String sourceCode = body.get("sourceCode");
            String language   = body.get("language");

            if (problemId == null || sourceCode == null || language == null)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Required fields: problemId, sourceCode, language"));

            Map<String, Object> result = contestService.submitSolution(
                    id, currentUser(), problemId, sourceCode, language);
            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {
            // Already solved, contest closed, not joined
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("submit contest={} user={}: {}", id, currentUser(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  LEADERBOARD
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Live and final leaderboard.
     * Served from a 15-second Redis cache — safe to poll every 10 seconds.
     * No auth required (public view).
     */
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable String id) {
        try {
            List<ContestLeaderboardEntry> lb = contestService.getLeaderboard(id);
            return ResponseEntity.ok(lb);
        } catch (Exception e) {
            log.error("getLeaderboard {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  DELETE / UPDATE (unchanged)
    // ════════════════════════════════════════════════════════════════════════════

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Map<String, String>> deleteContest(@PathVariable String id) {
        try {
            String username = currentUser();
            boolean deleted = contestService.deleteContestById(id, username);
            if (deleted) return ResponseEntity.ok(Map.of("message", "Contest deleted successfully"));
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error deleting contest"));
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateContest(@PathVariable String id, @RequestBody Contest newContest) {
        try {
            Contest updated = contestService.updateContest(id, newContest, currentUser());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
