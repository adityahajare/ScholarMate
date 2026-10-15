package com.scholarmate.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 900; // 15 minutes

    private static class AttemptRecord {
        int attempts;
        Instant firstAttemptTime;
        Instant lockedUntil;

        AttemptRecord() {
            this.attempts = 1;
            this.firstAttemptTime = Instant.now();
            this.lockedUntil = null;
        }
    }

    private final Map<String, AttemptRecord> attemptCache = new ConcurrentHashMap<>();

    public void checkRateLimit(String key) {
        if (key == null || key.isBlank()) return;
        AttemptRecord record = attemptCache.get(key.toLowerCase());
        if (record != null && record.lockedUntil != null) {
            if (Instant.now().isBefore(record.lockedUntil)) {
                long remainingSeconds = record.lockedUntil.getEpochSecond() - Instant.now().getEpochSecond();
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Too many failed login attempts. Please wait " + Math.max(1, remainingSeconds / 60) + " minute(s) before trying again."
                );
            } else {
                attemptCache.remove(key.toLowerCase());
            }
        }
    }

    public void recordFailedAttempt(String key) {
        if (key == null || key.isBlank()) return;
        String normalized = key.toLowerCase();
        attemptCache.compute(normalized, (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null) {
                return new AttemptRecord();
            }
            if (now.getEpochSecond() - existing.firstAttemptTime.getEpochSecond() > LOCK_DURATION_SECONDS) {
                return new AttemptRecord();
            }
            existing.attempts++;
            if (existing.attempts >= MAX_ATTEMPTS) {
                existing.lockedUntil = now.plusSeconds(LOCK_DURATION_SECONDS);
            }
            return existing;
        });
    }

    public void recordSuccessfulLogin(String key) {
        if (key == null) return;
        attemptCache.remove(key.toLowerCase());
    }
}
