# Chat Empresarial - Frontend (Vue 3 + Vite + Pinia)

Frontend moderno para la aplicación de Chat Empresarial construido con Vue 3, Vite y Pinia.

## 📁 Estructura del Proyecto

```
CapaPresentacion/Web/
├── public/                    # Archivos estáticos
│   └── index.html            # HTML principal
├── src/
│   ├── assets/
│   │   └── estilos.css       # Estilos globales
│   ├── componentes/          # Componentes reutilizables
│   │   ├── Chat.vue          # Componente principal de chat
│   │   ├── ListaConversaciones.vue
│   │   └── Mensaje.vue
│   ├── vistas/               # Componentes de página
│   │   ├── LoginVista.vue    # Pantalla de login
│   │   └── ChatVista.vue     # Pantalla principal del chat
│   ├── servicios/            # Servicios API
│   │   └── api.js            # Cliente HTTP para el backend
│   ├── almacen/              # Store de Pinia
│   │   └── index.js          # Estado global
│   ├── utilidades/           # Funciones auxiliares
│   │   ├── helpers.js        # Funciones de validación y formato
│   │   └── formateoFechas.js # Funciones para formatear fechas
│   ├── router/
│   │   └── index.js          # Configuración de rutas
│   ├── App.vue               # Componente raíz
│   └── main.js               # Punto de entrada
├── package.json
├── vite.config.js            # Configuración de Vite
└── .gitignore
```

## 🚀 Instalación y Ejecución

### 1. Instalar dependencias
```bash
cd CapaPresentacion/Web
npm install
```

### 2. Desarrollo local
```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`

### 3. Compilar para producción
```bash
npm run build
```

Genera la carpeta `dist/` lista para despliegue.

## 📦 Dependencias Principales

- **Vue 3**: Framework progresivo para construcción de UI
- **Vite**: Herramienta de construcción rápida
- **Pinia**: State management moderno
- **Vue Router**: Enrutamiento de la aplicación
- **Axios**: Cliente HTTP para llamadas a API

## 🔧 Configuración

### Backend API
La aplicación espera el backend en:
```
http://localhost:8080/chat-empresarial/api/v1
```

Modifica `src/servicios/api.js` si es necesario:
```javascript
const API_BASE_URL = 'http://localhost:8080/chat-empresarial/api/v1'
```

### Proxy en desarrollo
Vite incluye un proxy para el desarrollo. Modifica en `vite.config.js`:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080/chat-empresarial',
    changeOrigin: true
  }
}
```

## 📖 Uso de Componentes

### Chat.vue
Componente principal que muestra el historial de mensajes y permite enviar nuevos.

```vue
<Chat />
```

### ListaConversaciones.vue
Muestra la lista de conversaciones del usuario.

```vue
<ListaConversaciones />
```

### Mensaje.vue
Componente individual para mostrar un mensaje.

```vue
<Mensaje :mensaje="mensaje" />
```

## 🏪 Estado con Pinia

### Usar el almacén
```javascript
import { useAlmacen } from '@/almacen'

const almacen = useAlmacen()

// Acceder al estado
console.log(almacen.usuarioActual)
console.log(almacen.conversaciones)

// Actualizar estado
almacen.establecerToken(token)
almacen.establecerConversacionActual(conversacion)
```

## 🔐 Autenticación

El token JWT se almacena en `localStorage` bajo la clave `token`.

El cliente HTTP (`api.js`) agrega automáticamente el token en el header:
```
Authorization: Bearer <token>
```

## 📅 Formateo de Fechas

Las funciones de formateo usan locale `es-ES`:

```javascript
import { formatearFecha, formatearHora, formatearFechaInteligente } from '@/utilidades/formateoFechas'

formatearFecha(fecha)           // "16 de abril de 2026"
formatearHora(fecha)            // "23:52"
formatearFechaInteligente(fecha) // "hace 5 minutos" o "hoy 23:52"
```

## 🛠️ Utilidades

### helpers.js
```javascript
import { 
  validarEmail,
  validarContraseña,
  truncarTexto,
  obtenerIniciales,
  generarColorDelNombre
} from '@/utilidades/helpers'
```

## 🌐 Variables de Entorno

Crea un archivo `.env.local` para variables de entorno:
```
VITE_API_BASE_URL=http://localhost:8080/chat-empresarial/api/v1
```

Accede en componentes con:
```javascript
import.meta.env.VITE_API_BASE_URL
```

## 🎨 Temas y Estilos

Los colores están definidos como variables CSS en `src/assets/estilos.css`:

```css
--color-primario: #667eea
--color-secundario: #764ba2
--color-exito: #10b981
```

## 📱 Responsive Design

Los componentes utilizan flexbox y grid para ser responsive.

## 🐛 Debugging

Habilita el Vue DevTools en el navegador para debugging.

## 📝 Notas

- Todos los textos están en español
- Los estilos son CSS puro (sin Tailwind)
- Componentes simples y reutilizables
- State management centralizado con Pinia

## 🚀 Deployment

### En WildFly (mismo servidor que backend)
1. Compilar: `npm run build`
2. Copiar `dist/` a `WildFly/standalone/deployments/ROOT/`
3. O servir desde cualquier servidor web estático (Nginx, Apache, etc.)

### Vercel, Netlify, etc.
```bash
npm run build
# Deploy la carpeta dist/
```

## 📚 Recursos

- [Vue 3 Docs](https://vuejs.org/)
- [Vite Docs](https://vitejs.dev/)
- [Pinia Docs](https://pinia.vuejs.org/)
- [Vue Router Docs](https://router.vuejs.org/)

## 📄 Licencia

MIT
