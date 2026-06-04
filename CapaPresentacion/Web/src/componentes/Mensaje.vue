<template>
  <div v-if="esMensajeSistema" class="mensaje-sistema-container">
    <div class="mensaje-sistema">
      <v-icon v-if="mensaje.contenido.includes('cifrados')" size="12" color="#406D73" class="mr-1" style="opacity: 0.8;">mdi-lock</v-icon>
      {{ mensaje.contenido }}
    </div>
  </div>
  <div v-else class="mensaje-root" :class="{ propio }">
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
      <div class="burbuja" :class="[propio ? 'burbuja-me' : 'burbuja-them', mensaje.colorResaltado ? 'resaltado-' + mensaje.colorResaltado : '']">
      <span v-if="mostrarNombre" class="nombre-emisor">{{ mensaje.emisorNombre }}</span>
      <p class="contenido" :class="{ eliminado: mensaje.eliminado }" v-html="contenidoFormateado">
      </p>

      <!-- Adjuntos: imagen inline -->
      <img v-if="esImagenAdjunta" :src="mensaje.urlAdjunto" alt="adjunto" class="adjunto-imagen" />

      <!-- Adjuntos: documento u otro tipo, mostrar enlace de descarga -->
      <div v-if="esDocumentoAdjunto" class="adjunto-documento">
        <a :href="mensaje.urlAdjunto" target="_blank" rel="noopener noreferrer">Descargar archivo adjunto</a>
        <div class="nombre-archivo">{{ mensaje.contenido }}</div>
      </div>

      <div class="burbuja-footer">
        <span class="timestamp">{{ formatearHora(mensaje.fechaEnvio) }}</span>
        <v-icon v-if="propio" size="14" class="ml-1 icono-tick" :color="mensaje.leido ? '#4caf50' : '#8aa8ae'">
          {{ mensaje.leido ? 'mdi-check-all' : 'mdi-check' }}
        </v-icon>
      </div>
    </div>
    <!-- Menú de 3 puntos (para mensajes propios o si tiene permisos en el grupo) -->
    <div v-if="mostrarMenu" class="menu-mensaje">
      <v-menu content-class="menu-mensaje-flotante" transition="scale-transition" location="bottom end" :offset="[0, 16]">
        <template v-slot:activator="{ props }">
          <v-hover v-slot="{ isHovering, props: hoverProps }">
            <v-btn
                icon="mdi-dots-vertical"
                size="x-small"
                :variant="isHovering ? 'flat' : 'text'"
                color="#406D73"
                class="btn-opciones teal-hover-white"
                v-bind="{ ...props, ...hoverProps }"
            />
          </v-hover>
        </template>
        <v-list class="lista-opciones" density="compact">
          <!-- Opciones de resaltado (solo admins y moderadores de grupo) -->
          <v-list-item v-if="puedeResaltar">
            <div class="colores-resaltado-row">
              <button
                v-for="col in ['rojo', 'violeta', 'azul', 'verde', 'amarillo']"
                :key="col"
                class="btn-color-dot"
                :class="[col, { activo: mensaje.colorResaltado === col }]"
                @click="establecerResaltado(col)"
                :title="'Resaltar ' + col"
              ></button>
              <button
                v-if="mensaje.colorResaltado"
                class="btn-color-dot clear"
                @click="establecerResaltado(null)"
                title="Quitar resaltado"
              >
                <v-icon size="12" color="error">mdi-close</v-icon>
              </button>
            </div>
          </v-list-item>
          <v-divider v-if="puedeResaltar" class="my-1" />
          <v-list-item @click="$emit('ver-info', mensaje)" prepend-icon="mdi-information" class="item-info">
            <v-list-item-title>Info</v-list-item-title>
          </v-list-item>
          <v-list-item v-if="puedeFijar" @click="$emit('fijar', mensaje)" prepend-icon="mdi-pin" class="item-pin">
            <v-list-item-title>Fijar mensaje</v-list-item-title>
          </v-list-item>
          <v-list-item v-if="propio || esAdminGrupo" @click="$emit('eliminar', mensaje)" prepend-icon="mdi-delete" class="item-delete text-error">
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
import { formatearFecha, formatearHora } from '@/utilidades/formateoFechas'

const props = defineProps({
  mensaje: {
    type: Object,
    required: true,
  },
})

defineEmits(['ver-info', 'eliminar', 'fijar'])

const almacen = useAlmacen()
const usuarioActual = computed(() => almacen.usuarioActual)

const contenidoFormateado = computed(() => {
  if (props.mensaje.eliminado) return 'Mensaje eliminado'
  if (esSoloAdjunto.value) return ''
  if (!props.mensaje.contenido) return ''
  
  // Escapar HTML básico para prevenir XSS
  let texto = props.mensaje.contenido
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
  
  // Obtener usernames de los participantes de la conversación actual
  const usernames = []
  if (conversacionActual.value && conversacionActual.value.participantes) {
    conversacionActual.value.participantes.forEach(p => {
      if (p.usuario && p.usuario.username) {
        usernames.push(p.usuario.username)
      }
    })
  }
  usernames.push('todos')
  
  // Ordenar de mayor a menor longitud para evitar conflictos (por ejemplo: "Tester" vs "Tester Two")
  usernames.sort((a, b) => b.length - a.length)
  
  // Escapar caracteres especiales de regex
  const escaparRegex = (string) => string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  
  const partes = usernames.map(escaparRegex)
  // Añadir patrón genérico como alternativa para otras menciones que no sean miembros cargados
  partes.push('[a-zA-Z0-9_áéíóúÁÉÍÓÚñÑüÜ]+')
  
  const regexMencion = new RegExp(`@(${partes.join('|')})`, 'g')
  
  texto = texto.replace(regexMencion, (match) => {
    return `<span style="color: #00bcd4 !important; font-weight: 600; background-color: rgba(0, 188, 212, 0.08); padding: 1px 4px; border-radius: 4px; display: inline-block;">${match}</span>`
  })
  
  return texto
})

const propio = computed(() => props.mensaje.emisorId === usuarioActual.value?.id)

const conversacionActual = computed(() => almacen.conversacionActual)

const rolUsuarioActual = computed(() => {
  if (!conversacionActual.value || conversacionActual.value.tipo !== 'GRUPO') return null
  const participante = conversacionActual.value.participantes?.find(p => p.usuario.id === usuarioActual.value?.id)
  return participante?.rol
})

const puedeResaltar = computed(() => {
  return conversacionActual.value?.tipo === 'GRUPO' && 
         (rolUsuarioActual.value === 'ADMIN' || rolUsuarioActual.value === 'MODERADOR')
})

const esAdminGrupo = computed(() => {
  return conversacionActual.value?.tipo === 'GRUPO' && rolUsuarioActual.value === 'ADMIN'
})

const puedeFijar = computed(() => {
  return !props.mensaje.eliminado && !esMensajeSistema.value && !esTarea.value
})

const mostrarMenu = computed(() => propio.value || puedeResaltar.value || puedeFijar.value)

const establecerResaltado = async (color) => {
  try {
    const mActualizado = await servicioApi.resaltarMensaje(props.mensaje.id, color)
    almacen.actualizarMensaje(mActualizado)
  } catch (error) {
    console.error('Error al resaltar mensaje:', error)
  }
}

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
/* ==================================================
   Mensaje.vue — Burbujas Premium SaaS 2026
   ================================================== */

/* ---- Mensaje de sistema (cifrado / notificaciones) ---- */
.mensaje-sistema-container {
  width: 100%;
  text-align: center;
  display: flex;
  justify-content: center;
  margin: 12px 0;
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

/* ---- Contenedor root del mensaje ---- */
.mensaje-root {
  display: flex;
  flex-direction: column;
  width: 100%;
  align-items: flex-start;
}
.mensaje-root.propio {
  align-items: flex-end;
}

/* ---- Contenedor wrap de burbuja ---- */
.burbuja-wrap {
  display: flex;
  max-width: 76%;
  animation: fadeInUp .2s ease-out both;
  gap: 6px;
  align-items: flex-end;
}

.burbuja-wrap.propio {
  align-self: flex-end;
  flex-direction: row-reverse;
}

/* ---- Burbujas ---- */
.burbuja {
  border-radius: 18px;
  padding: 9px 13px 7px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  position: relative;
  min-width: 80px;
}

/* Mensajes ajenos — fondo blanco */
.burbuja-them {
  background: var(--msg-recibido-bg);
  color: var(--msg-texto-color);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08), 0 2px 8px rgba(0,0,0,0.04);
}

/* Mensajes propios — teal pastel */
.burbuja-me {
  background: var(--msg-enviado-bg);
  color: var(--msg-texto-color);
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 3px rgba(64,109,115,0.1), 0 2px 8px rgba(64,109,115,0.06);
}

/* ---- Nombre del emisor (grupos) ---- */
.nombre-emisor {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  margin-bottom: 3px;
  letter-spacing: 0.01em;
}

/* ---- Contenido ---- */
.contenido {
  margin: 0;
  font-size: 14px;
  font-family: 'Inter', system-ui, sans-serif;
}

.contenido.eliminado {
  opacity: 0.55;
  font-style: italic;
  font-size: 13px;
}

/* ---- Footer de burbuja (hora + leído) ---- */
.burbuja-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 3px;
  margin-top: 3px;
}

.timestamp {
  font-size: 10px;
  color: rgba(64,109,115,0.55);
  font-weight: 500;
  letter-spacing: 0.01em;
}

.burbuja-me .timestamp {
  color: rgba(22,46,49,0.5);
}

.icono-leido {
  font-size: 11px;
  color: #406D73;
  font-weight: 700;
  letter-spacing: -1px;
}

/* ---- Menú de 3 puntos ---- */
.menu-mensaje {
  opacity: 0;
  transition: opacity .15s ease;
  align-self: center;
}

.burbuja-wrap:hover .menu-mensaje {
  opacity: 1;
}

.teal-hover-white.v-btn--variant-flat {
  color: #ffffff !important;
}
.teal-hover-white.v-btn--variant-flat .v-icon,
.teal-hover-white.v-btn--variant-flat i {
  color: #ffffff !important;
}

:deep(.lista-opciones .v-list-item:hover) {
  background-color: rgba(64,109,115,0.04) !important;
}

/* ---- Adjuntos ---- */
.adjunto-imagen {
  max-width: 260px;
  max-height: 260px;
  border-radius: 12px;
  display: block;
  margin-top: 7px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform .2s ease;
}

.adjunto-imagen:hover {
  transform: scale(1.02);
}

.adjunto-documento {
  margin-top: 8px;
  background: rgba(64,109,115,0.06);
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.adjunto-documento a {
  color: #406D73;
  font-weight: 700;
  text-decoration: none;
  flex: 1;
}

.adjunto-documento a:hover {
  text-decoration: underline;
}

.nombre-archivo {
  font-size: 11px;
  color: #5a8a94;
  margin-top: 4px;
}

/* ===================================================
   TARJETA DE TAREA — Premium
   =================================================== */
.tarea-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 6px 12px;
  margin: 4px 0;
}

.tarea-card {
  width: 100%;
  max-width: 560px;
  background: var(--surface);
  border-radius: 16px;
  padding: 16px 18px;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 12px rgba(64,109,115,0.08), 0 1px 3px rgba(0,0,0,0.05);
  transition: transform .22s ease, box-shadow .22s ease;
  animation: fadeInUp .25s ease-out both;
}

.tarea-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(64,109,115,0.13), 0 2px 8px rgba(0,0,0,0.06);
}

.tarea-card.completada {
  background: rgba(106,158,125,0.08);
  border-color: rgba(106,158,125,0.2);
  opacity: 0.8;
}

.tarea-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tarea-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tarea-checkbox input[type="checkbox"] {
  width: 20px;
  height: 20px;
  accent-color: #6A9E7D;
  cursor: pointer;
  border-radius: 6px;
}

.tarea-titulo {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  letter-spacing: -0.01em;
}

.tarea-card.completada .tarea-titulo {
  text-decoration: line-through;
  color: #7aa8ae;
  font-weight: 500;
}

.tarea-acciones {
  flex-shrink: 0;
}

.tarea-cuerpo {
  font-size: 13px;
  color: #4d7278;
  margin-top: 10px;
  padding-left: 32px;
  white-space: pre-wrap;
  line-height: 1.55;
}

.tarea-card.completada .tarea-cuerpo {
  opacity: 0.65;
}

.tarea-footer {
  margin-top: 10px;
  font-size: 12px;
  color: #6A9E7D;
  font-weight: 500;
  padding-left: 32px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ---- Mensajes Resaltados (Colores suaves premium) ---- */
.resaltado-rojo {
  background-color: #ffebee !important;
  border: 1px solid #ffcdd2 !important;
  color: #b71c1c !important;
}
.resaltado-rojo .timestamp {
  color: rgba(183, 28, 28, 0.6) !important;
}

.resaltado-violeta {
  background-color: #f3e5f5 !important;
  border: 1px solid #e1bee7 !important;
  color: #4a148c !important;
}
.resaltado-violeta .timestamp {
  color: rgba(74, 20, 140, 0.6) !important;
}

.resaltado-azul {
  background-color: #e3f2fd !important;
  border: 1px solid #bbdefb !important;
  color: #0d47a1 !important;
}
.resaltado-azul .timestamp {
  color: rgba(13, 71, 161, 0.6) !important;
}

.resaltado-verde {
  background-color: #e8f5e9 !important;
  border: 1px solid #c8e6c9 !important;
  color: #1b5e20 !important;
}
.resaltado-verde .timestamp {
  color: rgba(27, 94, 32, 0.6) !important;
}

.resaltado-amarillo {
  background-color: #fffde7 !important;
  border: 1px solid #fff9c4 !important;
  color: #f57f17 !important;
}
.resaltado-amarillo .timestamp {
  color: rgba(245, 127, 23, 0.6) !important;
}

/* Color picker row for highlights */
.colores-resaltado-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
}

.btn-color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: transform 0.15s, border-color 0.15s;
}

.btn-color-dot:hover {
  transform: scale(1.25);
}

.btn-color-dot.activo {
  border-color: #37474f !important;
  transform: scale(1.1);
}

.btn-color-dot.rojo { background-color: #ffcdd2; }
.btn-color-dot.violeta { background-color: #e1bee7; }
.btn-color-dot.azul { background-color: #bbdefb; }
.btn-color-dot.verde { background-color: #c8e6c9; }
.btn-color-dot.amarillo { background-color: #fff9c4; }

.btn-color-dot.clear {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
}

.mencion-resaltada {
  color: #00bcd4 !important;
  font-weight: 600;
  background-color: rgba(0, 188, 212, 0.08);
  padding: 1px 4px;
  border-radius: 4px;
  display: inline-block;
}
</style>
