package com.example.WebSecurityExample.Pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
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
    private Date lastModified;
    private String type;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @JsonIgnore
    @DBRef
    private Course course;
//    private List<Course> course=new ArrayList<>();


    @NonNull
    private List<String> tags;

    private Map<String,String> testcase;

}
