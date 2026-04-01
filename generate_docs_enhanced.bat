@echo off
REM Enhanced Splendor Documentation Generator
REM Automatically generates Javadoc and UML diagrams

setlocal enabledelayedexpansion

set SOURCE_DIR=src
set OUTPUT_DIR=docs\javadoc
set DIAGRAMS_DIR=docs\diagrams
set PACKAGE=com.splendor

echo [INFO] Starting Splendor Documentation Generation...

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    exit /b 1
)

REM Check if javadoc is available
javadoc -help >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] javadoc is not available. Please install JDK.
    exit /b 1
)

REM Create output directories
echo [INFO] Creating output directories...
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist "%DIAGRAMS_DIR%" mkdir "%DIAGRAMS_DIR%"

REM Generate Javadoc
echo [INFO] Generating Javadoc...
javadoc ^
    -d "%OUTPUT_DIR%" ^
    -sourcepath "%SOURCE_DIR%" ^
    -subpackages "%PACKAGE%" ^
    -encoding UTF-8 ^
    -charset UTF-8 ^
    -docencoding UTF-8 ^
    -version ^
    -author ^
    -use ^
    -splitindex ^
    -windowtitle "Splendor Game API Documentation" ^
    -doctitle "Splendor Board Game - API Documentation" ^
    -header "<b>Splendor v1.0</b>" ^
    -footer "Generated on %date% %time%" ^
    -link "https://docs.oracle.com/en/java/javase/17/docs/api/" ^
    -Xdoclint:all ^
    -Xdoclint:-missing ^
    -quiet

if %errorlevel% equ 0 (
    echo [SUCCESS] Javadoc generated successfully at: %OUTPUT_DIR%\index.html
) else (
    echo [ERROR] Javadoc generation failed
    exit /b 1
)

REM Generate UML diagrams if PlantUML is available
echo [INFO] Checking for PlantUML...
if exist "%DIAGRAMS_DIR%\plantuml.jar" (
    echo [INFO] Generating UML diagrams...
    
    REM Generate all PlantUML diagrams
    for %%f in ("%DIAGRAMS_DIR%\*.puml") do (
        echo [INFO] Processing %%~nxf...
        java -jar "%DIAGRAMS_DIR%\plantuml.jar" "%%f"
        
        if !errorlevel! equ 0 (
            echo [SUCCESS] Generated %%~nf.png
        ) else (
            echo [ERROR] Failed to generate %%~nf.png
        )
    )
    
    echo [SUCCESS] UML diagram generation complete
) else (
    echo [INFO] PlantUML not found. Skipping UML diagram generation.
    echo [INFO] To enable UML generation, ensure plantuml.jar is in %DIAGRAMS_DIR%
)

REM Generate documentation index
echo [INFO] Creating documentation index...
(
echo ^<!DOCTYPE html^>
echo ^<html lang="en"^>
echo ^<head^>
echo     ^<meta charset="UTF-8"^>
echo     ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^>
echo     ^<title^>Splendor Game Documentation^</title^>
echo     ^<style^>
echo         body {
echo             font-family: Arial, sans-serif;
echo             max-width: 1200px;
echo             margin: 0 auto;
echo             padding: 20px;
echo             background-color: #f5f5f5;
echo         }
echo         .container {
echo             background: white;
echo             padding: 30px;
echo             border-radius: 10px;
echo             box-shadow: 0 2px 10px rgba(0,0,0,0.1);
echo         }
echo         h1 {
echo             color: #2c3e50;
echo             text-align: center;
echo             margin-bottom: 30px;
echo         }
echo         .section {
echo             margin: 30px 0;
echo             padding: 20px;
echo             border-left: 4px solid #3498db;
echo             background-color: #f8f9fa;
echo         }
echo         .section h2 {
echo             color: #2c3e50;
echo             margin-top: 0;
echo         }
echo         .link-grid {
echo             display: grid;
echo             grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
echo             gap: 20px;
echo             margin-top: 20px;
echo         }
echo         .link-card {
echo             background: white;
echo             padding: 20px;
echo             border-radius: 8px;
echo             box-shadow: 0 2px 5px rgba(0,0,0,0.1);
echo             text-decoration: none;
echo             color: inherit;
echo             transition: transform 0.2s;
echo         }
echo         .link-card:hover {
echo             transform: translateY(-2px);
echo             box-shadow: 0 4px 15px rgba(0,0,0,0.15);
echo         }
echo         .link-card h3 {
echo             color: #3498db;
echo             margin-top: 0;
echo         }
echo         .diagram-grid {
echo             display: grid;
echo             grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
echo             gap: 20px;
echo             margin-top: 20px;
echo         }
echo         .diagram-card {
echo             background: white;
echo             padding: 15px;
echo             border-radius: 8px;
echo             box-shadow: 0 2px 5px rgba(0,0,0,0.1);
echo             text-align: center;
echo         }
echo         .diagram-card img {
echo             max-width: 100%;
echo             height: auto;
echo             border: 1px solid #ddd;
echo             border-radius: 4px;
echo         }
echo         .update-info {
echo             text-align: center;
echo             color: #7f8c8d;
echo             font-style: italic;
echo             margin-top: 30px;
echo             padding-top: 20px;
echo             border-top: 1px solid #ecf0f1;
echo         }
echo     ^</style^>
echo ^</head^>
echo ^<body^>
echo     ^<div class="container"^>
echo         ^<h1^>🎲 Splendor Game Documentation^</h1^>
echo         
echo         ^<div class="section"^>
echo             ^<h2^>📚 API Documentation^</h2^>
echo             ^<div class="link-grid"^>
echo                 ^<a href="javadoc/index.html" class="link-card"^>
echo                     ^<h3^>📖 Java API Documentation^</h3^>
echo                     ^<p^>Complete Javadoc for all classes, methods, and packages in the Splendor game implementation.^</p^>
echo                 ^</a^>
echo                 ^<a href="diagrams/" class="link-card"^>
echo                     ^<h3^>🏗️ Architecture Diagrams^</h3^>
echo                     ^<p^>UML diagrams showing class relationships, dependencies, and system architecture.^</p^>
echo                 ^</a^>
echo             ^</div^>
echo         ^</div^>
echo.
echo         ^<div class="section"^>
echo             ^<h2^>🎯 UML Diagrams^</h2^>
echo             ^<div class="diagram-grid"^>
echo                 ^<div class="diagram-card"^>
echo                     ^<h3^>Class Diagram^</h3^>
echo                     ^<a href="diagrams/splendor.png" target="_blank"^>
echo                         ^<img src="diagrams/splendor.png" alt="Main Class Diagram" onerror="this.style.display='none'"^>
echo                     ^</a^>
echo                     ^<p^>Complete class structure and relationships^</p^>
echo                 ^</div^>
echo                 ^<div class="diagram-card"^>
echo                     ^<h3^>Lightweight Class Diagram^</h3^>
echo                     ^<a href="diagrams/splendor_class_light.png" target="_blank"^>
echo                         ^<img src="diagrams/splendor_class_light.png" alt="Lightweight Class Diagram" onerror="this.style.display='none'"^>
echo                     ^</a^>
echo                     ^<p^>Simplified view of main classes^</p^>
echo                 ^</div^>
echo                 ^<div class="diagram-card"^>
echo                     ^<h3^>Dependency Diagram^</h3^>
echo                     ^<a href="diagrams/splendor_dependency.png" target="_blank"^>
echo                         ^<img src="diagrams/splendor_dependency.png" alt="Dependency Diagram" onerror="this.style.display='none'"^>
echo                     ^</a^>
echo                     ^<p^>Package and class dependencies^</p^>
echo                 ^</div^>
echo                 ^<div class="diagram-card"^>
echo                     ^<h3^>Functional Flow^</h3^>
echo                     ^<a href="diagrams/splendor_functional.png" target="_blank"^>
echo                         ^<img src="diagrams/splendor_functional.png" alt="Functional Flow Diagram" onerror="this.style.display='none'"^>
echo                     ^</a^>
echo                     ^<p^>Game flow and sequence diagrams^</p^>
echo                 ^</div^>
echo                 ^<div class="diagram-card"^>
echo                     ^<h3^>Inheritance Diagram^</h3^>
echo                     ^<a href="diagrams/splendor_inheritance.png" target="_blank"^>
echo                         ^<img src="diagrams/splendor_inheritance.png" alt="Inheritance Diagram" onerror="this.style.display='none'"^>
echo                     ^</a^>
echo                     ^<p^>Class inheritance and interface hierarchy^</p^>
echo                 ^</div^>
echo             ^</div^>
echo         ^</div^>
echo.
echo         ^<div class="update-info"^>
echo             ^<p^>Documentation last generated on: ^<strong^>%date% %time%^</strong^>^</p^>
echo             ^<p^>🔄 Run ^<code^>generate_docs_enhanced.bat^</code^> to regenerate documentation^</p^>
echo         ^</div^>
echo     ^</div^>
echo ^</body^>
echo ^</html^>
echo.
) > "docs\index.html"

echo [SUCCESS] Documentation index created at: docs\index.html

REM Summary
echo.
echo [SUCCESS] 🎉 Documentation generation complete!
echo 📖 Javadoc: file://%~dp0%OUTPUT_DIR%\index.html
echo 🎯 Documentation Index: file://%~dp0docs\index.html
echo 🏗️  UML Diagrams: file://%~dp0%DIAGRAMS_DIR%\
echo.
echo 💡 Tip: Add this script to your CI/CD pipeline or git hooks for automatic updates!

endlocal