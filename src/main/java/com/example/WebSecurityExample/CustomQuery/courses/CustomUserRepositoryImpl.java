package com.example.WebSecurityExample.CustomQuery.courses;

import com.example.WebSecurityExample.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> findOfficialCoursesByName(String name, int skip , int limit) {
        try {

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("name").is(name)),
                    Aggregation.project()
                            .and(ArrayOperators.Slice.sliceArrayOf("courses").offset(skip).itemCount(limit)).as("courses")
            );

            AggregationResults<User> results = mongoTemplate.aggregate(aggregation, "users", User.class);

            return results.getMappedResults();
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception or handle it as needed
            return null;
        }
    }
}
