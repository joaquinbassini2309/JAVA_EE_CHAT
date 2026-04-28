<template>
  <div class="burbuja-wrap" :class="{ propio }">
    <div class="burbuja" :class="propio ? 'burbuja-me' : 'burbuja-them'">
      <span v-if="mostrarNombre" class="nombre-emisor">{{ mensaje.emisorNombre }}</span>
      <p class="contenido">{{ mensaje.contenido }}</p>
      <div class="burbuja-footer">
        <span class="timestamp">{{ formatearFecha(mensaje.fechaEnvio) }}</span>
        <span v-if="propio && mensaje.leido" class="icono-leido">✓✓</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { formatearFecha } from '@/utilidades/formateoFechas'

const props = defineProps({
  mensaje: {
    type: Object,
    required: true,
  },
})

const almacen = useAlmacen()
const usuarioActual = computed(() => almacen.usuarioActual)

const propio = computed(() => props.mensaje.emisorId === usuarioActual.value?.id)

const mostrarNombre = computed(
    () => !propio.value && almacen.conversacionActual?.tipo === 'GRUPO'
)
</script>

<style scoped>
.burbuja-wrap {
  display: flex;
  max-width: 78%;
  animation: slideIn 0.25s ease-out;
}

.burbuja-wrap.propio {
  align-self: flex-end;
  flex-direction: row-reverse;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

.burbuja {
  border-radius: 14px;
  padding: 9px 13px;
  font-size: 14px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}

/* Mensajes ajenos */
.burbuja-them {
  background: #b2c5c8;
  color: #2f4a4f;
  border-bottom-left-radius: 4px;
}

/* Mensajes propios */
.burbuja-me {
  background: rgba(179, 235, 242, 0.95);
  border: 1px solid rgba(64, 109, 115, 0.2);
  color: #2a4d52;
  border-bottom-right-radius: 4px;
}

.nombre-emisor {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  margin-bottom: 3px;
}

.contenido {
  margin: 0;
}

.burbuja-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 4px;
}

.timestamp {
  font-size: 10px;
  opacity: 0.55;
}

.icono-leido {
  font-size: 11px;
  color: #406D73;
}
</style>