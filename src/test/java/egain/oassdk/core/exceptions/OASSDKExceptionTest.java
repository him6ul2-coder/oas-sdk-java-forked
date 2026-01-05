package egain.oassdk.core.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for OASSDKException
 */
public class OASSDKExceptionTest {
    
    @Test
    public void testConstructorWithMessage() {
        String message = "Test exception message";
        OASSDKException exception = new OASSDKException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Root cause");
        OASSDKException exception = new OASSDKException(message, cause);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    public void testExceptionInheritance() {
        OASSDKException exception = new OASSDKException("Test");
        
        assertTrue(exception instanceof Exception);
    }
}

