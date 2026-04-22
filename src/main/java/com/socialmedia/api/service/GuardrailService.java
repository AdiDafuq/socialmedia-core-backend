package com.socialmedia.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class GuardrailService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_BOT_REPLIES = 100;
    private static final int MAX_DEPTH = 20;
    private static final Duration COOLDOWN_TTL = Duration.ofMinutes(10);

    public GuardrailService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String botCountKey(Long postId) {
        return "post:" + postId + ":bot_count";
    }

    private String cooldownKey(Long botId, Long humanId) {
        return "cooldown:bot_" + botId + ":human_" + humanId;
    }

    public boolean allowBotReply(Long postId) {
        String key = botCountKey(postId);

        Long count = redisTemplate.opsForValue().increment(key, 1);

        if (count != null && count > MAX_BOT_REPLIES) {
            redisTemplate.opsForValue().decrement(key, 1);
            return false;
        }
        return true;
    }

    public boolean allowDepth(int depth) {
        return depth <= MAX_DEPTH;
    }

    public boolean allowCooldown(Long botId, Long humanId) {
        String key = cooldownKey(botId, humanId);

        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }

        redisTemplate.opsForValue().set(key, "1", COOLDOWN_TTL);
        return true;
    }
}