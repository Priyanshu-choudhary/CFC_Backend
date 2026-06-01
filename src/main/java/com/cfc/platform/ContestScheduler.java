package com.cfc.platform;

import com.cfc.platform.Service.ContestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ContestScheduler — drives the Contest state machine.
 *
 * Runs every 30 seconds:
 *   SCHEDULED → LIVE  (when startedAt has passed)
 *   LIVE      → ENDED (when endedAt has passed)
 *
 * Why 30 seconds?
 *   - Good enough precision for a student-scale platform.
 *   - Zero external dependencies (no Quartz, no cron service).
 *   - Upgrade to 5-second ticks with no architectural change if needed.
 */
@Component
public class ContestScheduler {

    private static final Logger log = LoggerFactory.getLogger(ContestScheduler.class);

    @Autowired
    private ContestService contestService;

    /**
     * Tick: check for state transitions every 30 seconds.
     * fixedDelay (not fixedRate) ensures the next tick starts 30 s AFTER
     * the previous one finishes, preventing pile-up if DB is slow.
     */
    @Scheduled(fixedDelay = 30_000)
    public void tick() {
        try {
            contestService.tickScheduledToLive();
        } catch (Exception e) {
            log.warn("Scheduler SCHEDULED→LIVE error: {}", e.getMessage());
        }
        try {
            contestService.tickLiveToEnded();
        } catch (Exception e) {
            log.warn("Scheduler LIVE→ENDED error: {}", e.getMessage());
        }
    }
}
