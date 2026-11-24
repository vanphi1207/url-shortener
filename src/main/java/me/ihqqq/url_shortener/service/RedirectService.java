package me.ihqqq.url_shortener.service;

import lombok.extern.slf4j.Slf4j;
import me.ihqqq.url_shortener.entity.Url;
import me.ihqqq.url_shortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class RedirectService {

    static final String KEY_PREFIX = "cache:url:";

    static final String NOT_FOUND_MARKER = "__NOT_FOUND__";
    private static final Duration NOT_FOUND_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final UrlRepository urlRepository;
    private final Duration cacheTtl;
    private final boolean cacheEnabled;

    public RedirectService(StringRedisTemplate redisTemplate,
                           UrlRepository urlRepository,
                           @Value("${app.cache-ttl:24h}") Duration cacheTtl,
                           @Value("${app.cache-enabled:true}") boolean cacheEnabled) {
        this.redisTemplate = redisTemplate;
        this.urlRepository = urlRepository;
        this.cacheTtl = cacheTtl;
        this.cacheEnabled = cacheEnabled;
    }

    public Optional<String> resolve(String shortCode) {
        // Công tắc dùng cho bài đo hiệu năng: tắt cache thì luôn tra thẳng DB
        if (!cacheEnabled) {
            return urlRepository.findByShortCode(shortCode).map(Url::getLongUrl);
        }

        String key = KEY_PREFIX + shortCode;

        // 1. Cache hit
        String cached = readCache(key);
        if (cached != null) {
            return NOT_FOUND_MARKER.equals(cached) ? Optional.empty() : Optional.of(cached);
        }

        // 2. Cache miss: tra DB
        Optional<Url> url = urlRepository.findByShortCode(shortCode);
        if (url.isPresent()) {
            String longUrl = url.get().getLongUrl();
            writeCache(key, longUrl, cacheTtl);
            return Optional.of(longUrl);
        }

        // 3. Không có trong DB: nhớ ngắn hạn là "không tồn tại"
        writeCache(key, NOT_FOUND_MARKER, NOT_FOUND_TTL);
        return Optional.empty();
    }

    // Redis lỗi (sập, mất kết nối...) thì bỏ qua cache và đi tiếp xuống DB, không làm hỏng request.
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