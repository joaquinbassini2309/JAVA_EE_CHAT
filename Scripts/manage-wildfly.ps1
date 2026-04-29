# ========================================
# Utilidades para Gestionar WildFly
# ========================================
# Script con funciones útiles para
# monitorear y controlar WildFly

param(
    [string]$Action = "menu",
    [string]$WildFlyDir = "C:\wildfly-32.0.1.Final"
)

# --- Variables Globales ---
$wildflyCLI = "$WildFlyDir\bin\jboss-cli.bat"

# --- Funciones de Utilidad ---
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

# ========================================
# FUNCIÓN: Verificar Estado de WildFly (Robusta)
# ========================================
function Get-WildFlyStatus {
    try {
        $cliCheck = & $wildflyCLI --connect --command=":read-attribute(name=server-state)" --timeout=5000 2>$null
        if ($cliCheck -match '\"result\" => \"running\"') {
            return "running"
        } elseif ($cliCheck -match '\"result\" => \"starting\"') {
            return "starting"
        }
    } catch {
        # El comando falla si no puede conectar, lo que significa que está detenido.
    }
    return "stopped"
}

function Check-WildFlyStatus {
    Write-Info "Verificando estado de WildFly..."
    $status = Get-WildFlyStatus

    if ($status -eq "running") {
        Write-Success "[OK] WildFly esta CORRIENDO"
        Write-Info "   Admin Console: http://localhost:9990"
        
        # Verificar si la aplicación está desplegada
        try {
            $appResponse = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/InicioSesion.html" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($appResponse.StatusCode -eq 200) {
                Write-Success "[OK] Aplicacion DESPLEGADA en http://localhost:8080/chat-empresarial/InicioSesion.html"
            } else {
                Write-Warning-Custom "[!] La aplicación respondió, pero no con éxito (Código: $($appResponse.StatusCode))"
            }
        } catch {
            Write-Warning-Custom "[!] La aplicación NO responde. Puede que no esté desplegada o esté fallando."
        }
    } elseif ($status -eq "starting") {
        Write-Warning-Custom "[!] WildFly se está INICIANDO..."
    } else {
        Write-Error-Custom "[!] WildFly esta DETENIDO"
        Write-Info "   Puedes iniciarlo con la opción 2 del menú."
    }
}

# ========================================
# FUNCIÓN: Iniciar WildFly (Robusta)
# ========================================
function Start-WildFly {
    Write-Info "Iniciando WildFly..."
    
    $status = Get-WildFlyStatus
    if ($status -eq "running" -or $status -eq "starting") {
        Write-Warning-Custom "[!] WildFly ya se está ejecutando o iniciando."
        return
    }
    
    $wildflyStandalone = "$WildFlyDir\bin\standalone.bat"
    if (-not (Test-Path $wildflyStandalone)) {
        Write-Error-Custom "[!] standalone.bat no encontrado en $WildFlyDir"
        return
    }
    
    Write-Info "Iniciando en segundo plano..."
    Start-Process -FilePath $wildflyStandalone -WindowStyle Hidden
    
    Write-Info "Esperando a que WildFly esté completamente listo (hasta 60s)..."
    $maxAttempts = 60
    $attempt = 0
    $ready = $false
    while ($attempt -lt $maxAttempts -and -not $ready) {
        $currentStatus = Get-WildFlyStatus
        if ($currentStatus -eq "running") {
            $ready = $true
        } else {
            $attempt++
            Write-Host "." -NoNewline
            Start-Sleep -Seconds 1
        }
    }

    if ($ready) {
        Write-Success ""
        Write-Success "[OK] WildFly iniciado y listo para recibir comandos."
    } else {
        Write-Error-Custom ""
        Write-Error-Custom "[!] WildFly no terminó de iniciarse después de 60 segundos. Revisa los logs."
    }
}

# ========================================
# FUNCIÓN: Detener WildFly (Graceful)
# ========================================
function Stop-WildFly {
    Write-Info "Intentando apagar WildFly de forma segura (graceful shutdown)..."
    
    $status = Get-WildFlyStatus
    if ($status -eq "stopped") {
        Write-Info "WildFly ya está detenido."
        return
    }

    try {
        & $wildflyCLI --connect --command=":shutdown" 2>$null
        Write-Info "Comando de apagado enviado. Esperando hasta 20s..."

        $maxAttempts = 20
        $attempt = 0
        $stopped = $false
        while ($attempt -lt $maxAttempts -and -not $stopped) {
            $currentStatus = Get-WildFlyStatus
            if ($currentStatus -eq "stopped") {
                $stopped = $true
            } else {
                $attempt++
                Start-Sleep -Seconds 1
            }
        }

        if ($stopped) {
            Write-Success "[OK] WildFly detenido correctamente."
        } else {
            Write-Warning-Custom "[!] WildFly no se detuvo de forma segura. Forzando el cierre del proceso..."
            Force-Stop-WildFly
        }
    } catch {
        Write-Warning-Custom "[!] No se pudo conectar para el apagado seguro. Forzando el cierre del proceso..."
        Force-Stop-WildFly
    }
}

function Force-Stop-WildFly {
    $process = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "$WildFlyDir*" }
    if ($process) {
        $pid = $process.Id
        Stop-Process -Id $pid -Force
        Write-Success "[OK] Proceso de WildFly (PID: $pid) forzado a detenerse."
    } else {
        Write-Warning-Custom "No se encontró ningún proceso de WildFly para forzar la detención."
    }
}

# ========================================
# FUNCIÓN: Acceder a CLI de WildFly
# ========================================
function Open-WildFlyCLI {
    Write-Info "Abriendo WildFly CLI..."
    
    if (-not (Test-Path $wildflyCLI)) {
        Write-Error-Custom "[!] jboss-cli.bat no encontrado"
        return
    }
    
    if ((Get-WildFlyStatus) -ne "running") {
        Write-Warning-Custom "WildFly no está corriendo. El CLI podría no conectar."
    }

    Start-Process cmd -ArgumentList "/k `"$wildflyCLI`" --connect"
}

# ========================================
# FUNCIÓN: Ver Logs en Tiempo Real
# ========================================
function Watch-Logs {
    Write-Info "Mostrando logs en tiempo real..."
    Write-Info "(Presiona Ctrl+C para detener)"
    
    $logFile = "$WildFlyDir\standalone\log\server.log"
    if (-not (Test-Path $logFile)) {
        Write-Error-Custom "[!] Archivo de log no encontrado: $logFile"
        return
    }
    
    Get-Content -Path $logFile -Wait -Tail 50
}

# ========================================
# FUNCIÓN: Redeploy de la Aplicación
# ========================================
function Redeploy-App {
    Write-Info "Recompilando y desplegando la aplicación..."
    
    if ((Get-WildFlyStatus) -ne "running") {
        Write-Error-Custom "[!] WildFly no está corriendo. Por favor, inícialo primero (Opción 2)."
        return
    }
    
    $projectRoot = "C:\Users\Curbe\IdeaProjects\JAVA_EE_CHAT"
    if (-not (Test-Path "$projectRoot\pom.xml")) {
        Write-Error-Custom "[!] pom.xml no encontrado en $projectRoot"
        return
    }
    
    Push-Location $projectRoot
    
    Write-Info "Compilando con Maven..."
    & mvn clean install 2>&1 | Tee-Object -Variable mavenOutput
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "[!] Error en la compilación de Maven."
        Pop-Location
        return
    }
    
    Write-Success "[OK] Compilación exitosa."
    
    $warFile = "$projectRoot\CapaServicio\target\chat-empresarial.war"
    $deploymentsDir = "$WildFlyDir\standalone\deployments"
    
    if (Test-Path $warFile) {
        Write-Info "Desplegando WAR..."
        Copy-Item $warFile -Destination $deploymentsDir -Force
        Write-Success "[OK] WAR copiado a la carpeta de despliegue."
        Write-Info "WildFly detectará el cambio y redesplegará la aplicación automáticamente."
        Write-Info "Revisa los logs (Opción 5) para ver el progreso."
    } else {
        Write-Error-Custom "[!] El archivo .war no se encontró después de la compilación."
    }
    
    Pop-Location
}

# ========================================
# MENÚ INTERACTIVO
# ========================================
function Show-Menu {
    while ($true) {
        Write-Info ""
        Write-Info "=================================="
        Write-Info "     GESTOR DE WILDFLY"
        Write-Info "=================================="
        Write-Host "1. Ver estado de WildFly"
        Write-Host "2. Iniciar WildFly (seguro)"
        Write-Host "3. Detener WildFly (seguro)"
        Write-Host "4. Abrir CLI de WildFly"
        Write-Host "5. Ver logs en tiempo real"
        Write-Host "6. Recompilar y Desplegar App"
        Write-Host "7. Salir"
        Write-Host ""
        
        $choice = Read-Host "Selecciona una opción (1-7)"
        
        switch ($choice) {
            "1" { Check-WildFlyStatus; Read-Host "Presiona una tecla para continuar..." }
            "2" { Start-WildFly; Read-Host "Presiona una tecla para continuar..." }
            "3" { Stop-WildFly; Read-Host "Presiona una tecla para continuar..." }
            "4" { Open-WildFlyCLI }
            "5" { Watch-Logs }
            "6" { Redeploy-App; Read-Host "Presiona una tecla para continuar..." }
            "7" {
                Write-Success "Saliendo..."
                exit 0
            }
            default { Write-Error-Custom "Opción inválida" }
        }
    }
}

# ========================================
# EJECUTAR ACCIÓN
# ========================================
if ($Action -eq "menu") {
    Show-Menu
} else {
    switch ($Action.ToLower()) {
        "status"   { Check-WildFlyStatus }
        "start"    { Start-WildFly }
        "stop"     { Stop-WildFly }
        "cli"      { Open-WildFlyCLI }
        "logs"     { Watch-Logs }
        "redeploy" { Redeploy-App }
        default {
            Write-Error-Custom "Acción desconocida: $Action"
            Write-Info "Usos válidos: status, start, stop, cli, logs, redeploy"
        }
    }
}
