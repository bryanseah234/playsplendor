@echo off
echo Building Splendor Java Project...

if not exist classes mkdir classes

set LIB=lib
set CP=%LIB%\tui4j-0.3.3.jar;%LIB%\jline-terminal-jni-3.26.1.jar;%LIB%\jline-native-3.26.1.jar;%LIB%\jline-terminal-3.26.1.jar;%LIB%\icu4j-76.1.jar;%LIB%\commons-text-1.15.0.jar;%LIB%\commons-lang3-3.20.0.jar

echo Compiling Java sources...
javac -cp "%CP%" -d classes -sourcepath src ^
src/com/splendor/*.java ^
src/com/splendor/config/*.java ^
src/com/splendor/controller/*.java ^
src/com/splendor/exception/*.java ^
src/com/splendor/model/*.java ^
src/com/splendor/model/validator/*.java ^
src/com/splendor/network/*.java ^
src/com/splendor/util/*.java ^
src/com/splendor/view/*.java ^
src/com/splendor/view/tui/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

echo Build successful!
