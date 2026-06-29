package com.cfc.platform.Pojo;

import com.cfc.platform.Pojo.Posts.Posts;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Document(collection = "UserContestDetails")
public class UserContestDetails {

    @Id
    private String id;

    private String nameOfContest;
    private String nameOfOrganization;
    private Date date;
    private String type;
    private List<String> team = new ArrayList<>();
    private String fee;
    private List<String> rounds = new ArrayList<>();
    private List<String> rewards = new ArrayList<>();
    private List<String> winners = new ArrayList<>();
    private List<String> language = new ArrayList<>();
    private String timeTaken;
    private Date endTime;

    @DBRef
    private List<Posts> posts = new ArrayList<>();

    // No-args constructor
    public UserContestDetails() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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
        UserContestDetails that = (UserContestDetails) o;
        return Objects.equals(id, that.id) && Objects.equals(nameOfContest, that.nameOfContest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameOfContest);
    }
}
