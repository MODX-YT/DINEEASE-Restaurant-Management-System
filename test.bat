@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
echo Compiling DINEEASE...
javac -encoding UTF-8 -d out src\dineease\*.java
if errorlevel 1 goto :error

echo.
echo === Test 1: OOP and core logic ===
java -cp out dineease.DINEEASETest
if errorlevel 1 goto :error

echo.
echo === Test 2: Console integration ===
java -Ddineease.testMode=true -cp out dineease.DINEEASEIntegrationTest
if errorlevel 1 goto :error

echo.
echo === Test 3: Isolated data persistence ===
java -cp out dineease.DINEEASEPersistenceTest
if errorlevel 1 goto :error

echo.
echo ALL 51 TESTS PASSED!
pause
exit /b 0

:error
echo.
echo ONE OR MORE TESTS FAILED.
pause
exit /b 1
