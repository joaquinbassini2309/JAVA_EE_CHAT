@echo off
REM ========================================
REM Script de Limpieza - Desinstalar todo
REM ========================================

echo.
echo =========================================
echo   LIMPIEZA - ADVERTENCIA
echo =========================================
echo.
echo Este script va a eliminar:
echo   - Directorio WildFly completo
echo   - Archivos compilados
echo   - Opcionalmente: caché de Maven
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0cleanup.ps1"

echo.
pause
