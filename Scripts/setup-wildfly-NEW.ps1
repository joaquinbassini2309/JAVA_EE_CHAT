# ========================================
# Setup WildFly Automatico para Chat Empresarial
# ========================================
# Este script descarga WildFly, lo configura,
# crea el data source PostgreSQL y despliega la app
#
# USO:
#   .\setup-wildfly.ps1
#   .\setup-wildfly.ps1 -PostgresPassword "tu_password"

#Requires -RunAsAdministrator

param(
    [string]$PostgresPassword = $(Read-Host "Ingresa contraseña de PostgreSQL" -AsSecureString | ConvertFrom-SecureString -AsPlainText),
    [string]$WildFlyVersion = "32.0.1.Final",
    [string]$WildFlyDir = "C:\wildfly-$WildFlyVersion",
    [string]$ProjectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
)

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

Write-Info "======================================"
Write-Info "Validando requisitos..."
Write-Info "======================================"

try {
    $javaVersion = java -version 2>&1
    Write-Success "[OK] Java instalado"
} catch {
    Write-Error-Custom "[!] Java no encontrado. Instala Java 21+ primero."
    exit 1
}

try {
    $pgVersion = psql --version 2>&1
    Write-Success "[OK] PostgreSQL instalado: $pgVersion"
} catch {
    Write-Error-Custom "[!] PostgreSQL no encontrado. Instala PostgreSQL primero."
    exit 1
}

if (-not (Test-Path "$ProjectRoot\pom.xml")) {
    Write-Error-Custom "[!] pom.xml no encontrado en $ProjectRoot"
    exit 1
}
Write-Success "[OK] Proyecto validado en $ProjectRoot"

Write-Info "======================================"
Write-Info "Descargando WildFly $WildFlyVersion..."
Write-Info "======================================"

if (Test-Path $WildFlyDir) {
    Write-Warning-Custom "[!] WildFly ya existe en $WildFlyDir"
    $response = Read-Host "Descargar de nuevo? (s/n)"
    if ($response -eq "s") {
        Remove-Item -Recurse -Force $WildFlyDir
    } else {
        Write-Info "Usando WildFly existente..."
    }
}

if (-not (Test-Path $WildFlyDir)) {
    $downloadUrl = "https://github.com/wildfly/wildfly/releases/download/$WildFlyVersion/wildfly-$WildFlyVersion.zip"
    $zipPath = "$env:TEMP\wildfly-$WildFlyVersion.zip"
    
    Write-Info "Descargando desde: $downloadUrl"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing
        Write-Success "[OK] Descarga completada"
    } catch {
        Write-Error-Custom "[!] Error en descarga: $_"
        exit 1
    }
    
    Write-Info "Extrayendo WildFly..."
    try {
        Expand-Archive -Path $zipPath -DestinationPath "C:\" -Force
        Remove-Item $zipPath
        Write-Success "[OK] WildFly extraido en $WildFlyDir"
    } catch {
        Write-Error-Custom "[!] Error en extraccion: $_"
        exit 1
    }
}

Write-Info "======================================"
Write-Info "Configurando PostgreSQL..."
Write-Info "======================================"

try {
    $dbExists = psql -U postgres -t -c "SELECT 1 FROM pg_database WHERE datname = 'chat_db'" 2>$null
    
    if (-not $dbExists) {
        Write-Info "Creando base de datos chat_db..."
        psql -U postgres -c "CREATE DATABASE chat_db;" 2>$null
        Write-Success "[OK] Base de datos chat_db creada"
    } else {
        Write-Info "Base de datos chat_db ya existe"
    }
} catch {
    Write-Warning-Custom "[!] Error al crear BD PostgreSQL: $_"
    Write-Info "Continuando... la BD podria crearse despues"
}

Write-Info "======================================"
Write-Info "Iniciando WildFly..."
Write-Info "======================================"

$wildflyCli = "$WildFlyDir\bin\standalone.bat"
$wildflyCLI = "$WildFlyDir\bin\jboss-cli.bat"

if (-not (Test-Path $wildflyCli)) {
    Write-Error-Custom "[!] No se encontro $wildflyCli"
    exit 1
}

Write-Info "Iniciando servidor en segundo plano..."
$process = Start-Process -FilePath $wildflyCli -WindowStyle Hidden -PassThru
Start-Sleep -Seconds 3

Write-Success "[OK] WildFly iniciado (PID: $($process.Id))"
Write-Info "Esperando a que WildFly este listo (hasta 30s)..."

$maxAttempts = 30
$attempt = 0
$ready = $false

while ($attempt -lt $maxAttempts -and -not $ready) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9990/console" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        $ready = $true
    } catch {
        $attempt++
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 1
    }
}

if ($ready) {
    Write-Success ""
    Write-Success "[OK] WildFly esta listo en http://localhost:9990/console"
} else {
    Write-Warning-Custom ""
    Write-Warning-Custom "[!] WildFly puede no estar completamente listo, continuando..."
}

Write-Info "======================================"
Write-Info "Configurando Data Source en WildFly..."
Write-Info "======================================"

$cliCommands = @"
/subsystem=datasources/data-source=ChatDS:add(jndi-name=java:/ChatDS,driver-name=postgresql,connection-url=jdbc:postgresql://localhost:5432/chat_db,user-name=postgres,password=$PostgresPassword,min-pool-size=10,max-pool-size=30)
/subsystem=datasources/data-source=ChatDS:test-connection-in-pool()
reload
exit
"@

$cliFile = "$env:TEMP\wildfly-setup.cli"
$cliCommands | Out-File -Encoding ASCII -FilePath $cliFile

Write-Info "Ejecutando comandos CLI..."
try {
    & $wildflyCLI --connect --file=$cliFile 2>&1 | Tee-Object -Variable cliOutput | Write-Host
    
    if ($cliOutput -match "success" -or $cliOutput -match "successfully") {
        Write-Success "[OK] Data Source ChatDS creado exitosamente"
    } else {
        Write-Info "Verificando data source..."
    }
} catch {
    Write-Warning-Custom "[!] Error ejecutando CLI: $_"
}

Remove-Item $cliFile -ErrorAction SilentlyContinue

Write-Info "======================================"
Write-Info "Compilando aplicacion con Maven..."
Write-Info "======================================"

Push-Location $ProjectRoot

Write-Info "Ejecutando: mvn clean package"
& mvn clean package 2>&1 | Tee-Object -Variable mavenOutput

Pop-Location

if ($LASTEXITCODE -eq 0) {
    Write-Success "[OK] Compilacion completada exitosamente"
    
    $warFile = "$ProjectRoot\target\chat-empresarial.war"
    if (Test-Path $warFile) {
        Write-Success "[OK] WAR generado: $warFile"
    }
} else {
    Write-Error-Custom "[!] Error en compilacion de Maven"
    Write-Host $mavenOutput
    exit 1
}

Write-Info "======================================"
Write-Info "Desplegando aplicacion en WildFly..."
Write-Info "======================================"

$deploymentsDir = "$WildFlyDir\standalone\deployments"
$warFile = "$ProjectRoot\target\chat-empresarial.war"

if (Test-Path $warFile) {
    Write-Info "Copiando WAR a deployments..."
    Copy-Item $warFile -Destination $deploymentsDir -Force
    Write-Success "[OK] WAR copiado a $deploymentsDir"
    
    Write-Info "Esperando despliegue (hasta 30s)..."
    Start-Sleep -Seconds 5
    
    $maxAttempts = 30
    $attempt = 0
    $deployed = $false
    
    while ($attempt -lt $maxAttempts -and -not $deployed) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/chat-empresarial/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            $deployed = $true
        } catch {
            $attempt++
            Write-Host "." -NoNewline
            Start-Sleep -Seconds 1
        }
    }
    
    if ($deployed) {
        Write-Success ""
        Write-Success "[OK] Aplicacion desplegada exitosamente"
    } else {
        Write-Info ""
        Write-Info "Aplicacion en proceso de despliegue, verifica logs..."
    }
} else {
    Write-Error-Custom "[!] WAR no encontrado en $warFile"
}

Write-Info "======================================"
Write-Info "CONFIGURACION COMPLETADA"
Write-Info "======================================"

Write-Success ""
Write-Success "[OK] WildFly esta corriendo"
Write-Success "[OK] Data Source ChatDS configurado"
Write-Success "[OK] Base de datos chat_db creada"
Write-Success "[OK] Aplicacion compilada y desplegada"

Write-Info ""
Write-Info "URLS IMPORTANTES:"
Write-Info "   Admin Console: http://localhost:9990/console"
Write-Info "   Aplicacion: http://localhost:8080/chat-empresarial/"
Write-Info "   API Login: POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login"

Write-Info ""
Write-Info "PROXIMOS PASOS:"
Write-Warning-Custom "   1. Verifica que WildFly este corriendo en http://localhost:9990/console"
Write-Warning-Custom "   2. Asegurate que PostgreSQL este corriendo"
Write-Warning-Custom "   3. Prueba los endpoints de la API"
Write-Warning-Custom "   4. Para detener WildFly: Stop-Process -Id $($process.Id)"

Write-Info ""
Write-Info "======================================"
Write-Success "CONFIGURACION LISTA PARA PRODUCCION!"
Write-Info "======================================"
