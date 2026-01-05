package egain.oassdk.generators;

import egain.oassdk.config.GeneratorConfig;

/**
 * Interface for generators that can be configured
 */
public interface ConfigurableGenerator {

    /**
     * Set generator configuration
     *
     * @param config Generator configuration
     */
    void setConfig(GeneratorConfig config);

    /**
     * Get generator configuration
     *
     * @return Generator configuration
     */
    GeneratorConfig getConfig();
}
