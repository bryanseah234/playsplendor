@echo off
setlocal
cd /d "%~dp0\..\.."

bash test/ci/generate_javadoc.sh
exit /b %ERRORLEVEL%
