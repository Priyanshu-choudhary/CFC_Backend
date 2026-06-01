package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.ContestSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ContestSession documents.
 *
 * The compound index on (contestId, username) is declared on the POJO via
 * @CompoundIndex, ensuring all lookups are O(log n).
 */
public interface ContestSessionRepository extends MongoRepository<ContestSession, String> {

    /** Used by every submit and arena-state call. */
    Optional<ContestSession> findByContestIdAndUsername(String contestId, String username);

    /** Used to compute the leaderboard. */
    List<ContestSession> findByContestId(String contestId);

    /** Guard against double-join. */
    boolean existsByContestIdAndUsername(String contestId, String username);
}
