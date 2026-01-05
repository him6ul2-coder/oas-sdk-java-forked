package egain.oassdk.test.sequence;

import egain.oassdk.core.exceptions.GenerationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Generates randomized sequence testing of API using OpenAPI spec
 */
public class RandomizedSequenceTester {

    /**
     * Generate sequence tests
     *
     * @param spec      OpenAPI specification
     * @param outputDir Output directory
     * @param baseUrl   Base URL for the API under test
     * @throws GenerationException if generation fails
     */
    public void generateSequenceTests(Map<String, Object> spec, String outputDir, String baseUrl) throws GenerationException {
        try {
            Files.createDirectories(Paths.get(outputDir));

            // Generate sequence test framework
            generateSequenceTestFramework(spec, outputDir, baseUrl);

            // Generate random sequence generator
            generateRandomSequenceGenerator(spec, outputDir, baseUrl);

            // Generate sequence test cases
            generateSequenceTestCases(spec, outputDir, baseUrl);

            // Generate sequence test runner
            generateSequenceTestRunner(spec, outputDir, baseUrl);

            // Generate configuration
            generateSequenceTestConfig(spec, outputDir, baseUrl);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate sequence tests: " + e.getMessage(), e);
        }
    }

    /**
     * Generate sequence test framework
     */
    private void generateSequenceTestFramework(Map<String, Object> spec, String outputDir, String baseUrl) throws IOException {
        String framework = """
                package com.example.sequence;
                
                import jakarta.ws.rs.client.Client;
                import jakarta.ws.rs.client.ClientBuilder;
                import jakarta.ws.rs.client.Entity;
                import jakarta.ws.rs.client.WebTarget;
                import jakarta.ws.rs.core.MediaType;
                import jakarta.ws.rs.core.Response;
                import org.junit.jupiter.api.BeforeEach;
                import org.junit.jupiter.api.AfterEach;
                import java.util.*;
                import java.util.concurrent.*;
                import java.util.stream.Collectors;
                
                public class SequenceTestFramework {
                
                    protected Client client;
                    protected WebTarget target;
                
                    protected final String baseUrl = "%s";
                    protected final Random random = new Random();
                    protected final Map<String, Object> state = new ConcurrentHashMap<>();
                
                    @BeforeEach
                    public void setUp() {
                        client = ClientBuilder.newClient();
                        target = client.target(baseUrl);
                    }
                
                    @AfterEach
                    public void tearDown() {
                        if (client != null) {
                            client.close();
                        }
                    }
                
                    /**
                     * Execute a sequence of API calls
                     */
                    protected List<SequenceResult> executeSequence(List<APICall> sequence) {
                        List<SequenceResult> results = new ArrayList<>();
                
                        for (APICall call : sequence) {
                            try {
                                long startTime = System.currentTimeMillis();
                                Response response = executeAPICall(call);
                                long responseTime = System.currentTimeMillis() - startTime;
                
                                SequenceResult result = new SequenceResult(
                                    call, response, responseTime, true, null
                                );
                                results.add(result);
                
                                // Update state based on response
                                updateState(call, response);
                
                            } catch (Exception e) {
                                SequenceResult result = new SequenceResult(
                                    call, null, 0, false, e.getMessage()
                                );
                                results.add(result);
                            }
                        }
                
                        return results;
                    }
                
                    /**
                     * Execute a single API call
                     */
                    protected Response executeAPICall(APICall call) {
                        WebTarget callTarget = target.path(call.getPath());
                
                        switch (call.getMethod().toUpperCase()) {
                            case "GET":
                                return callTarget.request(MediaType.APPLICATION_JSON).get();
                            case "POST":
                                return callTarget.request(MediaType.APPLICATION_JSON)
                                    .post(Entity.entity(call.getBody(), MediaType.APPLICATION_JSON));
                            case "PUT":
                                return callTarget.request(MediaType.APPLICATION_JSON)
                                    .put(Entity.entity(call.getBody(), MediaType.APPLICATION_JSON));
                            case "DELETE":
                                return callTarget.request(MediaType.APPLICATION_JSON).delete();
                            default:
                                throw new IllegalArgumentException("Unsupported HTTP method: " + call.getMethod());
                        }
                    }
                
                    /**
                     * Update state based on API response
                     */
                    protected void updateState(APICall call, Response response) {
                        // Extract data from response and store in state
                        if (response.getStatus() == 200 && response.hasEntity()) {
                            // Parse response and extract relevant data
                            String responseBody = response.readEntity(String.class);
                
                            // Store response data in state for use in subsequent calls
                            state.put("last_response", responseBody);
                            state.put("last_status", response.getStatus());
                
                            // Extract specific data based on API endpoint
                            if (call.getPath().contains("/users/")) {
                                // Extract user ID from response
                                String userId = extractUserId(responseBody);
                                if (userId != null) {
                                    state.put("user_id", userId);
                                }
                            }
                        }
                    }
                
                    /**
                     * Extract user ID from response
                     */
                    private String extractUserId(String responseBody) {
                        // Simple extraction - in real implementation, use proper JSON parsing
                        if (responseBody.contains(""id"")) {
                            int start = responseBody.indexOf(""id":") + 5;
                            int end = responseBody.indexOf(",", start);
                            if (end == -1) end = responseBody.indexOf("}", start);
                            if (end > start) {
                                return responseBody.substring(start, end).trim().replace("\"", "");
                            }
                        }
                        return null;
                    }
                
                    /**
                     * Generate random sequence of API calls
                     */
                    protected List<APICall> generateRandomSequence(List<APICall> availableCalls, int maxLength) {
                        List<APICall> sequence = new ArrayList<>();
                        int sequenceLength = random.nextInt(maxLength) + 1;
                
                        for (int i = 0; i < sequenceLength; i++) {
                            APICall call = availableCalls.get(random.nextInt(availableCalls.size()));
                            sequence.add(call);
                        }
                
                        return sequence;
                    }
                
                    /**
                     * Validate sequence results
                     */
                    protected boolean validateSequenceResults(List<SequenceResult> results) {
                        for (SequenceResult result : results) {
                            if (!result.isSuccess()) {
                                return false;
                            }
                
                            if (result.getResponseTime() > 5000) { // 5 seconds
                                return false;
                            }
                        }
                
                        return true;
                    }
                
                    /**
                     * API Call representation
                     */
                    public static class APICall {
                        private final String method;
                        private final String path;
                        private final Object body;
                        private final Map<String, String> headers;
                
                        public APICall(String method, String path, Object body, Map<String, String> headers) {
                            this.method = method;
                            this.path = path;
                            this.body = body;
                            this.headers = headers != null ? headers : new HashMap<>();
                        }
                
                        public String getMethod() { return method; }
                        public String getPath() { return path; }
                        public Object getBody() { return body; }
                        public Map<String, String> getHeaders() { return headers; }
                    }
                
                    /**
                     * Sequence result representation
                     */
                    public static class SequenceResult {
                        private final APICall call;
                        private final Response response;
                        private final long responseTime;
                        private final boolean success;
                        private final String errorMessage;
                
                        public SequenceResult(APICall call, Response response,
                                            long responseTime, boolean success, String errorMessage) {
                            this.call = call;
                            this.response = response;
                            this.responseTime = responseTime;
                            this.success = success;
                            this.errorMessage = errorMessage;
                        }
                
                        public APICall getCall() { return call; }
                        public Response getResponse() { return response; }
                        public long getResponseTime() { return responseTime; }
                        public boolean isSuccess() { return success; }
                        public String getErrorMessage() { return errorMessage; }
                    }
                }
                """.formatted(baseUrl, baseUrl);

        Files.write(Paths.get(outputDir, "SequenceTestFramework.java"), framework.getBytes());
    }

    /**
     * Generate random sequence generator
     */
    private void generateRandomSequenceGenerator(Map<String, Object> spec, String outputDir, String baseUrl) throws IOException {
        String generator = """
                package com.example.sequence;
                
                import java.util.*;
                import java.util.stream.Collectors;
                
                public class RandomSequenceGenerator {
                
                    private final Random random = new Random();
                    private final List<APICall> availableCalls;
                
                    public RandomSequenceGenerator(List<APICall> availableCalls) {
                        this.availableCalls = availableCalls;
                    }
                
                    /**
                     * Generate random sequence based on OpenAPI spec
                     */
                    public List<APICall> generateRandomSequence(int maxLength) {
                        List<APICall> sequence = new ArrayList<>();
                        int sequenceLength = random.nextInt(maxLength) + 1;
                
                        for (int i = 0; i < sequenceLength; i++) {
                            APICall call = availableCalls.get(random.nextInt(availableCalls.size()));
                            sequence.add(call);
                        }
                
                        return sequence;
                    }
                
                    /**
                     * Generate weighted random sequence
                     */
                    public List<APICall> generateWeightedSequence(int maxLength, Map<String, Double> weights) {
                        List<APICall> sequence = new ArrayList<>();
                        int sequenceLength = random.nextInt(maxLength) + 1;
                
                        for (int i = 0; i < sequenceLength; i++) {
                            APICall call = selectWeightedCall(weights);
                            sequence.add(call);
                        }
                
                        return sequence;
                    }
                
                    /**
                     * Generate sequence with dependencies
                     */
                    public List<APICall> generateDependentSequence(int maxLength, Map<String, List<String>> dependencies) {
                        List<APICall> sequence = new ArrayList<>();
                        Set<String> executedCalls = new HashSet<>();
                        int sequenceLength = random.nextInt(maxLength) + 1;
                
                        for (int i = 0; i < sequenceLength; i++) {
                            APICall call = selectDependentCall(executedCalls, dependencies);
                            if (call != null) {
                                sequence.add(call);
                                executedCalls.add(call.getPath() + ":" + call.getMethod());
                            }
                        }
                
                        return sequence;
                    }
                
                    /**
                     * Select weighted call
                     */
                    private APICall selectWeightedCall(Map<String, Double> weights) {
                        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
                        double randomWeight = random.nextDouble() * totalWeight;
                
                        double currentWeight = 0;
                        for (APICall call : availableCalls) {
                            String key = call.getPath() + ":" + call.getMethod();
                            currentWeight += weights.getOrDefault(key, 1.0);
                            if (currentWeight >= randomWeight) {
                                return call;
                            }
                        }
                
                        return availableCalls.get(random.nextInt(availableCalls.size()));
                    }
                
                    /**
                     * Select dependent call
                     */
                    private APICall selectDependentCall(Set<String> executedCalls, Map<String, List<String>> dependencies) {
                        List<APICall> availableCalls = this.availableCalls.stream()
                            .filter(call -> {
                                String key = call.getPath() + ":" + call.getMethod();
                                List<String> deps = dependencies.get(key);
                                return deps == null || deps.stream().allMatch(executedCalls::contains);
                            })
                            .collect(Collectors.toList());
                
                        if (availableCalls.isEmpty()) {
                            return null;
                        }
                
                        return availableCalls.get(random.nextInt(availableCalls.size()));
                    }
                
                    /**
                     * Generate sequence with state transitions
                     */
                    public List<APICall> generateStatefulSequence(int maxLength, Map<String, String> stateTransitions) {
                        List<APICall> sequence = new ArrayList<>();
                        String currentState = "initial";
                        int sequenceLength = random.nextInt(maxLength) + 1;
                
                        for (int i = 0; i < sequenceLength; i++) {
                            APICall call = selectStatefulCall(currentState, stateTransitions);
                            if (call != null) {
                                sequence.add(call);
                                currentState = stateTransitions.getOrDefault(call.getPath() + ":" + call.getMethod(), currentState);
                            }
                        }
                
                        return sequence;
                    }
                
                    /**
                     * Select stateful call
                     */
                    private APICall selectStatefulCall(String currentState, Map<String, String> stateTransitions) {
                        List<APICall> availableCalls = this.availableCalls.stream()
                            .filter(call -> {
                                String key = call.getPath() + ":" + call.getMethod();
                                return stateTransitions.containsKey(key);
                            })
                            .collect(Collectors.toList());
                
                        if (availableCalls.isEmpty()) {
                            return null;
                        }
                
                        return availableCalls.get(random.nextInt(availableCalls.size()));
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RandomSequenceGenerator.java"), generator.getBytes());
    }

    /**
     * Generate sequence test cases
     */
    private void generateSequenceTestCases(Map<String, Object> spec, String outputDir, String baseUrl) throws IOException {
        String testCases = """
                package com.example.sequence;
                
                import org.junit.jupiter.api.Test;
                import org.junit.jupiter.api.BeforeEach;
                import static org.junit.jupiter.api.Assertions.*;
                
                public class SequenceTestCases extends SequenceTestFramework {
                
                    private List<APICall> availableCalls;
                    private RandomSequenceGenerator generator;
                
                    @BeforeEach
                    public void setUp() {
                        // Initialize available API calls based on OpenAPI spec
                        availableCalls = initializeAPICalls();
                        generator = new RandomSequenceGenerator(availableCalls);
                    }
                
                    @Test
                    public void testRandomSequence() {
                        List<APICall> sequence = generator.generateRandomSequence(10);
                        List<SequenceResult> results = executeSequence(sequence);
                
                        assertTrue(validateSequenceResults(results),
                            "Random sequence should execute successfully");
                    }
                
                    @Test
                    public void testWeightedSequence() {
                        Map<String, Double> weights = new HashMap<>();
                        weights.put("/api/users:GET", 0.3);
                        weights.put("/api/users:POST", 0.2);
                        weights.put("/api/products:GET", 0.3);
                        weights.put("/api/products:POST", 0.2);
                
                        List<APICall> sequence = generator.generateWeightedSequence(10, weights);
                        List<SequenceResult> results = executeSequence(sequence);
                
                        assertTrue(validateSequenceResults(results),
                            "Weighted sequence should execute successfully");
                    }
                
                    @Test
                    public void testDependentSequence() {
                        Map<String, List<String>> dependencies = new HashMap<>();
                        dependencies.put("/api/users:POST", Arrays.asList());
                        dependencies.put("/api/users:GET", Arrays.asList());
                        dependencies.put("/api/users/{id}:GET", Arrays.asList("/api/users:POST"));
                        dependencies.put("/api/users/{id}:PUT", Arrays.asList("/api/users:POST"));
                        dependencies.put("/api/users/{id}:DELETE", Arrays.asList("/api/users:POST"));
                
                        List<APICall> sequence = generator.generateDependentSequence(10, dependencies);
                        List<SequenceResult> results = executeSequence(sequence);
                
                        assertTrue(validateSequenceResults(results),
                            "Dependent sequence should execute successfully");
                    }
                
                    @Test
                    public void testStatefulSequence() {
                        Map<String, String> stateTransitions = new HashMap<>();
                        stateTransitions.put("/api/users:POST", "user_created");
                        stateTransitions.put("/api/users:GET", "users_listed");
                        stateTransitions.put("/api/users/{id}:GET", "user_retrieved");
                        stateTransitions.put("/api/users/{id}:PUT", "user_updated");
                        stateTransitions.put("/api/users/{id}:DELETE", "user_deleted");
                
                        List<APICall> sequence = generator.generateStatefulSequence(10, stateTransitions);
                        List<SequenceResult> results = executeSequence(sequence);
                
                        assertTrue(validateSequenceResults(results),
                            "Stateful sequence should execute successfully");
                    }
                
                    @Test
                    public void testConcurrentSequences() {
                        int numSequences = 10;
                        List<CompletableFuture<List<SequenceResult>>> futures = new ArrayList<>();
                
                        for (int i = 0; i < numSequences; i++) {
                            CompletableFuture<List<SequenceResult>> future = CompletableFuture.supplyAsync(() -> {
                                List<APICall> sequence = generator.generateRandomSequence(5);
                                return executeSequence(sequence);
                            });
                            futures.add(future);
                        }
                
                        // Wait for all sequences to complete
                        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                            futures.toArray(new CompletableFuture[0])
                        );
                
                        assertDoesNotThrow(() -> allFutures.get(),
                            "Concurrent sequences should execute without errors");
                    }
                
                    @Test
                    public void testSequencePerformance() {
                        long startTime = System.currentTimeMillis();
                
                        for (int i = 0; i < 100; i++) {
                            List<APICall> sequence = generator.generateRandomSequence(5);
                            List<SequenceResult> results = executeSequence(sequence);
                            assertTrue(validateSequenceResults(results));
                        }
                
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;
                
                        assertTrue(totalTime < 30000,
                            "100 sequences should complete in less than 30 seconds");
                    }
                
                    /**
                     * Initialize API calls based on OpenAPI spec
                     */
                    private List<APICall> initializeAPICalls() {
                        List<APICall> calls = new ArrayList<>();
                
                        // Add calls based on OpenAPI spec
                        calls.add(new APICall("GET", "/api/health", null, null));
                        calls.add(new APICall("GET", "/api/users", null, null));
                        calls.add(new APICall("POST", "/api/users", "{\\"name\\": \\"John Doe\\", \\"email\\": \\"john@example.com\\"}", null));
                        calls.add(new APICall("GET", "/api/users/1", null, null));
                        calls.add(new APICall("PUT", "/api/users/1", "{\\"name\\": \\"Jane Doe\\"}", null));
                        calls.add(new APICall("DELETE", "/api/users/1", null, null));
                
                        return calls;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "SequenceTestCases.java"), testCases.getBytes());
    }

    /**
     * Generate sequence test runner
     */
    private void generateSequenceTestRunner(Map<String, Object> spec, String outputDir, String baseUrl) throws IOException {
        String runner = """
                package com.example.sequence;
                
                import org.junit.platform.launcher.Launcher;
                import org.junit.platform.launcher.LauncherDiscoveryRequest;
                import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
                import org.junit.platform.launcher.core.LauncherFactory;
                import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
                import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
                
                public class SequenceTestRunner {
                
                    public static void main(String[] args) {
                        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                            .selectors(selectClass(SequenceTestCases.class))
                            .build();
                
                        Launcher launcher = LauncherFactory.create();
                        SummaryGeneratingListener listener = new SummaryGeneratingListener();
                        launcher.registerTestExecutionListeners(listener);
                        launcher.execute(request);
                
                        listener.getSummary().printTo(new java.io.PrintWriter(System.out));
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "SequenceTestRunner.java"), runner.getBytes());
    }

    /**
     * Generate sequence test configuration
     */
    private void generateSequenceTestConfig(Map<String, Object> spec, String outputDir, String baseUrl) throws IOException {
        String config = """
                package com.example.sequence;
                
                import java.util.ArrayList;
                import java.util.List;
                
                public class SequenceTestConfig {
                
                    public static final String BASE_URL = "http://localhost:8080";
                    public static final int MAX_SEQUENCES = 100;
                    public static final int MAX_SEQUENCE_LENGTH = 10;
                
                    public static RandomSequenceGenerator createRandomSequenceGenerator() {
                        // Initialize with available API calls
                        List<SequenceTestFramework.APICall> availableCalls = initializeAPICalls();
                        return new RandomSequenceGenerator(availableCalls);
                    }
                
                    private static List<SequenceTestFramework.APICall> initializeAPICalls() {
                        List<SequenceTestFramework.APICall> calls = new ArrayList<>();
                
                        // Add calls based on OpenAPI spec
                        calls.add(new SequenceTestFramework.APICall("GET", "/api/health", null, null));
                        calls.add(new SequenceTestFramework.APICall("GET", "/api/users", null, null));
                        calls.add(new SequenceTestFramework.APICall("POST", "/api/users", "{\\"name\\": \\"John Doe\\", \\"email\\": \\"john@example.com\\"}", null));
                        calls.add(new SequenceTestFramework.APICall("GET", "/api/users/1", null, null));
                        calls.add(new SequenceTestFramework.APICall("PUT", "/api/users/1", "{\\"name\\": \\"Jane Doe\\"}", null));
                        calls.add(new SequenceTestFramework.APICall("DELETE", "/api/users/1", null, null));
                
                        return calls;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "SequenceTestConfig.java"), config.getBytes());
    }
}
