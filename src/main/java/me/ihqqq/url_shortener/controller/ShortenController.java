package me.ihqqq.url_shortener.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.url_shortener.dto.request.ShortenRequest;
import me.ihqqq.url_shortener.dto.response.ShortenResponse;
import me.ihqqq.url_shortener.dto.response.ApiResponse;
import me.ihqqq.url_shortener.service.ShortenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShortenController {

    ShortenService shortenService;

    @PostMapping("/shorten")
    ApiResponse<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        return ApiResponse.<ShortenResponse>builder()
                .result(shortenService.shorten(request.longUrl()))
                .build();
    }
}