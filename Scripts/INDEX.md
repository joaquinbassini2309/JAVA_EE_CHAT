# 📑 Índice de Scripts

Guía rápida de todos los scripts en esta carpeta.

---

## 🚀 PRIMERO VEZ: Empieza aquí

### 1️⃣ run-setup.bat (Principal)

**Función:** Automatiza TODO (descarga, instala, configura, despliega)

**Uso:**
```batch
.\run-setup.bat
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

### 2️⃣ manage-wildfly.bat (Menú Interactivo)

**Función:** Control total de WildFly desde un menú

**Uso:**
```batch
.\manage-wildfly.bat
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

**Atajo directo desde PowerShell:**
```powershell
# Ver estado sin menú
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

### 3️⃣ cleanup.bat (Desinstalar)

**Función:** Elimina WildFly y archivos de compilación

**Uso:**
```batch
.\cleanup.bat
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

## 📄 Scripts PowerShell (Uso Avanzado)

### setup-wildfly.ps1
- Script principal de setup en PowerShell
- Llamado por `run-setup.bat`
- Uso directo para control fino

```powershell
.\setup-wildfly.ps1 `
    -PostgresPassword "tu_password" `
    -WildFlyVersion "32.0.1.Final" `
    -WildFlyDir "C:\wildfly-32.0.1.Final" `
    -ProjectRoot "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
```

### manage-wildfly.ps1
- Script de utilidades en PowerShell
- Llamado por `manage-wildfly.bat`
- Uso directo para acciones específicas

```powershell
# Menú interactivo
.\manage-wildfly.ps1

# O acciones directas
.\manage-wildfly.ps1 -Action status
.\manage-wildfly.ps1 -Action start
.\manage-wildfly.ps1 -Action stop
.\manage-wildfly.ps1 -Action logs
.\manage-wildfly.ps1 -Action redeploy
.\manage-wildfly.ps1 -Action test
.\manage-wildfly.ps1 -Action cli
```

### cleanup.ps1
- Script de limpieza en PowerShell
- Llamado por `cleanup.bat`
- Uso directo para desinstalar

```powershell
.\cleanup.ps1 `
    -WildFlyDir "C:\wildfly-32.0.1.Final" `
    -ProjectRoot "C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT"
```

---

## 🔄 Flujo Típico de Uso

### Primer uso (Setup inicial):
```
1. .\run-setup.bat
   ↓
   (Se descarga, instala, configura y despliega)
   ↓
2. Verificar en http://localhost:8080/chat-empresarial/
```

### Desarrollo (Cambios en código):
```
1. Editar código
   ↓
2. .\manage-wildfly.bat → Opción 6 (Redeploy)
   ↓
   o
   ↓
3. .\manage-wildfly.ps1 -Action redeploy
```

### Monitoreo:
```
.\manage-wildfly.bat → Opción 5 (Logs)

o

.\manage-wildfly.ps1 -Action logs
```

### Detener WildFly:
```
.\manage-wildfly.bat → Opción 3

o

.\manage-wildfly.ps1 -Action stop
```

### Desinstalar todo:
```
.\cleanup.bat
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
| "Puerto 8080 en uso" | Otro WildFly está corriendo → Stop-Process -Name java |
| "Permission denied" | Ejecuta PowerShell como Administrador |
| "Data source not found" | Ejecuta manage-wildfly.bat → Opción 4 (CLI) |

---

## 📞 Archivos Relacionados

- **README.md** - Documentación completa
- **WILDFLY_SETUP.md** - Guía de configuración manual
- **pom.xml** - Configuración Maven del proyecto
- **persistence.xml** - Configuración JPA/Hibernate

---

## ✨ Notas Importantes

✅ Los scripts son **idempotentes** (puedes ejecutarlos varias veces sin problemas)

✅ Si WildFly ya existe, te pregunta si descargar de nuevo

✅ Si la BD ya existe, la reutiliza

✅ Los logs se muestran en tiempo real

✅ Puedes cancelar con `Ctrl+C`

---

**¡Listo para desplegar!** 🚀
