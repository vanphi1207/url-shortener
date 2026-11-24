package me.ihqqq.url_shortener.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;
    private final AnalyticsService analyticsService;

    @Value("${app.analytics-enabled:true}")
    private boolean analyticsEnabled;

    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        Optional<String> longUrl = redirectService.resolve(shortCode);
        if (longUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (analyticsEnabled) {
            analyticsService.recordClick(
                    shortCode,
                    request.getRemoteAddr(),
                    request.getHeader(HttpHeaders.USER_AGENT),
                    request.getHeader(HttpHeaders.REFERER),
                    Instant.now());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl.get())
                .build();
    }
}