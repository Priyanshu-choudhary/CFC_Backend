package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.CustomQuery.FilterUsersNameOfContest;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepo extends MongoRepository<User, String>, FilterUsersNameOfContest {

    User findByName(String name);

    @Query("{ 'name': ?0 }")
    User findByQuery(String name);


}
