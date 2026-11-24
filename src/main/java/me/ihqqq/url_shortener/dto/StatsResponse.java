package me.ihqqq.url_shortener.dto;

import java.time.Instant;
import java.util.List;

/**
 * @param totalClicks   tổng số click từ trước đến nay
 * @param clicksPerDay  số click mỗi ngày (UTC) trong khoảng thời gian yêu cầu, ngày không có click ghi 0
 * @param topReferrers  các nguồn giới thiệu nhiều click nhất trong khoảng thời gian yêu cầu
 */
public record StatsResponse(
        String shortCode,
        String longUrl,
        Instant createdAt,
        long totalClicks,
        List<DailyClicks> clicksPerDay,
        List<ReferrerClicks> topReferrers
) {
}