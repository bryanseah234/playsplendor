@echo off
setlocal
cd /d "%~dp0\..\.."

where mmdc >nul 2>&1
if %ERRORLEVEL% EQU 0 (
  node render_diagrams.js
  if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
) else (
  echo [docs_pipeline] mmdc not found; skipping diagram render and continuing with existing PNGs
)

call test\ci\generate_javadoc.bat
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

node test/ci/verify_javadoc_index.js
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

call test\ci\docs_guard.bat
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [docs_pipeline] OK
