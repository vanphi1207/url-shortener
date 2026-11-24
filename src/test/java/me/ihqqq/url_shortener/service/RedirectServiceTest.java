package me.ihqqq.url_shortener.service;

import me.ihqqq.url_shortener.dto.ShortenResponse;
import me.ihqqq.url_shortener.repository.UrlRepository;
import me.ihqqq.url_shortener.util.Base62;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cần PostgreSQL và Redis đang chạy: docker compose up -d postgres redis
 */
@SpringBootTest
class RedirectServiceTest {

    @Autowired
    private ShortenService shortenService;

    @Autowired
    private RedirectService redirectService;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static String newUniqueLink() {
        return "https://example.com/" + UUID.randomUUID();
    }

    private static String cacheKey(String shortCode) {
        return RedirectService.KEY_PREFIX + shortCode;
    }

    @Test
    void lanDauTraDB_roiGhiVaoCacheKemTTL() {
        String link = newUniqueLink();
        String code = shortenService.shorten(link).shortCode();
        redisTemplate.delete(cacheKey(code)); // đảm bảo bắt đầu từ cache miss

        Optional<String> result = redirectService.resolve(code);

        assertEquals(Optional.of(link), result);
        assertEquals(link, redisTemplate.opsForValue().get(cacheKey(code)));
        Long ttl = redisTemplate.getExpire(cacheKey(code));
        assertNotNull(ttl);
        assertTrue(ttl > 0, "Key cache phải có TTL");

        redisTemplate.delete(cacheKey(code));
    }

    @Test
    void lanHai_layTuCache_duBanGhiTrongDBDaBiXoa() {
        String link = newUniqueLink();
        ShortenResponse response = shortenService.shorten(link);
        String code = response.shortCode();
        redisTemplate.delete(cacheKey(code));

        redirectService.resolve(code); // nạp cache

        // ID chính là giá trị giải mã của mã ngắn
        urlRepository.deleteById(Base62.decode(code));

        // DB không còn nhưng cache vẫn trả về được => chứng minh đã đọc từ cache
        assertEquals(Optional.of(link), redirectService.resolve(code));

        redisTemplate.delete(cacheKey(code));
    }

    @Test
    void maKhongTonTai_traEmpty_vaDuocNhoNganHan() {
        String code = "zzzzzzz";
        redisTemplate.delete(cacheKey(code));

        assertEquals(Optional.empty(), redirectService.resolve(code));
        assertEquals(RedirectService.NOT_FOUND_MARKER, redisTemplate.opsForValue().get(cacheKey(code)));

        // Lần gọi lặp lại vẫn trả empty (lấy từ cache)
        assertEquals(Optional.empty(), redirectService.resolve(code));

        redisTemplate.delete(cacheKey(code));
    }
}