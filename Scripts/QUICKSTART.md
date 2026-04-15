# ⚡ QUICKSTART - Despliegue en 30 Segundos

## 🎯 El Comando

```powershell
.\Scripts\setup-wildfly.ps1
```

**¡Eso es todo!** El script automatiza todo:

1. ✅ Descarga WildFly
2. ✅ Configura PostgreSQL
3. ✅ Crea el data source
4. ✅ Compila la app
5. ✅ Despliega en WildFly

---

## 📋 Requisitos (Solo 3)

Antes de ejecutar el comando anterior, verifica que tienes:

```bash
# 1. Java 21+
java -version

# 2. Maven 3.9+
mvn -version

# 3. PostgreSQL 12+ con acceso
psql --version
```

Si no tienes alguno, **[instálalo primero](README.md#requisitos-previos)**.

---

## 🚀 Después del Setup

Tu aplicación estará en:

```
Admin Console:  http://localhost:9990/console
Aplicación:     http://localhost:8080/chat-empresarial/
```

### Prueba rápida:

```bash
curl -X POST http://localhost:8080/chat-empresarial/api/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🎮 Después: Menú de Utilidades

Para controlar WildFly en cualquier momento:

```powershell
.\Scripts\manage-wildfly.ps1
```

Opciones:
- Ver estado
- Iniciar/Detener WildFly
- Ver logs
- Redeploy
- Probar API

---

## 🧹 Desinstalar

```powershell
.\Scripts\cleanup.ps1
```

---

## ❓ ¿Problemas?

1. **"Java no encontrado"** → [Instala Java 21+](README.md#requisitos-previos)
2. **"Maven no encontrado"** → [Instala Maven](README.md#requisitos-previos)
3. **"PostgreSQL no encontrado"** → [Instala PostgreSQL](README.md#requisitos-previos)
4. **Otro problema** → Ver [README.md](README.md#troubleshooting)

---

## 📚 Documentación

- **[INDEX.md](INDEX.md)** - Índice completo de scripts
- **[README.md](README.md)** - Documentación detallada
- **[../WILDFLY_SETUP.md](WILDFLY_SETUP.md)** - Configuración manual de WildFly

---

**¡A desplegar!** 🚀
