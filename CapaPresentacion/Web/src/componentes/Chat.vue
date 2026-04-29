<template>
  <div class="componente-chat">
    <template v-if="!mostrandoInfo">

      <!-- Encabezado con banner + avatar cuadrado -->
      <div class="chat-encabezado" @click="mostrandoInfo = true">
        <div class="profile-banner profile-banner--default" />
        <div class="profile-lower">
          <div class="profile-avatar-wrap">
            <div class="avatar-cuadrado">
              {{ destinatario.iniciales }}
            </div>
          </div>
          <div class="profile-title-row">
            <span class="profile-name text-truncate">{{ destinatario.nombre }}</span>
            <div class="encabezado-acciones" @click.stop>
              <v-btn icon="mdi-account-plus" variant="flat" color="accent" size="small" density="comfortable" @click="abrirModalAñadir" title="Añadir miembro" />
              <span class="badge-estado" :class="estadoUsuario">{{ estadoUsuario }}</span>
            </div>
          </div>
          <div class="profile-subtitle">En conversación</div>
        </div>
      </div>

      <!-- Modal Añadir Miembro -->
      <v-dialog v-model="mostrarModalAñadir" max-width="400">
        <v-card rounded="lg">
          <v-card-title class="modal-titulo">
            Añadir al Grupo
            <v-spacer />
            <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModalAñadir" />
          </v-card-title>
          <v-card-text class="pa-0">
            <div class="modal-busqueda-input">
              <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
              <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
            </div>
            <div class="modal-listado">
              <div
                  v-for="usuario in usuariosFiltrados"
                  :key="usuario.id"
                  class="item-usuario-modal"
                  @click="añadirMiembro(usuario.id)"
              >
                <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
                <div class="info-usuario-modal">
                  <span class="nombre">{{ usuario.username }}</span>
                </div>
                <v-icon size="20" color="#406D73">mdi-plus</v-icon>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <!-- Modal Info Mensaje -->
      <v-dialog v-model="mostrarModalInfo" max-width="400">
        <v-card v-if="mensajeParaInfo" rounded="lg">
          <v-card-title class="modal-titulo">
            Información del Mensaje
            <v-spacer />
            <v-btn icon="mdi-close" variant="text" size="small" @click="mostrarModalInfo = false" />
          </v-card-title>
          <v-card-text class="py-4">
            <p class="mb-2"><strong>Contenido:</strong><br><em>"{{ mensajeParaInfo.contenido }}"</em></p>
            <p class="mb-2"><strong>Enviado por:</strong> {{ esPropio(mensajeParaInfo) ? 'Tú' : (mensajeParaInfo.emisorNombre || 'Desconocido') }}</p>
            <p class="mb-2"><strong>Fecha:</strong> {{ formatearFecha(mensajeParaInfo.fechaEnvio) }}</p>
            <p><strong>Estado:</strong> {{ mensajeParaInfo.leido ? 'Leído' : 'Entregado' }}</p>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="mostrarModalInfo = false">Cerrar</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Área de mensajes -->
      <div ref="contenedorMensajes" class="contenedor-mensajes">
        <div
            v-for="mensaje in mensajesFiltrados"
            :key="mensaje.id"
            class="mensaje-wrap"
            :class="{ propio: esPropio(mensaje) }"
        >
          <Mensaje
              :mensaje="mensaje"
              @ver-info="mostrarInfoMensaje"
              @eliminar="eliminarMensaje"
          />
        </div>
      </div>

      <v-divider style="opacity:0.2; border-color:#406D73;" />

      <!-- Barra de entrada -->
      <div class="entrada-mensaje">
        <button class="btn-adjunto" title="Archivos adjunto">
          <v-icon size="18" color="#406D73" style="opacity:0.75">mdi-paperclip</v-icon>
          <span>Archivos adjunto</span>
        </button>
        <input
            v-model="contenidoNuevo"
            class="input-mensaje"
            type="text"
            placeholder="Barra de escribir mensajes"
            @keyup.enter="enviarMensaje"
        />
        <button class="btn-enviar" @click="enviarMensaje" :disabled="!contenidoNuevo.trim()">
          <v-icon size="16">mdi-send</v-icon>
          Enviar mensaje
        </button>
      </div>

    </template>

    <InfoGrupo v-else :conversacion="conversacionActual" @volver="mostrandoInfo = false" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import Mensaje from './Mensaje.vue'
import InfoGrupo from './InfoGrupo.vue'
import { formatearFecha } from '@/utilidades/formateoFechas'

const almacen = useAlmacen()
const contenidoNuevo = ref('')
const estadoUsuario = ref('ONLINE')
const ws = ref(null)
const contenedorMensajes = ref(null)
const mostrarModalAñadir = ref(false)
const mostrandoInfo = ref(false)
const terminoUsuario = ref('')
const usuariosDisponibles = ref([])
const mostrarModalInfo = ref(false)
const mensajeParaInfo = ref(null)

const conversacionActual = computed(() => almacen.conversacionActual)
const usuarioActual = computed(() => almacen.usuarioActual)
const mensajes = computed(() => almacen.mensajes)
const esGrupo = computed(() => conversacionActual.value?.tipo === 'GRUPO')

const destinatario = computed(() => {
  if (!conversacionActual.value) return { nombre: 'Chat', iniciales: '?' }
  return {
    nombre: conversacionActual.value.nombre,
    iniciales: conversacionActual.value.nombre?.charAt(0).toUpperCase() || '?',
  }
})

const usuariosFiltrados = computed(() => {
  const participantes = conversacionActual.value?.participanteIds || []
  let lista = usuariosDisponibles.value.filter(u => !participantes.includes(u.id))
  if (terminoUsuario.value) {
    const t = terminoUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const mensajesFiltrados = computed(() => mensajes.value)

const esPropio = (mensaje) => mensaje.emisorId === usuarioActual.value?.id

const scrollToBottom = async () => {
  await nextTick()
  if (contenedorMensajes.value) {
    contenedorMensajes.value.scrollTop = contenedorMensajes.value.scrollHeight
  }
}

const cargarMensajes = async () => {
  if (!conversacionActual.value) return
  try {
    const mensajesObtenidos = await servicioApi.obtenerMensajes(conversacionActual.value.id)
    almacen.establecerMensajes(mensajesObtenidos)
    scrollToBottom()
    await servicioApi.marcarConversacionLeida(conversacionActual.value.id)
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
  }
}

const abrirModalAñadir = async () => {
  try {
    usuariosDisponibles.value = await servicioApi.obtenerUsuarios()
    mostrarModalAñadir.value = true
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

const cerrarModalAñadir = () => {
  mostrarModalAñadir.value = false
  terminoUsuario.value = ''
}

const añadirMiembro = async (idUsuario) => {
  try {
    await servicioApi.añadirParticipante(conversacionActual.value.id, idUsuario)
    if (conversacionActual.value.participanteIds) {
      conversacionActual.value.participanteIds.push(idUsuario)
    }
    cerrarModalAñadir()
  } catch (error) {
    console.error('Error al añadir miembro:', error)
  }
}

const conectarWS = () => {
  if (ws.value) ws.value.close()
  if (conversacionActual.value && usuarioActual.value) {
    const token = almacen.token
    ws.value = servicioApi.conectarWebSocket(
        conversacionActual.value.id,
        usuarioActual.value.id,
        token
    )
    ws.value.onmessage = (event) => {
      const respuesta = JSON.parse(event.data)
      if (respuesta.tipo === 'mensaje') {
        almacen.agregarMensaje(respuesta.datos)
        scrollToBottom()
      }
    }
  }
}

const enviarMensaje = async () => {
  if (!contenidoNuevo.value.trim() || !conversacionActual.value) return
  const texto = contenidoNuevo.value.trim()
  contenidoNuevo.value = ''
  try {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify({ contenido: texto, tipoMensaje: 'TEXTO' }))
    } else {
      const m = await servicioApi.enviarMensaje({
        conversacionId: conversacionActual.value.id,
        contenido: texto,
        tipoMensaje: 'TEXTO',
      })
      almacen.agregarMensaje(m)
      scrollToBottom()
    }
  } catch (error) {
    console.error('Error al enviar mensaje:', error)
    contenidoNuevo.value = texto
  }
}

const mostrarInfoMensaje = (mensaje) => {
  mensajeParaInfo.value = mensaje
  mostrarModalInfo.value = true
}

const eliminarMensaje = async (mensaje) => {
  try {
    await servicioApi.eliminarMensaje(mensaje.id)
    // Marcar el mensaje como eliminado
    mensaje.eliminado = true
    mensaje.contenido = 'Mensaje eliminado'
  } catch (error) {
    console.error('Error al eliminar mensaje:', error)
  }
}

watch(conversacionActual, (nueva, vieja) => {
  if (nueva && nueva.id !== vieja?.id) {
    cargarMensajes()
    conectarWS()
  }
})

onMounted(() => {
  if (conversacionActual.value) {
    cargarMensajes()
    conectarWS()
  }
})

onUnmounted(() => {
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
/* ---- Raíz ---- */
.componente-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f7fcfd;
  overflow: hidden;
}

/* ---- Encabezado tipo banner ---- */
.chat-encabezado {
  flex-shrink: 0;
  cursor: pointer;
}

.profile-banner {
  height: 76px;
  background-color: #B3EBF2;
  background-image:
      linear-gradient(135deg, rgba(64,109,115,0.22) 0%, transparent 55%),
      linear-gradient(225deg, rgba(255,255,255,0.45) 0%, transparent 48%),
      radial-gradient(ellipse 90% 140% at 15% 0%, rgba(64,109,115,0.15), transparent);
  background-size: cover;
}

.profile-lower {
  position: relative;
  background: #f0f7f8;
  padding: 8px 14px 12px;
  padding-left: 100px;
  min-height: 72px;
}

.profile-avatar-wrap {
  position: absolute;
  left: 14px;
  top: -32px;
  z-index: 2;
}

.avatar-cuadrado {
  width: 64px;
  height: 64px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 10px;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 14px rgba(64,109,115,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
}

.profile-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.profile-name {
  font-size: 1rem;
  font-weight: 700;
  color: #2f4a4f;
  line-height: 1.25;
  letter-spacing: 0.01em;
}

.profile-subtitle {
  font-size: 12px;
  color: #5a8a94;
  margin-top: 2px;
}

.encabezado-acciones {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  transition: background 0.15s;
}

.btn-icon:hover {
  background: rgba(64,109,115,0.1);
}

.badge-estado {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.badge-estado.ONLINE {
  background: #6A9E7D;
  color: white;
}

.badge-estado.OFFLINE {
  background: #B2C5C8;
  color: white;
}

/* ---- Mensajes ---- */
.contenedor-mensajes {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: rgba(179, 235, 242, 0.28);
}

.mensaje-wrap {
  display: flex;
  animation: fadeIn 0.25s ease-out;
}

.mensaje-wrap.propio {
  justify-content: flex-end;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ---- Barra de entrada ---- */
.entrada-mensaje {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f7fcfd;
  flex-shrink: 0;
}

.btn-adjunto {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  background: #e4f0f2;
  border: 1px solid rgba(64,109,115,0.2);
  border-radius: 8px;
  color: #406D73;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}

.btn-adjunto:hover {
  background: #d2e8ec;
}

.input-mensaje {
  flex: 1;
  padding: 9px 14px;
  border: 1px solid rgba(64,109,115,0.22);
  border-radius: 8px;
  font-size: 14px;
  color: #2f4a4f;
  background: #ffffff;
  outline: none;
  min-width: 0;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.input-mensaje::placeholder {
  color: rgba(64,109,115,0.4);
}

.input-mensaje:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.1);
}

.btn-enviar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  background: #406D73;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.03em;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}

.btn-enviar:hover:not(:disabled) {
  background: #34585d;
}

.btn-enviar:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* ---- Modal ---- */
.modal-titulo {
  background: #406D73;
  color: white;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  padding: 14px 16px;
}

.modal-busqueda-input {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 12px 16px;
  background: #f0f7f8;
  border: 1px solid rgba(64,109,115,0.15);
  border-radius: 6px;
  padding: 6px 10px;
}

.modal-busqueda-input input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #2f4a4f;
  flex: 1;
  outline: none;
}

.modal-busqueda-input input::placeholder {
  color: rgba(64,109,115,0.4);
}

.modal-listado {
  max-height: 300px;
  overflow-y: auto;
}

.item-usuario-modal {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.12s;
}

.item-usuario-modal:hover {
  background: #f7fcfd;
}

.avatar-mini-modal {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.info-usuario-modal {
  flex: 1;
}

.info-usuario-modal .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #2f4a4f;
}
</style>