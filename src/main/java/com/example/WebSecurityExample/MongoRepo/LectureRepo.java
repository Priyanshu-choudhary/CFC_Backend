package com.example.WebSecurityExample.MongoRepo;


import com.example.WebSecurityExample.CustomQuery.Lecture.CustomQueryLecture;
import com.example.WebSecurityExample.Pojo.Lecture.Lecture;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LectureRepo extends MongoRepository<Lecture, String> , CustomQueryLecture {
    Lecture findByTitle(String title);
}
