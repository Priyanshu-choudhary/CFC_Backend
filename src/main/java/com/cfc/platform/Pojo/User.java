package com.cfc.platform.Pojo;

import com.cfc.platform.Pojo.Course.Course;
import com.cfc.platform.Pojo.Lecture.Lecture;
import com.cfc.platform.Pojo.TopicWiseSkills.TopicSkill;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String number;
    private String email;
    private String collage;
    private String branch;
    private String year;
    private String skills;
    private String badges;
    private String profileImg;
    private Integer rating;
    private String city;
    private Date lastModifiedUser;
    private transient int postCount;

    private List<String> roles;

    @DBRef
    private List<Course> courses = new ArrayList<>();

    @DBRef
    private List<Contest> contests = new ArrayList<>();

    @DBRef
    private List<UserContestDetails> userContestDetails = new ArrayList<>();

    @DBRef
    private List<Lecture> lectures = new ArrayList<>();

    @DBRef
    private List<TopicSkill> topicSkill = new ArrayList<>();

    // No-args constructor
    public User() {
    }

    // Parameterized constructor
    @JsonCreator
    public User(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollage() {
        return collage;
    }

    public void setCollage(String collage) {
        this.collage = collage;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getBadges() {
        return badges;
    }

    public void setBadges(String badges) {
        this.badges = badges;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Date lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Contest> getContests() {
        return contests;
    }

    public void setContests(List<Contest> contests) {
        this.contests = contests;
    }

    public List<UserContestDetails> getUserContestDetails() {
        return userContestDetails;
    }

    public void setUserContestDetails(List<UserContestDetails> userContestDetails) {
        this.userContestDetails = userContestDetails;
    }

    public List<Lecture> getLectures() {
        return lectures;
    }

    public void setLectures(List<Lecture> lectures) {
        this.lectures = lectures;
    }

    public List<TopicSkill> getTopicSkill() {
        return topicSkill;
    }

    public void setTopicSkill(List<TopicSkill> topicSkill) {
        this.topicSkill = topicSkill;
    }
}
