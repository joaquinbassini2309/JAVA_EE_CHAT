# Arquitectura del Proyecto: Chat Empresarial

Este documento describe la arquitectura de la aplicación de chat, que está organizada en un sistema de tres capas bien definidas: Negocio, Servicio y Presentación.

---

## 1. Capa de Negocio (`CapaNegocio`)

Es el corazón de la aplicación. Contiene toda la lógica de negocio, las reglas, las entidades de datos y el acceso a la base de datos. Es un proyecto Maven independiente que no sabe nada sobre la web o las APIs REST; su única responsabilidad es ejecutar las reglas del dominio.

### Componentes Clave:
*   **`clases` (Entidades):** Clases de persistencia (JPA) que se mapean a las tablas de la base de datos (`Usuario`, `Conversacion`, etc.).
*   **`Manejadores` (Handlers/Repositorios):** Clases responsables de las operaciones directas con la base de datos (CRUD) usando `EntityManager`.
*   **`Sistema` (Fachada/Facade):** Punto de entrada único a la capa de negocio (`ISistema`, `Sistema`). Orquesta los manejadores y aplica las reglas de negocio.
*   **`Enum`:** Define los tipos y estados fijos del sistema (`RolParticipante`, `TipoConversacion`, etc.).
*   **`Datatype` (DTOs):** Clases planas (`DtUsuario`, `DtConversacion`) para transferir datos de forma segura entre capas.
*   **`Observer`:** Implementación del patrón de diseño Observer para la comunicación en tiempo real.

---

## 2. Capa de Servicio (`CapaServicio`)

Actúa como un puente entre el frontend y la capa de negocio. Su función es exponer la lógica de negocio como una API RESTful y gestionar la comunicación en tiempo real.

### Componentes Clave:
*   **`rest` (Recursos JAX-RS):** Clases que definen los *endpoints* de la API.
*   **`seguridad`:** Clases para la autenticación y autorización.
*   **`websocket`:** Endpoints para la comunicación bidireccional en tiempo real.
*   **`exceptions`:** Clases para el manejo centralizado de errores.

---

## 3. Capa de Presentación (`CapaPresentacion/Web`)

Es la interfaz con la que interactúa el usuario, construida como una **Single Page Application (SPA)** con **Vue.js** y **Vuetify**.

### Componentes Clave:
*   **`vistas`:** Componentes de alto nivel que representan una "página" (`ChatVista.vue`).
*   **`componentes`:** Bloques reutilizables de la interfaz (`ListaConversaciones.vue`, `Chat.vue`).
*   **`servicios/api.js`:** Cliente API centralizado (`axios`) para comunicarse con la `CapaServicio`.
*   **`almacenes/almacen.js` (Pinia Store):** Gestor de estado global para la información compartida.
*   **`rutas/enrutador.js`:** Configuración de las rutas de la aplicación (`vue-router`).

---

## Detalles de Implementación de la Capa de Servicio

### Seguridad Basada en Tokens (JWT)

La seguridad de la API se gestiona mediante JSON Web Tokens (JWT), un estándar sin estado que evita tener que almacenar sesiones en el servidor. El flujo es el siguiente:

1.  **Autenticación:** El usuario envía su email y contraseña al endpoint `/usuarios/login`.
2.  **Validación:** El `UsuarioResource` llama al `Sistema` para validar las credenciales.
3.  **Generación del Token:** Si las credenciales son correctas, `JWTUtil.generateToken()` crea un token. Este token es una cadena de texto codificada que contiene información (claims) como el ID del usuario y una fecha de expiración.
4.  **Respuesta al Cliente:** El servidor devuelve el token al cliente, que lo almacena de forma segura (en `localStorage`).

**Componentes en `/seguridad`:**

*   **`JWTUtil`:** Una clase de utilidad para generar y validar tokens. Contiene la clave secreta (que nunca debe exponerse) y define la configuración de los tokens (algoritmo de firma, tiempo de expiración).
*   **`JWTFilter`:** Es un filtro JAX-RS (`@Provider`) que se ejecuta **antes** de cada petición a un endpoint protegido. Su lógica es:
    1.  Extrae el token del encabezado `Authorization: Bearer <token>`.
    2.  Usa `JWTUtil` para validar la firma y la expiración del token.
    3.  Si el token es válido, extrae el ID del usuario y lo inyecta en el `SecurityContext` de la petición. Esto permite que los endpoints sepan qué usuario está realizando la acción.
    4.  Si el token es inválido o no existe, aborta la petición con un error `401 Unauthorized`.
*   **`AuthService`:** Un servicio inyectable (`@ApplicationScoped`) que simplifica el acceso al `SecurityContext` para obtener el ID del usuario autenticado desde cualquier endpoint.

### Comunicación en Tiempo Real (`/websocket`)

La comunicación en tiempo real se implementa con **Jakarta WebSocket API**.

*   **`ChatWebSocketEndpoint`:** Es la clase principal, anotada con `@ServerEndpoint`. Define el ciclo de vida de una conexión WebSocket.
    *   `@OnOpen`: Se ejecuta cuando un cliente se conecta. Aquí es donde se registra el WebSocket como un `ChatObserver` en el `Sistema`.
    *   `@OnMessage`: Se ejecuta cuando el servidor recibe un mensaje a través del WebSocket. Lo procesa y lo envía al `Sistema` para que se guarde en la base de datos.
    *   `@OnClose`: Se ejecuta cuando un cliente se desconecta. Elimina el `ChatObserver` de la lista del `Sistema`.
    *   `@OnError`: Maneja cualquier error que ocurra durante la conexión.
*   **`ChatWebSocketConfigurator`:** Una clase de configuración que permite usar **Inyección de Dependencias (CDI)** dentro de los endpoints de WebSocket. Esto es crucial para poder inyectar el `ISistema` en `ChatWebSocketEndpoint` y así conectar la capa de servicio con la de negocio.

### Manejo Centralizado de Excepciones (`/exceptions`)

Para evitar bloques `try-catch` repetitivos en cada endpoint y estandarizar las respuestas de error, se utiliza un `ExceptionMapper`.

*   **`GlobalExceptionMapper`:** Es una clase anotada con `@Provider` que actúa como un manejador de excepciones global para toda la API.
    *   Implementa `ExceptionMapper<Throwable>`, lo que le permite "atrapar" cualquier excepción no controlada que ocurra en los endpoints.
    *   Dentro de su método `toResponse()`, analiza el tipo de excepción y genera una `Response` HTTP apropiada con el código de estado correcto (400, 403, 500, etc.) y un cuerpo JSON estandarizado.
*   **`ErrorResponse`:** Un simple POJO (Plain Old Java Object) que modela la estructura del JSON de error que se envía al cliente. Contiene campos como `statusCode`, `message` y `details`.

---

### Gestión de Dependencias con `pom.xml`

El proyecto utiliza **Apache Maven** para gestionar las dependencias, la compilación y el empaquetado del backend. El archivo `pom.xml` en la raíz del proyecto es el fichero de configuración principal.

*   **Estructura Multi-módulo:** El `pom.xml` principal define una estructura multi-módulo, donde `CapaNegocio` y `CapaServicio` son módulos independientes. Esto permite que la `CapaNegocio` se pueda empaquetar como una librería (`.jar`) sin incluir las dependencias web de la `CapaServicio`.

*   **Dependencias Clave:**
    *   **Jakarta EE 10:** La dependencia principal es `jakarta.platform:jakarta.jakartaee-web-api:10.0.0`. Esta es la "lista de materiales" (BOM) que importa todas las APIs estándar de Jakarta EE 10, como:
        *   **JAX-RS (REST):** `jakarta.ws.rs-api`
        *   **JPA (Persistencia):** `jakarta.persistence-api`
        *   **CDI (Inyección de Dependencias):** `jakarta.inject-api`
        *   **WebSockets:** `jakarta.websocket-api`
    *   **Hibernate:** Es la implementación de JPA utilizada para la persistencia de datos.
    *   **jBCrypt:** Librería para el hasheo seguro de contraseñas.
    *   **jjwt:** Librería para la creación y validación de JSON Web Tokens (JWT).

### Funcionalidades de la `CapaPresentacion`

La interfaz de usuario desarrollada con Vue.js y Vuetify actualmente soporta las siguientes funcionalidades:

*   **Autenticación de Usuarios:**
    *   Formularios de Inicio de Sesión y Registro.
    *   Gestión de sesión de usuario mediante JWT.
    *   Redirección automática a `/login` si el usuario no está autenticado.

*   **Gestión de Conversaciones:**
    *   Lista de conversaciones del usuario, ordenadas por el mensaje más reciente.
    *   Creación de nuevas conversaciones privadas con otros usuarios.
    *   Creación de grupos.

*   **Chat en Tiempo Real:**
    *   Visualización de mensajes en una conversación activa.
    *   Envío de mensajes de texto.
    *   Recepción de nuevos mensajes en tiempo real a través de WebSockets sin necesidad de recargar.
    *   Scroll automático al recibir un nuevo mensaje.

*   **Opciones de Mensajes:**
    *   Menú contextual en los mensajes propios para "Ver Información" o "Eliminar".
    *   La eliminación de un mensaje se refleja en la interfaz.

*   **Gestión de Grupos (Panel de Información):**
    *   Visualización de la lista de miembros de un grupo.
    *   **Para Administradores y Moderadores:**
        *   Posibilidad de editar el nombre del grupo.
    *   **Solo para Administradores:**
        *   Menú de acciones sobre otros participantes para:
            *   Asignar/Quitar rol de `MODERADOR`.
            *   `SILENCIAR` a un miembro.
            *   Eliminar un miembro del grupo.
    *   Insignias visuales (`v-chip`) para identificar los roles de `ADMIN`, `MODERADOR` y `SILENCIADO`.

---

## Decisiones Tecnológicas Clave

*   **Arquitectura en Capas:** Se eligió para separar responsabilidades. La `CapaNegocio` puede evolucionar de forma independiente, y la `CapaPresentacion` podría ser reemplazada por una aplicación de escritorio o móvil sin afectar la lógica central.

*   **Jakarta EE (con JAX-RS y JPA):** Es un estándar robusto y maduro para construir aplicaciones empresariales en Java. Proporciona un marco sólido para la persistencia de datos (JPA) y la creación de servicios web (JAX-RS) que es ideal para un backend escalable.

*   **Vue.js con Vuetify:** Vue.js es un framework progresivo y fácil de aprender, ideal para construir SPAs reactivas. Vuetify, una librería de componentes Material Design, acelera enormemente el desarrollo del frontend al proporcionar componentes de UI listos para usar y bien diseñados, permitiendo centrarse en la funcionalidad en lugar de en el CSS.

*   **Pinia para la Gestión de Estado:** En una aplicación de chat, el estado es complejo y compartido por muchos componentes. Pinia ofrece un almacén centralizado, simple y muy eficiente para gestionar este estado, haciendo que la aplicación sea más predecible y fácil de depurar.

*   **JSON Web Tokens (JWT) para la Seguridad:** JWT es un estándar moderno y sin estado para la autenticación. Permite que el servidor verifique la identidad del usuario en cada petición sin necesidad de almacenar la información de la sesión en el propio servidor, lo que facilita la escalabilidad.
