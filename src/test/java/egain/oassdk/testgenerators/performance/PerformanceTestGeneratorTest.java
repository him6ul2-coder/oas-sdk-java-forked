package egain.oassdk.testgenerators.performance;

import egain.oassdk.config.TestConfig;
import egain.oassdk.core.exceptions.GenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for PerformanceTestGenerator
 */
public class PerformanceTestGeneratorTest {
    
    private PerformanceTestGenerator generator;
    private Map<String, Object> spec;
    private TestConfig testConfig;
    
    @BeforeEach
    public void setUp() {
        generator = new PerformanceTestGenerator();
        spec = createValidOpenAPISpec();
        testConfig = new TestConfig();
    }
    
    @Test
    public void testGeneratorInitialization() {
        assertNotNull(generator);
    }
    
    @Test
    public void testGetName() {
        assertEquals("Performance Test Generator", generator.getName());
    }
    
    @Test
    public void testGetVersion() {
        assertEquals("1.0.0", generator.getVersion());
    }
    
    @Test
    public void testGetTestType() {
        assertEquals("performance", generator.getTestType());
    }
    
    @Test
    public void testGenerate_Success(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        testConfig.setTestFramework("junit5");
        
        // Act
        generator.generate(spec, tempDir.toString(), testConfig, "junit5");
        
        // Assert
        // Check that performance test directory was created
        Path performanceDir = tempDir.resolve("performance");
        assertTrue(Files.exists(performanceDir));
        assertTrue(Files.exists(performanceDir.resolve("performance-config.properties")));
    }
    
    @Test
    public void testGenerate_WithEmptySpec(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> emptySpec = new HashMap<>();
        emptySpec.put("openapi", "3.0.0");
        emptySpec.put("info", Map.of("title", "Test", "version", "1.0.0"));
        emptySpec.put("paths", Map.of());
        
        // Act
        generator.generate(emptySpec, tempDir.toString(), testConfig, "junit5");
        
        // Assert
        // Should not throw exception even with empty paths
        assertTrue(Files.exists(tempDir.resolve("performance")));
    }
    
    @Test
    public void testSetConfig() {
        // Arrange
        TestConfig config = new TestConfig();
        config.setTestFramework("junit5");
        
        // Act
        generator.setConfig(config);
        
        // Assert
        assertEquals(config, generator.getConfig());
    }
    
    @Test
    public void testGenerate_WithCustomPackageName(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("packageName", "com.custom.package");
        testConfig.setAdditionalProperties(additionalProps);
        
        // Act
        generator.generate(spec, tempDir.toString(), testConfig, "junit5");
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("performance")));
    }
    
    @Test
    public void testGenerate_WithServers(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> specWithServers = new HashMap<>(spec);
        specWithServers.put("servers", java.util.List.of(
            Map.of("url", "https://api.example.com", "description", "Production")
        ));
        
        // Act
        generator.generate(specWithServers, tempDir.toString(), testConfig, "junit5");
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("performance")));
    }
    
    /**
     * Helper method to create a valid OpenAPI specification
     */
    private Map<String, Object> createValidOpenAPISpec() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");
        spec.put("info", Map.of("title", "Test API", "version", "1.0.0", "description", "Test API Description"));
        
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> get = new HashMap<>();
        get.put("operationId", "getTest");
        get.put("summary", "Get test data");
        get.put("responses", Map.of("200", Map.of("description", "OK")));
        pathItem.put("get", get);
        paths.put("/test", pathItem);
        spec.put("paths", paths);
        
        return spec;
    }
}

