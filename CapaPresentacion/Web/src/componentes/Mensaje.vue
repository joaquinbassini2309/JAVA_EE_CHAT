<template>
  <div v-if="esMensajeSistema" class="mensaje-sistema">
    {{ mensaje.contenido }}
  </div>
  <div v-else>
    <!-- Renderizado especial para tareas locales como tarjeta ancha -->
    <div v-if="esTarea && esConversacionTareas" class="tarea-container">
      <div class="tarea-card" :class="{ 'completada': mensaje.completada }">
        <div class="tarea-header">
          <label class="tarea-checkbox">
            <input type="checkbox" :checked="mensaje.completada" @change.prevent="toggleCompletada" :disabled="cargando" />
          </label>
          <div class="tarea-titulo">{{ mensaje.titulo || 'Sin título' }}</div>
          <div class="tarea-acciones">
            <v-menu>
              <template v-slot:activator="{ props }">
                <v-btn icon="mdi-dots-vertical" size="x-small" variant="text" color="#406D73" v-bind="props" />
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
        <div v-if="mensaje.contenido" class="tarea-cuerpo">
          {{ mensaje.contenido }}
        </div>
        <div v-if="mensaje.fechaVencimiento" class="tarea-footer">
          <v-icon size="14" class="mr-1">mdi-calendar-clock</v-icon>
          Vence: {{ formatearFecha(mensaje.fechaVencimiento) }}
        </div>
      </div>
    </div>
    <!-- Fin tarjeta tarea -->
    <div v-else class="burbuja-wrap" :class="{ propio }">
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
      <v-menu content-class="menu-mensaje-flotante" transition="scale-transition">
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
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
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

const esMensajeSistema = computed(() => {
  return props.mensaje.contenido === 'Los mensajes están cifrados de extremo a extremo' || props.mensaje.contenido.includes('ha sido eliminado por')
})

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

// Tipo tarea
const esTarea = computed(() => String(tipoCampo.value)?.toUpperCase() === 'TAREA' || String(props.mensaje.tipo)?.toUpperCase() === 'TAREA')
const esConversacionTareas = computed(() => String(almacen.conversacionActual?.id || '').startsWith('tareas_'))
const cargando = ref(false)

const toggleCompletada = async () => {
    if (!esTarea.value) return
    if (!usuarioActual.value) return
    cargando.value = true
    try {
        const payload = {
            id: props.mensaje.id,
            completada: !props.mensaje.completada,
            titulo: props.mensaje.titulo,
            contenido: props.mensaje.contenido,
            fechaVencimiento: props.mensaje.fechaVencimiento
        }
        const actualizado = await servicioApi.actualizarTarea(usuarioActual.value.id, payload)
        // Mapear el resultado (tarea) al formato de mensaje que usa la app
        const mensajeActualizado = {
            id: actualizado.id,
            conversacionId: props.mensaje.conversacionId || `tareas_${usuarioActual.value.id}`,
            emisorId: actualizado.emisorId || usuarioActual.value.id,
            titulo: actualizado.titulo,
            contenido: actualizado.contenido,
            fechaEnvio: actualizado.fechaEnvio || props.mensaje.fechaEnvio,
            tipo: 'TAREA',
            fechaVencimiento: actualizado.fechaVencimiento || null,
            completada: !!actualizado.completada
        }
        almacen.actualizarMensaje(mensajeActualizado)
    } catch (e) {
        console.error('Error actualizando tarea:', e)
    } finally {
        cargando.value = false
    }
}
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

.mensaje-sistema {
  width: 100%;
  text-align: center;
  font-size: 11px;
  color: #7f9ea4;
  margin: 12px 0;
  font-weight: 600;
  display: flex;
  justify-content: center;
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

/* Estilos de Tarea */
.tarea-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 8px 16px;
  margin: 6px 0;
}
.tarea-card {
  width: 100%;
  max-width: 600px;
  background: linear-gradient(145deg, #ffffff, #f7fcfd);
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid rgba(64,109,115,0.15);
  box-shadow: 0 4px 12px rgba(64,109,115,0.08);
  transition: all 0.2s ease;
}
.tarea-card.completada {
  opacity: 0.7;
  background: #f0f7f8;
  border-color: rgba(106, 158, 125, 0.3);
}
.tarea-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64,109,115,0.12);
}
.tarea-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.tarea-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
}
.tarea-checkbox input {
  width: 22px;
  height: 22px;
  accent-color: #6A9E7D;
  cursor: pointer;
}
.tarea-titulo {
  flex: 1;
  font-size: 16px;
  color: #2a4d52;
  font-weight: 600;
  line-height: 1.4;
}
.tarea-card.completada .tarea-titulo {
  text-decoration: line-through;
  color: #5a8a94;
}
.tarea-cuerpo {
  font-size: 14px;
  color: #4a6c72;
  margin-top: 8px;
  padding-left: 38px;
  white-space: pre-wrap;
  line-height: 1.5;
}
.tarea-card.completada .tarea-cuerpo {
  opacity: 0.7;
}
.tarea-footer {
  margin-top: 12px;
  font-size: 13px;
  color: #6A9E7D;
  font-weight: 500;
  padding-left: 38px;
  display: flex;
  align-items: center;
}

</style>