package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepo extends MongoRepository<Posts, String> {
    List<Posts> findByTagsIn(List<String> tags);

}
