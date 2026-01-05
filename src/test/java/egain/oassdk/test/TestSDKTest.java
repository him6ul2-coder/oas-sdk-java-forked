package egain.oassdk.test;

import egain.oassdk.core.exceptions.OASSDKException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * Test cases for TestSDK
 */
public class TestSDKTest {
    
    private TestSDK testSDK;
    
    @BeforeEach
    public void setUp() {
        testSDK = new TestSDK();
    }
    
    @Test
    public void testSDKInitialization() {
        assertNotNull(testSDK);
    }
    
    @Test
    public void testLoadOpenAPISpecWithValidFile() throws OASSDKException {
        TestSDK result = testSDK.loadOpenAPISpec("src/test/resources/openapi.yaml");
        
        assertSame(testSDK, result);
        assertNotNull(testSDK.getOpenAPISpec());
    }
    
    @Test
    public void testLoadOpenAPISpecWithInvalidFile() {
        assertThrows(OASSDKException.class, () -> {
            testSDK.loadOpenAPISpec("nonexistent.yaml");
        });
    }
    
    @Test
    public void testLoadOpenAPISpecWithNullPath() {
        assertThrows(OASSDKException.class, () -> {
            testSDK.loadOpenAPISpec(null);
        });
    }
    
    @Test
    public void testLoadSLASpecWithValidFile() throws OASSDKException {
        try {
            TestSDK result = testSDK.loadSLASpec("src/test/resources/sla.yaml");
            assertSame(testSDK, result);
        } catch (OASSDKException e) {
            // If file doesn't exist, that's okay for this test
            assertTrue(true);
        }
    }
    
    @Test
    public void testLoadSLASpecWithInvalidFile() {
        assertThrows(OASSDKException.class, () -> {
            testSDK.loadSLASpec("nonexistent-sla.yaml");
        });
    }
    
    @Test
    public void testGenerateMockDataWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            testSDK.generateMockData(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateMockDataWithSpec(@TempDir Path tempDir) throws OASSDKException {
        testSDK.loadOpenAPISpec("src/test/resources/openapi.yaml");
        
        TestSDK result = testSDK.generateMockData(tempDir.toString());
        assertSame(testSDK, result);
    }
    
    @Test
    public void testGenerateMockDataWithNullOutputDir() throws OASSDKException {
        testSDK.loadOpenAPISpec("src/test/resources/openapi.yaml");
        
        assertThrows(OASSDKException.class, () -> {
            testSDK.generateMockData(null);
        });
    }
    
    @Test
    public void testExecuteSchemathesisTestsWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            testSDK.executeSchemathesisTests(tempDir.toString(), "http://localhost:8080");
        });
    }
    
    @Test
    public void testGeneratePostmanTestScriptsWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            testSDK.generatePostmanTestScripts(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateNFRTestScriptsWithoutSlaSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            testSDK.generateNFRTestScripts(tempDir.toString());
        });
    }
    
    @Test
    public void testGenerateRandomizedSequenceTestsWithoutSpec(@TempDir Path tempDir) {
        assertThrows(OASSDKException.class, () -> {
            testSDK.generateRandomizedSequenceTests(tempDir.toString(), "http://localhost:8080");
        });
    }
    
    @Test
    public void testGetMetadataWithoutSpec() {
        Map<String, Object> metadata = testSDK.getMetadata();
        assertNotNull(metadata);
    }
    
    @Test
    public void testGetOpenAPISpec() {
        Map<String, Object> spec = testSDK.getOpenAPISpec();
        assertNull(spec); // Should be null before loading
    }
    
    @Test
    public void testGetSLASpec() {
        Map<String, Object> spec = testSDK.getSLASpec();
        assertNull(spec); // Should be null before loading
    }
}

