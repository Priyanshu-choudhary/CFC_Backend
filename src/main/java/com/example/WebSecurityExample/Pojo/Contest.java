package com.example.WebSecurityExample.Pojo;

import com.example.WebSecurityExample.Pojo.Posts.Posts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "Contest")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contest {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @EqualsAndHashCode.Include
    private String nameOfContest;
    private String nameOfOrganization;
    private Date date;
    private String description;
    private String bannerImage;
    private String logo;
    private String type;
    private List<String> registeredUser =new ArrayList<>();
    private List<String> team =new ArrayList<>();
    private String fee ;
    private List<String> eligibility =new ArrayList<>();
    private List<String> rounds =new ArrayList<>();
    private List<String> rewards =new ArrayList<>();
    private List<String> faq =new ArrayList<>();
    private List<String> faqAnswer =new ArrayList<>();
    private List<String> rules =new ArrayList<>();
    private List<String> winners =new ArrayList<>();
    private List<String> language=new ArrayList<>();
    private String timeDuration;


    @DBRef
    private List<Posts> posts =new ArrayList<>();

}
