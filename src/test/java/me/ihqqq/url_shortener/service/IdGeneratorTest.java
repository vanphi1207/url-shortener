package me.ihqqq.url_shortener.service;

import me.ihqqq.url_shortener.util.Base62;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cần PostgreSQL và Redis đang chạy: docker compose up -d postgres redis
 * Lưu ý: test sẽ làm counter trong Redis dev tăng lên, không ảnh hưởng gì.
 */
@SpringBootTest
class IdGeneratorTest {

    @Autowired
    private IdGenerator idGenerator;

    @Test
    void nextId_lonHonHoacBangOffset_vaTangDan() {
        long first = idGenerator.nextId();
        long second = idGenerator.nextId();

        assertTrue(first > IdGenerator.OFFSET);
        assertTrue(second > first);
    }

    @Test
    void nextId_maHoaRaDung7KyTu() {
        long id = idGenerator.nextId();
        assertEquals(7, Base62.encode(id).length());
    }

    @Test
    void nextId_nhieuLuongCungGoi_khongTrungId() throws Exception {
        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(idGenerator::nextId);
            }

            Set<Long> ids = ConcurrentHashMap.newKeySet();
            for (Future<Long> f : pool.invokeAll(tasks)) {
                ids.add(f.get());
            }
            assertEquals(threads, ids.size(), "Có ID bị trùng");
        } finally {
            pool.shutdown();
        }
    }
}