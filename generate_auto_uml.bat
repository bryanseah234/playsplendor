@echo off
setlocal
cd /d "%~dp0"

bash generate_auto_uml.sh
exit /b %ERRORLEVEL%
