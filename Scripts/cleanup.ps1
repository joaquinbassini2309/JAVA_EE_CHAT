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
Write-Warning-Custom "`nEste script eliminará:"
Write-Warning-Custom "  • Directorio WildFly completo"
Write-Warning-Custom "  • Caché de Maven (opcional)"
Write-Warning-Custom "  • Archivos compilados (target/)"

Write-Info "`n¿Estás seguro? Los cambios no se pueden deshacer."
$confirm = Read-Host "Escribe 'ELIMINAR' para continuar (o presiona Enter para cancelar)"

if ($confirm -ne "ELIMINAR") {
    Write-Info "Operación cancelada"
    exit 0
}

# ========================================
# VERIFICAR Y DETENER WILDFLY
# ========================================
Write-Info "`n1. Deteniendo WildFly si está corriendo..."

$javaProcess = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" }
if ($javaProcess) {
    Write-Info "   Deteniendo proceso Java..."
    Stop-Process -Id $javaProcess.Id -Force
    Start-Sleep -Seconds 2
    Write-Success "   ✓ WildFly detenido"
} else {
    Write-Info "   WildFly no está corriendo"
}

# ========================================
# ELIMINAR WILDFLY
# ========================================
Write-Info "`n2. Eliminando directorio WildFly..."

if (Test-Path $WildFlyDir) {
    try {
        Remove-Item -Recurse -Force $WildFlyDir
        Write-Success "   ✓ WildFly eliminado: $WildFlyDir"
    } catch {
        Write-Error-Custom "   ✗ Error al eliminar WildFly: $_"
    }
} else {
    Write-Info "   WildFly no está instalado en $WildFlyDir"
}

# ========================================
# LIMPIAR PROYECTO
# ========================================
Write-Info "`n3. Limpiando proyecto Maven..."

if (Test-Path "$ProjectRoot\target") {
    try {
        Remove-Item -Recurse -Force "$ProjectRoot\target"
        Write-Success "   ✓ Directorio target/ eliminado"
    } catch {
        Write-Error-Custom "   ✗ Error al limpiar target/: $_"
    }
}

# ========================================
# LIMPIAR MAVEN CACHE (OPCIONAL)
# ========================================
Write-Info "`n4. ¿Eliminar caché de Maven también?"
$cleanMaven = Read-Host "Escribe 'sí' para eliminar ~/.m2/repository (o presiona Enter para saltar)"

if ($cleanMaven -eq "sí") {
    $m2Dir = "$env:USERPROFILE\.m2\repository"
    if (Test-Path $m2Dir) {
        Write-Info "   Eliminando caché de Maven (esto puede tomar un rato)..."
        try {
            Remove-Item -Recurse -Force $m2Dir
            Write-Success "   ✓ Caché de Maven eliminado"
        } catch {
            Write-Error-Custom "   ✗ Error al limpiar caché: $_"
        }
    }
} else {
    Write-Info "   Caché de Maven no eliminado"
}

# ========================================
# RESUMEN FINAL
# ========================================
Write-Info "`n=========================================="
Write-Success "✓ LIMPIEZA COMPLETADA"
Write-Info "=========================================="
Write-Info "`nArchivos eliminados:"
Write-Info "  ✓ Directorio WildFly"
Write-Info "  ✓ Archivos compilados (target/)"

Write-Warning-Custom "`n⚠ NOTAS:"
Write-Warning-Custom "  • Base de datos PostgreSQL NO fue eliminada"
Write-Warning-Custom "  • Código fuente NO fue modificado"
Write-Warning-Custom "  • Configuración local (pom.xml, persistence.xml) se mantiene"

Write-Info "`nPara reinstalar:"
Write-Info "  .\Scripts\run-setup.bat"

Write-Success "`n¡Limpieza finalizada!"
