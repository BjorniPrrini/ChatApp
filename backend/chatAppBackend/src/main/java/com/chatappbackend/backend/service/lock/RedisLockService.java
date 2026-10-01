package com.chatappbackend.backend.service.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisLockService implements LockService{
    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);

    public RedisLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Boolean tryAcquire(Long conversationId, String token, Duration lifespan) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(buildKey(conversationId), token, lifespan));
    }

    @Override
    public void release(Long conversationId, String token) {
        stringRedisTemplate.execute(RELEASE_SCRIPT, List.of(buildKey(conversationId)), token);
    }

    private String buildKey(Long conversationId){
        return "lock:summary:" + conversationId;
    }
}