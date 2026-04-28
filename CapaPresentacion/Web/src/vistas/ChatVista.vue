<template>
  <v-app class="chat-app-root">
    <v-main class="chat-main-fill bg-background pa-0">
      <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
        <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
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
        </v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import Chat from '@/componentes/Chat.vue'
import ListaConversaciones from '@/componentes/ListaConversaciones.vue'

const router = useRouter()
const almacen = useAlmacen()

const conversacionActual = computed(() => almacen.conversacionActual)

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
</style>