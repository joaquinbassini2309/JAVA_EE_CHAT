# ========================================================
# Script de Actualización Rápida (Hot Redeploy) para Windows
# Compila, empaqueta y redespliega sin reiniciar WildFly
# ========================================================

param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot/..").Path,
    [string]$WildFlyDir = $env:WILDFLY_HOME
)

# Colores de consola
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

$ErrorActionPreference = "Stop"

# Buscar WildFly si no está en la variable de entorno
if (-not $WildFlyDir) {
    $commonPaths = @(
        "$ProjectRoot/wildfly",
        "C:\wildfly-32.0.1.Final",
        "C:\wildfly",
        "$HOME/wildfly"
    )
    foreach ($path in $commonPaths) {
        if (Test-Path $path) { $WildFlyDir = $path; break }
    }
}

if (-not $WildFlyDir -or -not (Test-Path $WildFlyDir)) {
    Write-Error-Custom "[!] No se pudo encontrar el directorio de WildFly. Por favor, define la variable de entorno WILDFLY_HOME."
    exit 1
}

$wildFlyDeploy = Join-Path $WildFlyDir "standalone\deployments"
$warFile = Join-Path (Resolve-Path "$ProjectRoot/CapaServicio/target").Path "chat-empresarial.war" 2>$null
if (-not $warFile) {
    $warFile = Join-Path $ProjectRoot "CapaServicio\target\chat-empresarial.war"
}
$frontendDir = Join-Path $ProjectRoot "CapaPresentacion\Web"
$backendDir = Join-Path $ProjectRoot "CapaServicio"

Write-Info ">>> Iniciando actualización rápida (Hot Redeploy)..."

try {
    # 1. Compilar Frontend
    Write-Info "1. Compilando Frontend (Vite)..."
    Push-Location $frontendDir
    $env:VITE_CONTEXT_PATH = "/chat-empresarial/"
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "La compilación del frontend falló" }
    Pop-Location

    # 2. Copiar frontend a backend webapp
    Write-Info "2. Copiando frontend al directorio webapp del backend..."
    $distDir = Join-Path $frontendDir "dist"
    $webappDir = Join-Path (Join-Path $backendDir "src\main") "webapp"
    if (Test-Path $distDir) {
        if (Test-Path $webappDir) {
            Get-ChildItem -Path $webappDir -Exclude "WEB-INF" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        }
        Copy-Item -Path "$distDir\*" -Destination $webappDir -Recurse -Force
    }

    # 3. Compilar y empaquetar backend con Maven
    Write-Info "3. Compilando y empaquetando proyecto (.war) con Maven..."
    Push-Location $ProjectRoot
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "La compilación con Maven falló" }
    Pop-Location

    # 4. Redesplegar en WildFly
    Write-Info "4. Copiando nuevo .war a WildFly y solicitando redespliegue..."
    if (Test-Path $wildFlyDeploy) {
        # Copiar el war (esto desencadena el undeploy/deploy automático en WildFly)
        Copy-Item $warFile -Destination $wildFlyDeploy -Force
        
        # Crear archivo .dodeploy para forzar a WildFly a desplegar de nuevo
        $dodeployFile = Join-Path $wildFlyDeploy "chat-empresarial.war.dodeploy"
        New-Item -Path $dodeployFile -ItemType File -Force | Out-Null
        
        Write-Success ">>> [OK] ¡Redespliegue solicitado con éxito en WildFly!"
        Write-Warning-Custom "WildFly tardará unos segundos en cargar los cambios. Verifica la consola de WildFly para ver el progreso."
    } else {
        throw "No se encontró el directorio de deployments en $wildFlyDeploy"
    }
}
catch {
    Write-Error-Custom "[!] Error en la actualización: $($_.Exception.Message)"
    try { Pop-Location } catch {}
    exit 1
}
