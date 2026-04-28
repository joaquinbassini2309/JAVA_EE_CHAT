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
  padding: 8px 12px;
  border-radius: 12px;
  background-color: #ced6d9; /* other messages */
  color: #2f4a4f;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-wrap: break-word;
}

.mensaje.propio .contenedor-mensaje {
  background-color: #B2C5C8; /* own messages */
  color: #2f4a4f;
}

.timestamp {
  display: block;
  font-size: 10px;
  opacity: 0.6;
  margin-top: 4px;
  text-align: right;
}

.icono-leido {
  font-size: 12px;
  color: #406D73;
}
</style>
