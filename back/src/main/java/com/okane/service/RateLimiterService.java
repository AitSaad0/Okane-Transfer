package com.okane.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientKey) {
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(50, 50));
        return bucket.tryConsume();
    }

    // Inner class — pure Java token bucket
    static class TokenBucket {
        private final int maxTokens;
        private final int refillPerMinute;
        private int tokens;
        private long lastRefillTime;

        TokenBucket(int maxTokens, int refillPerMinute) {
            this.maxTokens = maxTokens;
            this.refillPerMinute = refillPerMinute;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            int tokensToAdd = (int) (elapsed * refillPerMinute / 60_000);
            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}