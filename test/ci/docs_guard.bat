@echo off
setlocal
cd /d "%~dp0\..\.."

bash test/ci/docs_guard.sh
exit /b %ERRORLEVEL%
