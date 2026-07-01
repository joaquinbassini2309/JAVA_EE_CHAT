<template>
  <div id="app-root">
    <!-- Panel Desplegable de Actualización PWA -->
    <transition name="desplazar-abajo">
      <div v-if="actualizarDisponible" class="banner-actualizacion">
        <div class="banner-contenido">
          <div class="banner-icono">
            <v-icon size="24" color="white">mdi-cellphone-arrow-down</v-icon>
          </div>
          <div class="banner-texto">
            <span class="titulo">Nueva versión disponible</span>
            <span class="subtitulo">Actualiza para recibir las últimas mejoras y correcciones.</span>
          </div>
          <button class="btn-actualizar" @click="aplicarActualizacion">
            <v-icon size="18" class="mr-1">mdi-refresh</v-icon>
            Actualizar
          </button>
        </div>
      </div>
    </transition>

    <!-- Panel Sin Conexión a Internet -->
    <transition name="desplazar-abajo">
      <div v-if="!estaOnline" class="banner-sin-conexion">
        <div class="banner-conexion-contenido">
          <div class="banner-conexion-icono">
            <v-icon size="22" color="white" class="icono-pulso">mdi-wifi-strength-alert-outline</v-icon>
          </div>
          <div class="banner-conexion-texto">
            <span class="titulo-conexion">Sin conexión a Internet</span>
            <span class="subtitulo-conexion">No recibirás nuevos mensajes y no podrás enviar mensajes en este momento.</span>
          </div>
        </div>
      </div>
    </transition>

    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useTheme } from 'vuetify'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const almacen = useAlmacen()
const theme = useTheme()

// WebSocket global de presencia y su intervalo
const wsPresencia = ref(null)
let intervaloPing = null
let timeoutReconexion = null

// Detección de conectividad offline/online
const estaOnline = ref(navigator.onLine)

const actualizarEstadoRed = () => {
  estaOnline.value = navigator.onLine
  if (navigator.onLine) {
    console.log('La red está de nuevo en línea, reconectando presencia...')
    conectarPresencia()
  }
}

const conectarPresencia = () => {
  if (!almacen.estaAutenticado || !almacen.usuarioActual || !almacen.token) return

  // Limpiar estados anteriores
  if (wsPresencia.value) {
    try { wsPresencia.value.close() } catch (e) {}
  }
  clearInterval(intervaloPing)
  clearTimeout(timeoutReconexion)

  const ws = servicioApi.conectarPresenciaWebSocket(almacen.usuarioActual.id, almacen.token)
  wsPresencia.value = ws

  ws.onopen = () => {
    console.log('WebSocket de Presencia conectado globalmente')
    // Iniciar ping cada 20 segundos para evitar timeout de inactividad de Wildfly/Nginx
    intervaloPing = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping')
      }
    }, 20000)
  }

  ws.onmessage = (event) => {
    try {
      if (event.data === 'pong') return
      const msg = JSON.parse(event.data)
      if (msg.tipo === 'PRESENCIA') {
        almacen.actualizarEstadoUsuario(msg.usuarioId, msg.estado)
      } else if (msg.tipo === 'NUEVA_CONVERSACION') {
        const c = msg.datos
        const existe = almacen.conversaciones.some(x => x.id === c.id)
        if (!existe) {
          almacen.agregarConversacion(c)
        }
      } else if (msg.tipo === 'NUEVO_MENSAJE_GLOBAL') {
        const m = msg.datos
        const existeConv = almacen.conversaciones.some(c => c.id === m.conversacionId)
        
        const procesarMensaje = () => {
          almacen.agregarMensaje(m)
          
          // Notificación de escritorio para mensajes entrantes de otros chats o cuando no hay foco
          if (typeof Notification !== 'undefined' && Notification.permission === 'granted') {
            const yoId = almacen.usuarioActual?.id
            const esActiva = almacen.conversacionActual && almacen.conversacionActual.id === m.conversacionId
            const chatFocado = esActiva && document.hasFocus()
            
            if (m.emisorId !== yoId && !chatFocado) {
              const conv = almacen.conversaciones.find(c => c.id === m.conversacionId)
              let nombre = 'Nuevo mensaje'
              if (conv) {
                if (conv.tipo === 'GRUPAL' || conv.tipo === 'AVISOS') {
                  nombre = conv.nombre
                } else if (conv.participantes) {
                  const otro = conv.participantes.find(p => p.usuario && p.usuario.id !== yoId)
                  if (otro && otro.usuario) nombre = otro.usuario.nombre || otro.usuario.username
                }
              }
              const username = almacen.usuarioActual?.username
              const esMencion = username && (m.contenido.includes(`@${username}`) || m.contenido.includes('@todos'))
              const titulo = esMencion ? `🔔 ¡Fuiste mencionado en ${nombre}!` : nombre
              
              new Notification(titulo, { body: m.contenido, icon: conv?.fotoUrl || null })
            }
          }
        }

        if (!existeConv) {
          // Si no tenemos el chat en el sidebar, lo cargamos desde la API
          servicioApi.obtenerConversacion(m.conversacionId).then(nuevaConv => {
            if (nuevaConv) {
              almacen.agregarConversacion(nuevaConv)
              procesarMensaje()
            }
          }).catch(err => {
            console.error('Error al obtener nueva conversación tras mensaje global:', err)
          })
        } else {
          procesarMensaje()
        }
      } else if (msg.tipo === 'NUEVA_INFO_USUARIO') {
        const datosUsuario = msg.datos
        almacen.actualizarInfoUsuarioGlobal(datosUsuario)
      } else if (msg.tipo === 'NUEVO_PARTICIPANTE') {
        const { conversacionId, participante } = msg.datos
        almacen.agregarParticipante(conversacionId, participante)
      }
    } catch (err) {
      console.error('Error procesando mensaje de presencia WS:', err)
    }
  }

  ws.onclose = (ev) => {
    console.log('WebSocket de Presencia cerrado:', ev.code, ev.reason)
    clearInterval(intervaloPing)
    // Reconectar si sigue autenticado tras 3 segundos
    if (almacen.estaAutenticado) {
      timeoutReconexion = setTimeout(() => {
        conectarPresencia()
      }, 3000)
    }
  }

  ws.onerror = (err) => {
    console.error('Error en WebSocket de Presencia:', err)
  }
}

watch(() => almacen.estaAutenticado, (autenticado) => {
  if (autenticado) {
    conectarPresencia()
  } else {
    clearInterval(intervaloPing)
    clearTimeout(timeoutReconexion)
    if (wsPresencia.value) {
      wsPresencia.value.close()
      wsPresencia.value = null
    }
  }
}, { immediate: true })

// Estado para actualización de la PWA
const actualizarDisponible = ref(false)
let swEnEspera = null

// Watch para alternar temas de Vuetify y la clase CSS global del modo oscuro
watch(() => almacen.temaOscuro, (nuevoValor) => {
  theme.global.name.value = nuevoValor ? 'temaProyectoOscuro' : 'temaProyecto'
  if (nuevoValor) {
    document.documentElement.classList.add('dark-mode')
    document.body.style.backgroundColor = '#0a1012'
  } else {
    document.documentElement.classList.remove('dark-mode')
    document.body.style.backgroundColor = '#eef5f7'
  }
}, { immediate: true })

const registrarServiceWorker = () => {
  if ('serviceWorker' in navigator) {
    const base = import.meta.env.BASE_URL || '/chat-empresarial/'
    navigator.serviceWorker.register(`${base}sw.js`).then((registro) => {
      // 1. Si ya hay un SW esperando activación
      if (registro.waiting) {
        swEnEspera = registro.waiting
        actualizarDisponible.value = true
      }

      // 2. Si se detecta una nueva versión instalándose
      registro.addEventListener('updatefound', () => {
        const nuevoSw = registro.installing
        if (nuevoSw) {
          nuevoSw.addEventListener('statechange', () => {
            if (nuevoSw.state === 'installed' && navigator.serviceWorker.controller) {
              swEnEspera = nuevoSw
              actualizarDisponible.value = true
            }
          })
        }
      })
    }).catch(err => {
      console.warn('Error al registrar Service Worker:', err)
    })

    // Escuchar cuando el nuevo SW toma control para recargar la app
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      window.location.reload()
    })
  }
}

const aplicarActualizacion = () => {
  if (swEnEspera) {
    swEnEspera.postMessage({ type: 'SKIP_WAITING' })
  }
}

onMounted(() => {
  // Cargar datos del localStorage al iniciar
  almacen.cargarDelAlmacenamientoLocal()
  // Registrar el SW
  registrarServiceWorker()

  // Registrar listeners de conexión a Internet
  window.addEventListener('online', actualizarEstadoRed)
  window.addEventListener('offline', actualizarEstadoRed)
})

onUnmounted(() => {
  // Limpiar listeners de red
  window.removeEventListener('online', actualizarEstadoRed)
  window.removeEventListener('offline', actualizarEstadoRed)
})
</script>


<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
}

body {
  font-family: 'Roboto', sans-serif;
  background-color: #e4f6f9;
  overflow: hidden;
}

#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

#app-root {
  width: 100%;
  height: 100%;
}

/* Responsive font sizes */
@media (max-width: 768px) {
  body {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  body {
    font-size: 12px;
  }
}
/* Forzar barras de scroll visibles en toda la app */
::-webkit-scrollbar {
  width: 10px !important;
  height: 10px !important;
  display: block !important;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05) !important;
}

::-webkit-scrollbar-thumb {
  background: #406D73 !important;
  border-radius: 50px !important;
  border: 1px solid white !important;
}

::-webkit-scrollbar-thumb:hover {
  background: #2f4a4f !important;
}

/* Para Firefox */
* {
  scrollbar-width: thin;
  scrollbar-color: #406D73 rgba(0, 0, 0, 0.05);
}

/* ---------- Banner Desplegable PWA ---------- */
.banner-actualizacion {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(64, 109, 115, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  color: white;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.25);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  padding: 0 16px;
}

.banner-contenido {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  max-width: 800px;
}

.banner-icono {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.banner-texto {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  text-align: left;
}

.banner-texto .titulo {
  font-weight: 700;
  font-size: 14px;
  letter-spacing: -0.01em;
}

.banner-texto .subtitulo {
  font-size: 11px;
  opacity: 0.85;
}

.btn-actualizar {
  background: #ffffff;
  color: #406D73;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, background-color 0.2s;
  flex-shrink: 0;
}

.btn-actualizar:hover {
  background-color: #f0f7f8;
  transform: translateY(-1px);
}

.btn-actualizar:active {
  transform: translateY(0);
}

/* Transición Desplazar Abajo */
.desplazar-abajo-enter-active,
.desplazar-abajo-leave-active {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.3s ease;
}

.desplazar-abajo-enter-from,
.desplazar-abajo-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

/* ---------- Banner Desplegable Sin Conexión ---------- */
.banner-sin-conexion {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: linear-gradient(135deg, #c62828 0%, #e53935 100%) !important;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  color: white;
  z-index: 10000;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.25);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0 16px;
}

.banner-conexion-contenido {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  max-width: 800px;
}

.banner-conexion-icono {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.banner-conexion-texto {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  text-align: left;
}

.banner-conexion-texto .titulo-conexion {
  font-weight: 700;
  font-size: 14px;
  letter-spacing: -0.01em;
}

.banner-conexion-texto .subtitulo-conexion {
  font-size: 11px;
  opacity: 0.9;
}

@keyframes pulso-icono {
  0% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.1); opacity: 1; text-shadow: 0 0 8px rgba(255,255,255,0.6); }
  100% { transform: scale(1); opacity: 0.9; }
}

.icono-pulso {
  animation: pulso-icono 2s infinite ease-in-out;
}
</style>
