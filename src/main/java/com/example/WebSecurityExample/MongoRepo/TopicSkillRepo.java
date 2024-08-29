package com.example.WebSecurityExample.MongoRepo;

import com.example.WebSecurityExample.Pojo.TopicWiseSkills.TopicSkill;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopicSkillRepo  extends MongoRepository<TopicSkill, String> {

}
