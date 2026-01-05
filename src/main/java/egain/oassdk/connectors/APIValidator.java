package egain.oassdk.connectors;

import java.util.Map;

/**
 * API Validator interface for validating requests and responses
 * <p>
 * This is a placeholder interface that would be implemented
 * by the actual validation logic in generated applications.
 */
public interface APIValidator {

    /**
     * Validate a request
     *
     * @param request The request to validate
     * @return true if valid, false otherwise
     */
    boolean validateRequest(Map<String, Object> request);

    /**
     * Validate a response
     *
     * @param response The response to validate
     * @return true if valid, false otherwise
     */
    boolean validateResponse(Map<String, Object> response);
}
