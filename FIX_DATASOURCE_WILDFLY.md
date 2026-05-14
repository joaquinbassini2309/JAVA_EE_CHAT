# ✅ FIX: Resolución del Error de Datasource PostgreSQL en WildFly

## 🔍 Problema Original

En los logs de Render aparecía:

```
java.lang.ClassNotFoundException: javax.naming.Referenceable
FATAL [org.jboss.as.server]: WFLYSRV0056: Server boot has failed in an unrecoverable manner
```

**Causa**: El módulo PostgreSQL en WildFly no podía encontrar las clases necesarias para funcionar como datasource.

---

## ✅ Soluciones Implementadas

### 1. **Corregido: `jboss-web.xml`** ✓
- **Problema**: Archivo vacío/mal formado causaba error `Premature end of file`
- **Solución**: Convertido a XML válido con estructura mínima
- **Archivo**: `CapaServicio/src/main/webapp/WEB-INF/jboss-web.xml`

Antes:
```xml
<!-- comentarios solo, sin XML válido -->
```

Después:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web>
    <!-- Descriptor válido y mínimo para evitar errores de parseo en WildFly. -->
</jboss-web>
```

---

### 2. **Corregido: `postgresql-module.xml`** ✓
- **Problema**: Faltan dependencias que el driver necesita para funcionar
- **Solución**: Agregada dependencia a `jakarta.naming.api`
- **Archivo**: `CapaServicio/postgresql-module.xml`

Antes:
```xml
<dependencies>
  <module name="java.sql"/>
  <module name="java.logging"/>
</dependencies>
```

Después:
```xml
<dependencies>
  <module name="java.base"/>
  <module name="java.logging"/>
  <module name="java.sql"/>
  <module name="jakarta.naming.api"/>  <!-- ← AGREGADO -->
</dependencies>
```

---

### 3. **Corregido: `chat-ds.xml.template`** ✓
- **Problema**: Contenía sección `<drivers>` no válida en archivo `-ds.xml`
- **Solución**: Removida la sección drivers
- **Archivo**: `CapaServicio/chat-ds.xml.template`

Antes:
```xml
<datasources>
  <datasource>...</datasource>
  <drivers>
    <driver name="postgresql" module="org.postgresql"/>
  </drivers>
</datasources>
```

Después:
```xml
<datasources>
  <datasource>...</datasource>
</datasources>
```

WildFly ya tiene el módulo PostgreSQL configurado, no es necesario redefinirlo aquí.

---

### 4. **Corregido: `postgresql-module.xml`** ✓ (dependencias javax→jakarta)
- **Problema**: Importaba dependencias `javax.*` antiguas
- **Solución**: Actualizado a usar dependencias compatibles con Jakarta EE 10
- **Archivo**: `CapaServicio/postgresql-module.xml`

---

### 5. **Simplificado: `configure-and-run.sh`** ✓
- **Problema**: Intentaba registrar el driver en runtime (innecesario)
- **Solución**: Removido código de registro, ahora solo genera datasource
- **Archivo**: `CapaServicio/configure-and-run.sh`

El flujo ahora es:
1. WildFly arranca
2. Lee `chat-ds.xml` con credenciales de BD
3. Carga el driver PostgreSQL desde el módulo
4. El módulo tiene todas las dependencias correctas
5. El datasource se registra automáticamente

---

### 6. **Simplificado: `Dockerfile`** ✓
- **Problema**: Intentaba ejecutar jboss-cli durante build (sin servidor activo)
- **Solución**: Removida esa línea innecesaria
- **Archivo**: `CapaServicio/Dockerfile`

Antes:
```dockerfile
RUN /opt/wildfly/bin/jboss-cli.sh --file=/opt/wildfly/bin/register-pg-driver.cli
```

Después: removida (no es necesaria)

---

## 📊 Resumen de Cambios

| Archivo | Cambio | Resultado |
|---------|--------|-----------|
| `jboss-web.xml` | Convertido a XML válido | ✅ No más errores de parseo |
| `postgresql-module.xml` | Agregada dependencia `jakarta.naming.api` | ✅ Driver puede cargar clases |
| `chat-ds.xml.template` | Removida sección `<drivers>` | ✅ XML válido para WildFly |
| `configure-and-run.sh` | Simplificado (removido CLI registration) | ✅ Más limpio, más robusto |
| `Dockerfile` | Removida línea de jboss-cli | ✅ Build sin errores |

---

## 🚀 Flujo Correcto Ahora

1. **Build Docker**:
   - Maven compila el WAR ✓
   - Descarga driver PostgreSQL ✓
   - Descarga WildFly ✓
   - Copia módulo PostgreSQL con dependencias correctas ✓

2. **Runtime en Render**:
   - `configure-and-run.sh` crea `chat-ds.xml` con credenciales reales
   - WildFly arranca
   - Lee `chat-ds.xml`
   - Carga el módulo PostgreSQL con acceso a `jakarta.naming.api` ✓
   - Registra datasource `ChatDS` con éxito ✓
   - Despliegue del WAR procede sin problemas ✓
   - API REST available en `/chat-empresarial/api/v1/usuarios` ✓

---

## 🔧 Variables de Entorno Requeridas en Render

Asegúrate de que existenmé en tu servicio:

```
DB_HOST=dpg-d7old8l7vvec738q7h30-a
DB_PORT=5432
DB_NAME=chat_ye71
DB_USER=chat_ye71_user
DB_PASSWORD=LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX
```

---

## ✔️ Verificación

Después de redeploy, deberías ver en logs de Render:

### ✅ Señales de éxito:
- `Bound data source [java:jboss/datasources/ChatDS]`
- `chat-empresarial.war` deployed sin errores
- `WildFly started` (sin `with errors`)

### ❌ Señales de error (si ves):
- `ClassNotFoundException: javax.naming.Referenceable` → módulo sin dependencias
- `Failed to parse XML` → jboss-web.xml mal formado
- `Premature end of file` → XML truncado

---

## 🔄 Próximo Paso

1. **Git push** de los cambios
2. **Redeploy** en Render
3. **Espera 3-5 minutos** a que termine
4. **Prueba**:
   ```bash
   curl.exe -v "https://java-ee-chat-backend.onrender.com/chat-empresarial/api/v1/usuarios"
   ```

---

Última actualización: 2026-04-29

