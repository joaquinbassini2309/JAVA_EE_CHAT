# ========================================
# Utilidades para Gestionar WildFly
# ========================================
# Script con funciones útiles para
# monitorear y controlar WildFly

param(
    [string]$Action = "menu",
    [string]$WildFlyDir = "C:\wildfly-32.0.1.Final"
)

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

# ========================================
# FUNCIÓN: Verificar Estado de WildFly
# ========================================
function Check-WildFlyStatus {
    Write-Info "Verificando estado de WildFly..."
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9990/console" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        Write-Success "[OK] WildFly esta CORRIENDO"
        Write-Success "   Admin Console: http://localhost:9990/console"
        Write-Success "   Aplicacion: http://localhost:8080/chat-empresarial/"
        
        # Verificar si la aplicación está desplegada
        try {
            $appResponse = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            Write-Success "[OK] Aplicacion DESPLEGADA"
        } catch {
            Write-Warning-Custom "[!] Aplicacion no responde"
        }
    } catch {
        Write-Error-Custom "[!] WildFly NO esta corriendo"
        Write-Info "   Inicia con: & '$WildFlyDir\bin\standalone.bat'"
    }
}

# ========================================
# FUNCIÓN: Iniciar WildFly
# ========================================
function Start-WildFly {
    Write-Info "Iniciando WildFly..."
    
    $wildflyCli = "$WildFlyDir\bin\standalone.bat"
    if (-not (Test-Path $wildflyCli)) {
        Write-Error-Custom "[!] standalone.bat no encontrado en $WildFlyDir"
        return
    }
    
    # Verificar si ya está corriendo
    $process = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" }
    if ($process) {
        Write-Warning-Custom "[!] WildFly ya esta corriendo (PID: $($process.Id))"
        return
    }
    
    Write-Info "Iniciando en segundo plano con argumentos de bind..."
    $process = Start-Process -FilePath $wildflyCli -WindowStyle Hidden -PassThru `
        -ArgumentList "-Djboss.bind.address=0.0.0.0", `
                      "-Djboss.bind.address.management=0.0.0.0"
    Write-Success "[OK] WildFly iniciado (PID: $($process.Id))"
    Write-Info "Esperando a que este listo (hasta 60s)..."
    
    # Esperar con timeout mas largo
    $maxAttempts = 60
    $attempt = 0
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:9990/console" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            Write-Success "[OK] WildFly listo en http://localhost:9990/console"
            return
        } catch {
            $attempt++
            Write-Host "." -NoNewline
            Start-Sleep -Seconds 1
        }
    }
    Write-Warning-Custom ""
    Write-Warning-Custom "[!] WildFly puede no estar completamente listo despues de 60s"
    Write-Info "Verifica manualmente en: http://localhost:9990/console"
}

# ========================================
# FUNCIÓN: Detener WildFly
# ========================================
function Stop-WildFly {
    Write-Info "Deteniendo WildFly..."
    
    $process = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" }
    if ($process) {
        $pid = $process.Id
        Stop-Process -Id $pid -Force
        Start-Sleep -Seconds 2
        Write-Success "[OK] WildFly detenido (PID: $pid)"
    } else {
        Write-Info "WildFly no esta corriendo"
    }
}

# ========================================
# FUNCIÓN: Acceder a CLI de WildFly
# ========================================
function Open-WildFlyCLI {
    Write-Info "Abriendo WildFly CLI..."
    
    $cliPath = "$WildFlyDir\bin\jboss-cli.bat"
    if (-not (Test-Path $cliPath)) {
        Write-Error-Custom "[!] jboss-cli.bat no encontrado"
        return
    }
    
    Write-Info "Conectando a WildFly (intenta conectar con --connect)..."
    & $cliPath --connect
}

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

function Redeploy-App {
    Write-Info "Recompilando y desplegando aplicacion..."
    
    $projectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
    
    if (-not (Test-Path "$projectRoot\pom.xml")) {
        Write-Error-Custom "[!] pom.xml no encontrado en $projectRoot"
        return
    }
    
    Push-Location $projectRoot
    
    Write-Info "Compilando con Maven..."
    & mvn clean package 2>&1 | Tee-Object -Variable mavenOutput
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "[!] Error en compilacion"
        Pop-Location
        return
    }
    
    Write-Success "[OK] Compilacion exitosa"
    
    $warFile = "$projectRoot\target\chat-empresarial.war"
    $deploymentsDir = "$WildFlyDir\standalone\deployments"
    
    if (Test-Path $warFile) {
        Write-Info "Desplegando WAR..."
        Copy-Item $warFile -Destination $deploymentsDir -Force
        Write-Success "[OK] WAR desplegado"
        
        Write-Info "Esperando despliegue (hasta 20s)..."
        Start-Sleep -Seconds 3
        
        $maxAttempts = 20
        $attempt = 0
        while ($attempt -lt $maxAttempts) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
                Write-Success "[OK] Aplicacion disponible en http://localhost:8080/chat-empresarial/"
                Pop-Location
                return
            } catch {
                $attempt++
                Write-Host "." -NoNewline
                Start-Sleep -Seconds 1
            }
        }
        Write-Info "Despliegue en progreso, verifica logs..."
    }
    
    Pop-Location
}

function Test-API {
    Write-Info "Probando endpoints de la API..."
    
    $baseUrl = "http://localhost:8080/chat-empresarial/api/v1"
    
    Write-Info ""
    Write-Info "1. Probando login..."
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/usuarios/login" `
            -Method POST `
            -ContentType "application/json" `
            -Body '{"username":"admin","password":"admin123"}' `
            -ErrorAction SilentlyContinue
        
        Write-Success "[OK] Login endpoint respondio"
        Write-Info "   Respuesta: $($response | ConvertTo-Json)"
    } catch {
        Write-Warning-Custom "[!] Login endpoint no respondio"
    }
}

# ========================================
# MENÚ INTERACTIVO
# ========================================
function Show-Menu {
    while ($true) {
        Write-Info ""
        Write-Info "=================================="
        Write-Info "     UTILIDADES DE WILDFLY"
        Write-Info "=================================="
        Write-Host "1. Ver estado de WildFly"
        Write-Host "2. Iniciar WildFly"
        Write-Host "3. Detener WildFly"
        Write-Host "4. Abrir CLI de WildFly"
        Write-Host "5. Ver logs en tiempo real"
        Write-Host "6. Redeploy de aplicacion"
        Write-Host "7. Probar API"
        Write-Host "8. Salir"
        Write-Host ""
        
        $choice = Read-Host "Selecciona una opcion (1-8)"
        
        switch ($choice) {
            "1" { Check-WildFlyStatus; Read-Host "Presiona Enter para continuar" }
            "2" { Start-WildFly; Read-Host "Presiona Enter para continuar" }
            "3" { Stop-WildFly; Read-Host "Presiona Enter para continuar" }
            "4" { Open-WildFlyCLI }
            "5" { Watch-Logs }
            "6" { Redeploy-App; Read-Host "Presiona Enter para continuar" }
            "7" { Test-API; Read-Host "Presiona Enter para continuar" }
            "8" { 
                Write-Success "Saliendo..."
                exit 0
            }
            default { Write-Error-Custom "Opcion invalida" }
        }
    }
}

# ========================================
# EJECUTAR
# ========================================
if ($Action -eq "menu") {
    Show-Menu
} else {
    switch ($Action.ToLower()) {
        "status" { Check-WildFlyStatus }
        "start" { Start-WildFly }
        "stop" { Stop-WildFly }
        "cli" { Open-WildFlyCLI }
        "logs" { Watch-Logs }
        "redeploy" { Redeploy-App }
        "test" { Test-API }
        default {
            Write-Error-Custom "Accion desconocida: $Action"
            Write-Info "Usos validos:"
            Write-Info "  .\manage-wildfly.ps1 menu      - Menu interactivo"
            Write-Info "  .\manage-wildfly.ps1 status    - Ver estado"
            Write-Info "  .\manage-wildfly.ps1 start     - Iniciar"
            Write-Info "  .\manage-wildfly.ps1 stop      - Detener"
            Write-Info "  .\manage-wildfly.ps1 cli       - Abrir CLI"
            Write-Info "  .\manage-wildfly.ps1 logs      - Ver logs"
            Write-Info "  .\manage-wildfly.ps1 redeploy  - Redeploy"
            Write-Info "  .\manage-wildfly.ps1 test      - Probar API"
        }
    }
}
