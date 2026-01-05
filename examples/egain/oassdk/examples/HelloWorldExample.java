package egain.oassdk.examples;

import egain.oassdk.OASSDK;
import egain.oassdk.config.GeneratorConfig;
import egain.oassdk.config.TestConfig;
import egain.oassdk.config.SLAConfig;
import egain.oassdk.core.exceptions.OASSDKException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Hello World API Example using OAS SDK
 * 
 * This example demonstrates how to use the OAS SDK to generate
 * a complete Jersey application from an OpenAPI specification.
 */
public class HelloWorldExample {
    
    public static void main(String[] args) {
        System.out.println("🚀 Hello World API Example using OAS SDK");
        System.out.println("=" + "=".repeat(50));
        
        try {
            // Initialize SDK with configuration
            GeneratorConfig generatorConfig = GeneratorConfig.builder()
                    .language("java")
                    .framework("jersey")
                    .packageName("com.example.helloworld")
                    .version("1.0.0")
                    .build();
            
            TestConfig testConfig = TestConfig.builder()
                    .unitTests(true)
                    .integrationTests(true)
                    .nfrTests(true)
                    .performanceTests(true)
                    .securityTests(true)
                    .testFramework("junit5")
                    .mockData(true)
                    .build();
            
            SLAConfig slaConfig = SLAConfig.builder()
                    .slaFile("sla.yaml")
                    .monitoring(true)
                    .alerting(true)
                    .compliance(Arrays.asList("gdpr", "iso27001"))
                    .build();
            
            // Initialize SDK
            OASSDK sdk = new OASSDK(generatorConfig, testConfig, slaConfig);
            
            // Load OpenAPI specification
            System.out.println("📋 Loading OpenAPI specification...");
            sdk.loadSpec("openapi.yaml");
            System.out.println("✅ Specification loaded successfully");
            
            // Validate specification
            System.out.println("\n🔍 Validating specification...");
            if (sdk.validateSpec()) {
                System.out.println("✅ Specification is valid");
            } else {
                System.out.println("❌ Specification is invalid");
                return;
            }
            
            // Get metadata
            System.out.println("\n📊 API Information:");
            var metadata = sdk.getMetadata();
            var apiInfo = (java.util.Map<String, Object>) metadata.get("api_info");
            System.out.println("  Title: " + apiInfo.get("title"));
            System.out.println("  Version: " + apiInfo.get("version"));
            System.out.println("  Description: " + apiInfo.get("description"));
            
            var stats = (java.util.Map<String, Object>) metadata.get("statistics");
            System.out.println("\n📈 Statistics:");
            System.out.println("  Endpoints: " + stats.get("endpoint_count"));
            System.out.println("  Models: " + stats.get("model_count"));
            System.out.println("  Paths: " + stats.get("path_count"));
            
            // Generate complete project
            System.out.println("\n🏗️ Generating complete project...");
            sdk.generateAll("./generated");
            System.out.println("✅ Complete project generated successfully");
            
            // Show generated structure
            System.out.println("\n📁 Generated project structure:");
            showDirectoryStructure("./generated");
            
            System.out.println("\n🎉 Example completed successfully!");
            System.out.println("\nNext steps:");
            System.out.println("1. Review the generated code in ./generated/");
            System.out.println("2. Run the generated tests");
            System.out.println("3. Deploy the application");
            System.out.println("4. Monitor SLA compliance");
            
        } catch (OASSDKException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show directory structure
     * 
     * @param directory Directory path
     */
    private static void showDirectoryStructure(String directory) {
        showDirectoryStructure(directory, "", 3, 0);
    }
    
    /**
     * Show directory structure with depth limit
     * 
     * @param directory Directory path
     * @param prefix Current prefix
     * @param maxDepth Maximum depth
     * @param currentDepth Current depth
     */
    private static void showDirectoryStructure(String directory, String prefix, int maxDepth, int currentDepth) {
        if (currentDepth >= maxDepth) {
            return;
        }
        
        try {
            Path path = Paths.get(directory);
            if (!Files.exists(path)) {
                return;
            }
            
            List<Path> items = Files.list(path)
                    .sorted()
                    .toList();
            
            for (int i = 0; i < items.size(); i++) {
                Path item = items.get(i);
                boolean isLast = i == items.size() - 1;
                String currentPrefix = isLast ? "└── " : "├── ";
                System.out.println(prefix + currentPrefix + item.getFileName());
                
                if (Files.isDirectory(item) && currentDepth < maxDepth - 1) {
                    String nextPrefix = prefix + (isLast ? "    " : "│   ");
                    showDirectoryStructure(item.toString(), nextPrefix, maxDepth, currentDepth + 1);
                }
            }
        } catch (IOException e) {
            System.out.println(prefix + "└── [Permission Denied]");
        }
    }
}
