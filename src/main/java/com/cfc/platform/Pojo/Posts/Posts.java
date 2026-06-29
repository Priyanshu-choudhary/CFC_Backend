package com.cfc.platform.Pojo.Posts;

import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.UserContestDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.cfc.platform.Pojo.Course.Course;

import java.util.*;

@Document(collection = "Posts")
public class Posts {
    @Id
    private String id;

    private String title;
    private int contestId;
    private String index;
    private String description;
    private String userName;
    private String Example;
    private String difficulty;
    private Map<String, SolutionCode> solution = new HashMap<>();
    private List<String> answer = new ArrayList<>();
    private List<String> companies = new ArrayList<>();
    private String accuracy;
    private String constrain;
    private String timecomplixity;
    private String avgtime;
    private Date lastModified;
    private String type;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String videoUrl;
    private String sequence;
    private String input;

    @JsonIgnore
    @DBRef
    private Course course;

    @JsonIgnore
    @DBRef
    private Contest contest;

    @JsonIgnore
    @DBRef
    private UserContestDetails userContestDetails;

    private List<String> tags = new ArrayList<>();

    private Map<String, String> testcase = new HashMap<>();

    private Map<String, HelperCode> codeTemplates = new HashMap<>();

    // Judge0 execution limits (null = use defaults: 5s, 256MB)
    private Double timeLimitSeconds;
    private Integer memoryLimitKb;
    private List<Integer> allowedLanguageIds;

    // No-args constructor
    public Posts() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setContestId(int contestId) {
        this.contestId=contestId;
    }
    public void setIndex(String index) {
            this.index=index;
        }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public int getContestId( ) {
        return  contestId;
    }
    public String getIndex( ) {
        return  index;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExample() {
        return Example;
    }

    public void setExample(String example) {
        Example = example;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Map<String, SolutionCode> getSolution() {
        return solution;
    }

    public void setSolution(Map<String, SolutionCode> solution) {
        this.solution = solution;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public List<String> getCompanies() {
        return companies;
    }

    public void setCompanies(List<String> companies) {
        this.companies = companies;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getConstrain() {
        return constrain;
    }

    public void setConstrain(String constrain) {
        this.constrain = constrain;
    }

    public String getTimecomplixity() {
        return timecomplixity;
    }

    public void setTimecomplixity(String timecomplixity) {
        this.timecomplixity = timecomplixity;
    }

    public String getAvgtime() {
        return avgtime;
    }

    public void setAvgtime(String avgtime) {
        this.avgtime = avgtime;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public UserContestDetails getUserContestDetails() {
        return userContestDetails;
    }

    public void setUserContestDetails(UserContestDetails userContestDetails) {
        this.userContestDetails = userContestDetails;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getTestcase() {
        return testcase;
    }

    public void setTestcase(Map<String, String> testcase) {
        this.testcase = testcase;
    }

    public Map<String, HelperCode> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(Map<String, HelperCode> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public Double getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Double timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public Integer getMemoryLimitKb() { return memoryLimitKb; }
    public void setMemoryLimitKb(Integer memoryLimitKb) { this.memoryLimitKb = memoryLimitKb; }

    public List<Integer> getAllowedLanguageIds() { return allowedLanguageIds; }
    public void setAllowedLanguageIds(List<Integer> allowedLanguageIds) { this.allowedLanguageIds = allowedLanguageIds; }

    // equals and hashCode (based on id and title)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Posts posts = (Posts) o;
        return Objects.equals(id, posts.id) && Objects.equals(title, posts.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
