package me.ihqqq.url_shortener.controller;

import lombok.RequiredArgsConstructor;
import me.ihqqq.url_shortener.dto.StatsResponse;
import me.ihqqq.url_shortener.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    public ResponseEntity<StatsResponse> stats(@PathVariable String shortCode,
                                               @RequestParam(defaultValue = "7") int days) {
        return analyticsService.getStats(shortCode, days)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}