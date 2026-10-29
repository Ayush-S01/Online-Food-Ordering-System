@echo off
title MySQL Database Setup - Online Food Ordering System
echo ========================================================
echo   Setting up MySQL Database: food_ordering_system
echo ========================================================
echo.
echo This script will execute schema.sql and sample-data.sql in MySQL.
echo Please ensure your MySQL server is running.
echo.
set /p MYSQL_USER="Enter MySQL Username [default: root]: "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo Executing schema.sql...
mysql -u %MYSQL_USER% -p < "%~dp0database\schema.sql"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to execute schema.sql. Please verify your password.
    pause
    exit /b 1
)

echo.
echo Executing sample-data.sql...
mysql -u %MYSQL_USER% -p < "%~dp0database\sample-data.sql"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to execute sample-data.sql.
    pause
    exit /b 1
)

echo.
echo ========================================================
echo [SUCCESS] Database and sample data initialized successfully!
echo ========================================================
pause
