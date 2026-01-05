package egain.oassdk.core.exceptions;

/**
 * Exception raised for code generation errors
 */
public class GenerationException extends OASSDKException {

    /**
     * Constructor with message
     *
     * @param message Exception message
     */
    public GenerationException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Exception message
     * @param cause   Cause of the exception
     */
    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
