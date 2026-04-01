@echo off
setlocal enabledelayedexpansion

echo 🔧 Splendor environment requirements check (Windows)
echo.

set "JUNIT_JAR=lib\junit-platform-console-standalone-1.10.2.jar"
set "JUNIT_URL=https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
set "PLANTUML_JAR=docs\diagrams\plantuml.jar"
set "PLANTUML_URL=https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar"

set MISSING=0

call :check java "Java runtime"
call :check javac "Java compiler"
call :check javadoc "Javadoc tool"
call :check node "Node.js"
call :check npm "npm"
call :check python "Python"
call :check pip "pip"
call :check dot "Graphviz dot (for PlantUML PNG rendering)"

if exist "node_modules\.bin\mmdc.cmd" (
  echo ✅ Mermaid CLI ^(local^): node_modules\.bin\mmdc.cmd
) else (
  echo ℹ️ Installing Node.js dependencies ^(Mermaid CLI^)...
  call npm install
  if errorlevel 1 (
    echo ❌ npm install failed
    set MISSING=1
  )
)

if exist "%JUNIT_JAR%" (
  echo ✅ JUnit console jar: %JUNIT_JAR%
) else (
  echo ℹ️ Downloading JUnit console jar...
  if not exist "lib" mkdir lib
  call :download "%JUNIT_URL%" "%JUNIT_JAR%" "JUnit console jar"
)

if exist "%PLANTUML_JAR%" (
  echo ✅ PlantUML jar: %PLANTUML_JAR%
) else (
  echo ℹ️ Downloading PlantUML jar...
  if not exist "docs\diagrams" mkdir docs\diagrams
  call :download "%PLANTUML_URL%" "%PLANTUML_JAR%" "PlantUML jar"
)

if not exist "%JUNIT_JAR%" (
  echo ❌ JUnit console jar: missing at %JUNIT_JAR%
  set MISSING=1
)

if not exist "%PLANTUML_JAR%" (
  echo ❌ PlantUML jar: missing at %PLANTUML_JAR%
  set MISSING=1
)

echo.
if "%MISSING%"=="0" (
  echo 🎉 All required tooling is present.
  exit /b 0
)

echo Some dependencies are missing.
echo Install guidance:
echo   - winget: winget install EclipseAdoptium.Temurin.17.JDK OpenJS.NodeJS Python.Python.3 Graphviz.Graphviz
echo   - choco : choco install temurin17 nodejs python graphviz
echo   - Re-run setup_requirements.bat after installing system packages
exit /b 1

:check
where %~1 >nul 2>nul
if errorlevel 1 (
  echo ❌ %~2: missing
  set MISSING=1
) else (
  for /f "delims=" %%i in ('where %~1') do (
    echo ✅ %~2: %%i
    goto :eof
  )
)
goto :eof

:download
set "URL=%~1"
set "DEST=%~2"
set "LABEL=%~3"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "try { Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%DEST%'; exit 0 } catch { exit 1 }"
if errorlevel 1 (
  echo ❌ %LABEL%: download failed
  set MISSING=1
) else (
  echo ✅ %LABEL%: %DEST%
)
goto :eof
