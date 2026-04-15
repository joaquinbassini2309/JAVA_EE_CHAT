@echo off
REM ========================================
REM Script Launcher para Setup WildFly
REM ========================================

echo.
echo =========================================
echo   SETUP AUTOMATICO - WILDFLY + POSTGRE
echo =========================================
echo.

REM Verificar que PowerShell está disponible
powershell -Command "Write-Host 'PowerShell disponible' " >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: PowerShell no se encuentra en el sistema
    pause
    exit /b 1
)
C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT\Scripts\run-setup.bat
REM Solicitar contraseña de PostgreSQL
set /p POSTGRES_PASS="Ingresa contraseña de PostgreSQL (default: postgres): "
if "%POSTGRES_PASS%"=="" set POSTGRES_PASS=postgres

echo.
echo Iniciando configuracion automatica...
echo.

REM Ejecutar script PowerShell con contraseña
powershell -ExecutionPolicy Bypass -File "%~dp0setup-wildfly.ps1" -PostgresPassword "%POSTGRES_PASS%"

if %errorlevel% equ 0 (
    echo.
    echo =========================================
    echo   CONFIGURACION EXITOSA
    echo =========================================
    echo.
    echo Presiona una tecla para continuar...
    pause
) else (
    echo.
    echo =========================================
    echo   ERROR EN LA CONFIGURACION
    echo =========================================
    echo.
    echo Presiona una tecla para salir...
    pause
    exit /b 1
)
