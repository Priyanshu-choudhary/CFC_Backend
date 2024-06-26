package com.example.WebSecurityExample.Pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NonNull
    @Indexed(unique = true)
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
    private Integer rating;
    private String city;
    private Date lastModifiedUser;
    private transient int postCount;

    private List<String> roles;

    @DBRef
    private List<Posts> posts = new ArrayList<>();

    @DBRef
    private List<Course> courses = new ArrayList<>();

    // Add a parameterized constructor
    @JsonCreator
    public User(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
