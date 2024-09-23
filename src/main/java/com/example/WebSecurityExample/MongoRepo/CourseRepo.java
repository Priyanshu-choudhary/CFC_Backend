package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Course.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRepo extends MongoRepository<Course, String> {
    Course findByTitle(String title);
    Page<Course> findByUserName(String userName, Pageable pageable);

}
