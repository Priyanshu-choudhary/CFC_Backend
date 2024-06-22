package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepo extends MongoRepository<Course, String> {
    Course findByTitle(String title);


}
