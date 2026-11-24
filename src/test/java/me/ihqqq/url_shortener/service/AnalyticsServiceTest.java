package me.ihqqq.url_shortener.service;

import me.ihqqq.url_shortener.dto.DailyClicks;
import me.ihqqq.url_shortener.dto.ReferrerClicks;
import me.ihqqq.url_shortener.dto.StatsResponse;
import me.ihqqq.url_shortener.repository.ClickEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cần PostgreSQL và Redis đang chạy: docker compose up -d postgres redis
 * recordClick chạy bất đồng bộ nên test phải chờ kết quả xuất hiện trong DB.
 * (Test dùng ngày UTC; chạy đúng lúc nửa đêm UTC có thể lệch 1 ngày, hiếm gặp.)
 */
@SpringBootTest
class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ShortenService shortenService;

    @Autowired
    private ClickEventRepository clickEventRepository;

    private static String newCode(ShortenService service) {
        return service.shorten("https://example.com/" + UUID.randomUUID()).shortCode();
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Hết thời gian chờ ghi click bất đồng bộ");
    }

    private void record(String code, String referrer) {
        analyticsService.recordClick(code, "127.0.0.1", "JUnit", referrer, Instant.now());
    }

    @Test
    void recordClick_ghiBatDongBo_vaCuoiCungCoTrongDB() throws Exception {
        String code = newCode(shortenService);

        record(code, "https://facebook.com");

        waitUntil(() -> clickEventRepository.countByShortCode(code) == 1);
        assertEquals(1, clickEventRepository.countByShortCode(code));
    }

    @Test
    void recordClick_duLieuQuaDai_duocCatNganVaKhongNemLoi() throws Exception {
        String code = newCode(shortenService);

        analyticsService.recordClick(code, "x".repeat(100), "u".repeat(1000), "r".repeat(3000), Instant.now());

        waitUntil(() -> clickEventRepository.countByShortCode(code) == 1);
    }

    @Test
    void getStats_tongClick_theoNgay_vaTopReferrer() throws Exception {
        String code = newCode(shortenService);
        record(code, "https://facebook.com");
        record(code, "https://facebook.com");
        record(code, "https://google.com");
        record(code, null); // truy cập trực tiếp, không có referrer
        waitUntil(() -> clickEventRepository.countByShortCode(code) == 4);

        Optional<StatsResponse> result = analyticsService.getStats(code, 7);

        assertTrue(result.isPresent());
        StatsResponse stats = result.get();
        assertEquals(code, stats.shortCode());
        assertEquals(4, stats.totalClicks());

        // 7 ngày, ngày cuối cùng là hôm nay (UTC) và có đủ 4 click
        assertEquals(7, stats.clicksPerDay().size());
        DailyClicks last = stats.clicksPerDay().get(6);
        assertEquals(LocalDate.now(ZoneOffset.UTC).toString(), last.date());
        assertEquals(4, last.clicks());
        assertEquals(0, stats.clicksPerDay().get(0).clicks());

        // facebook.com nhiều nhất (2 click)
        ReferrerClicks top = stats.topReferrers().get(0);
        assertEquals("https://facebook.com", top.referrer());
        assertEquals(2, top.clicks());
        assertTrue(stats.topReferrers().stream().anyMatch(r -> r.referrer().equals("(direct)")));
    }

    @Test
    void getStats_maKhongTonTai_traEmpty() {
        assertEquals(Optional.empty(), analyticsService.getStats("zzzzzzy", 7));
    }

    @Test
    void getStats_soNgayNgoaiKhoang_duocGioiHan() {
        String code = newCode(shortenService);

        StatsResponse tooMany = analyticsService.getStats(code, 1000).orElseThrow();
        StatsResponse tooFew = analyticsService.getStats(code, 0).orElseThrow();

        assertEquals(AnalyticsService.MAX_DAYS, tooMany.clicksPerDay().size());
        assertEquals(1, tooFew.clicksPerDay().size());
    }
}