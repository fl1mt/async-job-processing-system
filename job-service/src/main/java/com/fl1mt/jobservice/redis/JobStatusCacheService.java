package com.fl1mt.jobservice.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobStatusCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    public void setStatus(Long jobId, String status) {
        redisTemplate.opsForValue()
                .set(buildKey(jobId), status, TTL);
    }

    public Optional<String> getStatus(Long jobId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(buildKey(jobId))
        );
    }

    private String buildKey(Long jobId) {
        return "job:status:" + jobId;
    }
}