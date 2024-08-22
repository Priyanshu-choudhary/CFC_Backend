package com.example.WebSecurityExample.CustomQuery.Lecture;

import com.example.WebSecurityExample.Pojo.Lecture.Lecture;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Repository
public class CustomQueryLectureImpl implements CustomQueryLecture {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Logger logger = LoggerFactory.getLogger(CustomQueryLectureImpl.class);

    @Override
    public Optional<Lecture> findLectureByUsernameAndTitle(String username, String lectureTitle) {
        logger.info("Finding lecture with title '{}' for user '{}'", lectureTitle, username);

        // Build query to find the user with the specified username
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(username));

        // Project only the lectures field in the result
        query.fields().include("lectures");
        logger.debug("Query built: {}", query.toString());

        // Execute the query to find the user
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null) {
            logger.warn("No user found with username '{}'", username);
            return Optional.empty();
        }

        if (user.getLectures() == null || user.getLectures().isEmpty()) {
            logger.warn("No lectures found for user '{}'", username);
            return Optional.empty();
        }

        logger.info("User '{}' found, searching for lecture with title '{}'", username, lectureTitle);

        // Find the specific lecture within the user's lectures list
        Optional<Lecture> lecture = user.getLectures().stream()
                .filter(l -> l.getTitle().equals(lectureTitle))
                .findFirst();

        if (lecture.isPresent()) {
            logger.info("Lecture '{}' found for user '{}'", lectureTitle, username);
        } else {
            logger.warn("Lecture '{}' not found for user '{}'", lectureTitle, username);
        }

        return lecture;
    }
}
