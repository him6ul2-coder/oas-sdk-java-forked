package egain.oassdk.test;

import egain.oassdk.core.exceptions.OASSDKException;
import egain.oassdk.core.metadata.OASMetadata;
import egain.oassdk.core.parser.OASParser;
import egain.oassdk.test.mock.MockDataGenerator;
import egain.oassdk.test.nfr.NFRTestGenerator;
import egain.oassdk.test.postman.PostmanTestGenerator;
import egain.oassdk.test.schemathesis.SchemathesisTestRunner;
import egain.oassdk.test.sequence.RandomizedSequenceTester;

import java.util.Map;

/**
 * Testing SDK - Provides components for API testing
 * <p>
 * This SDK provides mock data generation, schemathesis testing, Postman scripts,
 * NFR testing, and randomized sequence testing for comprehensive API validation.
 */
public class TestSDK {

    private final OASParser parser;
    private final OASMetadata metadata;
    private final MockDataGenerator mockDataGenerator;
    private final SchemathesisTestRunner schemathesisRunner;
    private final PostmanTestGenerator postmanGenerator;
    private final NFRTestGenerator nfrTestGenerator;
    private final RandomizedSequenceTester sequenceTester;

    // Loaded specifications
    private Map<String, Object> openApiSpec;
    private Map<String, Object> slaSpec;

    public TestSDK() {
        this.parser = new OASParser();
        this.metadata = new OASMetadata();
        this.mockDataGenerator = new MockDataGenerator();
        this.schemathesisRunner = new SchemathesisTestRunner();
        this.postmanGenerator = new PostmanTestGenerator();
        this.nfrTestGenerator = new NFRTestGenerator();
        this.sequenceTester = new RandomizedSequenceTester();
    }

    /**
     * Load OpenAPI specification
     *
     * @param specPath Path to OpenAPI specification file
     * @return This SDK instance for method chaining
     * @throws OASSDKException if specification cannot be loaded
     */
    public TestSDK loadOpenAPISpec(String specPath) throws OASSDKException {
        try {
            this.openApiSpec = parser.parse(specPath);
            metadata.extract(this.openApiSpec);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to load OpenAPI specification: " + e.getMessage(), e);
        }
    }

    /**
     * Load SLA specification
     *
     * @param slaPath Path to SLA specification file
     * @return This SDK instance for method chaining
     * @throws OASSDKException if SLA specification cannot be loaded
     */
    public TestSDK loadSLASpec(String slaPath) throws OASSDKException {
        try {
            this.slaSpec = parser.parse(slaPath);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to load SLA specification: " + e.getMessage(), e);
        }
    }

    /**
     * Generate mock data using OpenAPI spec
     *
     * @param outputDir Output directory for mock data
     * @return This SDK instance for method chaining
     * @throws OASSDKException if generation fails
     */
    public TestSDK generateMockData(String outputDir) throws OASSDKException {
        if (openApiSpec == null) {
            throw new OASSDKException("No OpenAPI specification loaded. Call loadOpenAPISpec() first.");
        }

        try {
            mockDataGenerator.generateMockData(openApiSpec, outputDir);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to generate mock data: " + e.getMessage(), e);
        }
    }

    /**
     * Execute Schemathesis tests from OpenAPI spec
     *
     * @param outputDir Output directory for test results
     * @param baseUrl   Base URL for the API under test
     * @return This SDK instance for method chaining
     * @throws OASSDKException if test execution fails
     */
    public TestSDK executeSchemathesisTests(String outputDir, String baseUrl) throws OASSDKException {
        if (openApiSpec == null) {
            throw new OASSDKException("No OpenAPI specification loaded. Call loadOpenAPISpec() first.");
        }

        try {
            schemathesisRunner.executeTests(openApiSpec, outputDir, baseUrl);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to execute Schemathesis tests: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Postman test scripts for each API
     *
     * @param outputDir Output directory for Postman scripts
     * @return This SDK instance for method chaining
     * @throws OASSDKException if generation fails
     */
    public TestSDK generatePostmanTestScripts(String outputDir) throws OASSDKException {
        if (openApiSpec == null) {
            throw new OASSDKException("No OpenAPI specification loaded. Call loadOpenAPISpec() first.");
        }

        try {
            postmanGenerator.generateTestScripts(openApiSpec, outputDir);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to generate Postman test scripts: " + e.getMessage(), e);
        }
    }

    /**
     * Generate NFR testing scripts using SLA spec
     *
     * @param outputDir Output directory for NFR tests
     * @return This SDK instance for method chaining
     * @throws OASSDKException if generation fails
     */
    public TestSDK generateNFRTestScripts(String outputDir) throws OASSDKException {
        if (slaSpec == null) {
            throw new OASSDKException("No SLA specification loaded. Call loadSLASpec() first.");
        }

        try {
            nfrTestGenerator.generateNFRTests(slaSpec, outputDir);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to generate NFR test scripts: " + e.getMessage(), e);
        }
    }

    /**
     * Generate randomized sequence testing of API using OpenAPI spec
     *
     * @param outputDir Output directory for sequence tests
     * @param baseUrl   Base URL for the API under test
     * @return This SDK instance for method chaining
     * @throws OASSDKException if generation fails
     */
    public TestSDK generateRandomizedSequenceTests(String outputDir, String baseUrl) throws OASSDKException {
        if (openApiSpec == null) {
            throw new OASSDKException("No OpenAPI specification loaded. Call loadOpenAPISpec() first.");
        }

        try {
            sequenceTester.generateSequenceTests(openApiSpec, outputDir, baseUrl);
            return this;
        } catch (Exception e) {
            throw new OASSDKException("Failed to generate randomized sequence tests: " + e.getMessage(), e);
        }
    }

    /**
     * Generate all testing components
     *
     * @param outputDir Base output directory
     * @param baseUrl   Base URL for the API under test
     * @return This SDK instance for method chaining
     * @throws OASSDKException if generation fails
     */
    public TestSDK generateAll(String outputDir, String baseUrl) throws OASSDKException {
        generateMockData(outputDir + "/mock-data");
        executeSchemathesisTests(outputDir + "/schemathesis", baseUrl);
        generatePostmanTestScripts(outputDir + "/postman");
        generateNFRTestScripts(outputDir + "/nfr");
        generateRandomizedSequenceTests(outputDir + "/sequence", baseUrl);
        return this;
    }

    /**
     * Get loaded OpenAPI specification
     *
     * @return OpenAPI specification
     */
    public Map<String, Object> getOpenAPISpec() {
        return openApiSpec;
    }

    /**
     * Get loaded SLA specification
     *
     * @return SLA specification
     */
    public Map<String, Object> getSLASpec() {
        return slaSpec;
    }

    /**
     * Get extracted metadata
     *
     * @return Metadata from OpenAPI specification
     */
    public Map<String, Object> getMetadata() {
        return metadata.getMetadata();
    }
}
