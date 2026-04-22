package com.socialmedia.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.List;

@Component
public class NotificationSweeper {

    private final RedisTemplate<String, Object> redisTemplate;

    public NotificationSweeper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void sweepNotifications() {

        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {

            List<Object> notifications = redisTemplate.opsForList().range(key, 0, -1);

            if (notifications == null || notifications.isEmpty()) {
                continue;
            }

            int count = notifications.size();
            String firstMessage = notifications.get(0).toString();

            // Log summary
            System.out.println(firstMessage + " and [" + (count - 1) + "] others interacted...");

            // Clear list after processing
            redisTemplate.delete(key);
        }
    }
}