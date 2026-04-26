<template>
  <div class="mensaje" :class="{ propio }">
    <div class="contenedor-mensaje">
      <p class="contenido">{{ mensaje.contenido }}</p>
      <span class="timestamp">{{ formatearFecha(mensaje.fechaEnvio) }}</span>
    </div>
    <span v-if="propio && mensaje.leido" class="icono-leido">✓✓</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAlmacen } from '@/stores'
import { formatearFecha } from '@/utils/formateoFechas'

const props = defineProps({
  mensaje: {
    type: Object,
    required: true
  }
})

const almacen = useAlmacen()
const usuarioActual = computed(() => almacen.usuarioActual)

const propio = computed(() => {
  return props.mensaje.emisorId === usuarioActual.value?.id
})
</script>

<style scoped>
.mensaje {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 8px;
  animation: slideIn 0.3s ease-out;
}

.mensaje.propio {
  justify-content: flex-end;
  flex-direction: row-reverse;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.contenedor-mensaje {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 18px;
  background-color: #f3f4f6;
  color: #374151;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-wrap: break-word;
  position: relative;
}

.mensaje.propio .contenedor-mensaje {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.mensaje:not(.propio) .contenedor-mensaje {
  border-bottom-left-radius: 4px;
}

.contenido {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.5;
}

.timestamp {
  display: block;
  font-size: 10px;
  opacity: 0.6;
  margin-top: 4px;
  text-align: right;
}

.mensaje.propio .timestamp {
  color: rgba(255, 255, 255, 0.8);
}

.icono-leido {
  font-size: 12px;
  color: #10b981;
  margin-left: 4px;
}
</style>
