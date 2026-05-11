package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.Contest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContestRepo extends MongoRepository<Contest, String> {
    Contest findByNameOfContest(String nameOfContest);


}
