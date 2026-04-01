@echo off
REM ==============================================================================
REM Splendor Javadoc Generator (Windows)
REM ==============================================================================
REM This batch script generates comprehensive HTML API documentation from Java
REM source files using the native javadoc tool. It provides Windows-compatible
REM documentation generation with the same settings as the Unix version.
REM
REM Usage:
REM   generate_docs.bat [--clean]
REM
REM Options:
REM   --clean    Remove existing documentation before generating
REM
REM Requirements:
REM   - Java Development Kit (JDK) 11 or higher
REM   - Java binaries must be in system PATH
REM
REM Exit Codes:
REM   0 - Success
REM   1 - Javadoc generation failed
REM   2 - Invalid arguments
REM ==============================================================================

setlocal enabledelayedexpansion

REM Configuration
set "SOURCE_DIR=src"
set "OUTPUT_DIR=docs\javadoc"
set "PACKAGE=com.splendor"
set "SCRIPT_NAME=%~nx0"

REM Flags
set "CLEAN_BUILD=false"

REM ------------------------------------------------------------------------------
REM Utility Functions
REM ------------------------------------------------------------------------------

:print_info
echo [INFO] %~1
goto :eof

:print_success
echo [SUCCESS] %~1
goto :eof

:print_warning
echo [WARNING] %~1
goto :eof

:print_error
echo [ERROR] %~1 1>&2
goto :eof

:usage
echo.
echo Usage: %SCRIPT_NAME% [OPTIONS]
echo.
echo Generate Javadoc documentation for the Splendor project.
echo.
echo Options:
echo     --clean     Remove existing documentation before generating
echo     --help      Display this help message
echo.
echo Examples:
echo     %SCRIPT_NAME%                  REM Generate documentation
echo     %SCRIPT_NAME% --clean          REM Clean build
echo.
exit /b 0

:check_requirements
where javadoc >nul 2>&1
if errorlevel 1 (
    call :print_error "javadoc command not found. Please install JDK 11 or higher."
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    call :print_error "java command not found. Please install JDK 11 or higher."
    exit /b 1
)

if not exist "%SOURCE_DIR%" (
    call :print_error "Source directory '%SOURCE_DIR%' not found. Run this script from project root."
    exit /b 1
)
goto :eof

:clean_docs
if exist "%OUTPUT_DIR%" (
    call :print_info "Removing existing documentation..."
    rmdir /s /q "%OUTPUT_DIR%"
    call :print_success "Clean complete."
)
goto :eof

:generate_javadoc
call :print_info "Generating Javadoc for Splendor project..."
call :print_info "Source: %SOURCE_DIR%"
call :print_info "Output: %OUTPUT_DIR%"

REM Create output directory if it doesn't exist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Get current timestamp
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime ^| find "."') do set "dt=%%a"
set "TIMESTAMP=%date% %time%"

REM Run javadoc with comprehensive settings
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
    -header "^<b^>Splendor v1.0^</b^>" ^
    -footer "Generated on %TIMESTAMP%" ^
    -link https://docs.oracle.com/en/java/javase/17/docs/api/ ^
    -Xdoclint:all ^
    -Xdoclint:-missing ^
    -quiet

if errorlevel 1 (
    call :print_error "Javadoc generation failed with exit code %errorlevel%"
    exit /b 1
)

call :print_success "Javadoc generation complete!"
call :print_info "Documentation available at: %OUTPUT_DIR%\index.html"
goto :eof

REM ------------------------------------------------------------------------------
REM Main Execution
REM ------------------------------------------------------------------------------

:main
REM Parse arguments
:parse_args
if "%~1"=="" goto :after_parse
if /i "%~1"=="--clean" (
    set "CLEAN_BUILD=true"
    shift
    goto :parse_args
)
if /i "%~1"=="--help" (
    call :usage
)
call :print_error "Unknown option: %~1"
call :usage
exit /b 2

:after_parse
call :check_requirements

if "%CLEAN_BUILD%"=="true" (
    call :clean_docs
)

call :generate_javadoc
exit /b 0

REM Run main function
call :main
