package egain.oassdk.testgenerators;

import egain.oassdk.config.TestConfig;
import egain.oassdk.core.exceptions.GenerationException;

import java.util.Map;

/**
 * Interface for test generators
 */
public interface TestGenerator {

    /**
     * Generate tests from OpenAPI specification
     *
     * @param spec          Parsed OpenAPI specification
     * @param outputDir     Output directory for generated tests
     * @param config        Test configuration
     * @param testFramework Test framework (junit5, pytest, jest, etc.)
     * @throws GenerationException if generation fails
     */
    void generate(Map<String, Object> spec, String outputDir, TestConfig config, String testFramework) throws GenerationException;

    /**
     * Generate tests with default framework
     *
     * @param spec      Parsed OpenAPI specification
     * @param outputDir Output directory for generated tests
     * @param config    Test configuration
     * @throws GenerationException if generation fails
     */
    default void generate(Map<String, Object> spec, String outputDir, TestConfig config) throws GenerationException {
        generate(spec, outputDir, config, null);
    }

    /**
     * Get generator name
     *
     * @return Generator name
     */
    String getName();

    /**
     * Get generator version
     *
     * @return Generator version
     */
    String getVersion();

    /**
     * Get test type
     *
     * @return Test type
     */
    String getTestType();
}
