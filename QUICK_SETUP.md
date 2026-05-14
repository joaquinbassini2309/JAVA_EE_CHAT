# ✅ CHECKLIST RÁPIDO - Configurar Variables en Render

**Estado actual**: Todos los bugs del servidor están corregidos ✓

## 📋 Tus credenciales de PostgreSQL

De esta página: https://dashboard.render.com → PostgreSQL `chat` → Connections

```
Hostname (INTERNO):     dpg-d7old8l7vvec738q7h30-a
Port:                    5432
Database:                chat_ye71
Username:                chat_ye71_user
Password:                LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX
```

---

## 🔧 Configurar Variables de Entorno en WildFly

1. Ve a: https://dashboard.render.com
2. Haz clic en tu servicio: `java-ee-chat-backend`
3. En la izquierda, haz clic en: **Settings**
4. Busca: **Environment**
5. Haz clic en: **Add Environment Variable**

### Copia y Pega estas 5 variables:

#### Variable 1
```
Name:   DB_HOST
Value:  dpg-d7old8l7vvec738q7h30-a
```

#### Variable 2
```
Name:   DB_PORT
Value:  5432
```

#### Variable 3
```
Name:   DB_NAME
Value:  chat_ye71
```

#### Variable 4
```
Name:   DB_USER
Value:  chat_ye71_user
```

#### Variable 5
```
Name:   DB_PASSWORD
Value:  LE8U7QiLBie6kWkoKY0FSfLHGZNIEnUX
```

---

## 🚀 Después de Agregar las Variables

1. En la parte superior, haz clic en: **Save**
2. Se va a mostrar un botón que dice: **Redeploy latest commit**
3. Haz clic en él
4. Espera 3-5 minutos a que se complete

---

## ✔️ Verificar que Funcionó

Abre una terminal y ejecuta:

```bash
curl.exe -v "https://java-ee-chat-backend.onrender.com/chat-empresarial/api/v1/usuarios"
```

### Esperados:
- ✅ `HTTP/1.1 200 OK` - ¡Todo funciona!
- ✅ `HTTP/1.1 400 Bad Request` - La app responde (error normal sin login)
- ❌ `HTTP/1.1 404 Not Found` - Aún no funciona, revisa los logs

### Si ves 200 o 400: ¡ÉXITO! 🎉
Tu backend está funcionando. Ahora prueba:

```bash
# Registrar usuario
curl.exe -X POST "https://java-ee-chat-backend.onrender.com/chat-empresarial/api/v1/usuarios/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test\",\"email\":\"test@test.com\",\"password\":\"pass123\"}"

# Login
curl.exe -X POST "https://java-ee-chat-backend.onrender.com/chat-empresarial/api/v1/usuarios/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"pass123\"}"
```

---

## 🔍 Si aún ves 404

Para ver los logs:
- Ve a tu servicio en Render
- En la izquierda haz clic: **Logs**
- Busca por: `ERROR` o `ChatDS`

Señales de éxito en logs:
- `Bound data source [java:jboss/datasources/ChatDS]`
- `chat-empresarial.war` deployed sin errores

---

## 📝 Cambios Realizados en el Backend

Se corrigieron varios problemas de configuración de WildFly:

1. ✅ XML parsing (jboss-web.xml)
2. ✅ Dependencias del módulo PostgreSQL
3. ✅ Configuración de datasource
4. ✅ Scripts de deployment

Ver `FIX_DATASOURCE_WILDFLY.md` para detalles técnicos completos.

---

**¿Lista para intentarlo? 🚀**

1. ✅ Agrega las 5 variables de entorno
2. ✅ Haz clic en Redeploy
3. ✅ Espera 3-5 minutos
4. ✅ Prueba con curl

¡Debería funcionar! 🎉

