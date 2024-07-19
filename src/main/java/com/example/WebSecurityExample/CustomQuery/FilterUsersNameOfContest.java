package com.example.WebSecurityExample.CustomQuery;
import org.bson.Document;
import java.util.List;

public interface FilterUsersNameOfContest {
    List<Document> findUsersByContestName(String contestName);
}
