package com.carparking.config;

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

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int  MAX_AUTH_REQUESTS    = 10;
    private static final int  MAX_BOOKING_REQUESTS = 20;
    private static final int  MAX_CALLBACK_REQUESTS = 30;
    private static final long WINDOW_MS              = 60_000;

    private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        int limit = resolveLimit(path);
        if (limit < 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = path.split("/")[2] + ":" + request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();
        long[] data = requestCounts.computeIfAbsent(key, k -> new long[]{now, 0});

        synchronized (data) {
            if (now - data[0] > WINDOW_MS) {
                data[0] = now;
                data[1] = 0;
            }
            data[1]++;
            if (data[1] > limit) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"status\":429," +
                                "\"message\":\"Too many requests. Please wait 1 minute.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.startsWith("/api/auth/")) return MAX_AUTH_REQUESTS;
        if (path.startsWith("/api/bookings")) return MAX_BOOKING_REQUESTS;
        if (path.startsWith("/api/mpesa/callback")) return MAX_CALLBACK_REQUESTS;
        if (path.startsWith("/api/profile/password")) return MAX_AUTH_REQUESTS;
        return -1;
    }
}
