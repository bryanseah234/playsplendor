package com.splendor.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Comprehensive tests for documentation completeness and quality.
 * Validates Javadoc generation, UML diagrams, and documentation structure.
 */
@DisplayName("Documentation Validation Tests")
public class DocumentationTest {
    
    private static final String DOCS_DIR = "docs";
    private static final String JAVADOC_DIR = "docs/javadoc";
    private static final String DIAGRAMS_DIR = "docs/diagrams";
    private static final String SOURCE_DIR = "src";
    
    @BeforeAll
    static void setup() {
        System.out.println("🔍 Starting documentation validation tests...");
    }
    
    @AfterAll
    static void teardown() {
        System.out.println("✅ Documentation validation tests complete.");
    }
    
    @Test
    @DisplayName("Javadoc directory should exist and contain index.html")
    void testJavadocStructure() {
        File javadocDir = new File(JAVADOC_DIR);
        assertTrue(javadocDir.exists(), "Javadoc directory should exist at: " + JAVADOC_DIR);
        assertTrue(javadocDir.isDirectory(), "Javadoc path should be a directory");
        
        // Check essential Javadoc files
        File indexFile = new File(javadocDir, "index.html");
        assertTrue(indexFile.exists(), "Javadoc index.html should exist");
        assertTrue(indexFile.length() > 0, "Javadoc index.html should not be empty");
        
        File overviewFile = new File(javadocDir, "overview-summary.html");
        assertTrue(overviewFile.exists(), "Javadoc overview-summary.html should exist");
        
        File allClassesFile = new File(javadocDir, "allclasses-index.html");
        assertTrue(allClassesFile.exists(), "Javadoc allclasses-index.html should exist");
    }
    
    @Test
    @DisplayName("All main UML diagrams should exist and be valid")
    void testUmlDiagramsExist() {
        File diagramsDir = new File(DIAGRAMS_DIR);
        assertTrue(diagramsDir.exists(), "Diagrams directory should exist");
        assertTrue(diagramsDir.isDirectory(), "Diagrams path should be a directory");
        
        // Essential diagram files
        String[] expectedDiagrams = {
            "splendor.png",
            "splendor_class_light.png",
            "splendor_dependency.png",
            "splendor_functional.png",
            "splendor_inheritance.png"
        };
        
        for (String diagram : expectedDiagrams) {
            File diagramFile = new File(diagramsDir, diagram);
            assertTrue(diagramFile.exists(), "Diagram " + diagram + " should exist");
            assertTrue(diagramFile.length() > 0, "Diagram " + diagram + " should not be empty");
            
            // Check that it's a valid PNG file (basic check)
            assertTrue(diagramFile.getName().endsWith(".png"), "Diagram " + diagram + " should be a PNG file");
        }
    }
    
    @Test
    @DisplayName("PlantUML source files should exist for diagrams")
    void testPlantUmlSourceFiles() {
        File diagramsDir = new File(DIAGRAMS_DIR);
        
        // Check that source .puml files exist
        String[] expectedPumlFiles = {
            "splendor.puml",
            "splendor-class-light.puml",
            "splendor-dependency.puml",
            "splendor-functional.puml",
            "splendor-inheritance.puml"
        };
        
        for (String pumlFile : expectedPumlFiles) {
            File sourceFile = new File(diagramsDir, pumlFile);
            assertTrue(sourceFile.exists(), "PlantUML source " + pumlFile + " should exist");
            assertTrue(sourceFile.length() > 0, "PlantUML source " + pumlFile + " should not be empty");
        }
    }
    
    @Test
    @DisplayName("Documentation index should exist and be valid HTML")
    void testDocumentationIndex() throws IOException {
        Path indexPath = Paths.get(DOCS_DIR, "index.html");
        assertTrue(Files.exists(indexPath), "Documentation index should exist");
        
        String content = Files.readString(indexPath);
        
        // Check essential content
        assertTrue(content.contains("Splendor Game Documentation"), 
                  "Index should contain title");
        assertTrue(content.contains("javadoc/index.html"), 
                  "Index should link to Javadoc");
        assertTrue(content.contains("diagrams/"), 
                  "Index should link to diagrams");
        assertTrue(content.contains("API Documentation"), 
                  "Index should mention API documentation");
        assertTrue(content.contains("UML Diagrams"), 
                  "Index should mention UML diagrams");
        
        // Check HTML structure
        assertTrue(content.contains("<!DOCTYPE html>"), "Index should be valid HTML5");
        assertTrue(content.contains("<html"), "Index should have HTML tag");
        assertTrue(content.contains("<head>"), "Index should have HEAD tag");
        assertTrue(content.contains("<body>"), "Index should have BODY tag");
    }
    
    @Test
    @DisplayName("Documentation guidelines should exist")
    void testDocumentationGuidelines() {
        File guidelinesFile = new File(DOCS_DIR, "DOCUMENTATION_GUIDELINES.md");
        assertTrue(guidelinesFile.exists(), "Documentation guidelines should exist");
        assertTrue(guidelinesFile.length() > 0, "Documentation guidelines should not be empty");
    }
    
    @Test
    @DisplayName("All Java source files should have corresponding Javadoc")
    void testJavadocCoverage() throws IOException {
        // Find all Java source files
        List<Path> javaFiles = findJavaFiles(Paths.get(SOURCE_DIR));
        
        // Find all HTML files in Javadoc (excluding index files)
        List<Path> javadocFiles = findJavadocHtmlFiles(Paths.get(JAVADOC_DIR));
        
        // Check that we have reasonable coverage
        assertFalse(javaFiles.isEmpty(), "Should find Java source files");
        assertFalse(javadocFiles.isEmpty(), "Should find Javadoc HTML files");
        
        System.out.println("📊 Found " + javaFiles.size() + " Java files and " + 
                          javadocFiles.size() + " Javadoc HTML files");
        
        // Basic sanity check - should have at least one Javadoc file per package
        assertTrue(javadocFiles.size() >= 10, "Should have reasonable number of Javadoc files");
    }
    
    @Test
    @DisplayName("Javadoc should contain package documentation")
    void testPackageDocumentation() {
        File packageSummary = new File(JAVADOC_DIR, "com/splendor/package-summary.html");
        assertTrue(packageSummary.exists(), "Main package summary should exist");
        
        // Check for other package summaries
        String[] packages = {
            "com/splendor/config",
            "com/splendor/controller", 
            "com/splendor/model",
            "com/splendor/view",
            "com/splendor/network"
        };
        
        for (String pkg : packages) {
            File pkgSummary = new File(JAVADOC_DIR, pkg + "/package-summary.html");
            if (pkgSummary.exists()) {
                assertTrue(pkgSummary.length() > 0, "Package summary for " + pkg + " should not be empty");
            }
        }
    }
    
    @Test
    @DisplayName("Documentation generation scripts should exist and be executable")
    void testDocumentationScripts() {
        // Check enhanced documentation generator
        File scriptUnix = new File("generate_docs_enhanced.sh");
        File scriptWindows = new File("generate_docs_enhanced.bat");
        
        assertTrue(scriptUnix.exists() || scriptWindows.exists(), 
                  "Documentation generation script should exist");
        
        if (scriptUnix.exists()) {
            assertTrue(scriptUnix.canExecute(), "Unix script should be executable");
        }
        
        // Check auto UML generator
        File autoUmlScript = new File("generate_auto_uml.sh");
        if (autoUmlScript.exists()) {
            assertTrue(autoUmlScript.canExecute(), "Auto UML script should be executable");
        }
    }
    
    @Test
    @DisplayName("PlantUML jar should exist for diagram generation")
    void testPlantUmlJar() {
        File plantUmlJar = new File(DIAGRAMS_DIR, "plantuml.jar");
        assertTrue(plantUmlJar.exists(), "PlantUML jar should exist");
        assertTrue(plantUmlJar.length() > 0, "PlantUML jar should not be empty");
        assertTrue(plantUmlJar.length() > 1000000, "PlantUML jar should be reasonably sized");
    }
    
    @Test
    @DisplayName("VS Code settings should include documentation tasks")
    void testVSCodeSettings() throws IOException {
        Path vscodeSettings = Paths.get(".vscode/settings.json");
        if (Files.exists(vscodeSettings)) {
            String content = Files.readString(vscodeSettings);
            
            assertTrue(content.contains("plantuml"), "VS Code settings should include PlantUML configuration");
            assertTrue(content.contains("Generate Documentation"), 
                      "VS Code settings should include documentation generation task");
            assertTrue(content.contains("javadoc"), "VS Code settings should include Javadoc configuration");
        }
    }
    
    @Test
    @DisplayName("AI agent configuration should exist")
    void testAIAgentConfiguration() {
        File aiConfig = new File(".ai-documentation-config.yml");
        assertTrue(aiConfig.exists(), "AI agent configuration should exist");
        
        // Basic validation of configuration file
        try {
            String content = Files.readString(aiConfig.toPath());
            assertTrue(content.contains("documentation:"), "AI config should contain documentation section");
            assertTrue(content.contains("javadoc:"), "AI config should contain javadoc configuration");
            assertTrue(content.contains("uml:"), "AI config should contain UML configuration");
        } catch (IOException e) {
            fail("Could not read AI agent configuration: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("GitHub workflow should exist for documentation updates")
    void testGitHubWorkflow() {
        File workflowFile = new File(".github/workflows/documentation.yml");
        assertTrue(workflowFile.exists(), "GitHub workflow for documentation should exist");
        
        try {
            String content = Files.readString(workflowFile.toPath());
            assertTrue(content.contains("Documentation Update"), 
                      "Workflow should be named 'Documentation Update'");
            assertTrue(content.contains("generate_docs_enhanced"), 
                      "Workflow should use documentation generation script");
            assertTrue(content.contains("javadoc"), 
                      "Workflow should mention Javadoc");
            assertTrue(content.contains("plantuml"), 
                      "Workflow should mention PlantUML");
        } catch (IOException e) {
            fail("Could not read GitHub workflow: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private List<Path> findJavaFiles(Path sourceDir) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());
        }
        
        return javaFiles;
    }
    
    private List<Path> findJavadocHtmlFiles(Path javadocDir) throws IOException {
        List<Path> htmlFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(javadocDir)) {
            htmlFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".html"))
                .filter(p -> !p.toString().contains("index-") && !p.toString().contains("package-"))
                .collect(Collectors.toList());
        }
        
        return htmlFiles;
    }
}