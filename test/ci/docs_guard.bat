@echo off
setlocal
cd /d "%~dp0\..\.."

where bash >nul 2>&1
if %ERRORLEVEL% EQU 0 (
	bash test/ci/docs_guard.sh
	exit /b %ERRORLEVEL%
)

echo [docs_guard] bash not found; running Windows fallback checks

echo [docs_guard] Verifying Javadoc class index
node test/ci/verify_javadoc_index.js
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [docs_guard] Checking for inline mermaid blocks ^(should be externalized^)
powershell -NoProfile -Command " $matches = Get-ChildItem -Recurse -Filter *.md | Select-String -Pattern '```mermaid'; if ($matches) { Write-Host 'Inline mermaid blocks detected. Move source to docs/diagrams/mermaid/src and reference PNG paths.'; $matches | ForEach-Object { Write-Host (' - ' + $_.Path + ':' + $_.LineNumber) }; exit 3 } "
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [docs_guard] Verifying diagram source/output pairs
python test/ci/verify_diagram_assets.py
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [docs_guard] OK
exit /b 0
