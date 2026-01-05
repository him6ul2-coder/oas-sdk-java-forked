package egain.oassdk.docs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for OpenAPISpecGenerator
 */
public class OpenAPISpecGeneratorTest {
    
    private OpenAPISpecGenerator generator;
    private Map<String, Object> spec;
    
    @BeforeEach
    public void setUp() {
        generator = new OpenAPISpecGenerator();
        spec = createValidOpenAPISpec();
    }
    
    @Test
    public void testGeneratorInitialization() {
        assertNotNull(generator);
    }
    
    @Test
    public void testGenerateEnhancedOpenAPISpec_WithNullOutputDir() {
        // Arrange
        OpenAPISpecGenerator.OpenAPIConfig config = new OpenAPISpecGenerator.OpenAPIConfig();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateEnhancedOpenAPISpec(spec, null, config);
        });
    }
    
    @Test
    public void testOpenAPIConfig_SettersAndGetters() {
        // Arrange
        OpenAPISpecGenerator.OpenAPIConfig config = new OpenAPISpecGenerator.OpenAPIConfig();
        
        // Act
        config.setApiTitle("My API");
        config.setApiVersion("1.0.0");
        config.setApiDescription("My Description");
        config.setContactName("John Doe");
        config.setContactEmail("john@example.com");
        config.setContactUrl("https://example.com/contact");
        config.setLicenseName("MIT");
        config.setLicenseUrl("https://opensource.org/licenses/MIT");
        config.setTermsOfService("https://example.com/terms");
        config.setServerUrl("https://api.example.com");
        config.setServerDescription("Production Server");
        config.setExternalDocsUrl("https://docs.example.com");
        config.setAddExamples(false);
        config.setAddSecuritySchemes(false);
        config.setAddServerInfo(false);
        config.setAddTags(false);
        config.setAddExternalDocs(false);
        
        // Assert
        assertEquals("My API", config.getApiTitle());
        assertEquals("1.0.0", config.getApiVersion());
        assertEquals("My Description", config.getApiDescription());
        assertEquals("John Doe", config.getContactName());
        assertEquals("john@example.com", config.getContactEmail());
        assertEquals("https://example.com/contact", config.getContactUrl());
        assertEquals("MIT", config.getLicenseName());
        assertEquals("https://opensource.org/licenses/MIT", config.getLicenseUrl());
        assertEquals("https://example.com/terms", config.getTermsOfService());
        assertEquals("https://api.example.com", config.getServerUrl());
        assertEquals("Production Server", config.getServerDescription());
        assertEquals("https://docs.example.com", config.getExternalDocsUrl());
        assertFalse(config.isAddExamples());
        assertFalse(config.isAddSecuritySchemes());
        assertFalse(config.isAddServerInfo());
        assertFalse(config.isAddTags());
        assertFalse(config.isAddExternalDocs());
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

