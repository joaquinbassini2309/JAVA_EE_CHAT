# ========================================
# Script de Limpieza - Desinstalar WildFly
# ========================================
# Elimina WildFly, caché de Maven, etc.

param(
    [string]$WildFlyDir = "C:\wildfly-32.0.1.Final",
    [string]$ProjectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
)

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

Write-Info "=========================================="
Write-Warning-Custom "    LIMPIEZA DE ARCHIVOS - ADVERTENCIA"
Write-Info "=========================================="
Write-Warning-Custom ""
Write-Warning-Custom "Este script eliminara:"
Write-Warning-Custom "  - Directorio WildFly completo"
Write-Warning-Custom "  - Cache de Maven (opcional)"
Write-Warning-Custom "  - Archivos compilados (target/)"

Write-Info ""
Write-Info "Estás seguro? Los cambios no se pueden deshacer."
$confirm = Read-Host "Escribe 'ELIMINAR' para continuar (o presiona Enter para cancelar)"

if ($confirm -ne "ELIMINAR") {
    Write-Info "Operacion cancelada"
    exit 0
}

# ========================================
# VERIFICAR Y DETENER WILDFLY
# ========================================
Write-Info ""
Write-Info "1. Deteniendo WildFly si esta corriendo..."

$javaProcess = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" }
if ($javaProcess) {
    Write-Info "   Deteniendo proceso Java..."
    Stop-Process -Id $javaProcess.Id -Force
    Start-Sleep -Seconds 2
    Write-Success "   [OK] WildFly detenido"
} else {
    Write-Info "   WildFly no esta corriendo"
}

# ========================================
# ELIMINAR WILDFLY
# ========================================
Write-Info ""
Write-Info "2. Eliminando directorio WildFly..."

if (Test-Path $WildFlyDir) {
    try {
        Remove-Item -Recurse -Force $WildFlyDir
        Write-Success "   [OK] WildFly eliminado: $WildFlyDir"
    } catch {
        Write-Error-Custom "   [!] Error al eliminar WildFly: $_"
    }
} else {
    Write-Info "   WildFly no esta instalado en $WildFlyDir"
}

# ========================================
# LIMPIAR PROYECTO
# ========================================
Write-Info ""
Write-Info "3. Limpiando proyecto Maven..."

if (Test-Path "$ProjectRoot\target") {
    try {
        Remove-Item -Recurse -Force "$ProjectRoot\target"
        Write-Success "   [OK] Directorio target/ eliminado"
    } catch {
        Write-Error-Custom "   [!] Error al limpiar target/: $_"
    }
}

# ========================================
# LIMPIAR MAVEN CACHE (OPCIONAL)
# ========================================
Write-Info ""
Write-Info "4. Eliminar cache de Maven tambien?"
$cleanMaven = Read-Host "Escribe 'si' para eliminar ~/.m2/repository (o presiona Enter para saltar)"

if ($cleanMaven -eq "si") {
    $m2Dir = "$env:USERPROFILE\.m2\repository"
    if (Test-Path $m2Dir) {
        Write-Info "   Eliminando cache de Maven (esto puede tomar un rato)..."
        try {
            Remove-Item -Recurse -Force $m2Dir
            Write-Success "   [OK] Cache de Maven eliminado"
        } catch {
            Write-Error-Custom "   [!] Error al limpiar cache: $_"
        }
    }
} else {
    Write-Info "   Cache de Maven no eliminado"
}

# ========================================
# RESUMEN FINAL
# ========================================
Write-Info ""
Write-Info "=========================================="
Write-Success "[OK] LIMPIEZA COMPLETADA"
Write-Info "=========================================="
Write-Info ""
Write-Info "Archivos eliminados:"
Write-Info "  [OK] Directorio WildFly"
Write-Info "  [OK] Archivos compilados (target/)"

Write-Warning-Custom ""
Write-Warning-Custom "NOTAS:"
Write-Warning-Custom "  - Base de datos PostgreSQL NO fue eliminada"
Write-Warning-Custom "  - Codigo fuente NO fue modificado"
Write-Warning-Custom "  - Configuracion local (pom.xml, persistence.xml) se mantiene"

Write-Info ""
Write-Info "Para reinstalar:"
Write-Info "  .\Scripts\setup-wildfly.ps1"

Write-Success ""
Write-Success "¡Limpieza finalizada!"
