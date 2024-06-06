package com.example.WebSecurityExample.Pojo;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String password;
    private String number;
    private String email;
    private String collage;
    private String branch;
    private String year;
    private String skills;
    private String badges;


    private List<String> roles;

    // Getters and setters
    @DBRef
    private List<Posts> posts=new ArrayList<>();

}
