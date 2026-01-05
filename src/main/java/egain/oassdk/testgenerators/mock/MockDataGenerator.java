package egain.oassdk.testgenerators.mock;

import com.github.javafaker.Faker;
import egain.oassdk.Util;
import egain.oassdk.config.TestConfig;
import egain.oassdk.core.exceptions.GenerationException;
import egain.oassdk.testgenerators.ConfigurableTestGenerator;
import egain.oassdk.testgenerators.TestGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock data generator
 * Generates realistic test data based on OpenAPI schema definitions
 */
public class MockDataGenerator implements TestGenerator, ConfigurableTestGenerator {

    private TestConfig config;
    private final Random random = new Random();
    private final Faker faker = new Faker();

    @Override
    public void generate(Map<String, Object> spec, String outputDir, TestConfig config, String testFramework) throws GenerationException {
        this.config = config;

        try {
            // Create output directory structure
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            // Extract API information
            String apiTitle = getAPITitle(spec);

            // Generate mock data for all schemas
            generateMockDataForSchemas(spec, outputPath.toString(), apiTitle);

            // Generate mock data generator utility class
            generateMockDataGeneratorClass(outputPath.toString());

        } catch (Exception e) {
            throw new GenerationException("Failed to generate mock data: " + e.getMessage(), e);
        }
    }

    /**
     * Generate mock data for all schemas
     */
    private void generateMockDataForSchemas(Map<String, Object> spec, String outputDir, String apiTitle) throws IOException {
        Map<String, Object> components = Util.asStringObjectMap(spec.get("components"));
        if (components == null) {
            return;
        }

        Map<String, Object> schemas = Util.asStringObjectMap(components.get("schemas"));
        if (schemas == null || schemas.isEmpty()) {
            return;
        }

        // Generate mock data for each schema
        for (Map.Entry<String, Object> schemaEntry : schemas.entrySet()) {
            String schemaName = schemaEntry.getKey();
            Map<String, Object> schema = Util.asStringObjectMap(schemaEntry.getValue());

            if (schema == null) continue;

            // Generate multiple mock data instances
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> mockData = generateMockDataFromSchema(schema, schemaName);
                String jsonContent = convertToJson(mockData, 0);

                String fileName = schemaName + "_" + i + ".json";
                Files.write(Paths.get(outputDir, fileName), jsonContent.getBytes());
            }
        }

        // Generate a sample data file with all schemas
        generateSampleDataFile(schemas, outputDir);
    }

    /**
     * Generate mock data from a schema
     */

    private Map<String, Object> generateMockDataFromSchema(Map<String, Object> schema, String schemaName) {
        Map<String, Object> mockData = new LinkedHashMap<>();

        String type = (String) schema.get("type");
        if (type == null) {
            // Check for allOf, oneOf, anyOf
            if (schema.containsKey("allOf")) {
                List<Map<String, Object>> allOf = Util.asStringObjectMapList(schema.get("allOf"));
                for (Map<String, Object> subSchema : allOf) {
                    if (subSchema.containsKey("$ref")) {
                        // Skip refs for now
                        continue;
                    }
                    Map<String, Object> subData = generateMockDataFromSchema(subSchema, schemaName);
                    mockData.putAll(subData);
                }
                return mockData;
            }
            return mockData;
        }

        switch (type) {
            case "object":
                Map<String, Object> properties = Util.asStringObjectMap(schema.get("properties"));
                List<String> required = Util.asStringList(schema.get("required"));

                if (properties != null) {
                    for (Map.Entry<String, Object> propEntry : properties.entrySet()) {
                        String propName = propEntry.getKey();
                        Map<String, Object> propSchema = Util.asStringObjectMap(propEntry.getValue());

                        // Check if required
                        boolean isRequired = required != null && required.contains(propName);

                        // Generate value (always generate required, sometimes generate optional)
                        if (isRequired || random.nextBoolean()) {
                            Object value = generateValueFromSchema(propSchema, propName);
                            if (value != null) {
                                mockData.put(propName, value);
                            }
                        }
                    }
                }
                break;

            case "array":
                Map<String, Object> items = Util.asStringObjectMap(schema.get("items"));
                List<Object> arrayData = new ArrayList<>();
                int arraySize = random.nextInt(3) + 1; // 1-3 items
                for (int i = 0; i < arraySize; i++) {
                    Object item = generateValueFromSchema(items, "item");
                    if (item != null) {
                        arrayData.add(item);
                    }
                }
                return new LinkedHashMap<>(Collections.singletonMap("items", arrayData));
        }

        return mockData;
    }

    /**
     * Generate a value from a schema
     */

    private Object generateValueFromSchema(Map<String, Object> schema, String fieldName) {
        if (schema == null) {
            return null;
        }

        // Check for example first
        if (schema.containsKey("example")) {
            return schema.get("example");
        }

        // Check for enum
        if (schema.containsKey("enum")) {
            List<Object> enumValues = Util.asObjectList(schema.get("enum"));
            if (enumValues != null && !enumValues.isEmpty()) {
                return enumValues.get(random.nextInt(enumValues.size()));
            }
        }

        String type = (String) schema.get("type");
        if (type == null) {
            return "unknown";
        }

        switch (type) {
            case "string":
                return generateStringValue(schema, fieldName);
            case "integer":
                return generateIntegerValue(schema);
            case "number":
                return generateNumberValue(schema);
            case "boolean":
                return random.nextBoolean();
            case "array":
                return generateArrayValue(schema);
            case "object":
                return generateMockDataFromSchema(schema, fieldName);
            default:
                return "mock_" + fieldName;
        }
    }

    /**
     * Generate string value using Faker library
     */
    private String generateStringValue(Map<String, Object> schema, String fieldName) {
        // Check for pattern
        String pattern = (String) schema.get("pattern");
        if (pattern != null) {
            // Simple pattern matching - generate based on common patterns
            if (pattern.contains("^[a-zA-Z0-9]")) {
                return faker.regexify("[A-Z0-9]{4,10}");
            }
        }

        // Check for format
        String format = (String) schema.get("format");
        if (format != null) {
            switch (format) {
                case "date-time":
                    return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                case "date":
                    return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
                case "email":
                    return faker.internet().emailAddress();
                case "uri":
                    return faker.internet().url();
                case "uuid":
                    return UUID.randomUUID().toString();
            }
        }

        // Generate based on field name using Faker
        String name = fieldName.toLowerCase();
        if (name.contains("id") || name.contains("identifier")) {
            return String.valueOf(faker.number().numberBetween(1000, 99999));
        } else if (name.contains("name") || name.contains("title")) {
            if (name.contains("first")) {
                return faker.name().firstName();
            } else if (name.contains("last")) {
                return faker.name().lastName();
            } else if (name.contains("full")) {
                return faker.name().fullName();
            } else {
                String word = faker.lorem().word();
                return word.substring(0, Math.min(20, word.length()));
            }
        } else if (name.contains("description")) {
            return faker.lorem().sentence();
        } else if (name.contains("email")) {
            return faker.internet().emailAddress();
        } else if (name.contains("url") || name.contains("uri")) {
            return faker.internet().url();
        } else if (name.contains("phone")) {
            return faker.phoneNumber().phoneNumber();
        } else if (name.contains("address") || name.contains("street")) {
            return faker.address().streetAddress();
        } else if (name.contains("city")) {
            return faker.address().city();
        } else if (name.contains("state") || name.contains("province")) {
            return faker.address().state();
        } else if (name.contains("zip") || name.contains("postal")) {
            return faker.address().zipCode();
        } else if (name.contains("country")) {
            return faker.address().country();
        } else if (name.contains("company")) {
            return faker.company().name();
        } else if (name.contains("job") || name.contains("position")) {
            return faker.job().title();
        } else if (name.contains("color")) {
            return faker.color().name();
        } else if (name.contains("animal")) {
            return faker.animal().name();
        } else if (name.contains("lorem") || name.contains("text")) {
            return faker.lorem().sentence();
        }

        // Default: use Faker to generate a random word
        return faker.lorem().word();
    }

    /**
     * Generate integer value
     */
    private Integer generateIntegerValue(Map<String, Object> schema) {
        // Handle both Integer and Long types (YAML/JSON parsers may return Long)
        Object minObj = schema.get("minimum");
        Object maxObj = schema.get("maximum");

        int min = 1;
        int max = 1000;

        if (minObj != null) {
            if (minObj instanceof Integer) {
                min = (Integer) minObj;
            } else if (minObj instanceof Long) {
                min = ((Long) minObj).intValue();
            } else if (minObj instanceof Number) {
                min = ((Number) minObj).intValue();
            }
        }

        if (maxObj != null) {
            if (maxObj instanceof Integer) {
                max = (Integer) maxObj;
            } else if (maxObj instanceof Long) {
                max = ((Long) maxObj).intValue();
            } else if (maxObj instanceof Number) {
                max = ((Number) maxObj).intValue();
            }
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generate number value
     */
    private Double generateNumberValue(Map<String, Object> schema) {
        Number minimum = (Number) schema.get("minimum");
        Number maximum = (Number) schema.get("maximum");

        double min = minimum != null ? minimum.doubleValue() : 0.0;
        double max = maximum != null ? maximum.doubleValue() : 1000.0;

        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generate array value
     */
    private List<Object> generateArrayValue(Map<String, Object> schema) {
        Map<String, Object> items = Util.asStringObjectMap(schema.get("items"));
        List<Object> array = new ArrayList<>();
        int size = random.nextInt(3) + 1; // 1-3 items
        for (int i = 0; i < size; i++) {
            Object item = generateValueFromSchema(items, "item");
            if (item != null) {
                array.add(item);
            }
        }
        return array;
    }

    /**
     * Generate sample data file
     */

    private void generateSampleDataFile(Map<String, Object> schemas, String outputDir) throws IOException {
        Map<String, Object> sampleData = new LinkedHashMap<>();

        for (Map.Entry<String, Object> schemaEntry : schemas.entrySet()) {
            String schemaName = schemaEntry.getKey();
            Map<String, Object> schema = Util.asStringObjectMap(schemaEntry.getValue());

            if (schema != null) {
                Map<String, Object> mockData = generateMockDataFromSchema(schema, schemaName);
                sampleData.put(schemaName, mockData);
            }
        }

        String jsonContent = convertToJson(sampleData, 0);
        Files.write(Paths.get(outputDir, "sample-data.json"), jsonContent.getBytes());
    }

    /**
     * Generate mock data generator utility class
     */
    private void generateMockDataGeneratorClass(String outputDir) throws IOException {
        String classContent = "package com.example.api;\n\n" +
                "import java.util.*;\n" +
                "import java.util.concurrent.ThreadLocalRandom;\n" +
                "import java.time.LocalDateTime;\n" +
                "import java.time.format.DateTimeFormatter;\n" +
                "import java.util.UUID;\n\n" +
                "/**\n" +
                " * Mock Data Generator Utility\n" +
                " * Generated from OpenAPI specification\n" +
                " * \n" +
                " * This utility class provides methods to generate mock data\n" +
                " * for testing purposes based on OpenAPI schema definitions.\n" +
                " */\n" +
                "public class MockDataGenerator {\n\n" +
                "    private static final Random random = new Random();\n\n" +
                "    /**\n" +
                "     * Generate mock data for a schema\n" +
                "     */\n" +
                "    public static Map<String, Object> generateMockData(String schemaName) {\n" +
                "        // TODO: Implement schema-based mock data generation\n" +
                "        return new HashMap<>();\n" +
                "    }\n\n" +
                "    /**\n" +
                "     * Generate random string value\n" +
                "     */\n" +
                "    public static String generateString(String fieldName, int maxLength) {\n" +
                "        return \"mock_\" + fieldName + \"_\" + random.nextInt(1000);\n" +
                "    }\n\n" +
                "    /**\n" +
                "     * Generate random integer value\n" +
                "     */\n" +
                "    public static Integer generateInteger(int min, int max) {\n" +
                "        return ThreadLocalRandom.current().nextInt(min, max + 1);\n" +
                "    }\n\n" +
                "    /**\n" +
                "     * Generate random boolean value\n" +
                "     */\n" +
                "    public static Boolean generateBoolean() {\n" +
                "        return random.nextBoolean();\n" +
                "    }\n\n" +
                "    /**\n" +
                "     * Generate random date-time string\n" +
                "     */\n" +
                "    public static String generateDateTime() {\n" +
                "        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);\n" +
                "    }\n\n" +
                "    /**\n" +
                "     * Generate random UUID\n" +
                "     */\n" +
                "    public static String generateUUID() {\n" +
                "        return UUID.randomUUID().toString();\n" +
                "    }\n\n" +
                "}\n";

        String packageDir = outputDir + "/com/example/api";
        Files.createDirectories(Paths.get(packageDir));
        Files.write(Paths.get(packageDir, "MockDataGenerator.java"), classContent.getBytes());
    }

    /**
     * Convert map to JSON string
     */
    private String convertToJson(Map<String, Object> data, int indent) {
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);

        if (data.isEmpty()) {
            return "{}";
        }

        sb.append("{\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) sb.append(",\n");
            sb.append(indentStr).append("  \"").append(entry.getKey()).append("\": ");
            sb.append(valueToJson(entry.getValue(), indent + 1));
            first = false;
        }
        sb.append("\n").append(indentStr).append("}");

        return sb.toString();
    }

    /**
     * Convert value to JSON
     */
    private String valueToJson(Object value, int indent) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Map) {

            Map<String, Object> mapValue = Util.asStringObjectMap(value);
            return convertToJson(mapValue, indent);
        } else if (value instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            String indentStr = "  ".repeat(indent);
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",\n");
                sb.append(indentStr).append("  ").append(valueToJson(list.get(i), indent + 1));
            }
            sb.append("\n").append(indentStr).append("]");
            return sb.toString();
        } else {
            return "\"" + value.toString() + "\"";
        }
    }

    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Helper methods

    private String getAPITitle(Map<String, Object> spec) {
        Map<String, Object> info = Util.asStringObjectMap(spec.get("info"));
        return info != null ? (String) info.get("title") : "API";
    }

    @Override
    public String getName() {
        return "Mock Data Generator";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getTestType() {
        return "mock_data";
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
