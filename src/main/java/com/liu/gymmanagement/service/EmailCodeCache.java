package com.liu.gymmanagement.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailCodeCache {

    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    public void put(String email, String code, Duration ttl) {
        long expireAt = System.currentTimeMillis() + ttl.toMillis();
        cache.put(email, new CodeEntry(code, expireAt));
    }

    public String get(String email) {
        CodeEntry entry = cache.get(email);
        if (entry == null || entry.isExpired()) {
            cache.remove(email); // 清理过期的
            return null;
        }
        return entry.code;
    }

    static class CodeEntry {
        String code;
        long expireAt;

        CodeEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}
