package me.ihqqq.url_shortener.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdGenerator {

    public static final String COUNTER_KEY = "url:id-counter";
    public static final long OFFSET = 56_800_235_584L;

    StringRedisTemplate redisTemplate;

    public long nextId() {
        Long counter = redisTemplate.opsForValue().increment(COUNTER_KEY);
        if (counter == null) {
            throw new IllegalStateException("Redis không trả về giá trị cho " + COUNTER_KEY);
        }
        return OFFSET + counter;
    }
}