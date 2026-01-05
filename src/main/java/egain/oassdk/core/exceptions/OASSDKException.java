package egain.oassdk.core.exceptions;

/**
 * Base exception for OAS SDK
 */
public class OASSDKException extends Exception {

    /**
     * Constructor with message
     *
     * @param message Exception message
     */
    public OASSDKException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Exception message
     * @param cause   Cause of the exception
     */
    public OASSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
