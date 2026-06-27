<template>
  <v-app class="chat-app-root">
    <v-main style="height: 100dvh; overflow: hidden; display: flex; flex-direction: column;">
      <v-container fluid class="pa-2 pa-md-4" style="height: 100%; display: flex; flex-direction: column; min-height: 0;">
        <v-sheet elevation="0" rounded="xl" border style="height: 100%; display: flex; flex-direction: column; overflow: hidden; min-height: 0;">
          
          <!-- Vista móvil -->
          <div v-if="esModoMovil && panelTareasAbierto" style="flex: 1; min-height: 0; overflow: hidden; display: flex; flex-direction: column;">
            <PanelTareas />
          </div>

          <div v-else-if="esModoMovil && conversacionActual" style="flex: 1; min-height: 0; overflow: hidden; display: grid; grid-template-rows: 1fr;">
            <Chat />
          </div>

          <div v-else-if="esModoMovil" style="flex: 1; min-height: 0; overflow: hidden; display: flex; flex-direction: column;">
            <ListaConversaciones />
          </div>

          <!-- Vista desktop -->
          <div v-if="!esModoMovil" class="chat-desktop-layout" :style="{ gridTemplateColumns: panelTareasAbierto ? '390px 1fr 380px' : '390px 1fr' }">
            <div style="border-right: 1px solid rgba(0,0,0,0.1); height: 100%; overflow: hidden; display: flex; flex-direction: column; min-height: 0;">
              <ListaConversaciones />
            </div>
            <div style="height: 100%; overflow: hidden; display: flex; flex-direction: column; min-height: 0; position: relative;">
              <Chat v-if="conversacionActual" />
              <div v-else class="sin-conversacion d-flex flex-column align-center justify-center h-100">
                <v-icon size="80" color="accent" style="opacity:0.3">mdi-message-text-outline</v-icon>
                <p>Selecciona un chat</p>
              </div>
            </div>
            <div v-if="panelTareasAbierto" style="height: 100%; overflow: hidden; display: flex; flex-direction: column; min-height: 0;">
              <PanelTareas />
            </div>
          </div>

        </v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
import Chat from '@/componentes/Chat.vue'
import ListaConversaciones from '@/componentes/ListaConversaciones.vue'
import PanelTareas from '@/componentes/PanelTareas.vue'

const router = useRouter()
const almacen = useAlmacen()
const esModoMovil = ref(false)

const panelTareasAbierto = computed(() => almacen.panelTareasAbierto)

const conversacionActual = computed(() => almacen.conversacionActual)

const obtenerNombreConversacion = computed(() => {
  if (!conversacionActual.value) return 'Chat'
  const usuarioId = almacen.usuarioActual?.id
  return obtenerNombreVisibleConversacion(conversacionActual.value, usuarioId)
})

const volverALista = () => {
  almacen.establecerConversacionActual(null)
}

const detectarTipoVista = () => {
  esModoMovil.value = window.innerWidth < 960
}

const handlePopState = (event) => {
  if (esModoMovil.value) {
    if (panelTareasAbierto.value) {
      almacen.togglePanelTareas()
    } else if (conversacionActual.value) {
      almacen.establecerConversacionActual(null)
    }
  }
}

watch(conversacionActual, (newVal) => {
  if (esModoMovil.value && newVal) {
    window.history.pushState({ appNav: 'chat' }, '')
  }
})

watch(panelTareasAbierto, (newVal) => {
  if (esModoMovil.value && newVal) {
    window.history.pushState({ appNav: 'tareas' }, '')
  }
})

const wsPresencia = ref(null)

onMounted(async () => {
  if (!almacen.estaAutenticado) {
    router.push('/login')
    return
  }

  // Conectar al WebSocket de presencia
  if (almacen.usuarioActual && almacen.token) {
    wsPresencia.value = servicioApi.conectarPresenciaWebSocket(almacen.usuarioActual.id, almacen.token)
  }

  try {
    almacen.establecerCargando(true)
    const conversaciones = await servicioApi.obtenerConversaciones()
    almacen.establecerConversaciones(conversaciones)
  } catch (error) {
    console.error('Error al cargar conversaciones:', error)
  } finally {
    almacen.establecerCargando(false)
  }

  // Detectar tipo de vista
  detectarTipoVista()
  window.addEventListener('resize', detectarTipoVista)
  window.addEventListener('popstate', handlePopState)
})

// Limpiar event listeners cuando se desmonta el componente
onUnmounted(() => {
  window.removeEventListener('resize', detectarTipoVista)
  window.removeEventListener('popstate', handlePopState)
  if (wsPresencia.value) {
    wsPresencia.value.close()
  }
})
</script>

<style scoped>
/* Reset global */
:deep(html), :deep(body), :deep(#app) {
  height: 100%;
  margin: 0;
  overflow: hidden;
  font-family: 'Inter', system-ui, sans-serif;
}

.chat-app-root {
  background: var(--bg);
  /* Fondo sutil degradado SaaS */
  background-image:
    radial-gradient(ellipse at top left, rgba(179,235,242,0.15) 0%, transparent 60%),
    radial-gradient(ellipse at bottom right, rgba(64,109,115,0.04) 0%, transparent 60%);
}

.chat-shell-plain {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-radius: 20px;
  overflow: hidden;
  min-height: 0;
  /* Sombras progresivas premium */
  box-shadow:
    0 4px 24px rgba(64,109,115,0.08),
    0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid var(--border-color);
}

.chat-desktop-grid {
  display: grid;
  height: 100%;
  overflow: hidden;
}

.chat-desktop-layout {
  display: grid;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.sidebar-container {
  border-right: 1px solid var(--border-color);
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
  z-index: 5;
}

.chat-container {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  background: var(--bg);
}

/* Pantalla Vacía */
.sin-conversacion {
  background: var(--bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  height: 100%;
}

.sin-conv-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  letter-spacing: -0.01em;
  background: var(--surface);
  padding: 6px 16px;
  border-radius: 50px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

/* ---- MODO MÓVIL ---- */
.chat-movil-lista {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}

.chat-movil-completo {
  flex: 1;
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.header-retroceso {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: var(--surface);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  box-shadow: 0 1px 0 rgba(0,0,0,0.02);
}

.titulo-conversacion {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.chat-desktop {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-main-row {
  flex: 1;
  display: flex;
  min-height: 0;
}

/* ---- Media Queries ---- */
@media (max-width: 959px) {
  .chat-main-row {
    flex-direction: column;
    flex-wrap: nowrap;
  }
}

@media (max-width: 768px) {
  .chat-shell {
    border-radius: 0;
    box-shadow: none;
    border: none;
  }
}

@media (max-width: 480px) {
  .sin-conv-text {
    font-size: 14px;
  }
  .header-retroceso {
    padding: 8px 12px;
    gap: 8px;
  }
  .titulo-conversacion {
    font-size: 15px;
  }
}

/* ---- Mostrar/Ocultar según viewport ---- */
@media (min-width: 960px) {
  .chat-movil-lista,
  .chat-movil-completo {
    display: none;
  }
  .chat-desktop {
    display: flex;
  }
}

@media (max-width: 959px) {
  .chat-desktop {
    display: none;
  }
}
</style>