package com.cfc.platform.Pojo.Course.CourseDTO;

public class CourseDTO {
    private String id;
    private String title;
    private Integer progress;
    private String totalQuestions;
    private Integer rating;
    private String image;
    private String type;
    private String permission;

    // No-args constructor
    public CourseDTO() {
    }

    // All-args constructor
    public CourseDTO(String id, String title, Integer progress, String totalQuestions,
            Integer rating, String image, String type, String permission) {
        this.id = id;
        this.title = title;
        this.progress = progress;
        this.totalQuestions = totalQuestions;
        this.rating = rating;
        this.image = image;
        this.type = type;
        this.permission = permission;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(String totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
