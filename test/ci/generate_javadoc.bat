@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0\..\.."

where javadoc >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
	echo [generate_javadoc] javadoc not found. Install a JDK and ensure javadoc is in PATH.
	exit /b 1
)

if exist docs\javadoc rmdir /s /q docs\javadoc
mkdir docs\javadoc

set "SRC_LIST=%TEMP%\splendor_javadoc_sources_%RANDOM%%RANDOM%.txt"
break > "%SRC_LIST%"

for /r src %%f in (*.java) do (
	echo %%f>> "%SRC_LIST%"
)

for %%I in ("%SRC_LIST%") do set "SRC_SIZE=%%~zI"
if "%SRC_SIZE%"=="0" (
	echo [generate_javadoc] No Java sources found under src\
	del "%SRC_LIST%" >nul 2>&1
	exit /b 1
)

javadoc -d docs/javadoc -sourcepath src @"%SRC_LIST%"
set "RC=%ERRORLEVEL%"
del "%SRC_LIST%" >nul 2>&1

if %RC% NEQ 0 (
	echo [generate_javadoc] Javadoc generation failed.
	exit /b %RC%
)

echo Generated Javadoc at docs/javadoc/index.html
exit /b 0
