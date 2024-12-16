package com.api.notionary.util.scheduling;

import com.api.notionary.dto.UserDto;
import com.api.notionary.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserCleanupJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCleanupJob.class);

    private final UserService userService;

    @Autowired
    public UserCleanupJob(UserService userService) {
        this.userService = userService;
    }

    /**
     * Run every month
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void removeDisabledUsers() {
        LOGGER.info("Starting UserCleanupJob.");
        List<UserDto> expiredUsers = userService.getAllDisabledUsers();

        if (!expiredUsers.isEmpty()) {
            LOGGER.info("Removing unconfirmed expired users: {}", expiredUsers);
            userService.deleteUsers(expiredUsers);
        }
    }
}
