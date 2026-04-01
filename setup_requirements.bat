@echo off
setlocal enabledelayedexpansion

echo 🔧 Splendor environment requirements check (Windows)
echo.

set MISSING=0

call :check java "Java runtime"
call :check javac "Java compiler"
call :check javadoc "Javadoc tool"
call :check node "Node.js"
call :check npm "npm"
call :check python "Python"
call :check pip "pip"
call :check dot "Graphviz dot (for PlantUML PNG rendering)"

if exist "docs\diagrams\plantuml.jar" (
  echo ✅ PlantUML jar: docs\diagrams\plantuml.jar
) else (
  echo ❌ PlantUML jar: missing at docs\diagrams\plantuml.jar
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
echo   - PlantUML jar: place plantuml.jar under docs\diagrams\
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
