package com.cfc.platform.Pojo;

import com.cfc.platform.Pojo.Posts.Posts;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Document(collection = "Contest")
public class Contest {

    @Id
    private String id;

    private String nameOfContest;
    private String nameOfOrganization;
    private Date date;
    private String description;
    private String bannerImage;
    private String logo;
    private String type;
    private List<String> registeredUser = new ArrayList<>();
    private List<String> team = new ArrayList<>();
    private String fee;
    private List<String> eligibility = new ArrayList<>();
    private List<String> rounds = new ArrayList<>();
    private List<String> rewards = new ArrayList<>();
    private List<String> faq = new ArrayList<>();
    private List<String> faqAnswer = new ArrayList<>();
    private List<String> rules = new ArrayList<>();
    private List<String> winners = new ArrayList<>();
    private List<String> language = new ArrayList<>();
    private String timeDuration;

    @DBRef
    private List<Posts> posts = new ArrayList<>();

    // No-args constructor
    public Contest() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameOfContest() {
        return nameOfContest;
    }

    public void setNameOfContest(String nameOfContest) {
        this.nameOfContest = nameOfContest;
    }

    public String getNameOfOrganization() {
        return nameOfOrganization;
    }

    public void setNameOfOrganization(String nameOfOrganization) {
        this.nameOfOrganization = nameOfOrganization;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getRegisteredUser() {
        return registeredUser;
    }

    public void setRegisteredUser(List<String> registeredUser) {
        this.registeredUser = registeredUser;
    }

    public List<String> getTeam() {
        return team;
    }

    public void setTeam(List<String> team) {
        this.team = team;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public List<String> getEligibility() {
        return eligibility;
    }

    public void setEligibility(List<String> eligibility) {
        this.eligibility = eligibility;
    }

    public List<String> getRounds() {
        return rounds;
    }

    public void setRounds(List<String> rounds) {
        this.rounds = rounds;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public void setRewards(List<String> rewards) {
        this.rewards = rewards;
    }

    public List<String> getFaq() {
        return faq;
    }

    public void setFaq(List<String> faq) {
        this.faq = faq;
    }

    public List<String> getFaqAnswer() {
        return faqAnswer;
    }

    public void setFaqAnswer(List<String> faqAnswer) {
        this.faqAnswer = faqAnswer;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public List<String> getWinners() {
        return winners;
    }

    public void setWinners(List<String> winners) {
        this.winners = winners;
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public String getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(String timeDuration) {
        this.timeDuration = timeDuration;
    }

    public List<Posts> getPosts() {
        return posts;
    }

    public void setPosts(List<Posts> posts) {
        this.posts = posts;
    }

    // equals and hashCode (based on id and nameOfContest)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Contest contest = (Contest) o;
        return Objects.equals(id, contest.id) && Objects.equals(nameOfContest, contest.nameOfContest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameOfContest);
    }
}
