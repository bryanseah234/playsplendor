@echo off
setlocal
cd /d "%~dp0\..\.."

python test/ci/verify_diagram_assets.py
exit /b %ERRORLEVEL%
