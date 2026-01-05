package egain.oassdk.testgenerators;

import egain.oassdk.config.TestConfig;

/**
 * Interface for test generators that can be configured
 */
public interface ConfigurableTestGenerator {

    /**
     * Set test configuration
     *
     * @param config Test configuration
     */
    void setConfig(TestConfig config);

    /**
     * Get test configuration
     *
     * @return Test configuration
     */
    TestConfig getConfig();
}
