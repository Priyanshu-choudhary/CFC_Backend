package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.UserContestDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserContestDetailRepo extends MongoRepository<UserContestDetails, String> {
    Contest findByNameOfContest(String nameOfContest);
}
