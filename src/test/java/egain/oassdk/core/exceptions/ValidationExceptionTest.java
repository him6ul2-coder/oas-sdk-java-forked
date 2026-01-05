package egain.oassdk.core.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ValidationException
 */
public class ValidationExceptionTest {
    
    @Test
    public void testConstructorWithMessage() {
        String message = "Validation error message";
        ValidationException exception = new ValidationException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Validation error message";
        Throwable cause = new RuntimeException("Root cause");
        ValidationException exception = new ValidationException(message, cause);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    public void testExceptionInheritance() {
        ValidationException exception = new ValidationException("Test");
        
        assertTrue(exception instanceof OASSDKException);
        assertTrue(exception instanceof Exception);
    }
}

