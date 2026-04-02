@echo off
setlocal
cd /d "%~dp0\..\.."

node render_diagrams.js
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

call test\ci\generate_javadoc.bat
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

node test/ci/verify_javadoc_index.js
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

call test\ci\docs_guard.bat
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [docs_pipeline] OK
