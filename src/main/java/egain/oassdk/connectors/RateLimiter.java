package egain.oassdk.connectors;

/**
 * Rate Limiter interface for controlling request rates
 * <p>
 * This is a placeholder interface that would be implemented
 * by the actual rate limiting logic in generated applications.
 */
public interface RateLimiter {

    /**
     * Check if a request is allowed for the given client key
     *
     * @param clientKey The client identifier
     * @return true if allowed, false if rate limited
     */
    boolean isAllowed(String clientKey);

    /**
     * Get remaining requests for a client key
     *
     * @param clientKey The client identifier
     * @return number of remaining requests
     */
    int getRemainingRequests(String clientKey);
}
