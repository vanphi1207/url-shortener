package me.ihqqq.url_shortener.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.ihqqq.url_shortener.exception.AppException;
import me.ihqqq.url_shortener.exception.ErrorCode;
import me.ihqqq.url_shortener.service.AnalyticsService;
import me.ihqqq.url_shortener.service.RedirectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedirectController {

    RedirectService redirectService;
    AnalyticsService analyticsService;

    @NonFinal
    @Value("${app.analytics-enabled:true}")
    boolean analyticsEnabled;

    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = redirectService.resolve(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));

        if (analyticsEnabled) {
            analyticsService.recordClick(
                    shortCode,
                    request.getRemoteAddr(),
                    request.getHeader(HttpHeaders.USER_AGENT),
                    request.getHeader(HttpHeaders.REFERER),
                    Instant.now());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }
}