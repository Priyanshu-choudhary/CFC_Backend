package com.example.WebSecurityExample.CustomQuery;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class FilterUsersNameOfContestImpl implements FilterUsersNameOfContest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Document> findUsersByContestName(String contestName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection("users");

        List<Document> pipeline = Arrays.asList(
                new Document("$lookup",
                        new Document("from", "UserContestDetails")
                                .append("localField", "userContestDetails.$id")
                                .append("foreignField", "_id")
                                .append("as", "contestDetails")
                ),
                new Document("$unwind", "$contestDetails"),
                new Document("$match",
                        new Document("contestDetails.nameOfContest", contestName)
                ),
                new Document("$lookup",
                        new Document("from", "Posts")
                                .append("localField", "contestDetails.posts.id")
                                .append("foreignField", "_id")
                                .append("as", "detailedPosts")
                ),
                new Document("$project",
                        new Document("name", 1)
                                .append("email", 1)
                                .append("userContestDetails", 1)
                                .append("contestDetails", 1)
                                .append("detailedPosts", 1)
                )
        );

        return collection.aggregate(pipeline).into(new ArrayList<>());
    }
}
