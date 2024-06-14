package com.example.WebSecurityExample.Pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "Course")
@NoArgsConstructor
public class Course {

    @Id
    private String id;
    private String title;
    private String description;
    private String progress;
    private String totalQuestions;
    private String completeQuestions;

    @DBRef
    private List<Posts> posts=new ArrayList<>();


}
