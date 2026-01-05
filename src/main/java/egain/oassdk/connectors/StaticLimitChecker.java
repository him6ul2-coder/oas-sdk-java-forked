package egain.oassdk.connectors;

import java.util.Map;

/**
 * Static Limit Checker interface for checking request size, field lengths, etc.
 * <p>
 * This is a placeholder interface that would be implemented
 * by the actual static limit checking logic in generated applications.
 */
public interface StaticLimitChecker {

    /**
     * Check static limits for a request
     *
     * @param request The request to check
     * @return true if within limits, false otherwise
     */
    boolean checkLimits(Map<String, Object> request);

    /**
     * Check response size limits
     *
     * @param response The response to check
     * @return true if within limits, false otherwise
     */
    boolean checkResponseLimits(Map<String, Object> response);
}
