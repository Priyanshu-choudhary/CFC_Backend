package com.example.WebSecurityExample.Pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;


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
    private Integer progress;
    private String totalQuestions;
    private List<String> completeQuestions=new ArrayList<>();
    private Integer rating;
    private Integer newRating;
    private String image;
    private String type;
    private String permission;




    @DBRef
    private List<Posts> posts =new ArrayList<>();
}
