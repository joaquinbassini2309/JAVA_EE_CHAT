# ========================================
# Arranque completo de entorno (Windows)
# Adaptado de run.sh, con Docker
# ========================================

param(
    [string]$ProjectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT",
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
$dockerName = "postgres-chat"

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

# Cargar variables desde .env
$envFile = Join-Path $ProjectRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | Where-Object { $_ -match '^\s*([^#=]+)=(.*)$' } | ForEach-Object {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        if ($name -eq 'DB_URL' -and $value -match '://db:') {
            $value = $value -replace '://db:\d+', '://localhost:5433'
            $value = $value -replace '://db:', '://localhost:5433/'
        }
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
    Write-Success "[OK] Variables de entorno (.env) cargadas para WildFly."
}

Write-Info "Limpiando puertos 8080 y 9990..."
Stop-PortProcess -Port 8080
Stop-PortProcess -Port 9990
Start-Sleep -Seconds 1

try {
    Push-Location $frontendDir
    Invoke-Step "Compilando Frontend con npm run build..." {
        $env:VITE_CONTEXT_PATH = "/chat-empresarial"
        npm install
        if ($LASTEXITCODE -ne 0) { throw "npm install fallo" }
        npm run build
        if ($LASTEXITCODE -ne 0) { throw "npm run build fallo" }
    }
    Pop-Location

    Invoke-Step "Copiando frontend compilado a backend..." {
        $distDir = Join-Path $frontendDir "dist"
        $webappDir = Join-Path $backendDir "src\main\webapp"
        if (Test-Path $distDir) {
            Copy-Item -Path "$distDir\*" -Destination $webappDir -Recurse -Force
        } else {
            throw "No se encontro directorio dist"
        }
    }

    Push-Location $ProjectRoot
    Invoke-Step "Compilando proyecto con Maven..." {
        mvn clean package
    }
    if ($LASTEXITCODE -eq 0) {
        Write-Success "[OK] Compilación exitosa."
        # Limpiar y copiar el .war nuevo
        Invoke-Step "Copiando WAR a WildFly deployments..." {
            if (-not (Test-Path $wildFlyDeploy)) {
                throw "No existe carpeta deployments: $wildFlyDeploy"
            }

            Remove-Item "$wildFlyDeploy\*" -Force -Recurse -ErrorAction SilentlyContinue
            Copy-Item $warFile -Destination $wildFlyDeploy -Force
            New-Item -Path "$wildFlyDeploy\chat-empresarial.war.dodeploy" -ItemType File -Force | Out-Null
        }

        Invoke-Step "Configurando WildFly con PostgreSQL..." {
            $jarUrl = "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar"
            $tempJar = Join-Path $env:TEMP "postgresql-42.7.3.jar"
            Invoke-WebRequest -Uri $jarUrl -OutFile $tempJar -UseBasicParsing
            $setupCliContent = Get-Content "setup.cli" -Raw
            $tempJarUnix = $tempJar -replace "\\", "/"
            $setupCliContent = $setupCliContent -replace "/tmp/postgresql-42.7.3.jar", $tempJarUnix
            $tempCli = Join-Path $env:TEMP "setup_temp.cli"
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($tempCli, $setupCliContent, $utf8NoBom)
            $jbossCli = Join-Path $WildFlyDir "bin\jboss-cli.bat"
            & $jbossCli --file=$tempCli
            if ($LASTEXITCODE -ne 0) { throw "Configuración de WildFly fallo" }
        }
    } else {
        Write-Error-Custom "[!] Error en la compilación. Se intentará arrancar igual..."
    }
    Pop-Location

} catch {
    Write-Error-Custom "[!] Error durante build/deploy: $($_.Exception.Message)"
    try { Pop-Location } catch {}
    try { Pop-Location } catch {}
    exit 1
}

Write-Host ""
Write-Info "--- INICIANDO SERVICIOS ---"

Invoke-Step "Reiniciando contenedor Docker: $dockerName" {
    $containerId = docker ps -a --filter name=$dockerName -q 2>$null
    if (-not $containerId) {
        throw "El contenedor $dockerName no existe. Asegúrate de crearlo primero con: docker run -d --name $dockerName -e POSTGRES_DB=chatdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5433:5432 postgres:15-alpine"
    }
    docker stop $dockerName 2>$null >$null
    docker start $dockerName
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
Write-Info "--- CERRANDO SERVICIOS ---"

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

Invoke-Step "Deteniendo contenedor Docker: $dockerName" {
    docker stop $dockerName
}

Write-Host ""
Write-Success "Entorno cerrado."
