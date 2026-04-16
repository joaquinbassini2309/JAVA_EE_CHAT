@echo off
REM ========================================
REM Utilidades de WildFly - Menu Principal
REM ========================================

setlocal enabledelayedexpansion

powershell -ExecutionPolicy Bypass -NoExit -File "%~dp0manage-wildfly.ps1" -Action "menu"
