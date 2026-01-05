package egain.oassdk.sla;

import egain.oassdk.config.SLAConfig;
import egain.oassdk.core.exceptions.GenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;

/**
 * Test cases for SLAProcessor
 */
public class SLAProcessorTest {
    
    private SLAProcessor processor;
    private Map<String, Object> openApiSpec;
    private Map<String, Object> slaSpec;
    
    @BeforeEach
    public void setUp() {
        processor = new SLAProcessor();
        
        // Create minimal OpenAPI spec
        openApiSpec = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test API");
        info.put("version", "1.0.0");
        openApiSpec.put("info", info);
        openApiSpec.put("paths", new HashMap<>());
        
        // Create minimal SLA spec
        slaSpec = new HashMap<>();
        slaSpec.put("sla", new HashMap<>());
    }
    
    @Test
    public void testProcessorInitialization() {
        assertNotNull(processor);
    }
    
    @Test
    public void testGenerateEnforcement(@TempDir Path tempDir) throws GenerationException {
        SLAConfig config = new SLAConfig();
        
        assertDoesNotThrow(() -> {
            processor.generateEnforcement(openApiSpec, slaSpec, tempDir.toString(), config);
        });
    }
    
    @Test
    public void testGenerateEnforcementWithNullSpec() {
        SLAConfig config = new SLAConfig();
        
        assertThrows(GenerationException.class, () -> {
            processor.generateEnforcement(null, slaSpec, "./output", config);
        });
    }
    
    @Test
    public void testGenerateEnforcementWithNullSlaSpec(@TempDir Path tempDir) throws GenerationException {
        SLAConfig config = new SLAConfig();
        
        // The method may handle null gracefully, so we test it doesn't throw
        assertDoesNotThrow(() -> {
            processor.generateEnforcement(openApiSpec, null, tempDir.toString(), config);
        });
    }
    
    @Test
    public void testGenerateMonitoring(@TempDir Path tempDir) throws GenerationException {
        SLAConfig config = new SLAConfig();
        List<String> monitoringStack = Arrays.asList("prometheus", "grafana");
        
        assertDoesNotThrow(() -> {
            processor.generateMonitoring(openApiSpec, tempDir.toString(), config, monitoringStack);
        });
    }
    
    @Test
    public void testGenerateMonitoringWithNullSpec(@TempDir Path tempDir) throws GenerationException {
        SLAConfig config = new SLAConfig();
        List<String> monitoringStack = Arrays.asList("prometheus");
        
        // The method may handle null gracefully, so we test it doesn't throw
        assertDoesNotThrow(() -> {
            processor.generateMonitoring(null, tempDir.toString(), config, monitoringStack);
        });
    }
    
    @Test
    public void testGenerateMonitoringWithEmptyStack(@TempDir Path tempDir) throws GenerationException {
        SLAConfig config = new SLAConfig();
        List<String> monitoringStack = new ArrayList<>();
        
        assertDoesNotThrow(() -> {
            processor.generateMonitoring(openApiSpec, tempDir.toString(), config, monitoringStack);
        });
    }
}

