package com.api.notionary.util.scheduling;

import com.api.notionary.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserCleanupJob {

    private final UserService userService;

    /**
     * Remove from database users who have not verified their account.
     * Run cleanup job every night
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void removeDisabledUsers() {
        log.info("Starting UserCleanupJob...");
        userService.cleanupUnverifiedUsers();
        log.info("Finished UserCleanupJob.");
    }
}
