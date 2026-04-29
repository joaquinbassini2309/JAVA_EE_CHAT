# ========================================
# Arranque completo de entorno (Windows)
# Adaptado de run.sh, sin Docker
# ========================================

param(
    [string]$ProjectRoot = "C:\Users\Curbe\IdeaProjects\JAVA_EE_CHAT",
    [string]$WildFlyDir = "C:\wildfly-32.0.1.Final"
)

$ErrorActionPreference = "Stop"

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

function Stop-PortProcess {
    param([int]$Port)
    try {
        $pids = @()
        try {
            $pids = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop |
                Select-Object -ExpandProperty OwningProcess -Unique
        } catch {
            $pids = netstat -ano | Select-String ":$Port " | ForEach-Object {
                ($_ -split '\s+')[-1]
            } | Where-Object { $_ -match '^\d+$' } | Select-Object -Unique
        }
        if ($pids) {
            foreach ($pid in $pids) {
                try {
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                    Write-Warning-Custom "Puerto $Port liberado (PID $pid detenido)."
                } catch {
                    Write-Warning-Custom "No se pudo detener PID $pid para puerto $Port."
                }
            }
        }
    } catch {
        Write-Warning-Custom "No se pudo consultar el puerto $Port."
    }
}

function Invoke-Step {
    param(
        [string]$Title,
        [scriptblock]$Action
    )
    Write-Info $Title
    & $Action
    Write-Success "[OK] $Title"
}

$wildFlyBin = Join-Path $WildFlyDir "bin\standalone.bat"
$wildFlyDeploy = Join-Path $WildFlyDir "standalone\deployments"
$warFile = Join-Path $ProjectRoot "CapaServicio\target\chat-empresarial.war"
$frontendDir = Join-Path $ProjectRoot "CapaPresentacion\Web"
$backendDir = Join-Path $ProjectRoot "CapaServicio"

if (-not (Test-Path $wildFlyBin)) {
    Write-Error-Custom "[!] No se encontro WildFly en: $wildFlyBin"
    exit 1
}
if (-not (Test-Path (Join-Path $ProjectRoot "pom.xml"))) {
    Write-Error-Custom "[!] No se encontro pom.xml en: $ProjectRoot"
    exit 1
}
if (-not (Test-Path (Join-Path $frontendDir "package.json"))) {
    Write-Error-Custom "[!] No se encontro package.json en: $frontendDir"
    exit 1
}

Write-Info "--- PREPARANDO ENTORNO ---"

Write-Info "Limpiando puertos 8080 y 9990..."
Stop-PortProcess -Port 8080
Stop-PortProcess -Port 9990
Start-Sleep -Seconds 1

try {
    Push-Location $frontendDir
    Invoke-Step "Compilando Frontend con npm run build..." {
        npm install
        if ($LASTEXITCODE -ne 0) { throw "npm install fallo" }
        npm run build
        if ($LASTEXITCODE -ne 0) { throw "npm run build fallo" }
    }
    Pop-Location

    Push-Location $ProjectRoot
    Invoke-Step "Compilando proyecto con Maven..." {
        mvn clean package
        if ($LASTEXITCODE -ne 0) { throw "mvn clean package fallo" }
    }
    Pop-Location

    if (-not (Test-Path $warFile)) {
        throw "No se encontro WAR generado en $warFile"
    }

    Invoke-Step "Copiando WAR a WildFly deployments..." {
        if (-not (Test-Path $wildFlyDeploy)) {
            throw "No existe carpeta deployments: $wildFlyDeploy"
        }

        Remove-Item "$wildFlyDeploy\chat-empresarial.war*" -Force -Recurse -ErrorAction SilentlyContinue
        Copy-Item $warFile -Destination $wildFlyDeploy -Force
        New-Item -Path "$wildFlyDeploy\chat-empresarial.war.dodeploy" -ItemType File -Force | Out-Null
    }

} catch {
    Write-Error-Custom "[!] Error durante build/deploy: $($_.Exception.Message)"
    try { Pop-Location } catch {}
    try { Pop-Location } catch {}
    exit 1
}

Write-Host ""
Write-Info "--- INICIANDO WILDFLY ---"

$wfProcess = $null
try {
    $wfProcess = Start-Process -FilePath $wildFlyBin -ArgumentList "-b 0.0.0.0" -PassThru
    Write-Success "[OK] WildFly iniciado (PID: $($wfProcess.Id))"
} catch {
    Write-Error-Custom "[!] No se pudo iniciar WildFly: $($_.Exception.Message)"
    exit 1
}

Write-Host ""
Write-Info "======================================================================="
Write-Success "  >>> TODO EN MARCHA"
Write-Warning-Custom "  >>> PARA CERRAR TODO, PRESIONA [ENTER]"
Write-Info "======================================================================="
Write-Host ""
Write-Info "App: http://localhost:8080/chat-empresarial/"

[void](Read-Host -Prompt "Presiona ENTER para cerrar")

Write-Host ""
Write-Info "--- CERRANDO WILDFLY ---"

try {
    $jbossCli = Join-Path $WildFlyDir "bin\jboss-cli.bat"
    if (Test-Path $jbossCli) {
        & $jbossCli --connect --command=":shutdown" 2>$null | Out-Null
        Start-Sleep -Seconds 2
    }
} catch {
    Write-Warning-Custom "No se pudo apagar con CLI, se intentara forzar."
}

if ($wfProcess -and -not $wfProcess.HasExited) {
    try {
        Stop-Process -Id $wfProcess.Id -Force -ErrorAction SilentlyContinue
    } catch {}
}

Write-Success "Entorno cerrado."
