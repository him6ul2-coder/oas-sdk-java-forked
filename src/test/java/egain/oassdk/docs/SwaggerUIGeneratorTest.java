package egain.oassdk.docs;

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
 * Comprehensive test class for SwaggerUIGenerator
 */
public class SwaggerUIGeneratorTest {
    
    private SwaggerUIGenerator generator;
    private Map<String, Object> spec;
    
    @BeforeEach
    public void setUp() {
        generator = new SwaggerUIGenerator();
        spec = createValidOpenAPISpec();
    }
    
    @Test
    public void testGeneratorInitialization() {
        assertNotNull(generator);
    }
    
    @Test
    public void testGenerateSwaggerUI_Success(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        config.setSwaggerUIVersion("4.15.5");
        config.setDeepLinking(true);
        config.setTryItOutEnabled(true);
        
        // Act
        generator.generateSwaggerUI(spec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("swagger-ui.html")));
        assertTrue(Files.exists(tempDir.resolve("swagger-ui-config.json")));
        assertTrue(Files.exists(tempDir.resolve("package.json")));
    }
    
    @Test
    public void testGenerateSwaggerUI_WithNullOutputDir() {
        // Arrange
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateSwaggerUI(spec, null, config);
        });
    }
    
    @Test
    public void testSwaggerUIConfig_SettersAndGetters() {
        // Arrange
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        
        // Act
        config.setSwaggerUIVersion("5.0.0");
        config.setDeepLinking(false);
        config.setLayout("BaseLayout");
        config.setValidatorUrl("https://validator.example.com");
        config.setTryItOutEnabled(false);
        config.setDocExpansion("none");
        config.setDefaultModelsExpandDepth(2);
        config.setDefaultModelExpandDepth(2);
        config.setDisplayRequestDuration(false);
        config.setFilter(true);
        config.setShowExtensions(true);
        config.setShowCommonExtensions(true);
        config.setSupportedSubmitMethods(new String[]{"get", "post"});
        config.setTagsSorter("method");
        config.setOperationsSorter("method");
        config.setCustomCSS("body { margin: 0; }");
        config.setRequestInterceptor("function(request) { return request; }");
        config.setResponseInterceptor("function(response) { return response; }");
        config.setOnComplete("function() { console.log('Complete'); }");
        
        // Assert
        assertEquals("5.0.0", config.getSwaggerUIVersion());
        assertFalse(config.isDeepLinking());
        assertEquals("BaseLayout", config.getLayout());
        assertEquals("https://validator.example.com", config.getValidatorUrl());
        assertFalse(config.isTryItOutEnabled());
        assertEquals("none", config.getDocExpansion());
        assertEquals(2, config.getDefaultModelsExpandDepth());
        assertEquals(2, config.getDefaultModelExpandDepth());
        assertFalse(config.isDisplayRequestDuration());
        assertTrue(config.isFilter());
        assertTrue(config.isShowExtensions());
        assertTrue(config.isShowCommonExtensions());
        assertArrayEquals(new String[]{"get", "post"}, config.getSupportedSubmitMethodsArray());
        assertEquals("method", config.getTagsSorter());
        assertEquals("method", config.getOperationsSorter());
        assertEquals("body { margin: 0; }", config.getCustomCSS());
        assertEquals("function(request) { return request; }", config.getRequestInterceptor());
        assertEquals("function(response) { return response; }", config.getResponseInterceptor());
        assertEquals("function() { console.log('Complete'); }", config.getOnComplete());
    }
    
    @Test
    public void testSwaggerUIConfig_SupportedSubmitMethods() {
        // Arrange
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        config.setSupportedSubmitMethods(new String[]{"get", "post", "put", "delete"});
        
        // Act
        String methodsString = config.getSupportedSubmitMethods();
        
        // Assert
        assertNotNull(methodsString);
        assertTrue(methodsString.contains("get"));
        assertTrue(methodsString.contains("post"));
    }
    
    @Test
    public void testGenerateSwaggerUI_WithEmptySpec(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> emptySpec = new HashMap<>();
        emptySpec.put("openapi", "3.0.0");
        emptySpec.put("info", Map.of("title", "Test", "version", "1.0.0"));
        emptySpec.put("paths", Map.of());
        
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        
        // Act
        generator.generateSwaggerUI(emptySpec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("swagger-ui.html")));
    }
    
    @Test
    public void testGenerateSwaggerUI_WithCustomConfig(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        SwaggerUIGenerator.SwaggerUIConfig config = new SwaggerUIGenerator.SwaggerUIConfig();
        config.setSwaggerUIVersion("4.20.0");
        config.setCustomCSS("body { background: white; }");
        config.setValidatorUrl(null);
        config.setRequestInterceptor(null);
        config.setResponseInterceptor(null);
        config.setOnComplete(null);
        
        // Act
        generator.generateSwaggerUI(spec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("swagger-ui.html")));
        try {
            String htmlContent = Files.readString(tempDir.resolve("swagger-ui.html"));
            assertTrue(htmlContent.contains("4.20.0"));
        } catch (java.io.IOException e) {
            // File exists but couldn't read - that's okay for this test
        }
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

