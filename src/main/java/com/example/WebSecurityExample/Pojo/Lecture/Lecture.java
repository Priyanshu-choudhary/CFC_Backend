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
@Document(collection = "lectures") // MongoDB collection name
public class Lecture {

    @Id
    private String id; // Unique ID for each lecture
    private String title; // Title of the lecture
    private String subtitle; // Subtitle of the lecture
    private List<Section> sections; // List of sections within the lecture

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String id; // Unique ID for each section (used for navigation)
        private String heading; // Heading of the section
        private String content; // Content of the section
    }
}