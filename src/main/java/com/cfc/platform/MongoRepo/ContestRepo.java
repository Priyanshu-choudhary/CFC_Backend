package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.Contest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ContestRepo extends MongoRepository<Contest, String> {

    /** Used by legacy hub page. */
    Contest findByNameOfContest(String nameOfContest);

    /** Filter contests by status (e.g. "LIVE", "SCHEDULED", "ENDED"). */
    List<Contest> findByStatus(String status);

    /** Public-only scheduled view: status + public flag. */
    List<Contest> findByStatusAndIsPublic(String status, boolean isPublic);

    /**
     * Ticker: find SCHEDULED contests whose start time has passed.
     * → flip to LIVE.
     */
    List<Contest> findByStatusAndStartedAtBefore(String status, Date date);

    /**
     * Ticker: find LIVE contests whose pre-computed end time has passed.
     * → flip to ENDED.
     */
    List<Contest> findByStatusAndEndedAtBefore(String status, Date date);

    /**
     * Room join: look up a private room by its 6-char code.
     * Indexed (sparse) on roomCode in the Contest POJO.
     */
    Optional<Contest> findByRoomCode(String roomCode);
}
