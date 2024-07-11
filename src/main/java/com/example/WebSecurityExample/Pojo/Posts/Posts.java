package com.example.WebSecurityExample.Pojo.Posts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.example.WebSecurityExample.Pojo.Posts.HelperCode;
import com.example.WebSecurityExample.Pojo.Course;

import java.util.*;

@Data
@Document(collection = "Posts")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Posts {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @EqualsAndHashCode.Include
    @NonNull
    private String title;
    @NonNull
    private String description;
    private String Example;
    private String difficulty;
    private Map<String, SolutionCode> solution;

    private String answer;
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

    @JsonIgnore
    @DBRef
    private Course course;

    private List<String> tags;

    private Map<String, String> testcase;

    private Map<String, HelperCode> codeTemplates;
}
