@echo off
title Online Food Ordering System
echo ========================================================
echo   Launching Online Food Ordering System (JavaFX)
echo ========================================================
echo.

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

if not exist "%~dp0target\online-food-ordering-system-1.0-SNAPSHOT.jar" (
    echo [ERROR] Application JAR not found!
    echo Please run 'mvn package' or build the project first.
    echo.
    pause
    exit /b 1
)

"%JAVA_EXE%" -jar "%~dp0target\online-food-ordering-system-1.0-SNAPSHOT.jar"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTICE] Application closed or exited with code %ERRORLEVEL%.
    pause
)
