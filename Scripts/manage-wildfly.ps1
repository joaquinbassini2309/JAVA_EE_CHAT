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
        Write-Success "✓ WildFly está CORRIENDO"
        Write-Success "   Admin Console: http://localhost:9990/console"
        Write-Success "   Aplicación: http://localhost:8080/chat-empresarial/"
        
        # Verificar si la aplicación está desplegada
        try {
            $appResponse = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            Write-Success "✓ Aplicación DESPLEGADA"
        } catch {
            Write-Warning-Custom "⚠ Aplicación no responde"
        }
    } catch {
        Write-Error-Custom "✗ WildFly NO está corriendo"
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
        Write-Error-Custom "✗ standalone.bat no encontrado en $WildFlyDir"
        return
    }
    
    # Verificar si ya está corriendo
    $process = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" }
    if ($process) {
        Write-Warning-Custom "⚠ WildFly ya está corriendo (PID: $($process.Id))"
        return
    }
    
    Write-Info "Iniciando en segundo plano..."
    $process = Start-Process -FilePath $wildflyCli -WindowStyle Hidden -PassThru
    Write-Success "✓ WildFly iniciado (PID: $($process.Id))"
    Write-Info "Esperando a que esté listo..."
    
    # Esperar
    $maxAttempts = 30
    $attempt = 0
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:9990/console" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            Write-Success "✓ WildFly listo en http://localhost:9990/console"
            return
        } catch {
            $attempt++
            Write-Host "." -NoNewline
            Start-Sleep -Seconds 1
        }
    }
    Write-Warning-Custom "`n⚠ WildFly puede no estar completamente listo"
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
        Write-Success "✓ WildFly detenido (PID: $pid)"
    } else {
        Write-Info "WildFly no está corriendo"
    }
}

# ========================================
# FUNCIÓN: Acceder a CLI de WildFly
# ========================================
function Open-WildFlyCLI {
    Write-Info "Abriendo WildFly CLI..."
    
    $cliPath = "$WildFlyDir\bin\jboss-cli.bat"
    if (-not (Test-Path $cliPath)) {
        Write-Error-Custom "✗ jboss-cli.bat no encontrado"
        return
    }
    
    Write-Info "Conectando a WildFly (intenta conectar con --connect)..."
    & $cliPath --connect
}

# ========================================
# FUNCIÓN: Ver Logs en Tiempo Real
# ========================================
function Watch-Logs {
    Write-Info "Mostrando logs en tiempo real..."
    Write-Info "(Presiona Ctrl+C para detener)"
    
    $logFile = "$WildFlyDir\standalone\log\server.log"
    if (-not (Test-Path $logFile)) {
        Write-Error-Custom "✗ Archivo de log no encontrado: $logFile"
        return
    }
    
    # Windows no tiene 'tail -f', usamos Get-Content -Wait (PowerShell 3.0+)
    Get-Content -Path $logFile -Wait -Tail 50
}

# ========================================
# FUNCIÓN: Redeploy de la Aplicación
# ========================================
function Redeploy-App {
    Write-Info "Recompilando y desplegando aplicación..."
    
    $projectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
    
    if (-not (Test-Path "$projectRoot\pom.xml")) {
        Write-Error-Custom "✗ pom.xml no encontrado en $projectRoot"
        return
    }
    
    Push-Location $projectRoot
    
    Write-Info "Compilando con Maven..."
    & mvn clean package 2>&1 | Tee-Object -Variable mavenOutput
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "✗ Error en compilación"
        Pop-Location
        return
    }
    
    Write-Success "✓ Compilación exitosa"
    
    $warFile = "$projectRoot\target\chat-empresarial.war"
    $deploymentsDir = "$WildFlyDir\standalone\deployments"
    
    if (Test-Path $warFile) {
        Write-Info "Desplegando WAR..."
        Copy-Item $warFile -Destination $deploymentsDir -Force
        Write-Success "✓ WAR desplegado"
        
        Write-Info "Esperando despliegue (hasta 20s)..."
        Start-Sleep -Seconds 3
        
        $maxAttempts = 20
        $attempt = 0
        while ($attempt -lt $maxAttempts) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
                Write-Success "✓ Aplicación disponible en http://localhost:8080/chat-empresarial/"
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

# ========================================
# FUNCIÓN: Probar API
# ========================================
function Test-API {
    Write-Info "Probando endpoints de la API..."
    
    $baseUrl = "http://localhost:8080/chat-empresarial/api/v1"
    
    Write-Info "`n1. Probando login..."
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/usuarios/login" `
            -Method POST `
            -ContentType "application/json" `
            -Body '{"username":"admin","password":"admin123"}' `
            -ErrorAction SilentlyContinue
        
        Write-Success "✓ Login endpoint respondió"
        Write-Info "   Respuesta: $($response | ConvertTo-Json)"
    } catch {
        Write-Warning-Custom "⚠ Login endpoint no respondió"
    }
}

# ========================================
# MENÚ INTERACTIVO
# ========================================
function Show-Menu {
    while ($true) {
        Write-Info "`n=================================="
        Write-Info "     UTILIDADES DE WILDFLY"
        Write-Info "=================================="
        Write-Host "1. Ver estado de WildFly"
        Write-Host "2. Iniciar WildFly"
        Write-Host "3. Detener WildFly"
        Write-Host "4. Abrir CLI de WildFly"
        Write-Host "5. Ver logs en tiempo real"
        Write-Host "6. Redeploy de aplicación"
        Write-Host "7. Probar API"
        Write-Host "8. Salir"
        Write-Host ""
        
        $choice = Read-Host "Selecciona una opción (1-8)"
        
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
            default { Write-Error-Custom "Opción inválida" }
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
            Write-Error-Custom "Acción desconocida: $Action"
            Write-Info "Usos válidos:"
            Write-Info "  .\manage-wildfly.ps1 menu      - Menú interactivo"
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
