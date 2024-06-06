package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Posts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepo extends MongoRepository<Posts, String> {

}
