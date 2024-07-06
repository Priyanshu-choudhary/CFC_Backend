package com.example.WebSecurityExample.service;

import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepo userRepo;

    @Test
    public void testFindByUserNameRepo(){
        assertNotNull(userRepo.findByName("OfficialCources"));

    }

    @Test
    public void testFindByUserByQueary(){
        assertNotNull(userRepo.findByQuery("OfficialCources"));

    }

    @Test
    public void testFindByQuery() {
        String userName = "OfficialCources";
        long startTime = System.currentTimeMillis();
        User user = userRepo.findByName(userName);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time of findByQuery: " + (endTime - startTime) + " ms");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(userName);
    }
}
