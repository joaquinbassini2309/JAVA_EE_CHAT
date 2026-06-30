# Reporte de Seguridad — JAVA_EE_CHAT
### Revisión Exhaustiva | 30/06/2026

---

## Resumen de Hallazgos

| # | Severidad | Archivo | Descripción corta |
|---|-----------|---------|-------------------|
| 1 | 🔴 CRÍTICA | `JWTUtil.java:21` | Clave secreta JWT hardcodeada en código fuente |
| 2 | 🔴 CRÍTICA | `UsuarioResource.java:109` | Google Client ID hardcodeado |
| 3 | 🔴 CRÍTICA | `Sistema.java:710` | Backdoor: username `"sudo - admin"` con privilegios globales |
| 4 | 🔴 CRÍTICA | `ArchivoResource.java:134` | Path Traversal en descarga sin verificación de límite de directorio |
| 5 | 🔴 CRÍTICA | `TareaResource.java` | Clase completa sin autenticación — IDOR masivo en todas las operaciones |
| 6 | 🔴 CRÍTICA | `Dockerfile:22-25,34` | Credenciales de BD y admin WildFly hardcodeadas en imagen Docker |
| 7 | 🟠 ALTA | `MensajeResource.java:100-117` | IDOR: lectura de mensajes de conversaciones ajenas |
| 8 | 🟠 ALTA | `UsuarioResource.java:46-53` | `GET /usuarios` sin autenticación: enumeración de todos los usuarios |
| 9 | 🟠 ALTA | `JWTFilter.java:34-37` | Filtro permisivo: ausencia de token permite acceso sin autenticación |
| 10 | 🟠 ALTA | `ChatWebSocketEndpoint.java:53-72` | `usuarioId` en WebSocket viene del cliente sin validar contra el JWT |
| 11 | 🟠 ALTA | `ChatWebSocketEndpoint.java:59-61` | Token JWT aceptado como fallback en query param de la URL |
| 12 | 🟠 ALTA | `api.js:282,309` | Token JWT transmitido en URL del WebSocket |
| 13 | 🟠 ALTA | `UsuarioResource.java:90-96` | Verificación de Google OAuth via HTTP externo (tokeninfo API) |
| 14 | 🟠 ALTA | `TareaResource.java:52-56` | Stack trace completo de excepción enviado al cliente |
| 15 | 🟠 ALTA | `Sistema.java:496-502` | `encriptarMensaje()` usa Base64 — no es cifrado |
| 16 | 🟠 ALTA | `Sistema.java:429-432` | `marcarMensajeComoLeido()` sin validación de acceso (TODO abierto) |
| 17 | 🟠 ALTA | `TareaResource.java:101-103` | `usuarioId` en body de actualización de tarea no se valida contra JWT |
| 18 | 🟠 ALTA | `MensajeComp.vue:48,152-185` | XSS potencial via `v-html` con sanitización manual frágil |
| 19 | 🟡 MODERADA | `nginx.conf` | Sin cabeceras HTTP de seguridad (CSP, X-Frame-Options, HSTS, etc.) |
| 20 | 🟡 MODERADA | `nginx.conf` + `docker-compose.yml` | Sin HTTPS/TLS |
| 21 | 🟡 MODERADA | Todos los `Resource.java` | Sin CORS configurado explícitamente en el backend |
| 22 | 🟡 MODERADA | `JWTFilter.java:70-73` | `isUserInRole()` siempre retorna `false` — autorización por roles nunca funciona |
| 23 | 🟡 MODERADA | `nginx.conf` + `UsuarioResource.java` | Sin rate limiting en endpoints de autenticación |
| 24 | 🟡 MODERADA | `ManejadorMensaje.java:194` | Wildcards SQL sin escapar en cláusula LIKE (`%`, `_`) |
| 25 | 🟡 MODERADA | Todos los DTOs | Ausencia de Bean Validation (`@NotNull`, `@Size`, `@Email`) |
| 26 | 🟡 MODERADA | `persistence.xml:46` | `hibernate.hbm2ddl.auto=update` en producción |
| 27 | 🟡 MODERADA | `api.js:20` | JWT almacenado en `localStorage`, vulnerable a XSS |
| 28 | 🟡 MODERADA | `enrutador.js:36-49` | Guardia de navegación solo client-side |
| 29 | 🟡 MODERADA | `JWTUtil.java:23` | Sin mecanismo de invalidación de tokens (logout no invalida el JWT) |
| 30 | 🟡 MODERADA | `AuthService.java:40-53` | Reflexión insegura con `@SuppressWarnings("unchecked")` para resolver IDs |
| 31 | 🟡 MODERADA | `nginx.conf` | `client_max_body_size 25M` sin rate limiting en subida de archivos |
| 32 | 🟡 MODERADA | `rest.SpaFilter` + `seguridad.SpaFilter` | Dos clases `SpaFilter` duplicadas mapeadas a `/*` |
| 33 | 🟡 MODERADA | `Usuario.java` | Campo `favoritos` almacena IDs como CSV en string |
| 34 | 🟡 MODERADA | Múltiples archivos | Logs de debug con datos sensibles (`System.out.println("DEBUG ...")`) |
| 35 | 🟡 MODERADA | Múltiples archivos | `e.getMessage()` de excepción enviado directamente al cliente |
| 36 | 🟡 MODERADA | `GlobalExceptionMapper.java:76` | Nombre de clase de excepción expuesto al cliente |
| 37 | 🔵 BAJA | `.env:4` | Contraseña de PostgreSQL trivial (`postgres`) en texto plano |
| 38 | 🔵 BAJA | `DtUsuario.java:78` | `UsuarioResponseDTO` expone `email` a todos los usuarios |
| 39 | 🔵 BAJA | `ArchivoResource.java` | Sin validación de tipo MIME real (magic bytes) en subida |
| 40 | 🔵 BAJA | `persistence.xml` | Comentario con placeholder de contraseña (`password=tu_password`) |
| 41 | 🔵 BAJA | Toda la app | Sin logging de seguridad (intentos fallidos de login, accesos denegados) |
| 42 | 🔵 BAJA | `Dockerfile:27,41` | Instrucciones ejecutadas como `root` innecesariamente |
| 43 | 🔵 BAJA | `setup.cli:37-43` | `ExampleDS` creado con credenciales de producción |
| 44 | 🔵 BAJA | `CapaPresentacion` | Sin auditoría de dependencias npm/Maven |
| 45 | 🔵 BAJA | `StartupInitializer.java` | `System.err.println` con stack traces en stdout |
| 46 | 🔵 BAJA | `enrutador.js` | Ruta `/registro` accesible si el almacén Pinia se manipula en DevTools |

---

## 🔴 CRÍTICAS

---

### 1 — Clave Secreta JWT Hardcodeada
* **Archivo**: [`JWTUtil.java:21`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/seguridad/JWTUtil.java#L21)
* **OWASP**: A02:2021 Cryptographic Failures
* **Descripción**: `"tu-clave-secreta-super-segura-cambiar-en-produccion"` firma todos los JWT del sistema. El propio nombre deja claro que nunca fue reemplazada. Cualquier persona con acceso al repositorio puede forjar tokens para cualquier `usuarioId`.
* **Mitigación**:
  ```java
  private static final String SECRET_KEY = Objects.requireNonNull(
      System.getenv("JWT_SECRET_KEY"), "JWT_SECRET_KEY no configurado"
  );
  ```

---

### 2 — Google Client ID Hardcodeado
* **Archivo**: [`UsuarioResource.java:109`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/UsuarioResource.java#L109)
* **OWASP**: A02:2021 Cryptographic Failures / A05:2021 Security Misconfiguration
* **Descripción**: `"623580687397-vd3dbk2u500dsda1tiact908fc4lq9s7.apps.googleusercontent.com"` expuesto en el repositorio. Además el token se verifica llamando a `tokeninfo` via HTTP GET (línea 90), lo cual Google desaconseja explícitamente para producción porque el token viaja en la URL y queda en logs.
* **Mitigación**: Variable de entorno `GOOGLE_CLIENT_ID`. Reemplazar el HTTP GET por la librería `com.google.api-client:google-api-client` para verificación criptográfica local.

---

### 3 — Backdoor: Username Privilegiado Hardcodeado
* **Archivo**: [`Sistema.java:710`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Sistema/Sistema.java#L710)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**:
  ```java
  boolean esGlobalAdmin = p.getUsuario() != null && "sudo - admin".equals(p.getUsuario().getUsername());
  ```
  Cualquier usuario que se registre con el username `sudo - admin` obtiene privilegios de super-administrador para eliminar cualquier conversación del sistema. No existe ninguna restricción en `registrarUsuario()` que bloquee este nombre.
* **Mitigación**: Eliminar esta lógica. Implementar un campo `rolGlobal` en la entidad `Usuario` con valores `USUARIO`, `ADMIN_GLOBAL`. Añadir lista negra de usernames reservados en el registro.

---

### 4 — Path Traversal en Descarga de Archivos
* **Archivo**: [`ArchivoResource.java:134`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/ArchivoResource.java#L134)
* **OWASP**: A01:2021 Broken Access Control / A03:2021 Injection
* **Descripción**: `.normalize()` está presente pero no hay validación de que el path resuelto permanezca dentro de `STORAGE_DIR`. El endpoint de descarga no requiere autenticación. Un atacante puede solicitar `../../../../etc/passwd` como nombre de archivo.
* **Mitigación**:
  ```java
  Path storagePath = Paths.get(STORAGE_DIR).toRealPath();
  Path target = storagePath.resolve(nombre).normalize();
  if (!target.startsWith(storagePath)) {
      return Response.status(403).build();
  }
  ```
  Añadir verificación de autenticación al endpoint de descarga.

---

### 5 — TareaResource Completamente sin Autenticación (IDOR Masivo)
* **Archivo**: [`TareaResource.java:27-162`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/TareaResource.java#L27)
* **OWASP**: A01:2021 Broken Access Control / A07:2021 Identification and Authentication Failures
* **Descripción**: La clase entera no inyecta `AuthService` ni `SecurityContext`. Consecuencias:
  - `GET /tareas/{usuarioId}` — cualquier usuario anónimo puede ver las tareas de cualquier persona.
  - `POST /tareas` — crear tareas en nombre de cualquier `creadorId`.
  - `PUT /tareas/{tareaId}/estado` — actualizar tareas ajenas pasando `usuarioId` en el body.
  - `DELETE /tareas/{tareaId}` — eliminar tareas ajenas pasando `usuarioId` en query param.
* **Mitigación**: Inyectar `AuthService` + `SecurityContext`. Extraer `userId` exclusivamente del JWT. Validar que el `usuarioId` del request coincida con el autenticado.

---

### 6 — Credenciales Hardcodeadas en la Imagen Docker
* **Archivo**: [`Dockerfile:22-25,34`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/Dockerfile#L22)
* **OWASP**: A07:2021 Identification and Authentication Failures
* **Descripción**:
  ```dockerfile
  ENV DB_PASSWORD=postgres
  RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent
  ```
  La imagen Docker contiene la contraseña de la base de datos y crea un usuario administrador WildFly con credenciales `admin/admin`. Cualquier persona que extraiga la imagen tiene acceso a ambas cosas.
* **Mitigación**: Eliminar los `ENV` con valores por defecto del Dockerfile. Usar `ARG` con Docker BuildKit Secrets. La contraseña del admin de WildFly debe generarse aleatoriamente en el entrypoint.

---

## 🟠 ALTAS

---

### 7 — IDOR en GET /api/v1/mensajes/{id}
* **Archivo**: [`MensajeResource.java:100-117`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/MensajeResource.java#L100)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: El endpoint obtiene el mensaje por ID sin verificar que el usuario autenticado sea participante de la conversación a la que pertenece. Cualquier usuario autenticado puede leer mensajes de conversaciones privadas ajenas si conoce el ID del mensaje.
* **Mitigación**:
  ```java
  if (!sistema.usuarioEstaEnConversacion(usuarioId, msg.getConversacion().getId())) {
      return Response.status(403).build();
  }
  ```

---

### 8 — GET /usuarios sin Autenticación: Enumeración de Usuarios
* **Archivo**: [`UsuarioResource.java:46-53`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/UsuarioResource.java#L46)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: Devuelve username, email, foto y estado de todos los usuarios del sistema. No requiere autenticación porque `JWTFilter` hace `return` si no hay header `Authorization` (ver hallazgo 9).
* **Mitigación**: Exigir token válido. Retirar `email` del `UsuarioResponseDTO` público.

---

### 9 — JWTFilter Permisivo: Ausencia de Token Permite Acceso
* **Archivo**: [`JWTFilter.java:34-37`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/seguridad/JWTFilter.java#L34)
* **OWASP**: A07:2021 Identification and Authentication Failures
* **Descripción**:
  ```java
  if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return; // pasa la petición sin SecurityContext válido
  }
  ```
  Si no hay header `Authorization`, el filtro no interrumpe el request. Llega al recurso REST sin usuario autenticado. Todos los endpoints que no verifican manualmente el `SecurityContext` quedan expuestos.
* **Mitigación**: Implementar whitelist de rutas públicas (`/login`, `/register`, `/login-google`) y rechazar con 401 todo lo demás si no hay token:
  ```java
  if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      if (!esRutaPublica(requestContext.getUriInfo().getPath())) {
          requestContext.abortWith(Response.status(401).build());
      }
      return;
  }
  ```

---

### 10 — WebSocket: `usuarioId` del Path Param no Validado contra JWT
* **Archivo**: [`ChatWebSocketEndpoint.java:53-72`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/websocket/ChatWebSocketEndpoint.java#L53)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: El endpoint es `ws/conversacion/{conversacionId}/usuario/{idUsuario}`. El `idUsuario` lo envía el cliente en la URL. Aunque el JWT se valida, no se verifica que el `subject` del token coincida con `idUsuario`. Un atacante con token válido puede conectar un WebSocket haciéndose pasar por otra persona: envía mensajes como si fuera ese usuario.
* **Mitigación**:
  ```java
  Long idDelToken = servicioAutenticacion.validarTokenYExtraerIdUsuario(token);
  if (!idDelToken.equals(idUsuario)) {
      sesion.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Identidad inválida"));
      return;
  }
  ```

---

### 11 — JWT Aceptado como Query Param en WebSocket
* **Archivo**: [`ChatWebSocketEndpoint.java:59-61`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/websocket/ChatWebSocketEndpoint.java#L59) y [`api.js:282`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaPresentacion/Web/src/servicios/api.js#L282)
* **OWASP**: A02:2021 Cryptographic Failures
* **Descripción**: El token se pasa como `?token=<jwt>` en la URL. Las URLs quedan en logs de Nginx, logs de WildFly, historial del navegador y proxies intermedios.
* **Mitigación**: Eliminar el fallback de query param. `ChatWebSocketConfigurator` ya extrae el token del header — usar solo esa vía. En el frontend, pasar el token via `Sec-WebSocket-Protocol`.

---

### 12 — Verificación de Google OAuth via HTTP Externo
* **Archivo**: [`UsuarioResource.java:90-96`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/UsuarioResource.java#L90)
* **OWASP**: A07:2021 Identification and Authentication Failures
* **Descripción**: Llamada HTTP GET a `https://oauth2.googleapis.com/tokeninfo?id_token=<token>`. Problemas: (1) el token viaja en la URL del GET, exponiéndose en logs; (2) dependencia de disponibilidad de red; (3) Google lo desaconseja explícitamente en documentación oficial para producción.
* **Mitigación**: Usar `GoogleIdTokenVerifier` de `google-auth-library-java` para verificación criptográfica local de la firma RS256 del token.

---

### 13 — Stack Trace Completo Enviado al Cliente
* **Archivo**: [`TareaResource.java:52-56`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/TareaResource.java#L52)
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**:
  ```java
  StringWriter sw = new StringWriter();
  e.printStackTrace(new PrintWriter(sw));
  return Response.status(500).entity(new ErrorResponse(sw.toString())).build();
  ```
  El stack trace completo expone estructura de paquetes, nombres de clases internas, versiones de drivers JDBC y lógica interna de la aplicación.
* **Mitigación**: Loguear internamente con SLF4J. Devolver al cliente solo `{"error": "Error interno del servidor"}`.

---

### 14 — `encriptarMensaje()` usa Base64 — No es Cifrado
* **Archivo**: [`Sistema.java:496-502`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Sistema/Sistema.java#L496)
* **OWASP**: A02:2021 Cryptographic Failures
* **Descripción**: Base64 es codificación, no cifrado. Es trivialmente reversible sin clave. Si este método se usa en flujos con datos sensibles, da una falsa sensación de seguridad.
* **Mitigación**: Si se requiere cifrado real, usar AES-256-GCM con una clave gestionada de forma segura en variables de entorno.

---

### 15 — `marcarMensajeComoLeido()` sin Validación de Acceso
* **Archivo**: [`Sistema.java:429-432`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Sistema/Sistema.java#L429)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: El `TODO` en el código confirma explícitamente que falta la validación. Cualquier usuario autenticado puede marcar mensajes de conversaciones ajenas como leídos.
* **Mitigación**: Implementar la validación antes de la llamada al handler: verificar que el mensaje pertenece a una conversación donde el usuario participa.

---

### 16 — `usuarioId` en Body de PUT /tareas sin Validar contra JWT
* **Archivo**: [`TareaResource.java:101-103`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/TareaResource.java#L101)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: El endpoint acepta `usuarioId` del body JSON del cliente. Un atacante envía el ID de otra persona para ejecutar la operación con su identidad.
* **Mitigación**: Ignorar `usuarioId` del body. Obtenerlo únicamente del `SecurityContext`.

---

### 17 — XSS Potencial via `v-html` con Sanitización Manual
* **Archivo**: [`MensajeComp.vue:48,152-185`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaPresentacion/Web/src/componentes/MensajeComp.vue#L48)
* **OWASP**: A03:2021 Injection (XSS)
* **Descripción**: El contenido de mensajes se renderiza con `v-html`. La sanitización manual (escape de `&`, `<`, `>`, `"`, `'`) es frágil. El `style` inline inyectado en línea 181 abre CSS Injection. Cualquier cambio futuro en la lógica de formateo puede introducir XSS.
* **Mitigación**: Usar [DOMPurify](https://github.com/cure53/DOMPurify): `v-html="DOMPurify.sanitize(contenidoFormateado)"`. Sustituir `style` inline por clases CSS predefinidas.

---

## 🟡 MODERADAS

---

### 18 — Sin Cabeceras HTTP de Seguridad
* **Archivo**: [`nginx.conf`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/nginx.conf)
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**: Faltan: `Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security`, `Referrer-Policy`, `Permissions-Policy`.
* **Mitigación**:
  ```nginx
  add_header X-Content-Type-Options "nosniff" always;
  add_header X-Frame-Options "DENY" always;
  add_header Content-Security-Policy "default-src 'self'; connect-src 'self' wss:;" always;
  add_header Referrer-Policy "strict-origin-when-cross-origin" always;
  ```

---

### 19 — Sin HTTPS/TLS
* **Archivos**: [`nginx.conf:10`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/nginx.conf#L10) + [`docker-compose.yml`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/docker-compose.yml)
* **OWASP**: A02:2021 Cryptographic Failures
* **Descripción**: Todo el tráfico va por HTTP plano (puerto 8081). Los JWT y contraseñas viajan en texto claro por la red.
* **Mitigación**: Configurar TLS en Nginx con Let's Encrypt. Redirigir HTTP → HTTPS.

---

### 20 — Sin CORS Configurado Explícitamente
* **Archivos**: `JAXRSConfiguration.java` + todos los `*Resource.java`
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**: No existe `CorsFilter` ni configuración CORS en JAX-RS. El comportamiento real depende de la configuración de WildFly, que puede ser permisivo por defecto.
* **Mitigación**: Implementar un `ContainerResponseFilter` con `Access-Control-Allow-Origin` con dominio específico (nunca `*` con credenciales).

---

### 21 — `isUserInRole()` Siempre Retorna `false`
* **Archivo**: [`JWTFilter.java:70-73`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/seguridad/JWTFilter.java#L70)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**:
  ```java
  public boolean isUserInRole(String role) {
      // TODO: Implementar roles si es necesario
      return false;
  }
  ```
  Cualquier anotación `@RolesAllowed` nunca funcionará. Toda la autorización es manual, distribuida por el código, sin un punto central de control.
* **Mitigación**: Implementar roles en el `SecurityContext` usando claims del JWT, o migrar a MicroProfile JWT.

---

### 22 — Sin Rate Limiting en Autenticación
* **Archivos**: [`nginx.conf`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/nginx.conf) + [`UsuarioResource.java`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/UsuarioResource.java)
* **OWASP**: A07:2021 Identification and Authentication Failures
* **Descripción**: Los endpoints `/login`, `/register`, `/login-google` no tienen ningún límite de intentos. Permite ataques de fuerza bruta y registro masivo de cuentas.
* **Mitigación**:
  ```nginx
  limit_req_zone $binary_remote_addr zone=auth:10m rate=5r/m;
  location /api/v1/usuarios/login {
      limit_req zone=auth burst=3 nodelay;
  }
  ```

---

### 23 — Wildcards SQL sin Escapar en LIKE
* **Archivo**: [`ManejadorMensaje.java:194`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Manejadores/ManejadorMensaje.java#L194)
* **OWASP**: A03:2021 Injection
* **Descripción**: Patrón LIKE construido como `"%@" + username + "%"` sin escapar. Si el username contiene `%` o `_`, la consulta JPQL se comporta de forma inesperada, pudiendo retornar resultados incorrectos o forzar full-scans costosos.
* **Mitigación**:
  ```java
  String usernameSafe = username.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  ```

---

### 24 — Ausencia Total de Bean Validation en DTOs
* **Archivos**: Todos los DTOs en `chat/Datatype/` y los record/class internos en `*Resource.java`
* **OWASP**: A03:2021 Injection
* **Descripción**: Ningún DTO tiene `@NotNull`, `@NotBlank`, `@Size`, `@Email`. Sin longitud máxima en campos de texto, un atacante puede enviar strings de 100MB y saturar la base de datos o memoria.
* **Mitigación**: Añadir `@Valid` en parámetros de endpoints y anotaciones de validación en todos los DTOs.

---

### 25 — `hbm2ddl.auto=update` en Producción
* **Archivo**: [`persistence.xml:46`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/resources/META-INF/persistence.xml#L46)
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**: Hibernate modifica automáticamente el esquema de la BD en cada despliegue. Un bug en las entidades JPA puede corromper tablas en producción.
* **Mitigación**: Usar `validate` en producción. Gestionar migraciones con Flyway o Liquibase.

---

### 26 — JWT Almacenado en `localStorage`
* **Archivo**: [`api.js:20`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaPresentacion/Web/src/servicios/api.js#L20)
* **OWASP**: A02:2021 Cryptographic Failures
* **Descripción**: `localStorage` es accesible para cualquier script JavaScript en la página. Una vulnerabilidad XSS roba el token completamente.
* **Mitigación**: Almacenar el JWT en una cookie `HttpOnly; Secure; SameSite=Strict`.

---

### 27 — Sin Mecanismo de Invalidación de Tokens (Logout Real)
* **Archivos**: `JWTUtil.java`, `Sistema.java`, `api.js`
* **OWASP**: A07:2021 Identification and Authentication Failures
* **Descripción**: El logout solo elimina el token del `localStorage`. El token sigue siendo válido por 24 horas en el servidor. Un token interceptado previamente no puede ser revocado.
* **Mitigación**: Implementar lista negra de tokens (Redis o tabla en BD) o usar refresh tokens de corta duración.

---

### 28 — Logs de Debug con Datos Sensibles
* **Archivos**:
  - [`Sistema.java:706-707`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Sistema/Sistema.java#L706) — IDs de conversación, usuario, tipo y rol
  - [`MensajeResource.java:144,147`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/MensajeResource.java#L144) — IDs de usuario y conversación
  - [`ManejadorParticipante.java:68`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Manejadores/ManejadorParticipante.java#L68) — IDs internos
* **OWASP**: A09:2021 Security Logging Failures
* **Descripción**: `System.out.println("DEBUG...")` en producción filtra datos de negocio a los logs de Docker/WildFly sin control de acceso.
* **Mitigación**: Reemplazar con SLF4J nivel `TRACE`. Desactivar en producción.

---

### 29 — `e.getMessage()` de Excepción Enviado al Cliente
* **Archivos**: [`UsuarioResource.java:167`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/UsuarioResource.java#L167), [`ArchivoResource.java:121`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/ArchivoResource.java#L121), [`MensajeResource.java:92`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/MensajeResource.java#L92)
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**: Los mensajes de excepción del driver JDBC, Hibernate y capas internas se devuelven directamente en la respuesta HTTP al cliente.
* **Mitigación**: Devolver siempre `{"error": "Error interno del servidor"}`. Loguear el detalle internamente.

---

### 30 — `GlobalExceptionMapper` Expone Nombre de Clase de Excepción
* **Archivo**: [`GlobalExceptionMapper.java:76`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/exceptions/GlobalExceptionMapper.java#L76)
* **OWASP**: A05:2021 Security Misconfiguration
* **Descripción**: `exception.getClass().getSimpleName() + ": " + exception.getMessage()` expone la jerarquía de clases interna en la respuesta HTTP.
* **Mitigación**: Devolver siempre mensaje genérico. Loguear la clase de excepción solo en servidor.

---

### 31 — Reflexión Insegura en `AuthService`
* **Archivo**: [`AuthService.java:40-53`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/seguridad/AuthService.java#L40)
* **OWASP**: A01:2021 Broken Access Control
* **Descripción**: `user.getClass().getMethod("getId")` con `@SuppressWarnings("unchecked")`. Si la resolución falla silenciosamente devuelve `null`, lo que puede causar acceso anónimo no detectado en los endpoints que dependen de este método.
* **Mitigación**: Usar tipado fuerte directamente: `((Usuario) userOpt.get()).getId()`.

---

### 32 — Dos `SpaFilter` Duplicadas Mapeadas a `/*`
* **Archivos**: `rest.SpaFilter.java` + `seguridad.SpaFilter.java`
* **Descripción**: Dos clases `SpaFilter` registradas para el mismo path `/*`. El comportamiento de cuál se ejecuta primero es indeterminado y puede causar conflictos de seguridad.
* **Mitigación**: Eliminar una. Dejar la más restrictiva (la del paquete `seguridad`).

---

## 🔵 BAJAS

---

### 33 — `.env` con Contraseña Trivial Posiblemente Commiteado
* **Archivo**: [`.env:4`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/.env#L4)
* **Descripción**: `DB_PASSWORD=postgres`. Verificar que `.env` está en `.gitignore` y que no fue commiteado al historial de Git:
  ```bash
  git log --all --full-history -- .env
  ```

---

### 34 — Comentario con Placeholder de Contraseña en persistence.xml
* **Archivo**: [`persistence.xml:14-21`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/resources/META-INF/persistence.xml#L14)
* **Descripción**: `<!-- password=tu_password -->` en el historial de Git puede ocultar credenciales reales si alguna vez fueron escritas allí.

---

### 35 — `UsuarioResponseDTO` Expone Email de Todos los Usuarios
* **Archivo**: [`DtUsuario.java:78`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaNegocio/src/main/java/chat/Datatype/DtUsuario.java#L78)
* **Descripción**: El campo `email` se incluye en la respuesta pública de listado de usuarios, permitiendo enumeración masiva de emails.

---

### 36 — Sin Validación de Tipo MIME Real en Subida de Archivos
* **Archivo**: [`ArchivoResource.java`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/CapaServicio/src/main/java/rest/ArchivoResource.java)
* **Descripción**: No se verifica el tipo real del archivo via magic bytes. Un atacante puede subir un ejecutable renombrándolo con extensión `.jpg`.

---

### 37 — Sin Logging de Seguridad
* **Toda la app**
* **OWASP**: A09:2021 Security Logging Failures
* **Descripción**: No se registran: intentos de login fallidos, accesos denegados (403), tokens inválidos. Es imposible detectar ataques de fuerza bruta o IDOR sin estos logs.

---

### 38 — Dockerfile Ejecuta Instrucciones como `root` Innecesariamente
* **Archivo**: [`Dockerfile:27,41`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/Dockerfile#L27)
* **Descripción**: Múltiples cambios entre `USER root` y `USER jboss`. El principio de mínimos privilegios requiere minimizar las instrucciones ejecutadas como root.

---

### 39 — `ExampleDS` con Credenciales de Producción
* **Archivo**: [`setup.cli:37-43`](file:///home/guzman-perera/Documentos/UTEC/JAVA%20EE/JAVA_EE_CHAT/setup.cli#L37)
* **Descripción**: Segundo datasource innecesario con las mismas credenciales de producción, amplía la superficie de ataque.

---

### 40 — Sin Auditoría de Dependencias
* **OWASP**: A06:2021 Vulnerable and Outdated Components
* **Descripción**: Sin `npm audit` ni `mvn dependency-check:check` en el pipeline. Dependencias con vulnerabilidades conocidas pueden estar en uso.

---

## Resumen Ejecutivo

| Severidad | Cantidad |
|-----------|---------|
| 🔴 Crítica | 6 |
| 🟠 Alta | 11 |
| 🟡 Moderada | 15 |
| 🔵 Baja | 8 |
| **Total** | **40** |

### Las 3 acciones inmediatas más urgentes:
1. **`JWTUtil.java:21`** — Rotar el secret JWT. Todos los tokens actuales son forjables por cualquier persona con acceso al repositorio.
2. **`TareaResource.java`** — La clase entera es un IDOR masivo sin autenticación. Endpoint abierto a internet.
3. **`Sistema.java:710`** — Eliminar la backdoor `"sudo - admin"`. Cualquiera puede registrarse con ese username y tener privilegios globales.
