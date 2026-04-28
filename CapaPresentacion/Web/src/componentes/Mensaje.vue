<template>
  <div class="bubble" :class="esPropio ? 'bubble-me' : 'bubble-them'">
    {{ mensaje.contenido }}
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAlmacen } from '@/almacen'; // Corregido

const props = defineProps({
  mensaje: {
    type: Object,
    required: true
  }
});

const almacen = useAlmacen();
const usuario = computed(() => almacen.usuario);

const esPropio = computed(() => {
  if (!usuario.value || !props.mensaje) {
    return false;
  }
  return props.mensaje.emisorId === usuario.value.id;
});
</script>

<style scoped>
/* Tus estilos se mantienen intactos */
.bubble { border-radius: 14px; padding: 10px 14px; max-width: 78%; font-size: 14px; line-height: 1.45; white-space: pre-wrap; word-wrap: break-word; animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.bubble-them { background: #b2c5c8; color: #2f4a4f; align-self: flex-start; }
.bubble-me { background: rgba(179, 235, 242, 0.95); border: 1px solid rgba(64, 109, 115, 0.25); color: #2a4d52; align-self: flex-end; }
</style>
