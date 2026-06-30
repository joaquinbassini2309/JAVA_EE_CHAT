<template>
  <div class="area-mensajes" :style="estiloWallpaper">
    <div ref="contenedorMensajes" class="contenedor-mensajes">
      <!-- Cifrado estatico -->
      <div class="mensaje-sistema-container">
        <div class="mensaje-sistema">
          <v-icon size="12" color="#406D73" class="mr-1" style="opacity: 0.8;">mdi-lock</v-icon>
          Los mensajes están cifrados de extremo a extremo
        </div>
      </div>

      <!-- Boton Cargar Mas -->
      <div v-if="!todosCargados && mensajesFiltrados.length > 0" class="d-flex justify-center my-3">
        <v-btn
          variant="flat"
          color="#455A64"
          class="text-white btn-cargar-mas"
          size="small"
          rounded="pill"
          :loading="cargandoAnteriores"
          prepend-icon="mdi-history"
          @click="$emit('cargar-mas-mensajes')"
        >
          Cargar mensajes anteriores
        </v-btn>
      </div>

      <div
          v-for="(mensaje, index) in mensajesFiltrados"
          :key="mensaje.id"
          class="mensaje-agrupador"
      >
        <!-- Cabecera de fecha -->
        <div v-if="mostrarFechaHeader(index)" class="fecha-header-container">
          <div class="fecha-header">{{ formatearSoloFecha(mensaje.fechaEnvio) }}</div>
        </div>
        
        <div
            :id="`msg-${mensaje.id}`"
            class="mensaje-wrap"
            :class="{ propio: esPropio(mensaje) }"
        >
          <MensajeComp
              :mensaje="mensaje"
              @ver-info="$emit('ver-info', $event)"
              @eliminar="$emit('eliminar', $event)"
              @fijar="$emit('fijar', $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import MensajeComp from './MensajeComp.vue'
import { formatearSoloFecha } from '@/utilidades/formateoFechas'

const props = defineProps({
  mensajesFiltrados: {
    type: Array,
    required: true
  },
  todosCargados: {
    type: Boolean,
    default: false
  },
  cargandoAnteriores: {
    type: Boolean,
    default: false
  },
  estiloWallpaper: {
    type: Object,
    default: () => ({})
  },
  usuarioActual: {
    type: Object,
    default: null
  }
})

defineEmits(['cargar-mas-mensajes', 'ver-info', 'eliminar', 'fijar'])

const contenedorMensajes = ref(null)

// Determinar si se debe mostrar cabecera de fecha.
const mostrarFechaHeader = (index) => {
  if (index === 0) return true
  const msgActual = props.mensajesFiltrados[index]
  const msgAnterior = props.mensajesFiltrados[index - 1]
  
  if (!msgActual || !msgAnterior) return false
  
  const fechaActual = formatearSoloFecha(msgActual.fechaEnvio)
  const fechaAnterior = formatearSoloFecha(msgAnterior.fechaEnvio)
  
  return fechaActual !== fechaAnterior
}

// Determinar si el mensaje fue enviado por el usuario actual.
const esPropio = (mensaje) => mensaje.emisorId === props.usuarioActual?.id

// Desplazar el scroll hacia abajo.
const scrollToBottom = async () => {
  await nextTick()
  if (contenedorMensajes.value) {
    contenedorMensajes.value.scrollTop = contenedorMensajes.value.scrollHeight
  }
}

defineExpose({
  scrollToBottom,
  contenedorMensajes
})
</script>

<style scoped>
.area-mensajes {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: var(--chat-bg);
  flex: 1;
  min-height: 0;
}

.area-mensajes::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: var(--chat-wallpaper-url, radial-gradient(circle at 1px 1px, rgba(64,109,115,0.06) 1px, transparent 0));
  background-size: var(--chat-wallpaper-size, 24px 24px);
  background-position: center;
  background-repeat: var(--chat-wallpaper-repeat, repeat);
  opacity: var(--chat-wallpaper-opacity, 1);
  z-index: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.contenedor-mensajes {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
  background: transparent;
  z-index: 1;
}

.mensaje-agrupador {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.mensaje-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
  align-items: flex-start;
}

.mensaje-wrap.propio {
  align-items: flex-end;
}

.mensaje-sistema-container {
  width: 100%;
  text-align: center;
  display: flex;
  justify-content: center;
  margin: 12px 0 20px;
}
.mensaje-sistema {
  font-size: 11px;
  font-weight: 600;
  color: #406D73;
  background: var(--input-bg);
  border: 1px solid rgba(64,109,115,0.15);
  padding: 6px 14px;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.01em;
}

.fecha-header-container {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 16px 0 12px;
}
.fecha-header {
  background: var(--surface);
  backdrop-filter: blur(4px);
  color: #5a8a94;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
  border: 1px solid rgba(64,109,115,0.08);
}
</style>
