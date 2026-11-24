package me.ihqqq.url_shortener.ratelimit;

public record RateLimitResult(boolean allowed, long limit, long remaining, long retryAfterSeconds) {
}