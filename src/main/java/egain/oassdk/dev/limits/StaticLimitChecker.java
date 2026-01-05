package egain.oassdk.dev.limits;

import egain.oassdk.core.exceptions.GenerationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Generates static limit checkers based on OpenAPI specification
 */
public class StaticLimitChecker {

    /**
     * Generate static limit checkers
     *
     * @param spec      OpenAPI specification
     * @param outputDir Output directory
     * @throws GenerationException if generation fails
     */
    public void generateStaticLimitCheckers(Map<String, Object> spec, String outputDir) throws GenerationException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }
        try {
            Files.createDirectories(Paths.get(outputDir));

            // Generate request size limit checker
            generateRequestSizeLimitChecker(spec, outputDir);

            // Generate response size limit checker
            generateResponseSizeLimitChecker(spec, outputDir);

            // Generate field length limit checker
            generateFieldLengthLimitChecker(spec, outputDir);

            // Generate array size limit checker
            generateArraySizeLimitChecker(spec, outputDir);

            // Generate numeric range limit checker
            generateNumericRangeLimitChecker(spec, outputDir);

            // Generate configuration
            generateStaticLimitConfig(spec, outputDir);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate static limit checkers: " + e.getMessage(), e);
        }
    }

    /**
     * Generate request size limit checker
     */
    private void generateRequestSizeLimitChecker(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import jakarta.ws.rs.container.ContainerRequestContext;
                import jakarta.ws.rs.container.ContainerRequestFilter;
                import jakarta.ws.rs.core.Response;
                import jakarta.ws.rs.ext.Provider;
                import java.io.IOException;
                
                @Provider
                @Singleton
                public class RequestSizeLimitChecker implements ContainerRequestFilter {
                
                    private static final long MAX_REQUEST_SIZE = 1024 * 1024; // 1MB
                    private static final long MAX_HEADER_SIZE = 8 * 1024; // 8KB
                
                    @Override
                    public void filter(ContainerRequestContext requestContext) throws IOException {
                        // Check request size
                        int contentLength = requestContext.getLength();
                        if (contentLength > MAX_REQUEST_SIZE) {
                            requestContext.abortWith(
                                Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
                                    .entity("Request size exceeds maximum allowed size")
                                    .build()
                            );
                            return;
                        }
                
                        // Check header size
                        long headerSize = getHeaderSize(requestContext);
                        if (headerSize > MAX_HEADER_SIZE) {
                            requestContext.abortWith(
                                Response.status(431) // Request Header Fields Too Large
                                    .entity("Request headers exceed maximum allowed size")
                                    .build()
                            );
                        }
                    }
                
                    private long getHeaderSize(ContainerRequestContext requestContext) {
                        long size = 0;
                        for (String headerName : requestContext.getHeaders().keySet()) {
                            size += headerName.length();
                            String headerValue = requestContext.getHeaderString(headerName);
                            if (headerValue != null) {
                                size += headerValue.length();
                            }
                        }
                        return size;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RequestSizeLimitChecker.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate response size limit checker
     */
    private void generateResponseSizeLimitChecker(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import jakarta.ws.rs.container.ContainerResponseContext;
                import jakarta.ws.rs.container.ContainerResponseFilter;
                import jakarta.ws.rs.container.ContainerRequestContext;
                import jakarta.ws.rs.ext.Provider;
                import java.io.IOException;
                
                @Provider
                @Singleton
                public class ResponseSizeLimitChecker implements ContainerResponseFilter {
                
                    private static final long MAX_RESPONSE_SIZE = 10 * 1024 * 1024; // 10MB
                
                    @Override
                    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
                        // Check response size
                        if (responseContext.hasEntity()) {
                            int contentLength = responseContext.getLength();
                            if (contentLength > MAX_RESPONSE_SIZE) {
                                // Log warning or take appropriate action
                                // In a real implementation, this would use a proper logger
                                throw new RuntimeException("Response size exceeds maximum allowed size: " + contentLength);
                            }
                        }
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "ResponseSizeLimitChecker.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate field length limit checker
     */
    private void generateFieldLengthLimitChecker(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import com.fasterxml.jackson.databind.JsonNode;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import java.util.Map;
                import java.util.HashMap;
                
                @Singleton
                public class FieldLengthLimitChecker {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                    private final Map<String, Integer> fieldLengthLimits = new HashMap<>();
                
                    public FieldLengthLimitChecker() {
                        initializeFieldLimits();
                    }
                
                    private void initializeFieldLimits() {
                        // Initialize field length limits based on OpenAPI spec
                        // These would be generated from the OpenAPI specification
                        fieldLengthLimits.put("name", 100);
                        fieldLengthLimits.put("description", 500);
                        fieldLengthLimits.put("email", 255);
                        fieldLengthLimits.put("phone", 20);
                        fieldLengthLimits.put("address", 1000);
                    }
                
                    public boolean validateFieldLengths(String jsonString) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(jsonString);
                            return validateFieldLengths(jsonNode, "");
                        } catch (Exception e) {
                            return false;
                        }
                    }
                
                    private boolean validateFieldLengths(JsonNode jsonNode, String path) {
                        if (jsonNode.isObject()) {
                            jsonNode.fields().forEachRemaining(entry -> {
                                String fieldName = entry.getKey();
                                JsonNode fieldValue = entry.getValue();
                                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                
                                if (fieldValue.isTextual()) {
                                    String textValue = fieldValue.asText();
                                    Integer maxLength = fieldLengthLimits.get(fieldName);
                                    if (maxLength != null && textValue.length() > maxLength) {
                                        throw new RuntimeException("Field '" + currentPath + "' exceeds maximum length of " + maxLength);
                                    }
                                } else if (fieldValue.isObject()) {
                                    validateFieldLengths(fieldValue, currentPath);
                                } else if (fieldValue.isArray()) {
                                    for (int i = 0; i < fieldValue.size(); i++) {
                                        validateFieldLengths(fieldValue.get(i), currentPath + "[" + i + "]");
                                    }
                                }
                            });
                        }
                        return true;
                    }
                
                    public void addFieldLimit(String fieldName, int maxLength) {
                        fieldLengthLimits.put(fieldName, maxLength);
                    }
                
                    public void removeFieldLimit(String fieldName) {
                        fieldLengthLimits.remove(fieldName);
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "FieldLengthLimitChecker.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate array size limit checker
     */
    private void generateArraySizeLimitChecker(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import com.fasterxml.jackson.databind.JsonNode;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import java.util.Map;
                import java.util.HashMap;
                
                @Singleton
                public class ArraySizeLimitChecker {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                    private final Map<String, Integer> arraySizeLimits = new HashMap<>();
                
                    public ArraySizeLimitChecker() {
                        initializeArrayLimits();
                    }
                
                    private void initializeArrayLimits() {
                        // Initialize array size limits based on OpenAPI spec
                        // These would be generated from the OpenAPI specification
                        arraySizeLimits.put("items", 1000);
                        arraySizeLimits.put("tags", 50);
                        arraySizeLimits.put("categories", 20);
                        arraySizeLimits.put("users", 10000);
                    }
                
                    public boolean validateArraySizes(String jsonString) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(jsonString);
                            return validateArraySizes(jsonNode, "");
                        } catch (Exception e) {
                            return false;
                        }
                    }
                
                    private boolean validateArraySizes(JsonNode jsonNode, String path) {
                        if (jsonNode.isArray()) {
                            String arrayName = path.isEmpty() ? "array" : path;
                            Integer maxSize = arraySizeLimits.get(arrayName);
                            if (maxSize != null && jsonNode.size() > maxSize) {
                                throw new RuntimeException("Array '" + arrayName + "' exceeds maximum size of " + maxSize);
                            }
                
                            // Validate each element in the array
                            for (int i = 0; i < jsonNode.size(); i++) {
                                validateArraySizes(jsonNode.get(i), path + "[" + i + "]");
                            }
                        } else if (jsonNode.isObject()) {
                            jsonNode.fields().forEachRemaining(entry -> {
                                String fieldName = entry.getKey();
                                JsonNode fieldValue = entry.getValue();
                                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                                validateArraySizes(fieldValue, currentPath);
                            });
                        }
                        return true;
                    }
                
                    public void addArrayLimit(String arrayName, int maxSize) {
                        arraySizeLimits.put(arrayName, maxSize);
                    }
                
                    public void removeArrayLimit(String arrayName) {
                        arraySizeLimits.remove(arrayName);
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "ArraySizeLimitChecker.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate numeric range limit checker
     */
    private void generateNumericRangeLimitChecker(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                import com.fasterxml.jackson.databind.JsonNode;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import java.util.Map;
                import java.util.HashMap;
                import java.math.BigDecimal;
                
                @Singleton
                public class NumericRangeLimitChecker {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                    private final Map<String, NumericRange> numericRanges = new HashMap<>();
                
                    public static class NumericRange {
                        private final BigDecimal minimum;
                        private final BigDecimal maximum;
                
                        public NumericRange(BigDecimal minimum, BigDecimal maximum) {
                            this.minimum = minimum;
                            this.maximum = maximum;
                        }
                
                        public boolean isInRange(BigDecimal value) {
                            return (minimum == null || value.compareTo(minimum) >= 0) &&
                                   (maximum == null || value.compareTo(maximum) <= 0);
                        }
                    }
                
                    public NumericRangeLimitChecker() {
                        initializeNumericRanges();
                    }
                
                    private void initializeNumericRanges() {
                        // Initialize numeric ranges based on OpenAPI spec
                        // These would be generated from the OpenAPI specification
                        numericRanges.put("age", new NumericRange(BigDecimal.ZERO, new BigDecimal("150")));
                        numericRanges.put("price", new NumericRange(BigDecimal.ZERO, new BigDecimal("1000000")));
                        numericRanges.put("quantity", new NumericRange(BigDecimal.ZERO, new BigDecimal("10000")));
                        numericRanges.put("rating", new NumericRange(BigDecimal.ZERO, new BigDecimal("5")));
                    }
                
                    public boolean validateNumericRanges(String jsonString) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(jsonString);
                            return validateNumericRanges(jsonNode, "");
                        } catch (Exception e) {
                            return false;
                        }
                    }
                
                    private boolean validateNumericRanges(JsonNode jsonNode, String path) {
                        if (jsonNode.isNumber()) {
                            String fieldName = path.isEmpty() ? "number" : path;
                            NumericRange range = numericRanges.get(fieldName);
                            if (range != null) {
                                BigDecimal value = jsonNode.decimalValue();
                                if (!range.isInRange(value)) {
                                    throw new RuntimeException("Numeric field '" + fieldName + "' is out of range: " + value);
                                }
                            }
                        } else if (jsonNode.isObject()) {
                            jsonNode.fields().forEachRemaining(entry -> {
                                String fieldName = entry.getKey();
                                JsonNode fieldValue = entry.getValue();
                                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                                validateNumericRanges(fieldValue, currentPath);
                            });
                        } else if (jsonNode.isArray()) {
                            for (int i = 0; i < jsonNode.size(); i++) {
                                validateNumericRanges(jsonNode.get(i), path + "[" + i + "]");
                            }
                        }
                        return true;
                    }
                
                    public void addNumericRange(String fieldName, BigDecimal minimum, BigDecimal maximum) {
                        numericRanges.put(fieldName, new NumericRange(minimum, maximum));
                    }
                
                    public void removeNumericRange(String fieldName) {
                        numericRanges.remove(fieldName);
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "NumericRangeLimitChecker.java"), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate static limit configuration
     */
    private void generateStaticLimitConfig(Map<String, Object> spec, String outputDir) throws IOException {
        String content = """
                package com.example.limits;
                
                import jakarta.inject.Singleton;
                
                @Singleton
                public class StaticLimitConfig {
                
                    private final RequestSizeLimitChecker requestSizeLimitChecker;
                    private final ResponseSizeLimitChecker responseSizeLimitChecker;
                    private final FieldLengthLimitChecker fieldLengthLimitChecker;
                    private final ArraySizeLimitChecker arraySizeLimitChecker;
                    private final NumericRangeLimitChecker numericRangeLimitChecker;
                
                    public StaticLimitConfig() {
                        this.requestSizeLimitChecker = new RequestSizeLimitChecker();
                        this.responseSizeLimitChecker = new ResponseSizeLimitChecker();
                        this.fieldLengthLimitChecker = new FieldLengthLimitChecker();
                        this.arraySizeLimitChecker = new ArraySizeLimitChecker();
                        this.numericRangeLimitChecker = new NumericRangeLimitChecker();
                    }
                
                    public RequestSizeLimitChecker getRequestSizeLimitChecker() {
                        return requestSizeLimitChecker;
                    }
                
                    public ResponseSizeLimitChecker getResponseSizeLimitChecker() {
                        return responseSizeLimitChecker;
                    }
                
                    public FieldLengthLimitChecker getFieldLengthLimitChecker() {
                        return fieldLengthLimitChecker;
                    }
                
                    public ArraySizeLimitChecker getArraySizeLimitChecker() {
                        return arraySizeLimitChecker;
                    }
                
                    public NumericRangeLimitChecker getNumericRangeLimitChecker() {
                        return numericRangeLimitChecker;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "StaticLimitConfig.java"), content.getBytes(StandardCharsets.UTF_8));
    }
}
