package com.cfc.platform.MongoRepo;


import com.cfc.platform.CustomQuery.Lecture.CustomQueryLecture;
import com.cfc.platform.Pojo.Lecture.Lecture;
import com.cfc.platform.Pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LectureRepo extends MongoRepository<Lecture, String> , CustomQueryLecture {
    Lecture findByTitle(String title);
}
