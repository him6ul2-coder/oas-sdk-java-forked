package egain.oassdk.docs;

import egain.oassdk.core.exceptions.GenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;

/**
 * Test cases for DocumentationGenerator
 */
public class DocumentationGeneratorTest {
    
    private DocumentationGenerator generator;
    private Map<String, Object> openApiSpec;
    
    @BeforeEach
    public void setUp() {
        generator = new DocumentationGenerator();
        
        // Create minimal OpenAPI spec
        openApiSpec = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test API");
        info.put("version", "1.0.0");
        openApiSpec.put("info", info);
        openApiSpec.put("paths", new HashMap<>());
    }
    
    @Test
    public void testGeneratorInitialization() {
        assertNotNull(generator);
    }
    
    @Test
    public void testGenerateWithAllOptions(@TempDir Path tempDir) throws GenerationException {
        assertDoesNotThrow(() -> {
            generator.generate(openApiSpec, tempDir.toString(), true, true, true);
        });
    }
    
    @Test
    public void testGenerateWithAPIDocsOnly(@TempDir Path tempDir) throws GenerationException {
        assertDoesNotThrow(() -> {
            generator.generate(openApiSpec, tempDir.toString(), true, false, false);
        });
    }
    
    @Test
    public void testGenerateWithTestDocsOnly(@TempDir Path tempDir) throws GenerationException {
        assertDoesNotThrow(() -> {
            generator.generate(openApiSpec, tempDir.toString(), false, true, false);
        });
    }
    
    @Test
    public void testGenerateWithProjectDocsOnly(@TempDir Path tempDir) throws GenerationException {
        assertDoesNotThrow(() -> {
            generator.generate(openApiSpec, tempDir.toString(), false, false, true);
        });
    }
    
    @Test
    public void testGenerateWithNullSpec() {
        assertThrows(GenerationException.class, () -> {
            generator.generate(null, "./output", true, true, true);
        });
    }
    
    @Test
    public void testGenerateWithNullOutputDir() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(openApiSpec, null, true, true, true);
        });
    }
}

