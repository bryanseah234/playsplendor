@echo off
setlocal
cd /d "%~dp0\..\.."

node test/ci/verify_javadoc_index.js
exit /b %ERRORLEVEL%
