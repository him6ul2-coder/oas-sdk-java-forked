package egain.oassdk.connectors;

import java.util.Map;

/**
 * SLA Monitoring Controller interface for tracking SLA metrics
 * <p>
 * This is a placeholder interface that would be implemented
 * by the actual SLA monitoring logic in generated applications.
 */
public interface SLAMonitoringController {

    /**
     * Update SLA metrics for a request/response pair
     *
     * @param request  The request
     * @param response The response
     */
    void updateMetrics(Map<String, Object> request, Map<String, Object> response);

    /**
     * Get current SLA metrics
     *
     * @return Map containing current SLA metrics
     */
    Map<String, Object> getMetrics();

    /**
     * Check if SLA requirements are being met
     *
     * @return true if SLA requirements are met, false otherwise
     */
    boolean isSLACompliant();
}
