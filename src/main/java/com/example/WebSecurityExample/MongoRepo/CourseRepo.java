package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CourseRepo extends MongoRepository<Course, String> {
    Course findByTitle(String title);


}
