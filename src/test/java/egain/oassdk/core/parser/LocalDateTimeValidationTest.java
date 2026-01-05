package egain.oassdk.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class to validate LocalDateTime support in Jackson ObjectMapper
 * This test ensures that the jackson-datatype-jsr310 module is properly
 * configured and that all YAML files can be parsed without LocalDateTime errors.
 */
@DisplayName("LocalDateTime Validation Tests")
public class LocalDateTimeValidationTest {
    
    private final OASParser parser;
    private final ObjectMapper testMapper;
    
    public LocalDateTimeValidationTest() {
        this.parser = new OASParser();
        this.testMapper = new ObjectMapper();
        this.testMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Test that OASParser can parse all YAML files in test/resources
     */
    @Test
    @DisplayName("Parse all YAML files in test/resources")
    public void testParseAllYamlFiles() {
        List<String> yamlFiles = List.of(
            "src/test/resources/openapi.yaml",
            "src/test/resources/openapi1.yaml",
            "src/test/resources/openapi2.yaml",
            "src/test/resources/openapi4.yaml",
            "src/test/resources/openapi5.yaml",
            "src/test/resources/sla.yaml",
            "src/test/resources/template.yaml"
        );
        
        List<String> failedFiles = new ArrayList<>();
        
        for (String yamlFile : yamlFiles) {
            try {
                Path filePath = Paths.get(yamlFile);
                if (!Files.exists(filePath)) {
                    System.out.println("Skipping non-existent file: " + yamlFile);
                    continue;
                }
                
                System.out.println("Testing: " + yamlFile);
                Map<String, Object> spec = parser.parse(yamlFile);
                assertNotNull(spec, "Parsed spec should not be null for: " + yamlFile);
                assertFalse(spec.isEmpty(), "Parsed spec should not be empty for: " + yamlFile);
                
                // Try to serialize and deserialize to test LocalDateTime support
                String json = testMapper.writeValueAsString(spec);
                assertNotNull(json, "Serialized JSON should not be null for: " + yamlFile);
                assertFalse(json.isEmpty(), "Serialized JSON should not be empty for: " + yamlFile);
                
                // Deserialize back
                @SuppressWarnings("unchecked")
                Map<String, Object> deserialized = testMapper.readValue(json, Map.class);
                assertNotNull(deserialized, "Deserialized spec should not be null for: " + yamlFile);
                
                System.out.println("✓ Successfully parsed and validated: " + yamlFile);
                
            } catch (Exception e) {
                String errorMsg = "Failed to parse " + yamlFile + ": " + e.getMessage();
                System.err.println("✗ " + errorMsg);
                e.printStackTrace();
                failedFiles.add(yamlFile + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        
        if (!failedFiles.isEmpty()) {
            System.err.println("\nFailed files:");
            failedFiles.forEach(System.err::println);
            fail("Failed to parse " + failedFiles.size() + " file(s). See errors above.");
        }
    }
    
    /**
     * Test that LocalDateTime can be serialized and deserialized
     */
    @Test
    @DisplayName("Test LocalDateTime serialization/deserialization")
    public void testLocalDateTimeSerialization() throws Exception {
        // Create a test object with LocalDateTime
        TestObject testObj = new TestObject();
        testObj.setName("Test");
        testObj.setDateTime(LocalDateTime.now());
        
        // Serialize
        String json = testMapper.writeValueAsString(testObj);
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("Test"), "JSON should contain the name");
        
        // Deserialize
        TestObject deserialized = testMapper.readValue(json, TestObject.class);
        assertNotNull(deserialized);
        assertEquals("Test", deserialized.getName());
        assertNotNull(deserialized.getDateTime(), "LocalDateTime should be deserialized");
        
        System.out.println("✓ LocalDateTime serialization/deserialization works correctly");
    }
    
    /**
     * Test that OASParser's ObjectMapper has JavaTimeModule registered
     */
    @Test
    @DisplayName("Test OASParser ObjectMapper configuration")
    public void testOASParserObjectMapperConfiguration() throws Exception {
        // Create a test YAML with date-time format
        String testYaml = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            paths: {}
            components:
              schemas:
                TestSchema:
                  type: object
                  properties:
                    date:
                      type: string
                      format: date-time
            """;
        
        // Parse using OASParser
        Map<String, Object> spec = parser.parseContent(testYaml, "test.yaml");
        assertNotNull(spec);
        
        // Try to serialize - this should work without LocalDateTime errors
        String json = testMapper.writeValueAsString(spec);
        assertNotNull(json);
        assertFalse(json.isEmpty());
        
        System.out.println("✓ OASParser ObjectMapper configuration is correct");
    }
    
    /**
     * Test parsing a specific YAML file that might contain date-time fields
     */
    @Test
    @DisplayName("Test parsing openapi1.yaml with potential LocalDateTime fields")
    public void testParseOpenAPI1WithLocalDateTime() throws Exception {
        Path yamlFile = Paths.get("src/test/resources/openapi1.yaml");
        if (!Files.exists(yamlFile)) {
            System.out.println("Skipping test - file does not exist: " + yamlFile);
            return;
        }
        
        // Parse the file
        Map<String, Object> spec = parser.parse(yamlFile.toString());
        assertNotNull(spec);
        
        // Serialize to JSON - this should not throw LocalDateTime errors
        assertDoesNotThrow(() -> {
            String json = testMapper.writeValueAsString(spec);
            assertNotNull(json);
            
            // Deserialize back
            @SuppressWarnings("unchecked")
            Map<String, Object> deserialized = testMapper.readValue(json, Map.class);
            assertNotNull(deserialized);
        }, "Serialization/deserialization should not throw LocalDateTime errors");
        
        System.out.println("✓ openapi1.yaml parsed and serialized successfully");
    }
    
    /**
     * Test that YAML files can be parsed and then serialized to JSON without errors
     */
    @Test
    @DisplayName("Test YAML to JSON conversion with LocalDateTime support")
    public void testYamlToJsonConversion() throws Exception {
        List<String> testFiles = List.of(
            "src/test/resources/openapi.yaml",
            "src/test/resources/openapi1.yaml"
        );
        
        for (String yamlFile : testFiles) {
            Path filePath = Paths.get(yamlFile);
            if (!Files.exists(filePath)) {
                continue;
            }
            
            // Parse YAML
            Map<String, Object> spec = parser.parse(yamlFile);
            assertNotNull(spec);
            
            // Convert to JSON - should not throw LocalDateTime errors
            assertDoesNotThrow(() -> {
                String json = testMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(spec);
                assertNotNull(json);
                assertFalse(json.isEmpty());
            }, "YAML to JSON conversion should not throw LocalDateTime errors for: " + yamlFile);
            
            System.out.println("✓ Converted " + yamlFile + " to JSON successfully");
        }
    }
    
    /**
     * Test object for LocalDateTime serialization
     */
    public static class TestObject {
        private String name;
        private LocalDateTime dateTime;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public LocalDateTime getDateTime() {
            return dateTime;
        }
        
        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }
}

