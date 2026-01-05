package egain.oassdk.generators;

import egain.oassdk.config.GeneratorConfig;
import egain.oassdk.generators.csharp.ASPNETGenerator;
import egain.oassdk.generators.go.GinGenerator;
import egain.oassdk.generators.java.JerseyGenerator;
import egain.oassdk.generators.nodejs.ExpressGenerator;
import egain.oassdk.generators.python.FastAPIGenerator;
import egain.oassdk.generators.python.FlaskGenerator;

import java.util.Locale;


/**
 * Factory for creating code generators based on language and framework
 */
public class GeneratorFactory {

    /**
     * Get generator for specific language and framework
     *
     * @param language  Programming language
     * @param framework Framework
     * @return Code generator instance
     * @throws IllegalArgumentException if language/framework combination is not supported
     */
    public CodeGenerator getGenerator(String language, String framework) {
        String key = language.toLowerCase(Locale.ROOT) + "-" + framework.toLowerCase(Locale.ROOT);

        return switch (key) {
            case "java-jersey", "java-jax-rs", "java-jaxrs" -> new JerseyGenerator();
            case "python-fastapi" -> new FastAPIGenerator();
            case "python-flask" -> new FlaskGenerator();
            case "nodejs-express", "javascript-express" -> new ExpressGenerator();
            case "go-gin" -> new GinGenerator();
            case "csharp-aspnet", "csharp-asp.net" -> new ASPNETGenerator();
            default ->
                    throw new IllegalArgumentException("Unsupported language/framework combination: " + language + "/" + framework);
        };
    }

    /**
     * Get generator with configuration
     *
     * @param language  Programming language
     * @param framework Framework
     * @param config    Generator configuration
     * @return Code generator instance
     */
    public CodeGenerator getGenerator(String language, String framework, GeneratorConfig config) {
        CodeGenerator generator = getGenerator(language, framework);
        if (generator instanceof ConfigurableGenerator) {
            ((ConfigurableGenerator) generator).setConfig(config);
        }
        return generator;
    }

    /**
     * Get list of supported language/framework combinations
     *
     * @return Array of supported combinations
     */
    public String[] getSupportedCombinations() {
        return new String[]{
                "java-jersey",
                "python-fastapi",
                "python-flask",
                "nodejs-express",
                "go-gin",
                "csharp-aspnet"
        };
    }

    /**
     * Check if language/framework combination is supported
     *
     * @param language  Programming language
     * @param framework Framework
     * @return true if supported
     */
    public boolean isSupported(String language, String framework) {
        try {
            getGenerator(language, framework);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
