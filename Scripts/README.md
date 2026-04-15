# 🚀 Scripts de Setup - Chat Empresarial

Automatización completa para desplegar la aplicación en WildFly con 4 scripts principales.

---

## 📚 Scripts Disponibles

| Script | Propósito | Ejecución |
|--------|-----------|-----------|
| **run-setup.bat** | Setup inicial completo (recomendado) | `./Scripts/run-setup.bat` |
| **manage-wildfly.bat** | Menú de utilidades para WildFly | `./Scripts/manage-wildfly.bat` |
| **setup-wildfly.ps1** | Script PowerShell de setup (avanzado) | PowerShell |
| **manage-wildfly.ps1** | Script PowerShell de utilidades | PowerShell |
| **cleanup.bat** | Desinstalar WildFly | `./Scripts/cleanup.bat` |

---

## 📋 Requisitos Previos

Antes de ejecutar los scripts, asegúrate de tener instalado:

✅ **Java 21+**
```powershell
java -version
```

✅ **Maven 3.9+**
```powershell
mvn -version
```

✅ **PostgreSQL 12+**
```powershell
psql --version
```

❓ **WildFly** (Lo descarga el script automáticamente)

---

## 🎯 Forma Más Fácil: Usar run-setup.bat

### En Windows:

```bash
# Opción 1: Doble click en run-setup.bat
# (Desde el explorador de archivos)

# Opción 2: Desde terminal (Command Prompt o PowerShell)
.\Scripts\run-setup.bat
```

**Esto:**
1. ✅ Descarga WildFly 32.0.1.Final
2. ✅ Extrae en C:\wildfly-32.0.1.Final
3. ✅ Inicia WildFly automáticamente
4. ✅ Crea data source ChatDS en WildFly
5. ✅ Crea base de datos chat_db en PostgreSQL
6. ✅ Compila la aplicación (mvn clean package)
7. ✅ Despliega el WAR en WildFly
8. ✅ Muestra URLs de acceso

---

## 🎯 Forma Más Fácil: Usar run-setup.bat

### En Windows:

```bash
# Opción 1: Doble click en run-setup.bat
# (Desde el explorador de archivos)

# Opción 2: Desde terminal (Command Prompt o PowerShell)
.\Scripts\run-setup.bat
```

**Esto:**
1. ✅ Descarga WildFly 32.0.1.Final
2. ✅ Extrae en C:\wildfly-32.0.1.Final
3. ✅ Inicia WildFly automáticamente
4. ✅ Crea data source ChatDS en WildFly
5. ✅ Crea base de datos chat_db en PostgreSQL
6. ✅ Compila la aplicación (mvn clean package)
7. ✅ Despliega el WAR en WildFly
8. ✅ Muestra URLs de acceso

---

## 🎮 Script de Utilidades: manage-wildfly.bat

Después de ejecutar `run-setup.bat`, usa este script para:

```bash
# Abrir menú interactivo
.\Scripts\manage-wildfly.bat
```

### Opciones del menú:

```
1. Ver estado de WildFly
   → Verifica si WildFly y la aplicación están corriendo

2. Iniciar WildFly
   → Inicia WildFly en segundo plano

3. Detener WildFly
   → Detiene el proceso de WildFly

4. Abrir CLI de WildFly
   → Abre jboss-cli para ejecutar comandos

5. Ver logs en tiempo real
   → Muestra los logs de WildFly en vivo

6. Redeploy de aplicación
   → Recompila (mvn clean package) y redeploya

7. Probar API
   → Prueba los endpoints de la aplicación

8. Salir
   → Cierra el menú
```

### O ejecutar directamente desde PowerShell:

```powershell
# Ver estado
.\Scripts\manage-wildfly.ps1 -Action status

# Iniciar WildFly
.\Scripts\manage-wildfly.ps1 -Action start

# Detener WildFly
.\Scripts\manage-wildfly.ps1 -Action stop

# Ver logs
.\Scripts\manage-wildfly.ps1 -Action logs

# Redeploy
.\Scripts\manage-wildfly.ps1 -Action redeploy

# Probar API
.\Scripts\manage-wildfly.ps1 -Action test

# Menú interactivo
.\Scripts\manage-wildfly.ps1 -Action menu
```

---

## 🧹 Script de Limpieza: cleanup.bat

Para desinstalar WildFly y limpiar archivos:

```bash
# Abre el script de limpieza
.\Scripts\cleanup.bat
```

**Elimina:**
- ✓ Directorio WildFly completo
- ✓ Archivos compilados (target/)
- ✓ Opcionalmente: caché de Maven

**No elimina:**
- ✓ Base de datos PostgreSQL
- ✓ Código fuente
- ✓ Configuración (pom.xml, persistence.xml)

---

## 🔧 Uso Avanzado: Scripts PowerShell Directamente

Si prefieres más control:

```powershell
# Navega a la carpeta del proyecto
cd C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT

# Ejecuta el script con parámetros
.\Scripts\setup-wildfly.ps1 `
    -PostgresPassword "tu_contraseña" `
    -WildFlyVersion "32.0.1.Final" `
    -WildFlyDir "C:\wildfly-32.0.1.Final" `
    -ProjectRoot "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
```

### Parámetros disponibles:

| Parámetro | Valor por Defecto | Descripción |
|-----------|-------------------|-------------|
| `PostgresPassword` | `postgres` | Contraseña de usuario postgres |
| `WildFlyVersion` | `32.0.1.Final` | Versión a descargar |
| `WildFlyDir` | `C:\wildfly-32.0.1.Final` | Directorio de instalación |
| `ProjectRoot` | `C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT` | Raíz del proyecto |

---

## ⚙️ Qué Hace el Script

### Paso 1: Validar Requisitos
```
✓ Java instalado
✓ Maven instalado
✓ PostgreSQL instalado
✓ Proyecto validado
```

### Paso 2: Descargar WildFly
```
Descarga: https://github.com/wildfly/wildfly/releases/download/32.0.1.Final/wildfly-32.0.1.Final.zip
Extrae en: C:\wildfly-32.0.1.Final
```

### Paso 3: Configurar PostgreSQL
```sql
CREATE DATABASE chat_db;
```

### Paso 4: Iniciar WildFly
```
Inicia en segundo plano
Espera a que esté listo (puerto 9990)
```

### Paso 5: Crear Data Source
```bash
/subsystem=datasources/data-source=ChatDS:add(
    jndi-name=java:/ChatDS,
    driver-name=postgresql,
    connection-url=jdbc:postgresql://localhost:5432/chat_db,
    user-name=postgres,
    password=tu_contraseña,
    min-pool-size=10,
    max-pool-size=30)
```

### Paso 6: Compilar
```bash
mvn clean package
# Genera: target/chat-empresarial.war
```

### Paso 7: Desplegar
```
Copia WAR a: C:\wildfly-32.0.1.Final\standalone\deployments\
WildFly lo despliega automáticamente
```

---

## 🌐 URLs de Acceso

Después de ejecutar el script:

| URL | Descripción |
|-----|-------------|
| `http://localhost:9990/console` | Admin Console de WildFly |
| `http://localhost:8080/chat-empresarial/` | Aplicación |
| `http://localhost:8080/chat-empresarial/api/v1/usuarios/login` | Endpoint Login |

---

## ✅ Verificar Instalación

### 1. ¿WildFly está corriendo?
```bash
# Debería responder
curl http://localhost:9990/console
```

### 2. ¿PostgreSQL conecta?
```bash
psql -U postgres -d chat_db -c "SELECT 1;"
```

### 3. ¿Aplicación desplegada?
```bash
# Prueba un endpoint
curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

---

## 🛑 Parar WildFly

El script inicia WildFly en segundo plano. Para pararlo:

### Opción 1: Desde PowerShell
```powershell
Stop-Process -Name "java" -Force
```

### Opción 2: Admin Console
```
1. Ve a http://localhost:9990/console
2. Runtime → Server → Shutdown
```

### Opción 3: CLI
```bash
C:\wildfly-32.0.1.Final\bin\jboss-cli.bat --connect
:shutdown
```

---

## 🐛 Troubleshooting

### Error: "Java no encontrado"
```
Instala Java 21+
Agrega JAVA_HOME a variables de entorno
```

### Error: "PostgreSQL no encontrado"
```
Instala PostgreSQL
Verifica que psql esté en PATH
```

### Error: "Maven no encontrado"
```
Instala Maven 3.9+
Verifica que mvn esté en PATH
```

### Error: "Data source not found" (después de desplegar)
```
El script lo crea automáticamente
Si falla, ejecuta manualmente:
  1. Abre CLI: bin\jboss-cli.bat --connect
  2. Ejecuta los comandos de data source
  3. Reload
```

### Error: "Port 8080 already in use"
```
Otro WildFly está corriendo
Para el existente: taskkill /IM java.exe /F
O elige otro puerto en standalone.xml
```

### Error: "Database chat_db already exists"
```
Es normal, el script lo ignora
Si necesitas recrearlo:
  DROP DATABASE chat_db;
  CREATE DATABASE chat_db;
```

---

## 📝 Logs

Si algo va mal, consulta:

```
📂 WildFly Logs: C:\wildfly-32.0.1.Final\standalone\log\server.log

📂 PostgreSQL Logs: Varían según instalación

📂 Maven Output: Se muestra en consola durante compilación
```

---

## 🚀 Después de la Setup

1. **Verifica que todo esté corriendo:**
   ```bash
   curl http://localhost:8080/chat-empresarial/
   ```

2. **Prueba un endpoint:**
   ```bash
   curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

3. **Accede a Admin Console:**
   ```
   http://localhost:9990/console
   ```

4. **Revisa logs si hay problemas:**
   ```bash
   tail -f C:\wildfly-32.0.1.Final\standalone\log\server.log
   ```

---

## 📦 Cambios en Tiempo de Ejecución

Si cambias código:

```bash
# 1. Detén la aplicación anterior
Stop-Process -Name "java" -Force

# 2. Recompila
mvn clean package

# 3. Reinicia script
.\Scripts\run-setup.bat
```

O usa hot deploy:
```bash
mvn clean package && mvn wildfly:deploy
```

---

## ✨ Notas

- ✅ El script es **idempotente** - puedes ejecutarlo varias veces
- ✅ Si WildFly ya existe, pregunta si descargar de nuevo
- ✅ Si la BD ya existe, la usa sin recrear
- ✅ Los logs se muestran en tiempo real
- ✅ Puedes cancelar con `Ctrl+C` en cualquier momento

---

**¡Listo! Ahora tu aplicación está desplegada en WildFly.** 🎉
