package com.cfc.platform.Pojo;

import com.cfc.platform.Pojo.Posts.Posts;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

/**
 * Contest document — represents both public scheduled contests and
 * private custom rooms (isPublic=false, joined via roomCode).
 *
 * Status state-machine:
 *   DRAFT → SCHEDULED → LIVE → ENDED
 *                    ↘ CANCELLED
 */
@Document(collection = "Contest")
public class Contest {

    @Id
    private String id;

    // ── Identity ────────────────────────────────────────────────────────────────
    private String nameOfContest;
    private String nameOfOrganization;
    private String hostUsername;      // who created it (set by service layer)

    // ── Scheduling ──────────────────────────────────────────────────────────────
    /**
     * For SCHEDULED contests: when the contest should go LIVE.
     * For custom rooms: set to "now" on creation so it starts immediately
     * when the host clicks Start.
     */
    private Date date;                // legacy / human-readable display date

    /**
     * Actual start timestamp — set by /Contest/{id}/start.
     * The @Scheduled ticker reads this to determine LIVE→ENDED transitions.
     */
    private Date startedAt;

    /**
     * Pre-computed end time = startedAt + durationMinutes * 60 000 ms.
     * Set when the contest goes LIVE so the ticker can use a simple
     * findByStatusAndEndedAtBefore query.
     */
    private Date endedAt;

    /** Contest duration in minutes (default 120 = 2 hours). */
    private int durationMinutes = 120;

    // ── Lifecycle status ────────────────────────────────────────────────────────
    /**
     * DRAFT | SCHEDULED | LIVE | ENDED | CANCELLED
     * Default: DRAFT so a newly created contest isn't publicly visible.
     */
    private String status = "DRAFT";

    // ── Visibility ──────────────────────────────────────────────────────────────
    /** true → listed on the public hub; false → join by roomCode only. */
    private boolean isPublic = true;

    /**
     * 6-character alphanumeric code for private rooms (null for public contests).
     * Indexed so /Contest/join/{roomCode} lookups are O(1).
     */
    @Indexed(sparse = true)
    private String roomCode;

    // ── Scoring ─────────────────────────────────────────────────────────────────
    /**
     * Points awarded per problem.  Key = Posts.id, value = points (default 100).
     * Allows different weights for different difficulty problems.
     */
    private Map<String, Integer> problemWeights = new HashMap<>();

    // ── Content ─────────────────────────────────────────────────────────────────
    private String description;
    private String bannerImage;
    private String logo;
    private String type;              // Hackathon | Quiz | Contest | Room
    private String timeDuration;      // legacy display string
    private String fee;

    private List<String> registeredUser = new ArrayList<>();  // usernames who registered
    private List<String> team           = new ArrayList<>();
    private List<String> eligibility   = new ArrayList<>();
    private List<String> rounds        = new ArrayList<>();
    private List<String> rewards       = new ArrayList<>();
    private List<String> faq           = new ArrayList<>();
    private List<String> faqAnswer     = new ArrayList<>();
    private List<String> rules         = new ArrayList<>();
    private List<String> winners       = new ArrayList<>();
    private List<String> language      = new ArrayList<>();

    @DBRef
    private List<Posts> posts = new ArrayList<>();

    /**
     * Transient carrier — the frontend sends a list of Posts.id values when
     * creating/updating a contest or room. The service layer resolves these
     * against PostRepo and populates {@link #posts}. NOT persisted itself.
     */
    @Transient
    private List<String> problemIds = new ArrayList<>();

    // ────────────────────────────────────────────────────────────────────────────
    // Constructors
    // ────────────────────────────────────────────────────────────────────────────
    public Contest() {}

    // ────────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ────────────────────────────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNameOfContest() { return nameOfContest; }
    public void setNameOfContest(String nameOfContest) { this.nameOfContest = nameOfContest; }

    public String getNameOfOrganization() { return nameOfOrganization; }
    public void setNameOfOrganization(String nameOfOrganization) { this.nameOfOrganization = nameOfOrganization; }

    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getEndedAt() { return endedAt; }
    public void setEndedAt(Date endedAt) { this.endedAt = endedAt; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public Map<String, Integer> getProblemWeights() { return problemWeights; }
    public void setProblemWeights(Map<String, Integer> problemWeights) { this.problemWeights = problemWeights; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBannerImage() { return bannerImage; }
    public void setBannerImage(String bannerImage) { this.bannerImage = bannerImage; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimeDuration() { return timeDuration; }
    public void setTimeDuration(String timeDuration) { this.timeDuration = timeDuration; }

    public String getFee() { return fee; }
    public void setFee(String fee) { this.fee = fee; }

    public List<String> getRegisteredUser() { return registeredUser; }
    public void setRegisteredUser(List<String> registeredUser) { this.registeredUser = registeredUser; }

    public List<String> getTeam() { return team; }
    public void setTeam(List<String> team) { this.team = team; }

    public List<String> getEligibility() { return eligibility; }
    public void setEligibility(List<String> eligibility) { this.eligibility = eligibility; }

    public List<String> getRounds() { return rounds; }
    public void setRounds(List<String> rounds) { this.rounds = rounds; }

    public List<String> getRewards() { return rewards; }
    public void setRewards(List<String> rewards) { this.rewards = rewards; }

    public List<String> getFaq() { return faq; }
    public void setFaq(List<String> faq) { this.faq = faq; }

    public List<String> getFaqAnswer() { return faqAnswer; }
    public void setFaqAnswer(List<String> faqAnswer) { this.faqAnswer = faqAnswer; }

    public List<String> getRules() { return rules; }
    public void setRules(List<String> rules) { this.rules = rules; }

    public List<String> getWinners() { return winners; }
    public void setWinners(List<String> winners) { this.winners = winners; }

    public List<String> getLanguage() { return language; }
    public void setLanguage(List<String> language) { this.language = language; }

    public List<Posts> getPosts() { return posts; }
    public void setPosts(List<Posts> posts) { this.posts = posts; }

    public List<String> getProblemIds() { return problemIds; }
    public void setProblemIds(List<String> problemIds) { this.problemIds = problemIds; }

    // ────────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────────

    /** Points for a given problem — falls back to 100 if not explicitly set. */
    public int getWeight(String problemId) {
        return problemWeights.getOrDefault(problemId, 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contest contest = (Contest) o;
        return Objects.equals(id, contest.id) && Objects.equals(nameOfContest, contest.nameOfContest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameOfContest);
    }
}
