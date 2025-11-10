package com.study.FlowTrack.service.redis;

import com.study.FlowTrack.exception.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimiterService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void checkAndIncrement(String key, int maxRequests, long timeWindowInSeconds) {
        Long currentCount = stringRedisTemplate.opsForValue().increment(key, 1);

        if (currentCount == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(timeWindowInSeconds));
        }

        if (currentCount > maxRequests) {
            throw new RateLimitExceededException("Too many requests from key: " + key);
        }
    }
}
