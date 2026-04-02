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
        java -jar "%DIAGRAMS_DIR%\plantuml.jar" -SbackgroundColor=#FFFFFF -o "auto_generated" "%%f"
        
        if !errorlevel! equ 0 (
            echo [SUCCESS] Generated %%~nf.png in auto_generated directory
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
setlocal disabledelayedexpansion
(
echo ^<!DOCTYPE html^>
echo ^<html lang="en"^>
echo ^<head^>
echo   ^<meta charset="UTF-8"^>
echo   ^<title^>Splendor Game Documentation^</title^>
echo ^</head^>
echo ^<body^>
echo   ^<h1^>Splendor Game Documentation^</h1^>
echo   ^<ul^>
echo     ^<li^>^<a href="./javadoc/index.html"^>API Documentation^</a^>^</li^>
echo     ^<li^>^<a href="./diagrams/"^>UML Diagrams^</a^>^</li^>
echo   ^</ul^>
echo ^</body^>
echo ^</html^>
) > docs\index.html
endlocal
if %errorlevel% neq 0 (
    echo [ERROR] Failed to create docs\index.html
    exit /b 1
)

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
