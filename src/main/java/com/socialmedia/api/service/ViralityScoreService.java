package com.socialmedia.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViralityScoreService {

    private final RedisTemplate<String, Object> redisTemplate;

    public ViralityScoreService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getKey(Long postId) {
        return "post:" + postId + ":virality_score";
    }

    public Long incrementBotReply(Long postId) {
        return redisTemplate.opsForValue().increment(getKey(postId), 1);
    }

    public Long incrementHumanLike(Long postId) {
        return redisTemplate.opsForValue().increment(getKey(postId), 20);
    }

    public Long incrementHumanComment(Long postId) {
        return redisTemplate.opsForValue().increment(getKey(postId), 50);
    }

    public String getScore(Long postId) {
        Object value = redisTemplate.opsForValue().get(getKey(postId));
        return value != null ? value.toString() : "0";
    }
}