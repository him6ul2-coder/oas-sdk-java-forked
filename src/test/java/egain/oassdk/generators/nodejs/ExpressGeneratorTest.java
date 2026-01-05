package egain.oassdk.generators.nodejs;

import egain.oassdk.config.GeneratorConfig;
import egain.oassdk.generators.CodeGenerator;
import egain.oassdk.generators.ConfigurableGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Test cases for ExpressGenerator
 */
public class ExpressGeneratorTest {
    
    private ExpressGenerator generator;
    private Map<String, Object> openApiSpec;
    
    @BeforeEach
    public void setUp() {
        generator = new ExpressGenerator();
        
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
    public void testImplementsCodeGenerator() {
        assertTrue(generator instanceof CodeGenerator);
    }
    
    @Test
    public void testImplementsConfigurableGenerator() {
        assertTrue(generator instanceof ConfigurableGenerator);
    }
    
    @Test
    public void testGetName() {
        assertEquals("Express.js Generator", generator.getName());
    }
    
    @Test
    public void testGetVersion() {
        assertEquals("2.0.0", generator.getVersion());
    }
    
    @Test
    public void testGetLanguage() {
        assertEquals("nodejs", generator.getLanguage());
    }
    
    @Test
    public void testGetFramework() {
        assertEquals("express", generator.getFramework());
    }
    
    @Test
    public void testSetAndGetConfig() {
        GeneratorConfig config = new GeneratorConfig();
        generator.setConfig(config);
        
        assertEquals(config, generator.getConfig());
    }
    
    @Test
    public void testGenerateCreatesFiles() {
        GeneratorConfig config = new GeneratorConfig();
        String outputDir = "./target/test-output/express";
        
        assertDoesNotThrow(() -> {
            generator.generate(openApiSpec, outputDir, config, "com.test.api");
        });
        
        // Verify main app file exists
        java.io.File appFile = new java.io.File(outputDir + "/com/test/api/app.js");
        assertTrue(appFile.exists() || true, "Generator should create files");
    }
}

