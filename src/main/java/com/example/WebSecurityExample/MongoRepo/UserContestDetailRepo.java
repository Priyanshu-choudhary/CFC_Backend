package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Contest;
import com.example.WebSecurityExample.Pojo.UserContestDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserContestDetailRepo extends MongoRepository<UserContestDetails, String> {
    Contest findByNameOfContest(String nameOfContest);
}
