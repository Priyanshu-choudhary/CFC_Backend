package com.example.WebSecurityExample.Pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    @NonNull
    private String answer;



//    private String id;
//    private String question;
//    private String solution;
//    private String output;
//    private String testCase;





}
