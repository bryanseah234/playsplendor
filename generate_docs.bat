@echo off
REM Splendor Javadoc Generator for Windows
REM Generates HTML documentation from Java source files

setlocal enabledelayedexpansion

set SOURCE_DIR=src
set OUTPUT_DIR=docs\javadoc
set PACKAGE=com.splendor

echo Generating Javadoc for Splendor project...

REM Create output directory if it doesn't exist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Run javadoc with strict settings
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
    -footer "Generated on %DATE% %TIME%" ^
    -link "https://docs.oracle.com/en/java/javase/17/docs/api/" ^
    -Xdoclint:all ^
    -Xdoclint:-missing ^
    -quiet

if errorlevel 1 (
    echo ERROR: Javadoc generation failed with exit code %errorlevel%
    exit /b %errorlevel%
)

echo Javadoc generation complete. Documentation available at: %OUTPUT_DIR%\index.html
endlocal
