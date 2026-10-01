package com.chatappbackend.backend.service.lock;

import java.time.Duration;

public interface LockService {
    Boolean tryAcquire(Long conversationId, String token, Duration lifespan);
    void release(Long conversationId, String token);
}