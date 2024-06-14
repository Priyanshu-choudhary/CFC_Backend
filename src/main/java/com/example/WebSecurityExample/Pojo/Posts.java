package com.example.WebSecurityExample.Pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.List;
@Data
@Document(collection = "Posts")
@NoArgsConstructor
public class Posts {
    @Id
    private String id;
    @NonNull
    private String title;
    @NonNull
    private String description;
    private String Example;
    private String difficulty;
    private String solution;
    private String answer;
    private String constrain;
    private String timecomplixity;
    private String avgtime;
    private String boilerCode;
    private Date lastModified; // Add this field

    @NonNull
    private List<String> tags;

    private Map<String,String> testcase;
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }




//    private String id;
//    private String question;
//    private String solution;
//    private String output;
//    private String testCase;





}
