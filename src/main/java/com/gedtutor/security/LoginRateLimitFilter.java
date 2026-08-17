package com.gedtutor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP-based rate limiter covering POST /login and POST /register.
 *
 * <p>After {@value #MAX_ATTEMPTS} POSTs from the same IP within a
 * {@value #WINDOW_SECONDS}-second sliding window, subsequent requests
 * get a 429 until the window expires.  Separate counters are kept per
 * endpoint so a burst of registration attempts does not lock out login
 * and vice versa.
 *
 * <p>In-memory only — suitable for single-node deployments.
 * For multi-instance, swap the ConcurrentHashMap for a Redis-backed counter.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    static final int MAX_ATTEMPTS   = 5;
    static final int WINDOW_SECONDS = 15 * 60; // 15 minutes

    /** Per-IP tracking bucket. */
    private record Bucket(int count, Instant windowStart) {}

    // Separate maps so /login and /register limits are independent
    private final Map<String, Bucket> loginBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getServletPath();
        return !"/login".equals(path) && !"/register".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path   = request.getServletPath();
        boolean isReg = "/register".equals(path);

        Map<String, Bucket> buckets = isReg ? registerBuckets : loginBuckets;
        String backUrl  = isReg ? "/register" : "/login";
        String pageDesc = isReg ? "registration" : "login";

        String ip  = resolveClientIp(request);
        Instant now = Instant.now();

        Bucket bucket = buckets.compute(ip, (key, existing) -> {
            if (existing == null
                    || now.isAfter(existing.windowStart().plusSeconds(WINDOW_SECONDS))) {
                return new Bucket(1, now);
            }
            return new Bucket(existing.count() + 1, existing.windowStart());
        });

        if (bucket.count() > MAX_ATTEMPTS) {
            long secondsLeft = WINDOW_SECONDS
                    - (now.getEpochSecond() - bucket.windowStart().getEpochSecond());
            response.setStatus(429);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                    "<!DOCTYPE html><html><body>" +
                    "<h2>Too many " + pageDesc + " attempts.</h2>" +
                    "<p>Please wait " + Math.max(1, secondsLeft / 60) +
                    " minute(s) before trying again.</p>" +
                    "<a href='" + backUrl + "'>Back</a>" +
                    "</body></html>");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Respects X-Forwarded-For if set by a trusted reverse proxy. */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
