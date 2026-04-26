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
import { useAlmacen } from '@/almacen'
import { formatearFecha } from '@/utilidades/formateoFechas'

const props = defineProps({
  mensaje: {
    type: Object,
    required: true
  }
})

const almacen = useAlmacen()
// Corregido: Usar 'usuario' en lugar de 'usuarioActual'
const usuario = computed(() => almacen.usuario)

const propio = computed(() => {
  if (!usuario.value) return false
  return props.mensaje.emisorId === usuario.value?.id
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
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  background-color: #e5e7eb;
  word-wrap: break-word;
}

.mensaje.propio .contenedor-mensaje {
  background-color: #667eea;
  color: white;
}

.contenido {
  margin: 0 0 4px 0;
  font-size: 14px;
  line-height: 1.4;
}

.timestamp {
  display: block;
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
}

.icono-leido {
  font-size: 12px;
  color: #667eea;
  font-weight: bold;
}
</style>
