package com.example.WebSecurityExample.MongoRepo;


import com.example.WebSecurityExample.Pojo.Lecture.Lecture;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LectureRepo extends MongoRepository<Lecture, String> {

}
