package me.ihqqq.url_shortener.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdGenerator {

    static final String COUNTER_KEY = "url:id-counter";

    static final long OFFSET = 56_800_235_584L;

    private final StringRedisTemplate redisTemplate;

    public long nextId() {
        Long counter = redisTemplate.opsForValue().increment(COUNTER_KEY);
        if (counter == null) {
            throw new IllegalStateException("Redis không trả về giá trị cho " + COUNTER_KEY);
        }
        return OFFSET + counter;
    }
}