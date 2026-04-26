# Guía de Administración del Frontend (Vue + Vite)

Esta guía explica la organización de carpetas y archivos en el módulo de presentación.

## Estructura de Carpetas (`src/`)

Para mantener el proyecto organizado y escalable, se utilizan las siguientes carpetas:

- **`src/assets/`**: Archivos estáticos procesados por Vite (CSS global, imágenes, fuentes).
- **`src/components/`**: Componentes de Vue reutilizables (ej. botones, tarjetas de chat, listas).
- **`src/views/`**: Componentes que representan "páginas" completas (ej. Login, Chat Principal).
- **`src/router/`**: Configuración de navegación entre páginas.
- **`src/stores/`**: Gestión del estado global de la aplicación (Pinia). Aquí se guardan los datos del usuario, token y mensajes.
- **`src/services/`**: Lógica de comunicación con el Backend (Axios).
- **`src/plugins/`**: Configuraciones de librerías externas (ej. Vuetify).
- **`src/utils/`**: Funciones de ayuda y constantes.

## Flujo de Navegación (Rutas)

Las rutas se administran en `src/router/index.js`. Aquí se mapea una URL a un componente en `src/views/`.

| URL | Componente | Carpeta |
| :--- | :--- | :--- |
| `/` | `LoginVista.vue` | `src/views/` |
| `/chat` | `ChatVista.vue` | `src/views/` |
| `/registro` | `RegistroVista.vue` | `src/views/` |

**Ejemplo de uso:**
Para ir a una página desde el código: `router.push('/chat')`.
Para ir desde el HTML: `<router-link to="/chat">Ir al Chat</router-link>`.

## Archivos en `public/`

La carpeta `public/` se utiliza para archivos que **no** deben ser procesados por Vite. 
- Se sirven directamente en la raíz de la aplicación.
- Los archivos legacy (`VistaPrincipal.html`, etc.) se encuentran aquí por compatibilidad, pero la aplicación principal ahora utiliza `index.html` en la raíz del proyecto.

## Cómo Compilar

Para generar los archivos finales que utiliza WildFly:
1. Asegúrate de estar en `CapaPresentacion/Web`.
2. Ejecuta `npm run build`.
3. Esto generará la carpeta `dist/`.
4. El archivo `pom.xml` de `CapaServicio` tomará automáticamente lo que esté en `dist/` para incluirlo en el `.war`.
