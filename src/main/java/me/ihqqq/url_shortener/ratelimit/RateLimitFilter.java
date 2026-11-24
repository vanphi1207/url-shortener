package me.ihqqq.url_shortener.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String SHORTEN_PATH = "/api/v1/data/shorten";

    private final RateLimiter rateLimiter;
    private final int maxRequests;
    private final Duration window;

    public RateLimitFilter(RateLimiter rateLimiter,
                           @Value("${app.rate-limit.max-requests:10}") int maxRequests,
                           @Value("${app.rate-limit.window:1m}") Duration window) {
        this.rateLimiter = rateLimiter;
        this.maxRequests = maxRequests;
        this.window = window;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean isShorten = HttpMethod.POST.matches(request.getMethod())
                && SHORTEN_PATH.equals(request.getRequestURI());
        return !isShorten;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();

        RateLimitResult result = rateLimiter.check("shorten:" + clientIp, maxRequests, window);

        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (!result.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\","
                    + "\"message\":\"Bạn gửi quá nhiều yêu cầu, vui lòng thử lại sau "
                    + result.retryAfterSeconds() + " giây\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}