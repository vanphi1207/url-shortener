package me.ihqqq.url_shortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.url_shortener.dto.DailyClicks;
import me.ihqqq.url_shortener.dto.ReferrerClicks;
import me.ihqqq.url_shortener.dto.StatsResponse;
import me.ihqqq.url_shortener.entity.ClickEvent;
import me.ihqqq.url_shortener.entity.Url;
import me.ihqqq.url_shortener.repository.ClickEventRepository;
import me.ihqqq.url_shortener.repository.UrlRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    static final int MAX_DAYS = 90;
    private static final int TOP_REFERRERS = 10;

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    @Async("analyticsExecutor")
    public void recordClick(String shortCode, String ipAddress, String userAgent,
                            String referrer, Instant clickedAt) {
        try {
            clickEventRepository.save(ClickEvent.builder()
                    .shortCode(shortCode)
                    .clickedAt(clickedAt)
                    .ipAddress(truncate(ipAddress, 45))
                    .userAgent(truncate(userAgent, 512))
                    .referrer(truncate(referrer, 2048))
                    .build());
        } catch (RuntimeException e) {
            log.warn("Không ghi được lượt click cho {}: {}", shortCode, e.getMessage());
        }
    }

    public Optional<StatsResponse> getStats(String shortCode, int days) {
        int range = Math.min(Math.max(days, 1), MAX_DAYS);
        return urlRepository.findByShortCode(shortCode).map(url -> buildStats(url, range));
    }

    private StatsResponse buildStats(Url url, int days) {
        String code = url.getShortCode();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate firstDay = today.minusDays(days - 1L);
        Instant since = firstDay.atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<String, Long> perDay = new HashMap<>();
        for (ClickEventRepository.DailyCount row : clickEventRepository.countPerDay(code, since)) {
            perDay.put(row.getBucket(), row.getTotal());
        }

        List<DailyClicks> clicksPerDay = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String date = firstDay.plusDays(i).toString(); // dạng yyyy-MM-dd
            clicksPerDay.add(new DailyClicks(date, perDay.getOrDefault(date, 0L)));
        }

        List<ReferrerClicks> topReferrers = clickEventRepository
                .topReferrers(code, since, TOP_REFERRERS).stream()
                .map(row -> new ReferrerClicks(row.getReferrer(), row.getTotal()))
                .toList();

        return new StatsResponse(code, url.getLongUrl(), url.getCreatedAt(),
                clickEventRepository.countByShortCode(code), clicksPerDay, topReferrers);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}