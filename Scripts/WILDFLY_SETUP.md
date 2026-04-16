# Configuración de WildFly para Chat Empresarial

## 1. Descargar WildFly 32+

Descarga desde: https://www.wildfly.org/downloads/

```bash
# Descomprime en tu carpeta preferida
cd C:\wildfly-32.0.1.Final
```

## 2. Instalar Driver PostgreSQL en WildFly

### Opción A: Automática (Recomendada)

WildFly detectará automáticamente el driver PostgreSQL en el CLASSPATH del WAR.

### Opción B: Manual (Instalación global)

1. Descarga: `postgresql-42.7.2.jar`
2. Copia a: `C:\wildfly-32.0.1.Final\modules\system\layers\base\org\postgresql\main\`
3. Crea archivo `module.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.9" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-42.7.2.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```

## 3. Crear Data Source en WildFly

### Opción A: Usando WildFly CLI

1. Inicia WildFly:
```bash
C:\wildfly-32.0.1.Final\bin\standalone.bat
```

2. En otra terminal, conecta al CLI:
```bash
C:\wildfly-32.0.1.Final\bin\jboss-cli.bat --connect
```

3. Ejecuta estos comandos:

```bash
# Crear data source
/subsystem=datasources/data-source=ChatDS:add(
    jndi-name=java:/ChatDS,
    driver-name=postgresql,
    connection-url=jdbc:postgresql://localhost:5432/chat_db,
    user-name=postgres,
    password=tu_contraseña_postgres,
    min-pool-size=10,
    max-pool-size=30)

# Verificar conexión
/subsystem=datasources/data-source=ChatDS:test-connection-in-pool()

# Confirmar (si está OK)
reload
```

### Opción B: Admin Console

1. Abre: `http://localhost:9990/console`
2. Configuración → Subsistemas → Datasources → Data Sources
3. Nuevo Data Source:
   - JNDI: `java:/ChatDS`
   - Driver: PostgreSQL
   - URL: `jdbc:postgresql://localhost:5432/chat_db`
   - User: `postgres`
   - Password: tu_contraseña

## 4. Crear Base de Datos en PostgreSQL

```sql
-- Conecta como usuario postgres
psql -U postgres

-- Crea la base de datos
CREATE DATABASE chat_db;

-- Verifica
\l

-- Salir
\q
```

## 5. Copiar persistence.xml al Proyecto

1. Estructura correcta:
```
chat-empresarial/
├── src/
│   └── main/
│       ├── java/
│       │   └── chat/
│       └── resources/              ← CREAR SI NO EXISTE
│           └── META-INF/           ← CREAR SI NO EXISTE
│               └── persistence.xml ← COPIAR AQUÍ
├── pom.xml
└── ...
```

2. En IntelliJ IDEA:
   - Clic derecho en proyecto
   - New → Directory
   - Crear: `src/main/resources/META-INF`
   - Copiar `persistence.xml` desde la raíz del proyecto

## 6. Desplegar la Aplicación

```bash
# Compilar y empaquetar
cd C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT
mvn clean package

# Deploy a WildFly (si lo prefieres automático)
mvn wildfly:deploy

# O copia manualmente el WAR:
# Copia: target/chat-empresarial.war
# A: C:\wildfly-32.0.1.Final\standalone\deployments\
```

## 7. Verificar Despliegue

1. Abre: `http://localhost:8080/chat-empresarial/`
2. Prueba un endpoint:

```bash
# Login
curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

## 8. Ver Logs de WildFly

```bash
# Logs en tiempo real
tail -f C:\wildfly-32.0.1.Final\standalone\log\server.log
```

## Troubleshooting

### Error: "Data source not found"
→ Verifica que el data source ChatDS esté creado en WildFly CLI

### Error: "PostgreSQL driver not found"
→ El driver está en el WAR (pom.xml), debería funcionar automáticamente

### Error: "Connection refused"
→ Verifica que PostgreSQL esté corriendo en localhost:5432

### Error: "Database chat_db does not exist"
→ Crea la BD con los comandos SQL arriba

### Error: "Tabla usuarios no existe"
→ Revisa que `hibernate.hbm2ddl.auto=create` en persistence.xml

## Configuración de Producción

Para producción, cambia en persistence.xml:

```xml
<!-- Cambiar de -->
<property name="hibernate.hbm2ddl.auto" value="create"/>

<!-- A -->
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```

Y cambia también en pom.xml:
```xml
<!-- De -->
<property name="hibernate.show_sql" value="false"/>

<!-- A -->
<property name="hibernate.show_sql" value="true"/>  <!-- Para debug en producción -->
```

## URLs Útiles

- Admin Console: `http://localhost:9990/console`
- Aplicación: `http://localhost:8080/chat-empresarial/`
- PostgreSQL: `localhost:5432`
- CLI: `C:\wildfly-32.0.1.Final\bin\jboss-cli.bat`
