package egain.oassdk.dev.validators;

import egain.oassdk.core.exceptions.GenerationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Generates API validators based on OpenAPI specification
 */
public class APIValidator {

    /**
     * Generate API validators
     *
     * @param spec      OpenAPI specification
     * @param outputDir Output directory
     * @throws GenerationException if generation fails
     */
    public void generateValidators(Map<String, Object> spec, String outputDir) throws GenerationException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }
        try {
            Files.createDirectories(Paths.get(outputDir));

            // Generate request validators
            generateRequestValidators(spec, outputDir);

            // Generate response validators
            generateResponseValidators(spec, outputDir);

            // Generate schema validators
            generateSchemaValidators(spec, outputDir);

            // Generate validation configuration
            generateValidationConfig(spec, outputDir);

        } catch (Exception e) {
            throw new GenerationException("Failed to generate API validators: " + e.getMessage(), e);
        }
    }

    /**
     * Generate request validators
     */
    private void generateRequestValidators(Map<String, Object> spec, String outputDir) throws IOException {
        String requestValidatorContent = """
                package com.example.validators;
                
                import jakarta.inject.Singleton;
                import jakarta.validation.ConstraintViolation;
                import jakarta.validation.Validation;
                import jakarta.validation.ValidatorFactory;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.databind.JsonNode;
                import java.util.regex.Pattern;
                
                @Singleton
                public class RequestValidator {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                    private final jakarta.validation.Validator validator;
                
                    public RequestValidator() {
                        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                        this.validator = factory.getValidator();
                    }
                
                    public boolean validate(Object target) {
                        Set<ConstraintViolation<Object>> violations = validator.validate(target);
                        // Validate request body structure
                        validateRequestBody(target, errors);
                
                        // Validate required fields
                        validateRequiredFields(target, errors);
                
                        // Validate field formats
                        validateFieldFormats(target, errors);
                
                        // Validate field constraints
                        validateFieldConstraints(target, errors);
                    }
                
                    private void validateRequestBody(Object target, Errors errors) {
                        try {
                            String json = objectMapper.writeValueAsString(target);
                            JsonNode jsonNode = objectMapper.readTree(json);
                
                            // Validate against OpenAPI schema
                            validateAgainstSchema(jsonNode, errors);
                
                        } catch (Exception e) {
                            errors.reject("invalid.request.body", "Invalid request body format");
                        }
                    }
                
                    private void validateRequiredFields(Object target, Errors errors) {
                        // Implementation for required field validation
                        // This would be generated based on OpenAPI spec
                    }
                
                    private void validateFieldFormats(Object target, Errors errors) {
                        // Implementation for field format validation
                        // This would be generated based on OpenAPI spec
                    }
                
                    private void validateFieldConstraints(Object target, Errors errors) {
                        // Implementation for field constraint validation
                        // This would be generated based on OpenAPI spec
                    }
                
                    private void validateAgainstSchema(JsonNode jsonNode, Errors errors) {
                        // Implementation for schema validation
                        // This would be generated based on OpenAPI spec
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "RequestValidator.java"), requestValidatorContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate response validators
     */
    private void generateResponseValidators(Map<String, Object> spec, String outputDir) throws IOException {
        String responseValidatorContent = """
                package com.example.validators;
                
                import jakarta.inject.Singleton;
                import jakarta.ws.rs.core.Response;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.databind.JsonNode;
                
                @Singleton
                public class ResponseValidator {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                
                    /**
                     * Validate API response against OpenAPI specification
                     */
                    public boolean validateResponse(Response response, String endpoint, String method) {
                        try {
                            // Validate status code
                            if (!validateStatusCode(response, endpoint, method)) {
                                return false;
                            }
                
                            // Validate response body
                            if (!validateResponseBody(response, endpoint, method)) {
                                return false;
                            }
                
                            // Validate response headers
                            if (!validateResponseHeaders(response, endpoint, method)) {
                                return false;
                            }
                
                            return true;
                
                        } catch (Exception e) {
                            return false;
                        }
                    }
                
                    private boolean validateStatusCode(Response response, String endpoint, String method) {
                        // Implementation for status code validation
                        // This would be generated based on OpenAPI spec
                        return true;
                    }
                
                    private boolean validateResponseBody(Response response, String endpoint, String method) {
                        // Implementation for response body validation
                        // This would be generated based on OpenAPI spec
                        return true;
                    }
                
                    private boolean validateResponseHeaders(Response response, String endpoint, String method) {
                        // Implementation for response header validation
                        // This would be generated based on OpenAPI spec
                        return true;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "ResponseValidator.java"), responseValidatorContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate schema validators
     */
    private void generateSchemaValidators(Map<String, Object> spec, String outputDir) throws IOException {
        String schemaValidatorContent = """
                package com.example.validators;
                
                import jakarta.inject.Singleton;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.databind.JsonNode;
                import java.util.regex.Pattern;
                import java.math.BigDecimal;
                
                @Singleton
                public class SchemaValidator {
                
                    private final ObjectMapper objectMapper = new ObjectMapper();
                
                    /**
                     * Validate JSON against OpenAPI schema
                     */
                    public boolean validateSchema(JsonNode jsonNode, Map<String, Object> schema) {
                        try {
                            return validateSchemaNode(jsonNode, schema);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                
                    private boolean validateSchemaNode(JsonNode jsonNode, Map<String, Object> schema) {
                        String type = (String) schema.get("type");
                
                        switch (type) {
                            case "string":
                                return validateString(jsonNode, schema);
                            case "number":
                                return validateNumber(jsonNode, schema);
                            case "integer":
                                return validateInteger(jsonNode, schema);
                            case "boolean":
                                return validateBoolean(jsonNode, schema);
                            case "array":
                                return validateArray(jsonNode, schema);
                            case "object":
                                return validateObject(jsonNode, schema);
                            default:
                                return true;
                        }
                    }
                
                    private boolean validateString(JsonNode jsonNode, Map<String, Object> schema) {
                        if (!jsonNode.isTextual()) {
                            return false;
                        }
                
                        String value = jsonNode.asText();
                
                        // Validate min/max length
                        if (schema.containsKey("minLength")) {
                            int minLength = (Integer) schema.get("minLength");
                            if (value.length() < minLength) {
                                return false;
                            }
                        }
                
                        if (schema.containsKey("maxLength")) {
                            int maxLength = (Integer) schema.get("maxLength");
                            if (value.length() > maxLength) {
                                return false;
                            }
                        }
                
                        // Validate pattern
                        if (schema.containsKey("pattern")) {
                            String pattern = (String) schema.get("pattern");
                            if (!Pattern.matches(pattern, value)) {
                                return false;
                            }
                        }
                
                        // Validate enum
                        if (schema.containsKey("enum")) {
                            List<String> enumValues = Util.asStringList(schema.get("enum"));
                            if (!enumValues.contains(value)) {
                                return false;
                            }
                        }
                
                        return true;
                    }
                
                    private boolean validateNumber(JsonNode jsonNode, Map<String, Object> schema) {
                        if (!jsonNode.isNumber()) {
                            return false;
                        }
                
                        BigDecimal value = jsonNode.decimalValue();
                
                        // Validate minimum
                        if (schema.containsKey("minimum")) {
                            BigDecimal minimum = new BigDecimal(schema.get("minimum").toString());
                            if (value.compareTo(minimum) < 0) {
                                return false;
                            }
                        }
                
                        // Validate maximum
                        if (schema.containsKey("maximum")) {
                            BigDecimal maximum = new BigDecimal(schema.get("maximum").toString());
                            if (value.compareTo(maximum) > 0) {
                                return false;
                            }
                        }
                
                        return true;
                    }
                
                    private boolean validateInteger(JsonNode jsonNode, Map<String, Object> schema) {
                        if (!jsonNode.isInt()) {
                            return false;
                        }
                
                        int value = jsonNode.asInt();
                
                        // Validate minimum
                        if (schema.containsKey("minimum")) {
                            int minimum = (Integer) schema.get("minimum");
                            if (value < minimum) {
                                return false;
                            }
                        }
                
                        // Validate maximum
                        if (schema.containsKey("maximum")) {
                            int maximum = (Integer) schema.get("maximum");
                            if (value > maximum) {
                                return false;
                            }
                        }
                
                        return true;
                    }
                
                    private boolean validateBoolean(JsonNode jsonNode, Map<String, Object> schema) {
                        return jsonNode.isBoolean();
                    }
                
                    private boolean validateArray(JsonNode jsonNode, Map<String, Object> schema) {
                        if (!jsonNode.isArray()) {
                            return false;
                        }
                
                        // Validate min/max items
                        if (schema.containsKey("minItems")) {
                            int minItems = (Integer) schema.get("minItems");
                            if (jsonNode.size() < minItems) {
                                return false;
                            }
                        }
                
                        if (schema.containsKey("maxItems")) {
                            int maxItems = (Integer) schema.get("maxItems");
                            if (jsonNode.size() > maxItems) {
                                return false;
                            }
                        }
                
                        // Validate items
                        if (schema.containsKey("items")) {
                            Map<String, Object> itemsSchema = Util.asStringObjectMap(schema.get("items"));
                            for (JsonNode item : jsonNode) {
                                if (!validateSchemaNode(item, itemsSchema)) {
                                    return false;
                                }
                            }
                        }
                
                        return true;
                    }
                
                    private boolean validateObject(JsonNode jsonNode, Map<String, Object> schema) {
                        if (!jsonNode.isObject()) {
                            return false;
                        }
                
                        // Validate required properties
                        if (schema.containsKey("required")) {
                            List<String> required = Util.asStringList(schema.get("required"));
                            for (String requiredField : required) {
                                if (!jsonNode.has(requiredField)) {
                                    return false;
                                }
                            }
                        }
                
                        // Validate properties
                        if (schema.containsKey("properties")) {
                            Map<String, Object> properties = Util.asStringObjectMap(schema.get("properties"));
                            for (Map.Entry<String, Object> property : properties.entrySet()) {
                                String propertyName = property.getKey();
                                Map<String, Object> propertySchema = Util.asStringObjectMap(property.getValue());
                
                                if (jsonNode.has(propertyName)) {
                                    if (!validateSchemaNode(jsonNode.get(propertyName), propertySchema)) {
                                        return false;
                                    }
                                }
                            }
                        }
                
                        return true;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "SchemaValidator.java"), schemaValidatorContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate validation configuration
     */
    private void generateValidationConfig(Map<String, Object> spec, String outputDir) throws IOException {
        String configContent = """
                package com.example.validators;
                
                import jakarta.inject.Singleton;
                
                @Singleton
                public class ValidationConfig {
                
                    private final RequestValidator requestValidator;
                    private final ResponseValidator responseValidator;
                    private final SchemaValidator schemaValidator;
                
                    public ValidationConfig() {
                        this.requestValidator = new RequestValidator();
                        this.responseValidator = new ResponseValidator();
                        this.schemaValidator = new SchemaValidator();
                    }
                
                    public RequestValidator getRequestValidator() {
                        return requestValidator;
                    }
                
                    public ResponseValidator getResponseValidator() {
                        return responseValidator;
                    }
                
                    public SchemaValidator getSchemaValidator() {
                        return schemaValidator;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "ValidationConfig.java"), configContent.getBytes(StandardCharsets.UTF_8));
    }
}
