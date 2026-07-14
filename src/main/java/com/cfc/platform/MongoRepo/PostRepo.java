package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.Posts.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepo extends MongoRepository<Posts, String> {

    List<Posts> findByUserName(String username);

    Optional<Posts> findByUserNameAndContestIdAndIndex(String userName, int contestId, String index);

    Optional<Posts> findFirstByContestIdAndIndex(int contestId, String index);

    Optional<Posts> findFirstByTitleIgnoreCase(String title);

    Page<Posts> findByUserName(String username, Pageable pageable);

    List<Posts> findByTagsIn(List<String> tags);

    @Query("{ 'tags': { $all: ?0 } }")
    List<Posts> findByTagsAll(List<String> tags);

    @Query("{ 'userName': ?0, 'tags': { $all: ?1 } }")
    List<Posts> findByUserNameAndTagsAll(String userName, List<String> tags);

    @Query("{ 'course.$id': { $oid: ?0 } }")
    List<Posts> findByCourseId(String courseId);

    @Query("{ 'contest.$id': { $oid: ?0 } }")
    List<Posts> findByContestId(String contestId);

    long countByUserName(String username);
}
