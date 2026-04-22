package com.socialmedia.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisRateLimiterService rateLimiterService;

    private static final long COOLDOWN_MINUTES = 15;

    @Autowired
    public NotificationService(
            RedisTemplate<String, Object> redisTemplate,
            RedisRateLimiterService rateLimiterService
    ) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * MAIN GATE (Phase 4 requirement: concurrency safe entry point)
     */
    public String handleRequest(Long userId, String message) {

        // 1. GLOBAL RATE LIMIT CHECK (Redis Lua - atomic)
        boolean allowed = rateLimiterService.tryAcquire();

        if (!allowed) {
            return "Rejected: global limit reached (100 max)";
        }

        // 2. If allowed → proceed safely
        processNotification(userId, message);

        return "Success: request processed";
    }

    /**
     * USER NOTIFICATION LOGIC (cooldown + queueing)
     */
    @Transactional
    public void processNotification(Long userId, String message) {

        String cooldownKey = "user:" + userId + ":cooldown";
        String pendingKey = "user:" + userId + ":pending_notifs";

        try {
            Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);

            if (Boolean.TRUE.equals(hasCooldown)) {

                // queue message if user is cooling down
                redisTemplate.opsForList().rightPush(pendingKey, message);

                logger.info("User {} in cooldown. Notification queued.", userId);

            } else {

                // simulate DB + push success operation
                logger.info("Push Notification Sent to User {}: {}", userId, message);

                redisTemplate.opsForValue()
                        .set(cooldownKey, "1", Duration.ofMinutes(COOLDOWN_MINUTES));
            }

        } catch (Exception e) {
            logger.error("Error handling notification for user {}: {}", userId, e.getMessage(), e);
        }
    }
}   