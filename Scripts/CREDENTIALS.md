# Credenciales de Acceso - Chat Empresarial

## Admin Console (WildFly Management)

**URL:** `http://localhost:9990/console`

- **Usuario:** `admin`
- **Contraseña:** `Admin@123`

> ✅ **Creadas automáticamente** por `setup-wildfly.ps1` durante la instalación inicial

---

## Base de Datos PostgreSQL

- **Host:** `localhost`
- **Puerto:** `5432`
- **Database:** `chat_db`
- **Usuario:** `postgres`
- **Contraseña:** Se solicita durante la ejecución de `setup-wildfly.ps1`

---

## Aplicación REST API

**URL:** `http://localhost:8080/chat-empresarial/`

### Endpoints de Ejemplo

**Login de usuario:**
```bash
curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

**Ver Data Source:**

En Admin Console (9990):
- Configuration → Subsystems → Datasources → ChatDS

---

## Notas

- Las credenciales de admin se crean **una sola vez** durante el setup inicial
- Si necesitas cambiar la contraseña de admin, usa:
  ```powershell
  cd C:\wildfly-32.0.1.Final\bin
  .\add-user.bat
  ```
- La contraseña de PostgreSQL la defines en el setup

---

**Fecha de creación:** 2026-04-15  
**Ambiente:** Desarrollo Local
