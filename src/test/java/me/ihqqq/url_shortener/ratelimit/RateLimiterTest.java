package me.ihqqq.url_shortener.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cần PostgreSQL và Redis đang chạy: docker compose up -d postgres redis
 */
@SpringBootTest
class RateLimiterTest {

    private static final Duration WINDOW = Duration.ofSeconds(10);

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static String uniqueKey() {
        return "test:" + UUID.randomUUID();
    }

    private void cleanup(String key) {
        redisTemplate.delete(RateLimiter.KEY_PREFIX + key);
    }

    @Test
    void duoiNguong_choPhep_vuotNguong_chan() {
        String key = uniqueKey();
        try {
            for (int i = 1; i <= 3; i++) {
                RateLimitResult r = rateLimiter.check(key, 3, WINDOW);
                assertTrue(r.allowed(), "Request thứ " + i + " phải được phép");
                assertEquals(3 - i, r.remaining());
            }

            RateLimitResult blocked = rateLimiter.check(key, 3, WINDOW);
            assertFalse(blocked.allowed(), "Request thứ 4 phải bị chặn");
            assertEquals(0, blocked.remaining());
            assertTrue(blocked.retryAfterSeconds() > 0 && blocked.retryAfterSeconds() <= WINDOW.toSeconds());
        } finally {
            cleanup(key);
        }
    }

    @Test
    void haiKeyKhacNhau_doiLapVoiNhau() {
        String keyA = uniqueKey();
        String keyB = uniqueKey();
        try {
            assertTrue(rateLimiter.check(keyA, 1, WINDOW).allowed());
            assertFalse(rateLimiter.check(keyA, 1, WINDOW).allowed());

            // key B chưa dùng lần nào nên không bị ảnh hưởng bởi key A
            assertTrue(rateLimiter.check(keyB, 1, WINDOW).allowed());
        } finally {
            cleanup(keyA);
            cleanup(keyB);
        }
    }

    @Test
    void sauKhiHetCuaSo_duocPhepLai() throws Exception {
        String key = uniqueKey();
        try {
            Duration shortWindow = Duration.ofSeconds(1);
            assertTrue(rateLimiter.check(key, 1, shortWindow).allowed());
            assertFalse(rateLimiter.check(key, 1, shortWindow).allowed());

            Thread.sleep(1500); // chờ cửa sổ 1 giây hết hạn

            assertTrue(rateLimiter.check(key, 1, shortWindow).allowed());
        } finally {
            cleanup(key);
        }
    }

    @Test
    void nhieuLuongCungGoi_dungBangNguongChoPhep() throws Exception {
        String key = uniqueKey();
        int limit = 10;
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Boolean> task = () -> {
                    go.await();
                    return rateLimiter.check(key, limit, WINDOW).allowed();
                };
                futures.add(pool.submit(task));
            }
            go.countDown();

            int allowedCount = 0;
            for (Future<Boolean> f : futures) {
                if (f.get()) {
                    allowedCount++;
                }
            }
            // Đúng bằng ngưỡng: không hơn (không lọt request thừa), không kém
            assertEquals(limit, allowedCount);
        } finally {
            pool.shutdown();
            cleanup(key);
        }
    }
}