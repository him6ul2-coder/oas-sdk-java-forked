package egain.oassdk.core.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Comprehensive test cases for OASMetadata
 */
public class OASMetadataTest {
    
    private OASMetadata metadata;
    
    @BeforeEach
    public void setUp() {
        metadata = new OASMetadata();
    }
    
    @Test
    public void testMetadataInitialization() {
        assertNotNull(metadata);
        assertNotNull(metadata.getMetadata());
        assertTrue(metadata.getMetadata().isEmpty());
    }
    
    @Test
    public void testExtractBasicInfo() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertNotNull(result);
        assertTrue(result.containsKey("basic_info"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic_info");
        assertEquals("3.0.0", basicInfo.get("openapi_version"));
        assertEquals("Test API", basicInfo.get("title"));
        assertEquals("1.0.0", basicInfo.get("version"));
    }
    
    @Test
    public void testExtractAPIDetails() {
        Map<String, Object> spec = createOpenAPISpecWithPaths();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("api_details"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> apiDetails = (Map<String, Object>) result.get("api_details");
        assertTrue(apiDetails.containsKey("endpoint_count"));
        assertTrue(apiDetails.containsKey("operation_count"));
        assertTrue(apiDetails.containsKey("method_counts"));
    }
    
    @Test
    public void testExtractEndpoints() {
        Map<String, Object> spec = createOpenAPISpecWithPaths();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("endpoints"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) result.get("endpoints");
        assertFalse(endpoints.isEmpty());
    }
    
    @Test
    public void testExtractModels() {
        Map<String, Object> spec = createOpenAPISpecWithSchemas();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("models"));
        assertTrue(result.containsKey("model_count"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> models = (List<Map<String, Object>>) result.get("models");
        assertFalse(models.isEmpty());
    }
    
    @Test
    public void testExtractSecurity() {
        Map<String, Object> spec = createOpenAPISpecWithSecurity();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("security"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> security = (Map<String, Object>) result.get("security");
        assertTrue(security.containsKey("security_schemes"));
    }
    
    @Test
    public void testExtractServers() {
        Map<String, Object> spec = createOpenAPISpecWithServers();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("servers"));
        assertTrue(result.containsKey("server_count"));
    }
    
    @Test
    public void testExtractTags() {
        Map<String, Object> spec = createOpenAPISpecWithTags();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("tags"));
        assertTrue(result.containsKey("tag_count"));
    }
    
    @Test
    public void testExtractExternalDocs() {
        Map<String, Object> spec = createOpenAPISpecWithExternalDocs();
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        assertTrue(result.containsKey("external_docs"));
    }
    
    @Test
    public void testGetMetadataValue() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        metadata.extract(spec);
        
        Object title = metadata.getMetadataValue("basic_info.title");
        assertEquals("Test API", title);
    }
    
    @Test
    public void testGetMetadataValueNested() {
        Map<String, Object> spec = createOpenAPISpecWithPaths();
        metadata.extract(spec);
        
        Object endpointCount = metadata.getMetadataValue("api_details.endpoint_count");
        assertNotNull(endpointCount);
    }
    
    @Test
    public void testGetMetadataValueNonExistent() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        metadata.extract(spec);
        
        Object value = metadata.getMetadataValue("nonexistent.key");
        assertNull(value);
    }
    
    @Test
    public void testHasMetadata() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        metadata.extract(spec);
        
        assertTrue(metadata.hasMetadata("basic_info"));
        assertTrue(metadata.hasMetadata("basic_info.title"));
        assertFalse(metadata.hasMetadata("nonexistent"));
    }
    
    @Test
    public void testGetMetadataReturnsCopy() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        metadata.extract(spec);
        
        Map<String, Object> metadata1 = metadata.getMetadata();
        Map<String, Object> metadata2 = metadata.getMetadata();
        
        assertNotSame(metadata1, metadata2);
    }
    
    @Test
    public void testExtractWithSwagger() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("swagger", "2.0");
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test");
        info.put("version", "1.0.0");
        spec.put("info", info);
        spec.put("paths", Map.of());
        
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        @SuppressWarnings("unchecked")
        Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic_info");
        assertEquals("2.0", basicInfo.get("swagger_version"));
    }
    
    @Test
    public void testExtractWithContact() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test");
        info.put("version", "1.0.0");
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Test Contact");
        contact.put("email", "test@example.com");
        contact.put("url", "https://example.com");
        info.put("contact", contact);
        spec.put("info", info);
        spec.put("paths", Map.of());
        
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        @SuppressWarnings("unchecked")
        Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic_info");
        assertEquals("Test Contact", basicInfo.get("contact_name"));
        assertEquals("test@example.com", basicInfo.get("contact_email"));
        assertEquals("https://example.com", basicInfo.get("contact_url"));
    }
    
    @Test
    public void testExtractWithLicense() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test");
        info.put("version", "1.0.0");
        Map<String, Object> license = new HashMap<>();
        license.put("name", "MIT");
        license.put("url", "https://opensource.org/licenses/MIT");
        info.put("license", license);
        spec.put("info", info);
        spec.put("paths", Map.of());
        
        metadata.extract(spec);
        
        Map<String, Object> result = metadata.getMetadata();
        @SuppressWarnings("unchecked")
        Map<String, Object> basicInfo = (Map<String, Object>) result.get("basic_info");
        assertEquals("MIT", basicInfo.get("license_name"));
        assertEquals("https://opensource.org/licenses/MIT", basicInfo.get("license_url"));
    }
    
    // Helper methods to create test specifications
    
    private Map<String, Object> createBasicOpenAPISpec() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Test API");
        info.put("version", "1.0.0");
        spec.put("info", info);
        spec.put("paths", Map.of());
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithPaths() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> get = new HashMap<>();
        get.put("operationId", "getTest");
        get.put("responses", Map.of("200", Map.of("description", "OK")));
        pathItem.put("get", get);
        paths.put("/test", pathItem);
        spec.put("paths", paths);
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithSchemas() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> schemas = new HashMap<>();
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("id", Map.of("type", "string")));
        schemas.put("TestModel", schema);
        components.put("schemas", schemas);
        spec.put("components", components);
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithSecurity() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> securitySchemes = new HashMap<>();
        Map<String, Object> scheme = new HashMap<>();
        scheme.put("type", "apiKey");
        securitySchemes.put("apiKey", scheme);
        components.put("securitySchemes", securitySchemes);
        spec.put("components", components);
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithServers() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = new HashMap<>();
        server.put("url", "https://api.example.com");
        server.put("description", "Production server");
        servers.add(server);
        spec.put("servers", servers);
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithTags() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        List<Map<String, Object>> tags = new ArrayList<>();
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", "test");
        tag.put("description", "Test tag");
        tags.add(tag);
        spec.put("tags", tags);
        return spec;
    }
    
    private Map<String, Object> createOpenAPISpecWithExternalDocs() {
        Map<String, Object> spec = createBasicOpenAPISpec();
        Map<String, Object> externalDocs = new HashMap<>();
        externalDocs.put("description", "External documentation");
        externalDocs.put("url", "https://docs.example.com");
        spec.put("externalDocs", externalDocs);
        return spec;
    }
}

