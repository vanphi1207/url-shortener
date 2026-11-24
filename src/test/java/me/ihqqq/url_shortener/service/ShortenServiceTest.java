package me.ihqqq.url_shortener.service;

import me.ihqqq.url_shortener.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cần PostgreSQL và Redis đang chạy: docker compose up -d postgres redis
 * Mỗi test dùng link có UUID riêng nên không phụ thuộc dữ liệu cũ trong DB.
 */
@SpringBootTest
class ShortenServiceTest {

    @Autowired
    private ShortenService shortenService;

    private static String newUniqueLink() {
        return "https://example.com/" + UUID.randomUUID();
    }

    @Test
    void cungMotLink_haiLanRaCungMa() {
        String link = newUniqueLink();

        ShortenResponse first = shortenService.shorten(link);
        ShortenResponse second = shortenService.shorten(link);

        assertEquals(first.shortCode(), second.shortCode());
        assertEquals(first.shortUrl(), second.shortUrl());
    }

    @Test
    void linkCoKhoangTrangHaiDau_coiLaCungMotLink() {
        String link = newUniqueLink();

        ShortenResponse first = shortenService.shorten(link);
        ShortenResponse second = shortenService.shorten("  " + link + "  ");

        assertEquals(first.shortCode(), second.shortCode());
    }

    @Test
    void haiLinkKhacNhau_raHaiMaKhacNhau_va_dai7KyTu() {
        ShortenResponse a = shortenService.shorten(newUniqueLink());
        ShortenResponse b = shortenService.shorten(newUniqueLink());

        assertNotEquals(a.shortCode(), b.shortCode());
        assertEquals(7, a.shortCode().length());
        assertEquals(7, b.shortCode().length());
        assertTrue(a.shortUrl().endsWith("/" + a.shortCode()));
    }

    @Test
    void nhieuLuongCungRutGonMotLink_taCaDeuNhanCungMotMa() throws Exception {
        String link = newUniqueLink();
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> {
                    ready.countDown();
                    go.await(); // cho tất cả luồng xuất phát cùng lúc để tăng khả năng tranh chấp
                    return shortenService.shorten(link).shortCode();
                });
            }

            List<Future<String>> futures = new ArrayList<>();
            for (Callable<String> task : tasks) {
                futures.add(pool.submit(task));
            }
            ready.await();
            go.countDown();

            Set<String> codes = ConcurrentHashMap.newKeySet();
            for (Future<String> f : futures) {
                codes.add(f.get());
            }
            assertEquals(1, codes.size(), "Cùng một link nhưng nhận được nhiều mã khác nhau: " + codes);
        } finally {
            pool.shutdown();
        }
    }
}