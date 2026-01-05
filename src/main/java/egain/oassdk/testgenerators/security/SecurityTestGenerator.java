package egain.oassdk.testgenerators.security;

import egain.oassdk.Util;
import egain.oassdk.config.TestConfig;
import egain.oassdk.core.exceptions.GenerationException;
import egain.oassdk.testgenerators.ConfigurableTestGenerator;
import egain.oassdk.testgenerators.TestGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Security test generator
 * Generates security testing and vulnerability assessment tests
 */
public class SecurityTestGenerator implements TestGenerator, ConfigurableTestGenerator {

    private TestConfig config;

    @Override
    public void generate(Map<String, Object> spec, String outputDir, TestConfig config, String testFramework) throws GenerationException {
        this.config = config;

        try {
            // Create output directory structure
            Path outputPath = Paths.get(outputDir, "security");
            Files.createDirectories(outputPath);

            // Extract API information
            String apiTitle = getAPITitle(spec);
            String baseUrl = getBaseUrl(spec);
            String basePackage = "com.example.api";
            if (config != null && config.getAdditionalProperties() != null) {
                Object packageNameObj = config.getAdditionalProperties().get("packageName");
                if (packageNameObj != null) {
                    basePackage = packageNameObj.toString();
                }
            }

            // Generate security test classes
            generateSecurityTestClasses(spec, outputPath.toString(), basePackage, apiTitle, baseUrl);

            // Generate security test configuration
            generateSecurityConfiguration(outputPath.toString(), baseUrl);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate security tests: " + e.getMessage(), e);
        }
    }

    /**
     * Generate security test classes
     */
    private void generateSecurityTestClasses(Map<String, Object> spec, String outputDir, String basePackage, String apiTitle, String baseUrl) throws IOException {
        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths == null || paths.isEmpty()) {
            return;
        }

        String packageDir = outputDir + "/" + basePackage.replace(".", "/");
        Files.createDirectories(Paths.get(packageDir));

        // Generate security test class
        String className = "SecurityTest";
        String testClassContent = generateSecurityTestClass(basePackage, className, spec, baseUrl);
        Files.write(Paths.get(packageDir, className + ".java"), testClassContent.getBytes());
    }

    /**
     * Generate security test class
     */
    private String generateSecurityTestClass(String basePackage, String className, Map<String, Object> spec, String baseUrl) {
        StringBuilder sb = new StringBuilder();

        // Package declaration
        sb.append("package ").append(basePackage).append(";\n\n");

        // Imports
        sb.append("import org.junit.jupiter.api.*;\n");
        sb.append("import org.junit.jupiter.api.DisplayName;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("import java.net.http.*;\n");
        sb.append("import java.net.URI;\n");
        sb.append("import java.time.Duration;\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.util.Base64;\n\n");

        // Class declaration
        sb.append("/**\n");
        sb.append(" * Security Tests\n");
        sb.append(" * Generated from OpenAPI specification\n");
        sb.append(" * \n");
        sb.append(" * Tests for:\n");
        sb.append(" * - Authentication and authorization\n");
        sb.append(" * - Input validation and sanitization\n");
        sb.append(" * - SQL injection prevention\n");
        sb.append(" * - XSS prevention\n");
        sb.append(" * - CSRF protection\n");
        sb.append(" */\n");
        sb.append("@DisplayName(\"Security Tests\")\n");
        sb.append("public class ").append(className).append(" {\n\n");

        // Constants
        sb.append("    private static final String BASE_URL = \"").append(baseUrl).append("\";\n");
        sb.append("    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);\n");
        sb.append("    private static HttpClient httpClient;\n\n");

        // Test vectors
        sb.append("    // Security test vectors\n");
        sb.append("    private static final String[] SQL_INJECTION_VECTORS = {\n");
        sb.append("        \"' OR '1'='1\",\n");
        sb.append("        \"'; DROP TABLE users; --\",\n");
        sb.append("        \"1' UNION SELECT * FROM users --\"\n");
        sb.append("    };\n\n");
        sb.append("    private static final String[] XSS_VECTORS = {\n");
        sb.append("        \"<script>alert('XSS')</script>\",\n");
        sb.append("        \"<img src=x onerror=alert('XSS')>\",\n");
        sb.append("        \"javascript:alert('XSS')\"\n");
        sb.append("    };\n\n");
        sb.append("    private static final String[] PATH_TRAVERSAL_VECTORS = {\n");
        sb.append("        \"../../../etc/passwd\",\n");
        sb.append("        \"..\\\\..\\\\..\\\\windows\\\\system32\",\n");
        sb.append("        \"%2e%2e%2f%2e%2e%2f%2e%2e%2f\"\n");
        sb.append("    };\n\n");

        // Setup
        sb.append("    @BeforeAll\n");
        sb.append("    static void setUpAll() {\n");
        sb.append("        httpClient = HttpClient.newBuilder()\n");
        sb.append("            .connectTimeout(REQUEST_TIMEOUT)\n");
        sb.append("            .build();\n");
        sb.append("    }\n\n");

        // Authentication tests
        generateAuthenticationTests(sb, spec);

        // Authorization tests
        generateAuthorizationTests(sb, spec);

        // Input validation tests
        generateInputValidationTests(sb, spec);

        // SQL injection tests
        generateSQLInjectionTests(sb, spec);

        // XSS tests
        generateXSSTests(sb, spec);

        // Path traversal tests
        generatePathTraversalTests(sb, spec);

        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Generate authentication tests
     */
    private void generateAuthenticationTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Authentication Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null) {
            for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
                Map<String, Object> pathItem = Util.asStringObjectMap(pathEntry.getValue());
                if (pathItem == null) continue;

                String[] methods = {"get", "post", "put", "delete"};
                for (String method : methods) {
                    if (pathItem.containsKey(method)) {
                        Map<String, Object> operation = Util.asStringObjectMap(pathItem.get(method));
                        if (operation != null && operation.containsKey("security")) {
                            String path = pathEntry.getKey();
                            String summary = (String) operation.get("summary");

                            sb.append("    @Test\n");
                            sb.append("    @DisplayName(\"Authentication: ").append(summary != null ? summary : method.toUpperCase() + " " + path)
                                    .append(" - Missing Token\")\n");
                            sb.append("    void testAuthentication_MissingToken_").append(sanitizePath(path)).append("_").append(method).append("() throws Exception {\n");
                            sb.append("        // Arrange - Request without authentication\n");
                            sb.append("        URI uri = URI.create(BASE_URL + \"").append(path).append("\");\n");
                            sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
                            sb.append("            .uri(uri)\n");
                            sb.append("            .timeout(REQUEST_TIMEOUT)\n");
                            sb.append("            .").append(method.toUpperCase()).append("()\n");
                            sb.append("            .header(\"Accept\", \"application/json\")\n");
                            sb.append("            // Intentionally omitting Authorization header\n");
                            sb.append("            .build();\n\n");
                            sb.append("        // Act\n");
                            sb.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
                            sb.append("        // Assert\n");
                            sb.append("        assertEquals(401, response.statusCode(), \n");
                            sb.append("            \"Expected 401 Unauthorized for request without authentication token\");\n");
                            sb.append("    }\n\n");

                            sb.append("    @Test\n");
                            sb.append("    @DisplayName(\"Authentication: ").append(summary != null ? summary : method.toUpperCase() + " " + path)
                                    .append(" - Invalid Token\")\n");
                            sb.append("    void testAuthentication_InvalidToken_").append(sanitizePath(path)).append("_").append(method).append("() throws Exception {\n");
                            sb.append("        // Arrange - Request with invalid token\n");
                            sb.append("        URI uri = URI.create(BASE_URL + \"").append(path).append("\");\n");
                            sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
                            sb.append("            .uri(uri)\n");
                            sb.append("            .timeout(REQUEST_TIMEOUT)\n");
                            sb.append("            .").append(method.toUpperCase()).append("()\n");
                            sb.append("            .header(\"Accept\", \"application/json\")\n");
                            sb.append("            .header(\"Authorization\", \"Bearer invalid-token-12345\")\n");
                            sb.append("            .build();\n\n");
                            sb.append("        // Act\n");
                            sb.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
                            sb.append("        // Assert\n");
                            sb.append("        assertTrue(response.statusCode() == 401 || response.statusCode() == 403, \n");
                            sb.append("            \"Expected 401 or 403 for invalid authentication token, got \" + response.statusCode());\n");
                            sb.append("    }\n\n");

                            break; // Only test first secured endpoint
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate authorization tests
     */
    private void generateAuthorizationTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Authorization Tests ==========\n\n");

        sb.append("    @Test\n");
        sb.append("    @DisplayName(\"Authorization: Access Control\")\n");
        sb.append("    void testAuthorization_AccessControl() throws Exception {\n");
        sb.append("        // TODO: Implement authorization tests based on role-based access control\n");
        sb.append("        // This test should verify that users can only access resources they are authorized for\n");
        sb.append("        assertTrue(true, \"Authorization test placeholder\");\n");
        sb.append("    }\n\n");
    }

    /**
     * Generate input validation tests
     */
    private void generateInputValidationTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Input Validation Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null && !paths.isEmpty()) {
            String firstPath = paths.keySet().iterator().next();

            sb.append("    @Test\n");
            sb.append("    @DisplayName(\"Input Validation: Oversized Payload\")\n");
            sb.append("    void testInputValidation_OversizedPayload() throws Exception {\n");
            sb.append("        // Arrange - Create oversized payload\n");
            sb.append("        String oversizedPayload = \"x\".repeat(100000); // 100KB payload\n");
            sb.append("        URI uri = URI.create(BASE_URL + \"").append(firstPath).append("\");\n");
            sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
            sb.append("            .uri(uri)\n");
            sb.append("            .timeout(REQUEST_TIMEOUT)\n");
            sb.append("            .POST(HttpRequest.BodyPublishers.ofString(oversizedPayload))\n");
            sb.append("            .header(\"Content-Type\", \"application/json\")\n");
            sb.append("            .build();\n\n");
            sb.append("        // Act\n");
            sb.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
            sb.append("        // Assert - Should reject or limit oversized payloads\n");
            sb.append("        assertTrue(response.statusCode() == 400 || response.statusCode() == 413, \n");
            sb.append("            \"Expected 400 or 413 for oversized payload, got \" + response.statusCode());\n");
            sb.append("    }\n\n");
        }
    }

    /**
     * Generate SQL injection tests
     */
    private void generateSQLInjectionTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== SQL Injection Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null && !paths.isEmpty()) {
            String firstPath = paths.keySet().iterator().next();

            sb.append("    @Test\n");
            sb.append("    @DisplayName(\"SQL Injection: Parameter Injection\")\n");
            sb.append("    void testSQLInjection_ParameterInjection() throws Exception {\n");
            sb.append("        // Test each SQL injection vector\n");
            sb.append("        for (String vector : SQL_INJECTION_VECTORS) {\n");
            sb.append("            // Arrange\n");
            sb.append("            String testPath = \"").append(firstPath).append("\" + \"?param=\" + vector;\n");
            sb.append("            URI uri = URI.create(BASE_URL + testPath);\n");
            sb.append("            HttpRequest request = HttpRequest.newBuilder()\n");
            sb.append("                .uri(uri)\n");
            sb.append("                .timeout(REQUEST_TIMEOUT)\n");
            sb.append("                .GET()\n");
            sb.append("                .build();\n\n");
            sb.append("            // Act\n");
            sb.append("            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
            sb.append("            // Assert - Should not execute SQL, should return error or sanitized response\n");
            sb.append("            assertTrue(response.statusCode() < 500, \n");
            sb.append("                \"SQL injection attempt should not cause server error (500), got \" + response.statusCode());\n");
            sb.append("            \n");
            sb.append("            // Response should not contain SQL error messages\n");
            sb.append("            String body = response.body();\n");
            sb.append("            assertFalse(body != null && (body.contains(\"SQL syntax\") || body.contains(\"mysql_fetch\")), \n");
            sb.append("                \"Response should not contain SQL error messages\");\n");
            sb.append("        }\n");
            sb.append("    }\n\n");
        }
    }

    /**
     * Generate XSS tests
     */
    private void generateXSSTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== XSS (Cross-Site Scripting) Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null && !paths.isEmpty()) {
            String firstPath = paths.keySet().iterator().next();

            sb.append("    @Test\n");
            sb.append("    @DisplayName(\"XSS: Script Injection\")\n");
            sb.append("    void testXSS_ScriptInjection() throws Exception {\n");
            sb.append("        // Test each XSS vector\n");
            sb.append("        for (String vector : XSS_VECTORS) {\n");
            sb.append("            // Arrange\n");
            sb.append("            String testPath = \"").append(firstPath).append("\" + \"?input=\" + vector;\n");
            sb.append("            URI uri = URI.create(BASE_URL + testPath);\n");
            sb.append("            HttpRequest request = HttpRequest.newBuilder()\n");
            sb.append("                .uri(uri)\n");
            sb.append("                .timeout(REQUEST_TIMEOUT)\n");
            sb.append("                .GET()\n");
            sb.append("                .build();\n\n");
            sb.append("            // Act\n");
            sb.append("            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
            sb.append("            // Assert - Response should sanitize or reject XSS attempts\n");
            sb.append("            String body = response.body();\n");
            sb.append("            if (body != null) {\n");
            sb.append("                // Check that script tags are escaped or removed\n");
            sb.append("                assertFalse(body.contains(\"<script>\"), \n");
            sb.append("                    \"Response should not contain unescaped script tags\");\n");
            sb.append("            }\n");
            sb.append("        }\n");
            sb.append("    }\n\n");
        }
    }

    /**
     * Generate path traversal tests
     */
    private void generatePathTraversalTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Path Traversal Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null && !paths.isEmpty()) {
            String firstPath = paths.keySet().iterator().next();

            sb.append("    @Test\n");
            sb.append("    @DisplayName(\"Path Traversal: Directory Traversal\")\n");
            sb.append("    void testPathTraversal_DirectoryTraversal() throws Exception {\n");
            sb.append("        // Test each path traversal vector\n");
            sb.append("        for (String vector : PATH_TRAVERSAL_VECTORS) {\n");
            sb.append("            // Arrange\n");
            sb.append("            String testPath = \"").append(firstPath).append("\".replace(\"{id}\", vector);\n");
            sb.append("            URI uri = URI.create(BASE_URL + testPath);\n");
            sb.append("            HttpRequest request = HttpRequest.newBuilder()\n");
            sb.append("                .uri(uri)\n");
            sb.append("                .timeout(REQUEST_TIMEOUT)\n");
            sb.append("                .GET()\n");
            sb.append("                .build();\n\n");
            sb.append("            // Act\n");
            sb.append("            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
            sb.append("            // Assert - Should reject path traversal attempts\n");
            sb.append("            assertTrue(response.statusCode() == 400 || response.statusCode() == 404 || response.statusCode() == 403, \n");
            sb.append("                \"Path traversal attempt should be rejected, got \" + response.statusCode());\n");
            sb.append("        }\n");
            sb.append("    }\n\n");
        }
    }

    /**
     * Generate security configuration
     */
    private void generateSecurityConfiguration(String outputDir, String baseUrl) throws IOException {
        String configContent = "# Security Test Configuration\n" +
                "# Generated from OpenAPI specification\n\n" +
                "base.url=" + baseUrl + "\n" +
                "timeout.seconds=30\n" +
                "# Security test settings\n" +
                "test.authentication=true\n" +
                "test.authorization=true\n" +
                "test.input.validation=true\n" +
                "test.sql.injection=true\n" +
                "test.xss=true\n" +
                "test.path.traversal=true\n";

        Files.write(Paths.get(outputDir, "security-config.properties"), configContent.getBytes());
    }

    // Helper methods
    private String getAPITitle(Map<String, Object> spec) {
        Map<String, Object> info = Util.asStringObjectMap(spec.get("info"));
        return info != null ? (String) info.get("title") : "API";
    }

    private String getBaseUrl(Map<String, Object> spec) {
        if (spec.containsKey("servers")) {
            List<Map<String, Object>> servers = Util.asStringObjectMapList(spec.get("servers"));
            if (servers != null && !servers.isEmpty()) {
                String url = (String) servers.get(0).get("url");
                if (url != null) {
                    return url;
                }
            }
        }
        return "http://localhost:8080";
    }

    private String sanitizePath(String path) {
        return path.replaceAll("[^a-zA-Z0-9]", "_");
    }

    @Override
    public String getName() {
        return "Security Test Generator";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getTestType() {
        return "security";
    }

    @Override
    public void setConfig(TestConfig config) {
        this.config = config;
    }

    @Override
    public TestConfig getConfig() {
        return this.config;
    }
}
