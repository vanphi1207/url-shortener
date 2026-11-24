package me.ihqqq.url_shortener.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.url_shortener.dto.response.StatsResponse;
import me.ihqqq.url_shortener.dto.response.ApiResponse;
import me.ihqqq.url_shortener.exception.AppException;
import me.ihqqq.url_shortener.exception.ErrorCode;
import me.ihqqq.url_shortener.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsController {

    AnalyticsService analyticsService;

    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    ApiResponse<StatsResponse> stats(@PathVariable String shortCode,
                                     @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.<StatsResponse>builder()
                .result(analyticsService.getStats(shortCode, days)
                        .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND)))
                .build();
    }
}