package com.example.WebSecurityExample.Pojo.Course.CourseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {
    private String id;
    private String title;
    private Integer progress;
    private String totalQuestions;
    private Integer rating;
    private String image;
    private String type;
    private String permission;
}
