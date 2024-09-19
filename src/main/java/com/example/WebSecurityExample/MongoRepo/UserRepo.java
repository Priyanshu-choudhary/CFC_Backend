package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.CustomQuery.FilterUsersNameOfContest;
import com.example.WebSecurityExample.CustomQuery.courses.CustomUserRepository;
import com.example.WebSecurityExample.Pojo.Posts.Posts;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepo extends MongoRepository<User, String>, FilterUsersNameOfContest, CustomUserRepository {

    User findByName(String name);

    @Query("{ 'name': ?0 }")
    User findByQuery(String name);
//    @Query(value = "{ 'name': ?0 }", fields = "{ '_class': 0, 'contests': 0, 'userContestDetails': 0, 'posts': 0, 'name': 0, 'password': 0, 'postCount': 0, 'roles': 0, 'courses': { '$slice': [0, 1000] } }")
//    List<User> findOfficialCoursesByNameRandom(String name);


}
