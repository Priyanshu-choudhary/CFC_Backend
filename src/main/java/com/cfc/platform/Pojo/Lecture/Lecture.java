package com.cfc.platform.Pojo.Lecture;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "lectures-3") // MongoDB collection name
public class Lecture {

    @Id
    private String id; // Unique ID for each lecture
    private String title; // Title of the lecture
    private String author; // Author of the lecture
    private List<Heading> headings; // List of headings within the lecture

    // No-args constructor
    public Lecture() {
    }

    // All-args constructor
    public Lecture(String id, String title, String author, List<Heading> headings) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.headings = headings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Heading> getHeadings() {
        return headings;
    }

    public void setHeadings(List<Heading> headings) {
        this.headings = headings;
    }

    // Nested Heading class
    public static class Heading {
        private String title; // Title of the heading
        private List<SubHeading> subHeadings; // List of subheadings under the heading

        // No-args constructor
        public Heading() {
        }

        // All-args constructor
        public Heading(String title, List<SubHeading> subHeadings) {
            this.title = title;
            this.subHeadings = subHeadings;
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<SubHeading> getSubHeadings() {
            return subHeadings;
        }

        public void setSubHeadings(List<SubHeading> subHeadings) {
            this.subHeadings = subHeadings;
        }
    }

    // Nested SubHeading class
    public static class SubHeading {
        private String title; // Title of the subheading
        private String content; // Content of the subheading

        // No-args constructor
        public SubHeading() {
        }

        // All-args constructor
        public SubHeading(String title, String content) {
            this.title = title;
            this.content = content;
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
