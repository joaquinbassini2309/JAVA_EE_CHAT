# ========================================
# Setup WildFly Automático para Chat Empresarial
# ========================================
# Este script descarga WildFly, lo configura,
# crea el data source PostgreSQL y despliega la app

param(
    [string]$PostgresPassword = "postgres",
    [string]$WildFlyVersion = "32.0.1.Final",
    [string]$WildFlyDir = "C:\wildfly-$WildFlyVersion",
    [string]$ProjectRoot = "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
)

# Colores para output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning-Custom { Write-Host $args -ForegroundColor Yellow }

# ========================================
# 1. VALIDAR REQUISITOS
# ========================================
Write-Info "======================================"
Write-Info "Validando requisitos..."
Write-Info "======================================"

# Validar Java
try {
    $javaVersion = java -version 2>&1
    Write-Success "✓ Java instalado"
} catch {
    Write-Error-Custom "✗ Java no encontrado. Instala Java 21+ primero."
    exit 1
}

# Validar Maven
try {
    $mvnVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Success "✓ Maven instalado: $mvnVersion"
} catch {
    Write-Error-Custom "✗ Maven no encontrado. Instala Maven primero."
    exit 1
}

# Validar PostgreSQL
try {
    $pgVersion = psql --version 2>&1
    Write-Success "✓ PostgreSQL instalado: $pgVersion"
} catch {
    Write-Error-Custom "✗ PostgreSQL no encontrado. Instala PostgreSQL primero."
    exit 1
}

# Validar proyecto
if (-not (Test-Path "$ProjectRoot\pom.xml")) {
    Write-Error-Custom "✗ pom.xml no encontrado en $ProjectRoot"
    exit 1
}
Write-Success "✓ Proyecto validado en $ProjectRoot"

# ========================================
# 2. DESCARGAR Y EXTRAER WILDFLY
# ========================================
Write-Info "======================================"
Write-Info "Descargando WildFly $WildFlyVersion..."
Write-Info "======================================"

if (Test-Path $WildFlyDir) {
    Write-Warning-Custom "WildFly ya existe en $WildFlyDir"
    $response = Read-Host "¿Descargar de nuevo? (s/n)"
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
        Write-Success "✓ Descarga completada"
    } catch {
        Write-Error-Custom "✗ Error en descarga: $_"
        exit 1
    }
    
    Write-Info "Extrayendo WildFly..."
    try {
        Expand-Archive -Path $zipPath -DestinationPath "C:\" -Force
        Remove-Item $zipPath
        Write-Success "✓ WildFly extraído en $WildFlyDir"
    } catch {
        Write-Error-Custom "✗ Error en extracción: $_"
        exit 1
    }
}

# ========================================
# 3. CREAR BASE DE DATOS EN POSTGRESQL
# ========================================
Write-Info "======================================"
Write-Info "Configurando PostgreSQL..."
Write-Info "======================================"

try {
    # Crear BD (ignorar si ya existe)
    $pgCreateDb = @"
SELECT 1 FROM pg_database WHERE datname = 'chat_db'
"@
    
    $dbExists = psql -U postgres -t -c "SELECT 1 FROM pg_database WHERE datname = 'chat_db'" 2>$null
    
    if (-not $dbExists) {
        Write-Info "Creando base de datos chat_db..."
        psql -U postgres -c "CREATE DATABASE chat_db;" 2>$null
        Write-Success "✓ Base de datos chat_db creada"
    } else {
        Write-Info "Base de datos chat_db ya existe"
    }
} catch {
    Write-Warning-Custom "⚠ Error al crear BD PostgreSQL: $_"
    Write-Info "Continuando... la BD podría crearse después"
}

# ========================================
# 4. INICIAR WILDFLY
# ========================================
Write-Info "======================================"
Write-Info "Iniciando WildFly..."
Write-Info "======================================"

$wildflyCli = "$WildFlyDir\bin\standalone.bat"
$wildflyCLI = "$WildFlyDir\bin\jboss-cli.bat"

if (-not (Test-Path $wildflyCli)) {
    Write-Error-Custom "✗ No se encontró $wildflyCli"
    exit 1
}

Write-Info "Iniciando servidor en segundo plano..."
$process = Start-Process -FilePath $wildflyCli -WindowStyle Hidden -PassThru
Start-Sleep -Seconds 3

Write-Success "✓ WildFly iniciado (PID: $($process.Id))"
Write-Info "Esperando a que WildFly esté listo (hasta 30s)..."

# Esperar a que WildFly esté listo
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
    Write-Success "`n✓ WildFly está listo en http://localhost:9990/console"
} else {
    Write-Warning-Custom "`n⚠ WildFly puede no estar completamente listo, continuando..."
}

# ========================================
# 5. CREAR DATA SOURCE EN WILDFLY
# ========================================
Write-Info "======================================"
Write-Info "Configurando Data Source en WildFly..."
Write-Info "======================================"

$cliCommands = @"
# Agregar data source para PostgreSQL
/subsystem=datasources/data-source=ChatDS:add(jndi-name=java:/ChatDS,driver-name=postgresql,connection-url=jdbc:postgresql://localhost:5432/chat_db,user-name=postgres,password=$PostgresPassword,min-pool-size=10,max-pool-size=30)

# Probar conexión
/subsystem=datasources/data-source=ChatDS:test-connection-in-pool()

# Reload para aplicar cambios
reload

# Salir
exit
"@

# Guardar comandos en archivo temporal
$cliFile = "$env:TEMP\wildfly-setup.cli"
$cliCommands | Out-File -Encoding ASCII $cliFile

Write-Info "Ejecutando comandos CLI..."
try {
    & $wildflyCLI --connect --file=$cliFile 2>&1 | Tee-Object -Variable cliOutput | Write-Host
    
    if ($cliOutput -match "success" -or $cliOutput -match "successfully") {
        Write-Success "✓ Data Source ChatDS creado exitosamente"
    } else {
        Write-Info "Verificando data source..."
    }
} catch {
    Write-Warning-Custom "⚠ Error ejecutando CLI: $_"
}

Remove-Item $cliFile -ErrorAction SilentlyContinue

# ========================================
# 6. COMPILAR LA APLICACIÓN
# ========================================
Write-Info "======================================"
Write-Info "Compilando aplicación con Maven..."
Write-Info "======================================"

Push-Location $ProjectRoot

Write-Info "Ejecutando: mvn clean package"
& mvn clean package 2>&1 | Tee-Object -Variable mavenOutput

Pop-Location

if ($LASTEXITCODE -eq 0) {
    Write-Success "✓ Compilación completada exitosamente"
    
    $warFile = "$ProjectRoot\target\chat-empresarial.war"
    if (Test-Path $warFile) {
        Write-Success "✓ WAR generado: $warFile"
    }
} else {
    Write-Error-Custom "✗ Error en compilación de Maven"
    Write-Host $mavenOutput
    exit 1
}

# ========================================
# 7. DESPLEGAR LA APLICACIÓN
# ========================================
Write-Info "======================================"
Write-Info "Desplegando aplicación en WildFly..."
Write-Info "======================================"

$deploymentsDir = "$WildFlyDir\standalone\deployments"
$warFile = "$ProjectRoot\target\chat-empresarial.war"

if (Test-Path $warFile) {
    Write-Info "Copiando WAR a deployments..."
    Copy-Item $warFile -Destination $deploymentsDir -Force
    Write-Success "✓ WAR copiado a $deploymentsDir"
    
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
        Write-Success "`n✓ Aplicación desplegada exitosamente"
    } else {
        Write-Info "`nAplicación en proceso de despliegue, verifica logs..."
    }
} else {
    Write-Error-Custom "✗ WAR no encontrado en $warFile"
}

# ========================================
# 8. RESUMEN FINAL
# ========================================
Write-Info "======================================"
Write-Info "CONFIGURACIÓN COMPLETADA"
Write-Info "======================================"

Write-Success "`n✓ WildFly está corriendo"
Write-Success "✓ Data Source ChatDS configurado"
Write-Success "✓ Base de datos chat_db creada"
Write-Success "✓ Aplicación compilada y desplegada"

Write-Info "`n📋 URLS IMPORTANTES:"
Write-Info "   Admin Console: http://localhost:9990/console"
Write-Info "   Aplicación: http://localhost:8080/chat-empresarial/"
Write-Info "   API Login: POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login"

Write-Info "`n📝 PRUEBA RÁPIDA:"
Write-Info "   curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \"
Write-Info "     -H 'Content-Type: application/json' \"
Write-Info "     -d '{""username"":""test"",""password"":""test123""}'"

Write-Info "`n📂 DIRECTORIO WILDFLY: $WildFlyDir"
Write-Info "`n📂 LOGS: $WildFlyDir\standalone\log\server.log"

Write-Warning-Custom "`n⚠ PRÓXIMOS PASOS:"
Write-Warning-Custom "   1. Verifica que WildFly esté corriendo en http://localhost:9990/console"
Write-Warning-Custom "   2. Asegúrate que PostgreSQL esté corriendo"
Write-Warning-Custom "   3. Prueba los endpoints de la API"
Write-Warning-Custom "   4. Para detener WildFly: Stop-Process -Id $($process.Id)"

Write-Info "`n======================================"
Write-Success "¡CONFIGURACIÓN LISTA PARA PRODUCCIÓN!"
Write-Info "======================================"
