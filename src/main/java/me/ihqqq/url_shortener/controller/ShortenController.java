package me.ihqqq.url_shortener.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ihqqq.url_shortener.dto.ShortenRequest;
import me.ihqqq.url_shortener.dto.ShortenResponse;
import me.ihqqq.url_shortener.service.ShortenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class ShortenController {

    private final ShortenService shortenService;

    @PostMapping("/shorten")
    public ShortenResponse shorten(@Valid @RequestBody ShortenRequest request) {
        return shortenService.shorten(request.longUrl());
    }
}