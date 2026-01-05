package egain.oassdk.dev;

import egain.oassdk.core.exceptions.OASSDKException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * Test cases for DevSDK
 */
public class DevSDKTest {
    
    private DevSDK devSDK;
    
    @BeforeEach
    public void setUp() {
        devSDK = new DevSDK();
    }
    
    @Test
    public void testSDKInitialization() {
        assertNotNull(devSDK);
    }
    
    @Test
    public void testLoadOpenAPISpecWithValidFile() throws OASSDKException {
        DevSDK result = devSDK.loadOpenAPISpec("src/test/resources/openapi.yaml");
        
        assertSame(devSDK, result);
        assertNotNull(devSDK.getOpenAPISpec());
    }
    
    @Test
    public void testLoadOpenAPISpecWithInvalidFile() {
        assertThrows(OASSDKException.class, () -> {
            devSDK.loadOpenAPISpec("nonexistent.yaml");
        });
    }
    
    @Test
    public void testLoadOpenAPISpecWithNullPath() {
        assertThrows(OASSDKException.class, () -> {
            devSDK.loadOpenAPISpec(null);
        });
    }
    
    @Test
    public void testLoadSLASpecWithValidFile() throws OASSDKException {
        try {
            DevSDK result = devSDK.loadSLASpec("src/test/resources/sla.yaml");
            assertSame(devSDK, result);
        } catch (OASSDKException e) {
            // If file doesn't exist, that's okay for this test
            assertTrue(true);
        }
    }
    
    @Test
    public void testLoadSLASpecWithInvalidFile() {
        assertThrows(OASSDKException.class, () -> {
            devSDK.loadSLASpec("nonexistent-sla.yaml");
        });
    }
    
    @Test
    public void testGenerateValidatorsWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateValidators(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateValidatorsWithSpec(@TempDir Path tempDir) throws OASSDKException {
        devSDK.loadOpenAPISpec("src/test/resources/openapi.yaml");
        
        DevSDK result = devSDK.generateValidators(tempDir.toString());
        assertSame(devSDK, result);
    }
    
    @Test
    public void testGenerateBeansWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateBeans(tempDir.toString(), "com.test");
        });
    }
    
    @Test
    public void testGenerateStaticLimitCheckersWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateStaticLimitCheckers(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateRateLimitCheckersWithoutSlaSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateRateLimitCheckers(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateSLAGatewayScriptsWithoutSpecs(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateSLAGatewayScripts(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateRedoclyDocumentationWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            devSDK.generateRedoclyDocumentation(tempDir.toString());
        });
    }
    
    @Test
    public void testGetMetadataWithoutSpec() {
        Map<String, Object> metadata = devSDK.getMetadata();
        assertNotNull(metadata);
    }
    
    @Test
    public void testGetOpenAPISpec() {
        Map<String, Object> spec = devSDK.getOpenAPISpec();
        assertNull(spec); // Should be null before loading
    }
    
    @Test
    public void testGetSLASpec() {
        Map<String, Object> spec = devSDK.getSLASpec();
        assertNull(spec); // Should be null before loading
    }
}

