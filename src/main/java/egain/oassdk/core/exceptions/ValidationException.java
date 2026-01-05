package egain.oassdk.core.exceptions;

/**
 * Exception raised for validation errors
 */
public class ValidationException extends OASSDKException {

    /**
     * Constructor with message
     *
     * @param message Exception message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Exception message
     * @param cause   Cause of the exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
