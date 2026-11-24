package me.ihqqq.url_shortener.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.url_shortener.entity.Url;
import me.ihqqq.url_shortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedirectService {

    public static final String KEY_PREFIX = "cache:url:";
    public static final String NOT_FOUND_MARKER = "__NOT_FOUND__";
    private static final Duration NOT_FOUND_TTL = Duration.ofSeconds(60);

    StringRedisTemplate redisTemplate;
    UrlRepository urlRepository;

    @NonFinal
    @Value("${app.cache-ttl:24h}")
    Duration cacheTtl;

    @NonFinal
    @Value("${app.cache-enabled:true}")
    boolean cacheEnabled;

    public Optional<String> resolve(String shortCode) {
        if (!cacheEnabled) {
            return urlRepository.findByShortCode(shortCode).map(Url::getLongUrl);
        }

        String key = KEY_PREFIX + shortCode;

        String cached = readCache(key);
        if (cached != null) {
            return NOT_FOUND_MARKER.equals(cached) ? Optional.empty() : Optional.of(cached);
        }

        Optional<Url> url = urlRepository.findByShortCode(shortCode);
        if (url.isPresent()) {
            String longUrl = url.get().getLongUrl();
            writeCache(key, longUrl, cacheTtl);
            return Optional.of(longUrl);
        }

        writeCache(key, NOT_FOUND_MARKER, NOT_FOUND_TTL);
        return Optional.empty();
    }

    private String readCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException e) {
            log.warn("Không đọc được cache Redis cho key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException e) {
            log.warn("Không ghi được cache Redis cho key {}: {}", key, e.getMessage());
        }
    }
}