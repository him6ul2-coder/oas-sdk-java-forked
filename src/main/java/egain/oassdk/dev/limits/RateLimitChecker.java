package egain.oassdk.dev.limits;

import egain.oassdk.core.exceptions.GenerationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Generates rate limit checkers based on SLA specification
 */
public class RateLimitChecker {

    /**
     * Generate rate limit checkers
     *
     * @param slaSpec   SLA specification
     * @param outputDir Output directory
     * @throws GenerationException if generation fails
     */
    public void generateRateLimitCheckers(Map<String, Object> slaSpec, String outputDir) throws GenerationException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }
        try {
            Files.createDirectories(Paths.get(outputDir));

            // Generate rate limiter
            generateRateLimiter(slaSpec, outputDir);

            // Generate rate limit interceptor
            generateRateLimitInterceptor(slaSpec, outputDir);

            // Generate rate limit configuration
            generateRateLimitConfig(slaSpec, outputDir);

            // Generate rate limit service
            generateRateLimitService(slaSpec, outputDir);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate rate limit checkers: " + e.getMessage(), e);
        }
    }

    /**
     * Generate rate limiter
     */
    private void generateRateLimiter(Map<String, Object> slaSpec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import java.util.concurrent.ConcurrentHashMap;
                import java.util.concurrent.atomic.AtomicLong;
                import java.time.LocalDateTime;
                import java.time.temporal.ChronoUnit;
                
                @Singleton
                public class RateLimiter {
                
                    private final ConcurrentHashMap<String, RateLimitData> rateLimitMap = new ConcurrentHashMap<>();
                    private final int maxRequestsPerMinute;
                    private final int maxRequestsPerHour;
                    private final int maxRequestsPerDay;
                
                    public RateLimiter() {
                        // Default values - these would be loaded from SLA spec
                        this.maxRequestsPerMinute = 1000;
                        this.maxRequestsPerHour = 10000;
                        this.maxRequestsPerDay = 100000;
                    }
                
                    public RateLimiter(int maxRequestsPerMinute, int maxRequestsPerHour, int maxRequestsPerDay) {
                        this.maxRequestsPerMinute = maxRequestsPerMinute;
                        this.maxRequestsPerHour = maxRequestsPerHour;
                        this.maxRequestsPerDay = maxRequestsPerDay;
                    }
                
                    public boolean isAllowed(String key) {
                        return isAllowed(key, maxRequestsPerMinute, ChronoUnit.MINUTES);
                    }
                
                    public boolean isAllowed(String key, int maxRequests, ChronoUnit timeUnit) {
                        RateLimitData data = rateLimitMap.computeIfAbsent(key, k -> new RateLimitData());
                
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime windowStart = now.minus(1, timeUnit);
                
                        // Clean old entries
                        data.cleanOldEntries(windowStart);
                
                        // Check if limit is exceeded
                        if (data.getRequestCount(windowStart) >= maxRequests) {
                            return false;
                        }
                
                        // Add new request
                        data.addRequest(now);
                        return true;
                    }
                
                    public boolean isAllowedPerMinute(String key) {
                        return isAllowed(key, maxRequestsPerMinute, ChronoUnit.MINUTES);
                    }
                
                    public boolean isAllowedPerHour(String key) {
                        return isAllowed(key, maxRequestsPerHour, ChronoUnit.HOURS);
                    }
                
                    public boolean isAllowedPerDay(String key) {
                        return isAllowed(key, maxRequestsPerDay, ChronoUnit.DAYS);
                    }
                
                    public int getRemainingRequests(String key, ChronoUnit timeUnit) {
                        RateLimitData data = rateLimitMap.get(key);
                        if (data == null) {
                            return getMaxRequests(timeUnit);
                        }
                
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime windowStart = now.minus(1, timeUnit);
                        data.cleanOldEntries(windowStart);
                
                        int currentCount = data.getRequestCount(windowStart);
                        int maxRequests = getMaxRequests(timeUnit);
                
                        return Math.max(0, maxRequests - currentCount);
                    }
                
                    private int getMaxRequests(ChronoUnit timeUnit) {
                        switch (timeUnit) {
                            case MINUTES:
                                return maxRequestsPerMinute;
                            case HOURS:
                                return maxRequestsPerHour;
                            case DAYS:
                                return maxRequestsPerDay;
                            default:
                                return maxRequestsPerMinute;
                        }
                    }
                
                    private static class RateLimitData {
                        private final List<LocalDateTime> requests = new ArrayList<>();
                
                        public synchronized void addRequest(LocalDateTime timestamp) {
                            requests.add(timestamp);
                        }
                
                        public synchronized int getRequestCount(LocalDateTime windowStart) {
                            return (int) requests.stream()
                                    .filter(timestamp -> timestamp.isAfter(windowStart))
                                    .count();
                        }
                
                        public synchronized void cleanOldEntries(LocalDateTime windowStart) {
                            requests.removeIf(timestamp -> timestamp.isBefore(windowStart));
                        }
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RateLimiter.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate rate limit interceptor
     */
    private void generateRateLimitInterceptor(Map<String, Object> slaSpec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Inject;
                import jakarta.inject.Singleton;
                import jakarta.ws.rs.container.ContainerRequestContext;
                import jakarta.ws.rs.container.ContainerRequestFilter;
                import jakarta.ws.rs.core.Response;
                import jakarta.ws.rs.ext.Provider;
                import java.io.IOException;
                import java.time.temporal.ChronoUnit;
                
                @Provider
                @Singleton
                public class RateLimitInterceptor implements ContainerRequestFilter {
                
                    @Inject
                    private RateLimiter rateLimiter;
                
                    @Override
                    public void filter(ContainerRequestContext requestContext) throws IOException {
                        String clientKey = getClientKey(requestContext);
                
                        // Check rate limit
                        if (!rateLimiter.isAllowedPerMinute(clientKey)) {
                            handleRateLimitExceeded(requestContext, clientKey);
                            return;
                        }
                
                        // Add rate limit headers
                        addRateLimitHeaders(requestContext, clientKey);
                    }
                
                    private String getClientKey(ContainerRequestContext requestContext) {
                        // You can use API key, user ID, or IP address
                        String apiKey = requestContext.getHeaderString("X-API-Key");
                        if (apiKey != null && !apiKey.isEmpty()) {
                            return apiKey;
                        }
                
                        // Fallback to IP address from headers
                        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
                        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                            return xForwardedFor.split(",")[0].trim();
                        }
                
                        String xRealIP = requestContext.getHeaderString("X-Real-IP");
                        if (xRealIP != null && !xRealIP.isEmpty()) {
                            return xRealIP;
                        }
                
                        return "default-client";
                    }
                
                    private void handleRateLimitExceeded(ContainerRequestContext requestContext, String clientKey) {
                        int remainingRequests = rateLimiter.getRemainingRequests(clientKey, ChronoUnit.MINUTES);
                
                        Response response = Response.status(Response.Status.TOO_MANY_REQUESTS)
                            .entity("Rate limit exceeded. Please try again later.")
                            .header("Retry-After", "60")
                            .header("X-RateLimit-Remaining", String.valueOf(remainingRequests))
                            .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000))
                            .build();
                
                        requestContext.abortWith(response);
                    }
                
                    private void addRateLimitHeaders(ContainerRequestContext requestContext, String clientKey) {
                        int remainingRequests = rateLimiter.getRemainingRequests(clientKey, ChronoUnit.MINUTES);
                        requestContext.getHeaders().add("X-RateLimit-Remaining", String.valueOf(remainingRequests));
                        requestContext.getHeaders().add("X-RateLimit-Limit", "1000");
                        requestContext.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RateLimitInterceptor.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate rate limit configuration
     */
    private void generateRateLimitConfig(Map<String, Object> slaSpec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                
                @Singleton
                public class RateLimitConfig {
                
                    private final int maxRequestsPerMinute;
                    private final int maxRequestsPerHour;
                    private final int maxRequestsPerDay;
                
                    public RateLimitConfig() {
                        // Default values - these would be loaded from configuration
                        this.maxRequestsPerMinute = 1000;
                        this.maxRequestsPerHour = 10000;
                        this.maxRequestsPerDay = 100000;
                    }
                
                    public RateLimitConfig(int maxRequestsPerMinute, int maxRequestsPerHour, int maxRequestsPerDay) {
                        this.maxRequestsPerMinute = maxRequestsPerMinute;
                        this.maxRequestsPerHour = maxRequestsPerHour;
                        this.maxRequestsPerDay = maxRequestsPerDay;
                    }
                
                    public RateLimiter createRateLimiter() {
                        return new RateLimiter(maxRequestsPerMinute, maxRequestsPerHour, maxRequestsPerDay);
                    }
                
                    public int getMaxRequestsPerMinute() {
                        return maxRequestsPerMinute;
                    }
                
                    public int getMaxRequestsPerHour() {
                        return maxRequestsPerHour;
                    }
                
                    public int getMaxRequestsPerDay() {
                        return maxRequestsPerDay;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RateLimitConfig.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate rate limit service
     */
    private void generateRateLimitService(Map<String, Object> slaSpec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Inject;
                import jakarta.inject.Singleton;
                import java.time.temporal.ChronoUnit;
                import java.util.Map;
                import java.util.HashMap;
                
                @Singleton
                public class RateLimitService {
                
                    @Inject
                    private RateLimiter rateLimiter;
                
                    public RateLimitInfo getRateLimitInfo(String clientKey) {
                        int remainingPerMinute = rateLimiter.getRemainingRequests(clientKey, ChronoUnit.MINUTES);
                        int remainingPerHour = rateLimiter.getRemainingRequests(clientKey, ChronoUnit.HOURS);
                        int remainingPerDay = rateLimiter.getRemainingRequests(clientKey, ChronoUnit.DAYS);
                
                        return new RateLimitInfo(
                            remainingPerMinute,
                            remainingPerHour,
                            remainingPerDay,
                            System.currentTimeMillis() + 60000 // Reset time
                        );
                    }
                
                    public boolean checkRateLimit(String clientKey, ChronoUnit timeUnit) {
                        return rateLimiter.isAllowed(clientKey, getMaxRequests(timeUnit), timeUnit);
                    }
                
                    public Map<String, Object> getRateLimitStatus(String clientKey) {
                        Map<String, Object> status = new HashMap<>();
                        status.put("clientKey", clientKey);
                        status.put("remainingPerMinute", rateLimiter.getRemainingRequests(clientKey, ChronoUnit.MINUTES));
                        status.put("remainingPerHour", rateLimiter.getRemainingRequests(clientKey, ChronoUnit.HOURS));
                        status.put("remainingPerDay", rateLimiter.getRemainingRequests(clientKey, ChronoUnit.DAYS));
                        status.put("resetTime", System.currentTimeMillis() + 60000);
                        return status;
                    }
                
                    private int getMaxRequests(ChronoUnit timeUnit) {
                        switch (timeUnit) {
                            case MINUTES:
                                return 1000;
                            case HOURS:
                                return 10000;
                            case DAYS:
                                return 100000;
                            default:
                                return 1000;
                        }
                    }
                
                    public static class RateLimitInfo {
                        private final int remainingPerMinute;
                        private final int remainingPerHour;
                        private final int remainingPerDay;
                        private final long resetTime;
                
                        public RateLimitInfo(int remainingPerMinute, int remainingPerHour, int remainingPerDay, long resetTime) {
                            this.remainingPerMinute = remainingPerMinute;
                            this.remainingPerHour = remainingPerHour;
                            this.remainingPerDay = remainingPerDay;
                            this.resetTime = resetTime;
                        }
                
                        public int getRemainingPerMinute() {
                            return remainingPerMinute;
                        }
                
                        public int getRemainingPerHour() {
                            return remainingPerHour;
                        }
                
                        public int getRemainingPerDay() {
                            return remainingPerDay;
                        }
                
                        public long getResetTime() {
                            return resetTime;
                        }
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RateLimitService.java"), content.getBytes(StandardCharsets.UTF_8));
    }
}
