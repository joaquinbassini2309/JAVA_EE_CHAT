<template>
  <v-app class="chat-app-root">
    <v-main class="chat-main-fill bg-background pa-0">
      <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
        <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
          <!-- Vista móvil: condicional para mostrar lista o chat -->
          <div v-if="esModoMovil && !conversacionActual" class="chat-movil-lista">
            <ListaConversaciones />
          </div>

          <!-- Vista móvil: chat con botón atrás -->
          <div v-if="esModoMovil && conversacionActual" class="chat-movil-completo">
            <!-- Botón volver atrás -->
            <div class="header-retroceso">
              <v-btn
                icon="mdi-arrow-left"
                variant="flat"
                color="accent"
                size="small"
                density="comfortable"
                @click="volverALista"
                title="Volver a conversaciones"
              />
              <span class="titulo-conversacion">{{ obtenerNombreConversacion }}</span>
            </div>
            <Chat v-if="conversacionActual" />
          </div>

          <!-- Vista desktop: diseño original de dos columnas -->
          <div v-if="!esModoMovil" class="chat-desktop">
            <v-row no-gutters class="chat-main-row" style="flex:1 1 auto; min-height:0;">
              <!-- Sidebar -->
              <v-col cols="12" md="4" class="sidebar-col border-e">
                <ListaConversaciones />
              </v-col>
              <!-- Área de chat -->
              <v-col cols="12" md="8" class="chat-col">
                <Chat v-if="conversacionActual" />
                <div v-else class="sin-conversacion d-flex flex-column align-center justify-center h-100">
                  <v-icon size="80" color="accent" class="mb-4" style="opacity:0.45">mdi-message-text-outline</v-icon>
                  <p class="sin-conv-text">Selecciona una conversación para empezar</p>
                </div>
              </v-col>
            </v-row>
          </div>
        </v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
import Chat from '@/componentes/Chat.vue'
import ListaConversaciones from '@/componentes/ListaConversaciones.vue'

const router = useRouter()
const almacen = useAlmacen()
const esModoMovil = ref(false)

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

onMounted(async () => {
  if (!almacen.estaAutenticado) {
    router.push('/login')
    return
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
})

// Limpiar event listeners cuando se desmonta el componente
onUnmounted(() => {
  window.removeEventListener('resize', detectarTipoVista)
})
</script>

<style scoped>
/* Reset global */
:deep(html), :deep(body), :deep(#app) {
  height: 100%;
  margin: 0;
  overflow: hidden;
}

.chat-app-root {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-main-fill {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-container-fill {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
}

.chat-shell {
  border: 2px solid rgba(64, 109, 115, 0.28);
  border-radius: 12px;
  overflow: hidden;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
}

.sidebar-col {
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(64, 109, 115, 0.18);
}

.chat-col {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.sin-conversacion {
  background: rgba(179, 235, 242, 0.18);
}

.sin-conv-text {
  font-size: 14px;
  font-weight: 500;
  color: #406D73;
  letter-spacing: 0.01em;
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
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}

.header-retroceso {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f7fcfd;
  border-bottom: 1px solid rgba(64, 109, 115, 0.15);
  flex-shrink: 0;
}

.titulo-conversacion {
  font-weight: 600;
  font-size: 15px;
  color: #2f4a4f;
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

/* ---- Media Queries for Desktop Layout ---- */
@media (max-width: 959px) {
  .chat-main-row {
    flex-direction: column;
    flex-wrap: nowrap;
  }
  .sidebar-col {
    flex: 0 1 auto;
    max-height: min(48vh, 440px);
    border-right: none;
    border-bottom: 1px solid rgba(64, 109, 115, 0.18);
  }
  .chat-col {
    flex: 1 1 auto;
  }
}

@media (max-width: 768px) {
  .chat-container-fill {
    padding: 0.5rem 0.75rem !important;
  }

  .chat-shell {
    border-radius: 8px;
    border: 1px solid rgba(64, 109, 115, 0.15);
  }

  .sidebar-col {
    max-height: min(42vh, 380px);
  }
}

@media (max-width: 480px) {
  .chat-container-fill {
    padding: 0.25rem !important;
  }

  .chat-shell {
    border-radius: 4px;
    border: none;
  }

  .sidebar-col {
    max-height: min(35vh, 280px);
  }

  .sin-conversacion {
    background: rgba(179, 235, 242, 0.1);
  }

  .sin-conv-text {
    font-size: 13px;
  }

  .header-retroceso {
    padding: 6px 10px;
    gap: 10px;
  }

  .titulo-conversacion {
    font-size: 14px;
  }
}

/* ---- Mobile First - Mostrar/ocultar según viewport ---- */
@media (min-width: 960px) {
  /* En desktop, siempre mostrar chat-desktop */
  .chat-movil-lista,
  .chat-movil-completo {
    display: none;
  }

  .chat-desktop {
    display: flex;
  }
}

@media (max-width: 959px) {
  /* En móvil con pantalla < 960px, usar las vistas móviles */
  .chat-desktop {
    display: none;
  }
}
</style>