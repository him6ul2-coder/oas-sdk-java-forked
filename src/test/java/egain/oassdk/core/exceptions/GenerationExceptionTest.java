package egain.oassdk.core.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for GenerationException
 */
public class GenerationExceptionTest {
    
    @Test
    public void testConstructorWithMessage() {
        String message = "Generation error message";
        GenerationException exception = new GenerationException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Generation error message";
        Throwable cause = new RuntimeException("Root cause");
        GenerationException exception = new GenerationException(message, cause);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    public void testExceptionInheritance() {
        GenerationException exception = new GenerationException("Test");
        
        assertTrue(exception instanceof OASSDKException);
        assertTrue(exception instanceof Exception);
    }
}

