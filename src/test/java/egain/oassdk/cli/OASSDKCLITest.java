package egain.oassdk.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for OASSDKCLI
 */
public class OASSDKCLITest {
    
    @Test
    public void testCLIInitialization() {
        OASSDKCLI cli = new OASSDKCLI();
        assertNotNull(cli);
    }
    
    @Test
    public void testCall() throws Exception {
        OASSDKCLI cli = new OASSDKCLI();
        Integer result = cli.call();
        
        assertNotNull(result);
        assertEquals(0, result);
    }
    
    @Test
    public void testGenerateCommandInitialization() {
        OASSDKCLI.GenerateCommand command = new OASSDKCLI.GenerateCommand();
        assertNotNull(command);
    }
    
    @Test
    public void testTestsCommandInitialization() {
        OASSDKCLI.TestsCommand command = new OASSDKCLI.TestsCommand();
        assertNotNull(command);
    }
    
    @Test
    public void testMockDataCommandInitialization() {
        OASSDKCLI.MockDataCommand command = new OASSDKCLI.MockDataCommand();
        assertNotNull(command);
    }
    
    @Test
    public void testSLACommandInitialization() {
        OASSDKCLI.SLACommand command = new OASSDKCLI.SLACommand();
        assertNotNull(command);
    }
    
    @Test
    public void testAllCommandInitialization() {
        OASSDKCLI.AllCommand command = new OASSDKCLI.AllCommand();
        assertNotNull(command);
    }
    
    @Test
    public void testValidateCommandInitialization() {
        OASSDKCLI.ValidateCommand command = new OASSDKCLI.ValidateCommand();
        assertNotNull(command);
    }
    
    @Test
    public void testInfoCommandInitialization() {
        OASSDKCLI.InfoCommand command = new OASSDKCLI.InfoCommand();
        assertNotNull(command);
    }
}

