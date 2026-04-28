<template>
  <v-app class="chat-app-root">
    <v-main class="chat-main-fill bg-background pa-0">
      <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
        <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
          <v-row no-gutters class="chat-main-row">

            <v-col cols="12" md="4" class="sidebar-col">
              <ListaConversaciones />
            </v-col>

            <v-col cols="12" md="8" class="chat-col">
              <Chat v-if="conversacionActiva" />
              <div v-else class="d-flex flex-column align-center justify-center h-100">
                <v-icon size="80" color="accent" class="mb-4">mdi-message-text-outline</v-icon>
                <p class="heading-14">Selecciona una conversación para empezar</p>
              </div>
            </v-col>

          </v-row>
        </v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import { useAlmacen } from '@/almacen'; // Corregido
import { useRouter } from 'vue-router';
import { servicioApi } from '@/services/api'; // Corregido
import ListaConversaciones from '@/componentes/ListaConversaciones.vue';
import Chat from '@/componentes/Chat.vue';

const almacen = useAlmacen();
const router = useRouter();

const conversacionActiva = computed(() => almacen.conversacionActual);

onMounted(async () => {
  if (!almacen.estaAutenticado) {
    router.push('/login');
    return;
  }
  try {
    const conversaciones = await servicioApi.obtenerConversaciones();
    almacen.establecerConversaciones(conversaciones);
  } catch (error) {
    console.error('Error al cargar conversaciones:', error);
  }
});
</script>

<style scoped>
.chat-app-root, .chat-main-fill, .chat-container-fill, .chat-shell, .chat-main-row {
  height: 100%;
  margin: 0;
  padding: 0;
}
.sidebar-col, .chat-col {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.heading-14 {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: 0.01em;
  color: rgb(var(--v-theme-accent));
}
@media (max-width: 959px) {
  .chat-main-row {
    flex-direction: column;
    flex-wrap: nowrap;
  }
  .sidebar-col {
    flex: 0 1 auto;
    max-height: min(48vh, 440px);
    border-inline-end: none;
    border-bottom: 1px solid rgba(var(--v-theme-accent), 0.25);
  }
  .chat-col {
    flex: 1 1 auto;
  }
}
</style>
