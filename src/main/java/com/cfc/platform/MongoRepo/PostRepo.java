package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.Posts.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PostRepo extends MongoRepository<Posts, String> {

    // Direct collection query â€” no @DBRef expansion, single MongoDB query
    List<Posts> findByUserName(String username);

    Page<Posts> findByUserName(String username, Pageable pageable);

    // Any-of tag match (used by some endpoints)
    List<Posts> findByTagsIn(List<String> tags);

    // All-of tag match ($all operator) â€” replaces in-memory Java filter
    @Query("{ 'tags': { $all: ?0 } }")
    List<Posts> findByTagsAll(List<String> tags);

    // All-of match scoped to one user (for user-specific tag filtering)
    @Query("{ 'userName': ?0, 'tags': { $all: ?1 } }")
    List<Posts> findByUserNameAndTagsAll(String userName, List<String> tags);

    // Find posts belonging to a specific course (by course ObjectId)
    @Query("{ 'course.$id': { $oid: ?0 } }")
    List<Posts> findByCourseId(String courseId);

    // Find posts belonging to a specific contest (by contest ObjectId)
    @Query("{ 'contest.$id': { $oid: ?0 } }")
    List<Posts> findByContestId(String contestId);

    long countByUserName(String username);
}
