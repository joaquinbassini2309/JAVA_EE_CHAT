# 📑 Índice de Scripts - SOLO POWERSHELL

Guía rápida de todos los scripts en esta carpeta (versión 100% PowerShell).

---

## 🚀 PRIMERO VEZ: Empieza aquí

### 1️⃣ setup-wildfly.ps1 (Principal)

**Función:** Automatiza TODO (descarga, instala, configura, despliega)

**Uso:**
```powershell
.\setup-wildfly.ps1
```

**Con contraseña:**
```powershell
.\setup-wildfly.ps1 -PostgresPassword "tu_password"
```

**Lo que hace:**
- Valida Java, Maven, PostgreSQL
- Descarga WildFly 32.0.1.Final
- Extrae en C:\wildfly-32.0.1.Final
- Inicia WildFly
- Crea data source ChatDS
- Crea BD chat_db en PostgreSQL
- Compila el proyecto (mvn clean package)
- Despliega el WAR en WildFly
- Muestra URLs finales

**Salida:**
```
http://localhost:9990/console      (Admin Console)
http://localhost:8080/chat-empresarial/   (App)
```

---

## 🎮 DESPUÉS DEL SETUP: Usa estos

### 2️⃣ manage-wildfly.ps1 (Menú Interactivo)

**Función:** Control total de WildFly desde un menú

**Uso:**
```powershell
.\manage-wildfly.ps1
```

**Opciones:**
```
1. Ver estado       → ¿WildFly y app están corriendo?
2. Iniciar          → Inicia WildFly
3. Detener          → Para WildFly
4. CLI              → Abre jboss-cli para comandos avanzados
5. Logs             → Muestra logs en tiempo real
6. Redeploy         → Recompila y vuelve a desplegar
7. Probar API       → Prueba endpoints con curl
8. Salir            → Cierra el menú
```

**Atajo directo sin menú:**
```powershell
# Ver estado
.\manage-wildfly.ps1 -Action status

# Otros atajos:
.\manage-wildfly.ps1 -Action start
.\manage-wildfly.ps1 -Action stop
.\manage-wildfly.ps1 -Action logs
.\manage-wildfly.ps1 -Action redeploy
.\manage-wildfly.ps1 -Action test
.\manage-wildfly.ps1 -Action cli
```

---

## 🧹 DESINSTALAR: Scripts de Limpieza

### 3️⃣ cleanup.ps1 (Desinstalar)

**Función:** Elimina WildFly y archivos de compilación

**Uso:**
```powershell
.\cleanup.ps1
```

**Elimina:**
- Directorio WildFly completo
- target/ (archivos compilados)
- Opcionalmente: caché de Maven (~/.m2/)

**NO elimina:**
- PostgreSQL o BD
- Código fuente
- Configuración (pom.xml, persistence.xml)

---

## 🔄 Flujo Típico de Uso

### Primer uso (Setup inicial):
```powershell
1. .\setup-wildfly.ps1
   ↓
   (Se descarga, instala, configura y despliega)
   ↓
2. Verificar en http://localhost:8080/chat-empresarial/
```

### Desarrollo (Cambios en código):
```powershell
1. Editar código
   ↓
2. .\manage-wildfly.ps1 -Action redeploy
```

### Monitoreo:
```powershell
.\manage-wildfly.ps1 -Action logs
```

### Detener WildFly:
```powershell
.\manage-wildfly.ps1 -Action stop
```

### Desinstalar todo:
```powershell
.\cleanup.ps1
```

---

## 📋 Requisitos Previos

Antes de ejecutar los scripts:

```
✅ Java 21+         → java -version
✅ Maven 3.9+       → mvn -version
✅ PostgreSQL 12+   → psql --version
❓ WildFly          → Se descarga automáticamente
```

---

## 🐛 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| "Java no encontrado" | Instala Java 21+ y agrega a PATH |
| "Maven no encontrado" | Instala Maven y agrega a PATH |
| "PostgreSQL no encontrado" | Instala PostgreSQL y agrega psql a PATH |
| "Puerto 8080 en uso" | `.\manage-wildfly.ps1 -Action stop` |
| "Permission denied" | Ejecuta como Administrador |
| "ExecutionPolicy error" | `Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser` |

---

## 📞 Archivos Relacionados

- **HOW-TO-USE-PS.txt** - Guía rápida de uso
- **QUICKSTART.md** - Setup en 30 segundos
- **README.md** - Documentación completa
- **WILDFLY_SETUP.md** - Guía de configuración manual
- **pom.xml** - Configuración Maven del proyecto
- **persistence.xml** - Configuración JPA/Hibernate

---

## ✨ Notas Importantes

✅ Scripts **idempotentes** - Ejecuta varias veces sin problema

✅ Si WildFly existe, te pregunta si descargar de nuevo

✅ Si la BD existe, la reutiliza

✅ Logs en tiempo real

✅ Cancela con `Ctrl+C`

✅ **100% PowerShell** - Sin .bat ni Command Prompt

---

**¡Listo para desplegar!** 🚀

Comando para empezar: `.\setup-wildfly.ps1`
