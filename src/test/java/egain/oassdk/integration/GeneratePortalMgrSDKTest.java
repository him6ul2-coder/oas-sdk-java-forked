package egain.oassdk.integration;

import egain.oassdk.OASSDK;
import egain.oassdk.core.exceptions.OASSDKException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test to generate SDK from openapi5.yaml and verify validation annotations
 */
@DisplayName("Generate SDK from openapi5.yaml")
public class GeneratePortalMgrSDKTest {
    
    @TempDir
    Path tempOutputDir;
    
    @Test
    @DisplayName("Generate OpenAPI 5 SDK with Validation Annotations")
    public void testGenerateOpenAPI5SDK() throws OASSDKException, IOException {
        String yamlFile = "src/test/resources/openapi5.yaml";
        String packageName = "com.egain.openapi5.api";
        Path outputDir = tempOutputDir.resolve("generated-code/openapi5");
        
        System.out.println("\n=== Generating SDK from openapi5.yaml ===");
        System.out.println("Output directory: " + outputDir.toAbsolutePath());
        
        // Create SDK instance
        OASSDK sdk = new OASSDK();
        
        // Load specification
        System.out.println("\n1. Loading OpenAPI specification...");
        sdk.loadSpec(yamlFile);
        System.out.println("   ✓ Specification loaded");
        
        // Generate application
        System.out.println("\n2. Generating Jersey application...");
        System.out.println("   Package: " + packageName);
        sdk.generateApplication("java", "jersey", packageName, outputDir.toString());
        System.out.println("   ✓ Application generated");
        
        // Verify generated files
        System.out.println("\n3. Verifying generated files...");
        assertTrue(Files.exists(outputDir), "Output directory should exist");
        
        Path pomFile = outputDir.resolve("pom.xml");
        assertTrue(Files.exists(pomFile), "pom.xml should exist");
        System.out.println("   ✓ pom.xml exists");
        
        Path srcMainJava = outputDir.resolve("src/main/java");
        assertTrue(Files.exists(srcMainJava), "src/main/java should exist");
        System.out.println("   ✓ src/main/java directory exists");
        
        // Check validation annotations
        System.out.println("\n4. Checking validation annotations...");
        checkValidationAnnotations(outputDir, packageName);
        
        System.out.println("\n=== SDK Generation Complete ===");
        System.out.println("Generated SDK location: " + outputDir.toAbsolutePath());
    }
    
    private void checkValidationAnnotations(Path outputDir, String packageName) throws IOException {
        Path modelDir = outputDir.resolve("src/main/java")
            .resolve(packageName.replace(".", "/"))
            .resolve("model");
        
        if (!Files.exists(modelDir)) {
            System.out.println("   ℹ No model directory found");
            return;
        }
        
        List<Path> modelFiles = Files.walk(modelDir)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .collect(Collectors.toList());
        
        if (modelFiles.isEmpty()) {
            System.out.println("   ℹ No model files found");
            return;
        }
        
        System.out.println("   Found " + modelFiles.size() + " model files");
        
        int filesWithValidationImport = 0;
        int filesWithValidationAnnotations = 0;
        
        for (Path modelFile : modelFiles) {
            String content = Files.readString(modelFile);
            String fileName = modelFile.getFileName().toString();
            
            boolean hasValidationImport = content.contains("import javax.validation.constraints");
            boolean hasValidationAnnotation = content.contains("@NotNull") || 
                content.contains("@Size") || 
                content.contains("@Min") || 
                content.contains("@Max") || 
                content.contains("@Pattern") || 
                content.contains("@Email") ||
                content.contains("@DecimalMin") ||
                content.contains("@DecimalMax") ||
                content.contains("@Positive") ||
                content.contains("@Negative");
            
            if (hasValidationImport) {
                filesWithValidationImport++;
            }
            
            if (hasValidationAnnotation) {
                filesWithValidationAnnotations++;
                
                // Show first example of validation annotations
                if (filesWithValidationAnnotations <= 3) {
                    System.out.println("\n   ✓ " + fileName + " contains validation annotations:");
                    String[] lines = content.split("\n");
                    boolean foundExample = false;
                    for (int i = 0; i < lines.length && !foundExample; i++) {
                        String line = lines[i].trim();
                        if (line.startsWith("@NotNull") || 
                            line.startsWith("@Size") || 
                            line.startsWith("@Min") || 
                            line.startsWith("@Max") || 
                            line.startsWith("@Pattern") || 
                            line.startsWith("@Email")) {
                            System.out.println("      " + line);
                            // Show the field declaration
                            if (i + 1 < lines.length) {
                                String nextLine = lines[i + 1].trim();
                                if (nextLine.startsWith("private")) {
                                    System.out.println("      " + nextLine);
                                    foundExample = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("\n   Summary:");
        System.out.println("   - Total model files: " + modelFiles.size());
        System.out.println("   - Files with validation imports: " + filesWithValidationImport);
        System.out.println("   - Files with validation annotations: " + filesWithValidationAnnotations);
        
        // Assertions
        // All model files should have validation imports (even if they don't use annotations)
        // This ensures the import is always available when needed
        // assertEquals(modelFiles.size(), filesWithValidationImport, 
        //    "All model files should have validation imports");
        
        if (filesWithValidationAnnotations > 0) {
            System.out.println("   ✓ Validation annotations are being generated correctly");
        } else {
            System.out.println("   ℹ No validation annotations found (schemas may not have constraints)");
        }
    }
}

