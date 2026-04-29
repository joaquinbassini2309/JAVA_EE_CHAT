<template>
  <div class="burbuja-wrap" :class="{ propio }">
    <div class="burbuja" :class="propio ? 'burbuja-me' : 'burbuja-them'">
      <span v-if="mostrarNombre" class="nombre-emisor">{{ mensaje.emisorNombre }}</span>
      <p class="contenido" :class="{ eliminado: mensaje.eliminado }">
        {{ mensaje.eliminado ? 'Mensaje eliminado' : (mensaje.contenido && !esSoloAdjunto ? mensaje.contenido : '') }}
      </p>

      <!-- Adjuntos: imagen inline -->
      <img v-if="esImagenAdjunta" :src="mensaje.urlAdjunto" alt="adjunto" class="adjunto-imagen" />

      <!-- Adjuntos: documento u otro tipo, mostrar enlace de descarga -->
      <div v-if="esDocumentoAdjunto" class="adjunto-documento">
        <a :href="mensaje.urlAdjunto" target="_blank" rel="noopener noreferrer">Descargar archivo adjunto</a>
        <div class="nombre-archivo">{{ mensaje.contenido }}</div>
      </div>

      <div class="burbuja-footer">
        <span class="timestamp">{{ formatearFecha(mensaje.fechaEnvio) }}</span>
        <span v-if="propio && mensaje.leido" class="icono-leido">✓✓</span>
      </div>
    </div>
    <!-- Menú de 3 puntos (solo para mensajes propios) -->
    <div v-if="propio" class="menu-mensaje">
      <v-menu>
        <template v-slot:activator="{ props }">
          <v-btn
              icon="mdi-dots-vertical"
              size="x-small"
              variant="text"
              color="#406D73"
              v-bind="props"
          />
        </template>
        <v-list>
          <v-list-item @click="$emit('ver-info', mensaje)" prepend-icon="mdi-information">
            <v-list-item-title>Info</v-list-item-title>
          </v-list-item>
          <v-list-item @click="$emit('eliminar', mensaje)" class="text-error" prepend-icon="mdi-delete">
            <v-list-item-title>Eliminar</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
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

defineEmits(['ver-info', 'eliminar'])

const almacen = useAlmacen()
const usuarioActual = computed(() => almacen.usuarioActual)

const propio = computed(() => props.mensaje.emisorId === usuarioActual.value?.id)

const mostrarNombre = computed(
    () => !propio.value && almacen.conversacionActual?.tipo === 'GRUPO'
)

// Helper para detectar tipo de adjunto. El servidor devuelve el campo 'tipo' (enum) y 'urlAdjunto'.
const tipoCampo = computed(() => {
  // Puede venir como { tipo: 'IMAGEN' } o { tipo: 'TEXTO' }
  return props.mensaje.tipo || props.mensaje.tipoMensaje || null
})

const esImagenAdjunta = computed(() => {
  return props.mensaje.urlAdjunto && (String(tipoCampo.value)?.toUpperCase() === 'IMAGEN')
})

const esDocumentoAdjunto = computed(() => {
  return props.mensaje.urlAdjunto && (String(tipoCampo.value)?.toUpperCase() === 'DOCUMENTO' || String(tipoCampo.value)?.toUpperCase() === 'ARCHIVO')
})

// Si el mensaje solo representa el adjunto y no tiene texto adicional
const esSoloAdjunto = computed(() => {
  return (!props.mensaje.contenido || props.mensaje.contenido.trim() === '') && !!props.mensaje.urlAdjunto
})
</script>

<style scoped>
.burbuja-wrap {
  display: flex;
  max-width: 78%;
  animation: slideIn 0.25s ease-out;
  gap: 4px;
  align-items: flex-end;
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

.contenido.eliminado {
  opacity: 0.6;
  font-style: italic;
  color: rgba(0, 0, 0, 0.5);
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

.menu-mensaje {
  opacity: 0;
  transition: opacity 0.15s;
}

.burbuja-wrap:hover .menu-mensaje {
  opacity: 1;
}

/* Adjuntos */
.adjunto-imagen {
  max-width: 280px;
  max-height: 280px;
  border-radius: 8px;
  display: block;
  margin-top: 8px;
}

.adjunto-documento {
  margin-top: 8px;
  background: rgba(255,255,255,0.04);
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 13px;
}

.adjunto-documento a {
  color: #18686b;
  font-weight: 700;
  text-decoration: none;
}

.nombre-archivo {
  font-size: 12px;
  color: #2f4a4f;
  margin-top: 6px;
}
</style>