package com.example.WebSecurityExample.Pojo.Lecture;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lectures-3") // MongoDB collection name
public class Lecture {

    @Id
    private String id; // Unique ID for each lecture
    private String title; // Title of the lecture
    private String author; // Author of the lecture
    private List<Heading> headings; // List of headings within the lecture

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Heading {
        private String title; // Title of the heading
        private List<SubHeading> subHeadings; // List of subheadings under the heading
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubHeading {
        private String title; // Title of the subheading
        private String content; // Content of the subheading
    }
}
