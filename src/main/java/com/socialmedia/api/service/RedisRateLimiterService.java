package com.socialmedia.api.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "bot_requests_count";
    private static final int LIMIT = 100;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire() {

        String luaScript = """
            local current = redis.call('GET', KEYS[1])
            if not current then
                current = 0
            else
                current = tonumber(current)
            end

            if current < tonumber(ARGV[1]) then
                redis.call('INCR', KEYS[1])
                return 1
            else
                return 0
            end
        """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(KEY),
                String.valueOf(LIMIT)
        );

        return result != null && result == 1;
    }
}