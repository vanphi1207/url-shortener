package me.ihqqq.url_shortener.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiter {

    static final String KEY_PREFIX = "ratelimit:";

    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            local ttl = redis.call('TTL', KEYS[1])
            return {current, ttl}
            """, List.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitResult check(String key, int maxRequests, Duration window) {
        long windowSeconds = Math.max(1, window.toSeconds());
        try {
            @SuppressWarnings("rawtypes")
            List result = redisTemplate.execute(
                    INCREMENT_SCRIPT,
                    List.of(KEY_PREFIX + key),
                    String.valueOf(windowSeconds));

            long current = ((Number) result.get(0)).longValue();
            long ttl = ((Number) result.get(1)).longValue();

            boolean allowed = current <= maxRequests;
            long remaining = Math.max(0, maxRequests - current);
            long retryAfter = ttl > 0 ? ttl : windowSeconds;
            return new RateLimitResult(allowed, maxRequests, remaining, retryAfter);
        } catch (RuntimeException e) {
            // Redis lỗi thì cho request đi tiếp (fail-open) để dịch vụ không bị tê liệt theo Redis
            log.warn("Rate limiter không dùng được Redis, cho phép request đi qua: {}", e.getMessage());
            return new RateLimitResult(true, maxRequests, maxRequests, 0);
        }
    }
}