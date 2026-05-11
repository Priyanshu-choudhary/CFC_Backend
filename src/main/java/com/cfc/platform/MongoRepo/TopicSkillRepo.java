package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.TopicWiseSkills.TopicSkill;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopicSkillRepo  extends MongoRepository<TopicSkill, String> {

}
