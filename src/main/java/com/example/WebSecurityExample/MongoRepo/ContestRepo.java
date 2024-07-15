package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Contest;
import com.example.WebSecurityExample.Pojo.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContestRepo extends MongoRepository<Contest, String> {
    Contest findByNameOfContest(String nameOfContest);


}
