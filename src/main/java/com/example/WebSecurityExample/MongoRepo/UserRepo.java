package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {
    User findByName(String name);

    void deleteByName(String usermane);
}
