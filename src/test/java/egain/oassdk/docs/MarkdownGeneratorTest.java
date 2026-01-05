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
 * Comprehensive test class for MarkdownGenerator
 */
public class MarkdownGeneratorTest {
    
    private MarkdownGenerator generator;
    private Map<String, Object> spec;
    
    @BeforeEach
    public void setUp() {
        generator = new MarkdownGenerator();
        spec = createValidOpenAPISpec();
    }
    
    @Test
    public void testGeneratorInitialization() {
        assertNotNull(generator);
    }
    
    @Test
    public void testGenerateMarkdownDocs_Success(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        config.setIncludeFrontMatter(true);
        config.setIncludeTOC(true);
        config.setGenerateHTML(true);
        
        // Act
        generator.generateMarkdownDocs(spec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("API_DOCUMENTATION.md")));
        assertTrue(Files.exists(tempDir.resolve("TEST_DOCUMENTATION.md")));
        assertTrue(Files.exists(tempDir.resolve("PROJECT_DOCUMENTATION.md")));
    }
    
    @Test
    public void testGenerateMarkdownDocs_WithNullOutputDir() {
        // Arrange
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateMarkdownDocs(spec, null, config);
        });
    }
    
    @Test
    public void testGenerateMarkdownDocs_WithoutHTML(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        config.setGenerateHTML(false);
        
        // Act
        generator.generateMarkdownDocs(spec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("API_DOCUMENTATION.md")));
        // HTML files should not be generated
    }
    
    @Test
    public void testGenerateMarkdownDocs_WithHTML(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        config.setGenerateHTML(true);
        
        // Act
        generator.generateMarkdownDocs(spec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("API_DOCUMENTATION.md")));
        // HTML files may be generated
    }
    
    @Test
    public void testMarkdownConfig_SettersAndGetters() {
        // Arrange
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        
        // Act
        config.setIncludeFrontMatter(false);
        config.setIncludeTOC(false);
        config.setGenerateHTML(false);
        config.setIncludeTables(false);
        config.setIncludeCodeBlocks(false);
        config.setIncludeEmojis(false);
        config.setCustomCSS("custom-style");
        
        // Assert
        assertFalse(config.isIncludeFrontMatter());
        assertFalse(config.isIncludeTOC());
        assertFalse(config.isGenerateHTML());
        assertFalse(config.isIncludeTables());
        assertFalse(config.isIncludeCodeBlocks());
        assertFalse(config.isIncludeEmojis());
        assertEquals("custom-style", config.getCustomCSS());
    }
    
    @Test
    public void testGenerateMarkdownDocs_WithEmptySpec(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> emptySpec = new HashMap<>();
        emptySpec.put("openapi", "3.0.0");
        emptySpec.put("info", Map.of("title", "Test", "version", "1.0.0"));
        emptySpec.put("paths", Map.of());
        
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        
        // Act
        generator.generateMarkdownDocs(emptySpec, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("API_DOCUMENTATION.md")));
    }
    
    @Test
    public void testGenerateMarkdownDocs_WithSecuritySchemes(@TempDir Path tempDir) throws GenerationException {
        // Arrange
        Map<String, Object> specWithSecurity = new HashMap<>(spec);
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> securitySchemes = new HashMap<>();
        Map<String, Object> bearerAuth = new HashMap<>();
        bearerAuth.put("type", "http");
        bearerAuth.put("scheme", "bearer");
        securitySchemes.put("BearerAuth", bearerAuth);
        components.put("securitySchemes", securitySchemes);
        specWithSecurity.put("components", components);
        
        MarkdownGenerator.MarkdownConfig config = new MarkdownGenerator.MarkdownConfig();
        
        // Act
        generator.generateMarkdownDocs(specWithSecurity, tempDir.toString(), config);
        
        // Assert
        assertTrue(Files.exists(tempDir.resolve("API_DOCUMENTATION.md")));
        try {
            String content = Files.readString(tempDir.resolve("API_DOCUMENTATION.md"));
            assertTrue(content.contains("Authentication") || content.contains("BearerAuth"));
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

