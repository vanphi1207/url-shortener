package me.ihqqq.url_shortener.repository;

import me.ihqqq.url_shortener.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortCode(String shortCode);

    @Query(value = """
            SELECT to_char(clicked_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS bucket,
                   COUNT(*) AS total
            FROM click_events
            WHERE short_code = :shortCode AND clicked_at >= :since
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<DailyCount> countPerDay(@Param("shortCode") String shortCode,
                                 @Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(NULLIF(referrer, ''), '(direct)') AS referrer,
                   COUNT(*) AS total
            FROM click_events
            WHERE short_code = :shortCode AND clicked_at >= :since
            GROUP BY COALESCE(NULLIF(referrer, ''), '(direct)')
            ORDER BY total DESC, 1 ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<ReferrerCount> topReferrers(@Param("shortCode") String shortCode,
                                     @Param("since") Instant since,
                                     @Param("limit") int limit);

    interface DailyCount {
        String getBucket();

        Long getTotal();
    }

    interface ReferrerCount {
        String getReferrer();

        Long getTotal();
    }
}