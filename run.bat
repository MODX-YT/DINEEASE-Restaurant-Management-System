@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
echo Compiling DINEEASE (Java 8 compatible)...
javac -encoding UTF-8 -d out src\dineease\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Check errors above.
    pause
    exit /b 1
)
echo.
echo Starting DINEEASE...
java -cp out dineease.Main
pause
endlocal
