package egain.oassdk.testgenerators.nfr;

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
 * Non-Functional Requirements (NFR) test generator
 * Generates tests for performance, scalability, reliability, and availability
 */
public class NFRTestGenerator implements TestGenerator, ConfigurableTestGenerator {

    private TestConfig config;

    @Override
    public void generate(Map<String, Object> spec, String outputDir, TestConfig config, String testFramework) throws GenerationException {
        this.config = config;

        try {
            // Create output directory structure
            Path outputPath = Paths.get(outputDir, "nfr");
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

            // Generate NFR test classes
            generateNFRTestClasses(spec, outputPath.toString(), basePackage, apiTitle, baseUrl);

            // Generate NFR configuration
            generateNFRConfiguration(outputPath.toString(), baseUrl);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate NFR tests: " + e.getMessage(), e);
        }
    }

    /**
     * Generate NFR test classes
     */
    private void generateNFRTestClasses(Map<String, Object> spec, String outputDir, String basePackage, String apiTitle, String baseUrl) throws IOException {
        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths == null || paths.isEmpty()) {
            return;
        }

        String packageDir = outputDir + "/" + basePackage.replace(".", "/");
        Files.createDirectories(Paths.get(packageDir));

        // Generate comprehensive NFR test class
        String className = "NFRTest";
        String testClassContent = generateNFRTestClass(basePackage, className, spec, baseUrl);
        Files.write(Paths.get(packageDir, className + ".java"), testClassContent.getBytes());
    }

    /**
     * Generate NFR test class
     */
    private String generateNFRTestClass(String basePackage, String className, Map<String, Object> spec, String baseUrl) {
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
        sb.append("import java.util.concurrent.*;\n");
        sb.append("import java.util.stream.IntStream;\n\n");

        // Class declaration
        sb.append("/**\n");
        sb.append(" * Non-Functional Requirements (NFR) Tests\n");
        sb.append(" * Generated from OpenAPI specification\n");
        sb.append(" * \n");
        sb.append(" * Tests for:\n");
        sb.append(" * - Performance (response time, throughput)\n");
        sb.append(" * - Scalability (concurrent requests)\n");
        sb.append(" * - Reliability (error rates, retry logic)\n");
        sb.append(" * - Availability (uptime, health checks)\n");
        sb.append(" */\n");
        sb.append("@DisplayName(\"NFR Tests\")\n");
        sb.append("public class ").append(className).append(" {\n\n");

        // Constants
        sb.append("    private static final String BASE_URL = \"").append(baseUrl).append("\";\n");
        sb.append("    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);\n");
        sb.append("    private static final int MAX_RESPONSE_TIME_MS = 2000; // 2 seconds\n");
        sb.append("    private static final int CONCURRENT_USERS = 10;\n");
        sb.append("    private static final int REQUESTS_PER_USER = 10;\n");
        sb.append("    private static final double MAX_ERROR_RATE = 0.01; // 1%\n");
        sb.append("    private static HttpClient httpClient;\n\n");

        // Setup
        sb.append("    @BeforeAll\n");
        sb.append("    static void setUpAll() {\n");
        sb.append("        httpClient = HttpClient.newBuilder()\n");
        sb.append("            .connectTimeout(REQUEST_TIMEOUT)\n");
        sb.append("            .build();\n");
        sb.append("    }\n\n");

        // Performance tests
        generatePerformanceTests(sb, spec);

        // Scalability tests
        generateScalabilityTests(sb, spec);

        // Reliability tests
        generateReliabilityTests(sb, spec);

        // Availability tests
        generateAvailabilityTests(sb, spec);

        // Helper methods
        sb.append("    /**\n");
        sb.append("     * Send HTTP request and measure response time\n");
        sb.append("     */\n");
        sb.append("    private long sendRequestAndMeasureTime(HttpRequest request) throws Exception {\n");
        sb.append("        long startTime = System.currentTimeMillis();\n");
        sb.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n");
        sb.append("        long endTime = System.currentTimeMillis();\n");
        sb.append("        return endTime - startTime;\n");
        sb.append("    }\n\n");

        sb.append("    /**\n");
        sb.append("     * Execute concurrent requests\n");
        sb.append("     */\n");
        sb.append("    private List<Long> executeConcurrentRequests(HttpRequest request, int count) {\n");
        sb.append("        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);\n");
        sb.append("        List<CompletableFuture<Long>> futures = new ArrayList<>();\n");
        sb.append("        \n");
        sb.append("        for (int i = 0; i < count; i++) {\n");
        sb.append("            futures.add(CompletableFuture.supplyAsync(() -> {\n");
        sb.append("                try {\n");
        sb.append("                    return sendRequestAndMeasureTime(request);\n");
        sb.append("                } catch (Exception e) {\n");
        sb.append("                    return -1L; // Error indicator\n");
        sb.append("                }\n");
        sb.append("            }, executor));\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        List<Long> responseTimes = new ArrayList<>();\n");
        sb.append("        for (CompletableFuture<Long> future : futures) {\n");
        sb.append("            try {\n");
        sb.append("                responseTimes.add(future.get());\n");
        sb.append("            } catch (Exception e) {\n");
        sb.append("                responseTimes.add(-1L);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        executor.shutdown();\n");
        sb.append("        return responseTimes;\n");
        sb.append("    }\n\n");

        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Generate performance tests
     */
    private void generatePerformanceTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Performance Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null) {
            for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
                String path = pathEntry.getKey();
                Map<String, Object> pathItem = Util.asStringObjectMap(pathEntry.getValue());

                if (pathItem == null) continue;

                if (pathItem.containsKey("get")) {
                    Map<String, Object> operation = Util.asStringObjectMap(pathItem.get("get"));
                    String summary = (String) operation.get("summary");

                    sb.append("    @Test\n");
                    sb.append("    @DisplayName(\"Performance: ").append(summary != null ? summary : "GET " + path)
                            .append(" - Response Time\")\n");
                    sb.append("    void testPerformance_ResponseTime_").append(sanitizePath(path)).append("() throws Exception {\n");
                    sb.append("        // Arrange\n");
                    sb.append("        URI uri = URI.create(BASE_URL + \"").append(path).append("\");\n");
                    sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
                    sb.append("            .uri(uri)\n");
                    sb.append("            .timeout(REQUEST_TIMEOUT)\n");
                    sb.append("            .GET()\n");
                    sb.append("            .header(\"Accept\", \"application/json\")\n");
                    sb.append("            .build();\n\n");
                    sb.append("        // Act - Measure response time\n");
                    sb.append("        long responseTime = sendRequestAndMeasureTime(request);\n\n");
                    sb.append("        // Assert\n");
                    sb.append("        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, \n");
                    sb.append("            \"Response time should be less than \" + MAX_RESPONSE_TIME_MS + \"ms, but was \" + responseTime + \"ms\");\n");
                    sb.append("    }\n\n");
                }
            }
        }
    }

    /**
     * Generate scalability tests
     */
    private void generateScalabilityTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Scalability Tests ==========\n\n");

        Map<String, Object> paths = Util.asStringObjectMap(spec.get("paths"));
        if (paths != null && !paths.isEmpty()) {
            String firstPath = paths.keySet().iterator().next();

            sb.append("    @Test\n");
            sb.append("    @DisplayName(\"Scalability: Concurrent Request Handling\")\n");
            sb.append("    void testScalability_ConcurrentRequests() throws Exception {\n");
            sb.append("        // Arrange\n");
            sb.append("        URI uri = URI.create(BASE_URL + \"").append(firstPath).append("\");\n");
            sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
            sb.append("            .uri(uri)\n");
            sb.append("            .timeout(REQUEST_TIMEOUT)\n");
            sb.append("            .GET()\n");
            sb.append("            .header(\"Accept\", \"application/json\")\n");
            sb.append("            .build();\n\n");
            sb.append("        // Act - Execute concurrent requests\n");
            sb.append("        int totalRequests = CONCURRENT_USERS * REQUESTS_PER_USER;\n");
            sb.append("        List<Long> responseTimes = executeConcurrentRequests(request, totalRequests);\n\n");
            sb.append("        // Assert\n");
            sb.append("        long successCount = responseTimes.stream().filter(rt -> rt > 0).count();\n");
            sb.append("        double successRate = (double) successCount / totalRequests;\n");
            sb.append("        \n");
            sb.append("        assertTrue(successRate >= (1.0 - MAX_ERROR_RATE), \n");
            sb.append("            \"Success rate should be at least \" + (1.0 - MAX_ERROR_RATE) + \", but was \" + successRate);\n");
            sb.append("        \n");
            sb.append("        // Calculate average response time\n");
            sb.append("        double avgResponseTime = responseTimes.stream()\n");
            sb.append("            .filter(rt -> rt > 0)\n");
            sb.append("            .mapToLong(Long::longValue)\n");
            sb.append("            .average()\n");
            sb.append("            .orElse(0.0);\n");
            sb.append("        \n");
            sb.append("        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS * 2, \n");
            sb.append("            \"Average response time under load should be reasonable, but was \" + avgResponseTime + \"ms\");\n");
            sb.append("    }\n\n");
        }
    }

    /**
     * Generate reliability tests
     */
    private void generateReliabilityTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Reliability Tests ==========\n\n");

        sb.append("    @Test\n");
        sb.append("    @DisplayName(\"Reliability: Error Rate Under Normal Load\")\n");
        sb.append("    void testReliability_ErrorRate() throws Exception {\n");
        sb.append("        // Arrange\n");
        sb.append("        URI uri = URI.create(BASE_URL);\n");
        sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
        sb.append("            .uri(uri)\n");
        sb.append("            .timeout(REQUEST_TIMEOUT)\n");
        sb.append("            .GET()\n");
        sb.append("            .build();\n\n");
        sb.append("        // Act - Execute multiple requests\n");
        sb.append("        int requestCount = 100;\n");
        sb.append("        int errorCount = 0;\n");
        sb.append("        \n");
        sb.append("        for (int i = 0; i < requestCount; i++) {\n");
        sb.append("            try {\n");
        sb.append("                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n");
        sb.append("                if (response.statusCode() >= 500) {\n");
        sb.append("                    errorCount++;\n");
        sb.append("                }\n");
        sb.append("            } catch (Exception e) {\n");
        sb.append("                errorCount++;\n");
        sb.append("            }\n");
        sb.append("        }\n\n");
        sb.append("        // Assert\n");
        sb.append("        double errorRate = (double) errorCount / requestCount;\n");
        sb.append("        assertTrue(errorRate <= MAX_ERROR_RATE, \n");
        sb.append("            \"Error rate should be at most \" + MAX_ERROR_RATE + \", but was \" + errorRate);\n");
        sb.append("    }\n\n");
    }

    /**
     * Generate availability tests
     */
    private void generateAvailabilityTests(StringBuilder sb, Map<String, Object> spec) {
        sb.append("    // ========== Availability Tests ==========\n\n");

        sb.append("    @Test\n");
        sb.append("    @DisplayName(\"Availability: Health Check\")\n");
        sb.append("    void testAvailability_HealthCheck() throws Exception {\n");
        sb.append("        // Arrange\n");
        sb.append("        URI uri = URI.create(BASE_URL);\n");
        sb.append("        HttpRequest request = HttpRequest.newBuilder()\n");
        sb.append("            .uri(uri)\n");
        sb.append("            .timeout(REQUEST_TIMEOUT)\n");
        sb.append("            .GET()\n");
        sb.append("            .build();\n\n");
        sb.append("        // Act\n");
        sb.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
        sb.append("        // Assert\n");
        sb.append("        assertTrue(response.statusCode() < 500, \n");
        sb.append("            \"Service should be available (status code < 500), but got \" + response.statusCode());\n");
        sb.append("    }\n\n");
    }

    /**
     * Generate NFR configuration
     */
    private void generateNFRConfiguration(String outputDir, String baseUrl) throws IOException {
        String configContent = "# NFR Test Configuration\n" +
                "# Generated from OpenAPI specification\n\n" +
                "base.url=" + baseUrl + "\n" +
                "max.response.time.ms=2000\n" +
                "concurrent.users=10\n" +
                "requests.per.user=10\n" +
                "max.error.rate=0.01\n" +
                "timeout.seconds=30\n";

        Files.write(Paths.get(outputDir, "nfr-config.properties"), configContent.getBytes());
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
        return "NFR Test Generator";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getTestType() {
        return "nfr";
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
