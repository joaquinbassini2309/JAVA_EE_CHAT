# 🔧 Configuración de WildFly y Base de Datos en Render.com

## 📋 Problema Encontrado

El servidor WildFly está corriendo, pero devuelve **404** porque:

1. **El datasource PostgreSQL NO se está creando** - Las variables de entorno de base de datos no están configuradas en Render
2. **Los beans CDI están fallando** - Sin el datasource, los `Manejadores` (que usan `@PersistenceContext`) no pueden inyectarse
3. **La aplicación no se despliega correctamente** - Sin los beans, todos los servicios REST fallan

### Evidencia en los logs:
```
21:57:40,033 ERROR [org.jboss.as] WFLYSRV0026: WildFly started (with errors) 
- Started 349 of 577 services 
- 4 services failed or missing dependencies   ❌
-  322 services are lazy, passive or on-demand
```

Y luego:
```
21:57:09,733 INFO [org.jboss.as.connector.subsystems.datasources] 
WFLYJCA0001: Bound data source [java:jboss/datasources/ExampleDS]  ❌ H2, no PostgreSQL
```

## ✅ Soluciones Implementadas

### 1. Corregido: `chat-ds.xml.template`
- **Problema**: Contenía una sección `<drivers>` inválida para archivos `-ds.xml`
- **Solución**: Removida la sección `<drivers>` (WildFly ya tiene el módulo PostgreSQL configurado)

### 2. Corregido: `postgresql-module.xml`
- **Problema**: Importaba dependencias javax (Java EE antiguo) en lugar de jakarta
- **Solución**: Actualizado a usar solo `java.sql` y `java.logging` (compatible con Jakarta EE)

### 3. Mejorado: `configure-and-run.sh`
- **Ahora**: Proporciona mensajes de error claros si las variables de BD no están configuradas

---

## 🚀 INSTRUCCIONES PARA RENDER.COM

### Paso 1: Obtener Credenciales de PostgreSQL
1. Ve a https://dashboard.render.com
2. Ve a **PostgreSQL** → Tu base de datos `chat`
3. En la sección **Connections**, copia estos valores:
   - **Hostname** (interno): `dpg-d7old8l7vvec738q7h30-a`
   - **Port**: `5432`
   - **Database**: `chat_ye71`
   - **Username**: `chat_ye71_user`
   - **Password**: `LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX`

### Paso 2: Acceder a tu Servicio WildFly
1. Ve a https://dashboard.render.com
2. Selecciona tu servicio `java-ee-chat-backend`
3. Ve a **Settings** en la parte izquierda

### Paso 3: Configurar Variables de Entorno
En la sección **Environment**, añade estas variables exactamente:

| Variable | Valor (Tu caso) |
|----------|-----------------|
| `DB_HOST` | `dpg-d7old8l7vvec738q7h30-a` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `chat_ye71` |
| `DB_USER` | `chat_ye71_user` |
| `DB_PASSWORD` | `LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX` |

⚠️ **IMPORTANTE**: 
- Usa el **Hostname interno** (`dpg-d7old8l7vvec738q7h30-a`), NO el externo
- El servicio WildFly está en la misma red de Render, usa la URL interna

### Paso 4: Desplegar
1. En el dashboard de tu servicio `java-ee-chat-backend`, haz clic en **"Redeploy"**
2. Espera a que se complete el despliegue (verás en verde "Deployed ✓")
3. El despliegue típicamente tarda 2-3 minutos

### Paso 5: Verificar
```bash
# Verifica que el servidor esté respondiendo
curl -I https://java-ee-chat-backend.onrender.com/

# Intenta acceder a la aplicación
curl -I https://java-ee-chat-backend.onrender.com/chat-empresarial/

# Intenta acceder a un endpoint de API
curl -I https://java-ee-chat-backend.onrender.com/chat-empresarial/api/v1/usuarios
```

**Esperados:**
- ✅ Status `200` o `304` para `/` y `/chat-empresarial/`
- ✅ Status `200` para `/api/v1/usuarios` (aunque devuelva `[]` está bien)

---

## 🔍 Diagnóstico

### Si aún ves 404:

1. **Verificar logs en Render**:
   - Ve a tu servicio en Render
   - Abre la sección "Logs"
   - Busca por `ERROR` o `FAILED`

2. **Señales de éxito**:
   - Deberías ver: `INFO [org.jboss.as.connector.subsystems.datasources] WFLYJCA0001: Bound data source [java:jboss/datasources/ChatDS]`
   - Y NO deberías ver: `ERROR [org.jboss.as]` con servicios fallidos

3. **Si el datasource se crea pero aún hay errores**:
   - Verifica que la conexión a PostgreSQL funciona:
     ```bash
     PGPASSWORD=LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX psql -h dpg-d7old8l7vvec738q7h30-a.oregon-postgres.render.com -U chat_ye71_user chat_ye71
     ```
   - Verifica que la base de datos `chat_ye71` existe
   - Verifica que el usuario `chat_ye71_user` tiene permisos

---

## 📝 Notas Técnicas

### Cómo funciona:
1. **`configure-and-run.sh`** (script de entrada del Docker):
   - Lee las variables de entorno
   - Reemplaza las variables en `chat-ds.xml.template`
   - Genera `chat-ds.xml` con las credenciales reales
   - WildFly detecta y despliega el datasource automáticamente

2. **`persistence.xml`** (configuración de JPA):
   - Especifica `<jta-data-source>java:/ChatDS</jta-data-source>`
   - Los `Manejadores` usan `@PersistenceContext(unitName = "chatPU")`
   - Sin el datasource, estos beans no se pueden inicializar

3. **Módulo PostgreSQL**:
   - Instalado en WildFly como `org.postgresql.module`
   - El datasource lo referencia con `<driver>postgresql</driver>`

---

## 🚨 Próximos Pasos

Después de configurar las variables de BD:

1. **Redeploy** la aplicación
2. **Espera 5 minutos** para que se estabilice
3. **Prueba los endpoints** desde tu frontend

Si aún hay problemas, revisa los logs de Render buscando:
- `WildFly started` (debería tener 0 servicios fallidos, no 4)
- `Bound data source [java:jboss/datasources/ChatDS]` (debe estar presente)
- `chat-empresarial.war` deployment success

---

## 📞 Soporte

Si aún no funciona después de estos pasos:

1. **Captura los logs completos** de Render (Settings → Logs, copia los últimos 50 líneas)
2. **Verifica las credenciales de BD** - ¿Son exactamente iguales a las de PostgreSQL?
3. **Prueba la conexión a PostgreSQL** desde tu máquina local:
   ```bash
   PGPASSWORD=LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX psql -h dpg-d7old8l7vvec738q7h30-a.oregon-postgres.render.com -U chat_ye71_user -d chat_ye71
   ```
   
   Si ves `chat_ye71=>` el prompt, la conexión funciona ✅

4. **Verifica que las tablas se crearon**:
   ```sql
   SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
   ```
   Deberías ver: `usuario`, `conversacion`, `mensaje`, `participante`

---

Última actualización: 2026-04-29

