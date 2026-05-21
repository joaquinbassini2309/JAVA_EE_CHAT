<template>
  <div class="componente-chat" style="display: grid; grid-template-rows: auto minmax(0, 1fr) auto; height: 100%; width: 100%; overflow: hidden;">
    <template v-if="!mostrandoInfo">

      <!-- Encabezado con banner + avatar cuadrado -->
      <div class="chat-encabezado" @click="mostrandoInfo = true">
        <div class="profile-banner profile-banner--default"
          :style="conversacionActual?.imagenBanner ? { backgroundImage: `url('${conversacionActual.imagenBanner}')` } : {}"
          style="background-size: cover; background-position: center;"
        />
        <div class="profile-lower">
          <div class="profile-avatar-wrap">
            <div class="avatar-cuadrado">
              <img v-if="conversacionActual?.fotoUrl" :src="conversacionActual.fotoUrl" class="avatar-img" alt="avatar" />
              <span v-else>{{ destinatario.iniciales }}</span>
            </div>
          </div>
          <div class="profile-title-row">
            <span class="profile-name text-truncate">{{ destinatario.nombre }}</span>
            <div class="encabezado-acciones" @click.stop>
              <v-btn icon="mdi-account-plus" variant="flat" color="accent" size="small" density="comfortable" @click="abrirModalAñadir" title="Añadir miembro" />
              <v-btn v-if="!esAviso || rolUsuario === 'ADMIN'" icon="mdi-account-plus" variant="flat" color="accent" size="small" density="comfortable" @click="abrirModalAñadir" title="Añadir miembro" />
              <span class="badge-estado" :class="estadoUsuario">{{ estadoUsuario }}</span>
              <v-btn icon="mdi-close" variant="flat" color="error" size="small" density="comfortable" @click="cerrarConversacion" title="Cerrar conversación" />
            </div>
          </div>
          <div class="profile-subtitle">
            <template v-if="!esGrupo && otroUsuario">
              <span :class="{'texto-online': otroUsuario.estado === 'ONLINE', 'texto-offline': otroUsuario.estado === 'OFFLINE'}">
                {{ otroUsuario.estado === 'ONLINE' ? 'En línea' : 'Desconectado' }}
              </span>
            </template>
            <template v-else>
              Grupo
            </template>
          </div>
        </div>
      </div>

      <!-- Modal Añadir Miembro -->
      <v-dialog v-model="mostrarModalAñadir" max-width="420">
        <v-card rounded="2xl" class="modal-anadir-miembro">
          <v-card-title class="modal-titulo-anadir">
            <v-icon size="18" color="white" class="mr-2">mdi-account-plus</v-icon>
            Añadir al Grupo
            <v-spacer />
            <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModalAñadir" />
          </v-card-title>
          <v-card-text class="modal-contenido-anadir">
            <div class="modal-busqueda-anadir">
              <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
              <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
            </div>
            <div class="modal-listado-anadir">
              <div
                  v-for="usuario in usuariosFiltrados"
                  :key="usuario.id"
                  class="item-usuario-anadir"
                  @click="añadirMiembro(usuario.id)"
              >
                <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
                <div class="info-usuario-modal">
                  <span class="nombre">{{ usuario.username }}</span>
                </div>
                <v-icon size="18" color="#406D73" class="icon-plus-anadir">mdi-plus-circle</v-icon>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <!-- Modal Info Mensaje -->
      <v-dialog v-model="mostrarModalInfo" max-width="420">
        <v-card v-if="mensajeParaInfo" rounded="2xl" class="modal-info-mensaje">
          <v-card-title class="modal-titulo-mejorado">
            <v-icon size="18" color="white" class="mr-2">mdi-information-outline</v-icon>
            Información del Mensaje
            <v-spacer />
            <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalInfo = false" />
          </v-card-title>
          <v-card-text class="modal-contenido-mejorado">
            <!-- Contenido -->
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-message-text</v-icon>
                Contenido
              </div>
              <p class="info-valor">{{ mensajeParaInfo.contenido }}</p>
            </div>

            <!-- Enviado por -->
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-account</v-icon>
                Enviado por
              </div>
              <p class="info-valor">{{ esPropio(mensajeParaInfo) ? 'Tú' : (mensajeParaInfo.emisorNombre || 'Desconocido') }}</p>
            </div>

            <!-- Fecha -->
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-calendar-outline</v-icon>
                Fecha
              </div>
              <p class="info-valor">{{ formatearFecha(mensajeParaInfo.fechaEnvio) }}</p>
            </div>

            <!-- Estado -->
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">{{ mensajeParaInfo.leido ? 'mdi-check-all' : 'mdi-check' }}</v-icon>
                Estado
              </div>
              <p class="info-valor">
                <v-chip
                  :color="mensajeParaInfo.leido ? '#6A9E7D' : '#B2C5C8'"
                  text-color="white"
                  size="small"
                  label
                >
                  {{ mensajeParaInfo.leido ? '✓✓ Leído' : '✓ Entregado' }}
                </v-chip>
              </p>
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <!-- Área de mensajes -->
      <div ref="contenedorMensajes" class="contenedor-mensajes">
        <!-- Botón Cargar Más -->
        <div v-if="!todosCargados" class="d-flex justify-center my-3">
          <v-btn
            variant="flat"
            color="#455A64"
            class="text-white btn-cargar-mas"
            size="small"
            rounded="pill"
            :loading="cargandoAnteriores"
            prepend-icon="mdi-history"
            @click="cargarMasMensajes"
          >
            Cargar mensajes anteriores
          </v-btn>
        </div>
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

      <!-- Barra de entrada o Banner de Solo Lectura -->
      <div v-if="esAviso && rolUsuario !== 'ADMIN'" class="read-only-banner d-flex align-center justify-center pa-4 text-subtitle-2 font-weight-bold" style="background: rgba(64,109,115,0.08); border-top: 1px solid rgba(0,0,0,0.1); color: #406D73; gap: 8px;">
        <v-icon size="18" color="#406D73">mdi-lock</v-icon>
        Solo los administradores pueden enviar mensajes en este canal.
      </div>
      <div v-else class="entrada-mensaje">
        <!-- Nuevo: input de archivo oculto -->
        <input
            ref="fileInput"
            type="file"
            accept="image/*,application/pdf"
            @change="handleFileSelected"
            style="display:none"
        />
        <button class="btn-adjunto" title="Archivos adjunto" @click="seleccionarArchivo">
          <v-icon size="18" color="#406D73" style="opacity:0.75">mdi-paperclip</v-icon>
          <span class="btn-texto">Archivos adjunto</span>
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
          <span class="btn-texto">Enviar mensaje</span>
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
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
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

// Nuevo: input de archivo y estado de subida
const fileInput = ref(null)
const subiendoArchivo = ref(false)
const cargandoMas = ref(false)
const offsetMensajes = ref(0)
const todosCargados = ref(false)
const cargandoAnteriores = computed(() => cargandoMas.value)

const conversacionActual = computed(() => almacen.conversacionActual)
const usuarioActual = computed(() => almacen.usuarioActual)
const mensajes = computed(() => almacen.mensajes)
const esGrupo = computed(() => conversacionActual.value?.tipo === 'GRUPO')
const esAviso = computed(() => conversacionActual.value?.tipo === 'AVISO')
const rolUsuario = ref('ADMIN')

const determinarRolUsuario = async () => {
  if (!conversacionActual.value) return
  if (esAviso.value) {
    try {
      const res = await servicioApi.obtenerRolEnConversacion(conversacionActual.value.id)
      rolUsuario.value = res.rol || 'MIEMBRO'
    } catch (error) {
      console.error('Error al obtener rol en canal de avisos:', error)
      rolUsuario.value = 'MIEMBRO'
    }
  } else {
    rolUsuario.value = 'ADMIN'
  }
}

const destinatario = computed(() => {
  if (!conversacionActual.value) return { nombre: 'Chat', iniciales: '?' }
  const nombreVisible = obtenerNombreVisibleConversacion(conversacionActual.value, usuarioActual.value?.id)
  return {
    nombre: nombreVisible,
    iniciales: nombreVisible?.charAt(0).toUpperCase() || '?',
  }
})

const otroUsuario = computed(() => {
  if (esGrupo.value) return null
  const participante = conversacionActual.value?.participantes?.find(p => p.usuario.id !== usuarioActual.value?.id)
  return participante?.usuario
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
    offsetMensajes.value = 0
    todosCargados.value = false
    const mensajesObtenidos = await servicioApi.obtenerMensajes(conversacionActual.value.id, 6, 0)
    almacen.establecerMensajes(mensajesObtenidos)
    if (mensajesObtenidos.length < 6) {
      todosCargados.value = true
    }
    scrollToBottom()
    await servicioApi.marcarConversacionLeida(conversacionActual.value.id)
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
  }
}

const cargarMasMensajes = async () => {
  if (!conversacionActual.value || cargandoMas.value || todosCargados.value) return
  try {
    cargandoMas.value = true
    offsetMensajes.value += 6
    const anteriores = await servicioApi.obtenerMensajes(conversacionActual.value.id, 6, offsetMensajes.value)
    if (anteriores.length > 0) {
      almacen.establecerMensajes([...anteriores, ...mensajes.value])
      if (anteriores.length < 6) {
        todosCargados.value = true
      }
    } else {
      todosCargados.value = true
    }
  } catch (error) {
    console.error('Error al cargar más mensajes:', error)
  } finally {
    cargandoMas.value = false
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

// Nuevo: abrir selector de archivos
const seleccionarArchivo = () => {
  if (fileInput.value) fileInput.value.click()
}

const handleFileSelected = async (event) => {
  const f = event.target.files && event.target.files[0]
  if (!f) return

  // Tamaño máximo consistente con backend (10 MB)
  const MAX_BYTES = 10 * 1024 * 1024
  if (f.size > MAX_BYTES) {
    console.error('Archivo demasiado grande')
    alert('El archivo supera el tamaño máximo permitido (10 MB).')
    return
  }

  try {
    subiendoArchivo.value = true
    const reader = new FileReader()
    reader.onload = async (e) => {
      try {
        let base64 = e.target.result
        // El método uploadFile espera base64 posiblemente con prefijo data:...; lo aceptamos
        const nombre = f.name
        const resp = await servicioApi.uploadFile(nombre, base64)
        // resp.url contiene la ruta pública
        const urlAdjunto = resp.url

        // Determinar tipoMensaje
        const tipo = f.type.startsWith('image/') ? 'IMAGEN' : 'DOCUMENTO'

        // Enviar mensaje con adjunto
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
          // Si WebSocket está abierto, enviar por WebSocket
          // El servidor responderá con el mensaje en el mismo canal
          ws.value.send(JSON.stringify({
            contenido: nombre,
            tipoMensaje: tipo,
            urlAdjunto: urlAdjunto
          }))
        } else {
          // Si no hay WebSocket, enviar por HTTP y agregar localmente
          const m = await servicioApi.enviarMensaje({
            conversacionId: conversacionActual.value.id,
            contenido: nombre,
            tipoMensaje: tipo,
            urlAdjunto: urlAdjunto
          })
          almacen.agregarMensaje(m)
          scrollToBottom()
        }
      } catch (err) {
        console.error('Error al subir o enviar archivo:', err)
        alert('Error al subir el archivo. Intenta de nuevo.')
      } finally {
        subiendoArchivo.value = false
        // limpiar input
        if (fileInput.value) fileInput.value.value = null
      }
    }
    reader.onerror = (err) => {
      console.error('Error leyendo archivo:', err)
      subiendoArchivo.value = false
    }
    reader.readAsDataURL(f)
  } catch (err) {
    console.error('Error procesando archivo:', err)
    subiendoArchivo.value = false
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
  
  const esPrimerMensaje = mensajes.value.length === 0 && !esGrupo.value;
  
  try {
    if (esPrimerMensaje) {
      // Enviar mensaje de cifrado ANTES del mensaje del usuario
      const cifradoMsg = await servicioApi.enviarMensaje({
        conversacionId: conversacionActual.value.id,
        contenido: 'Los mensajes están cifrados de extremo a extremo',
        tipoMensaje: 'TEXTO'
      })
      almacen.agregarMensaje(cifradoMsg)
      scrollToBottom()
    }

    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      // Si WebSocket está abierto, enviar por WebSocket
      ws.value.send(JSON.stringify({ contenido: texto, tipoMensaje: 'TEXTO' }))
    } else {
      // Si no hay WebSocket, enviar por HTTP y agregar localmente
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

const cerrarConversacion = () => {
  almacen.establecerConversacionActual(null)
}

watch(conversacionActual, (nueva, vieja) => {
  if (nueva && nueva.id !== vieja?.id) {
    determinarRolUsuario()
    cargarMensajes()
    conectarWS()
  }
})

onMounted(() => {
  if (conversacionActual.value) {
    determinarRolUsuario()
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
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  height: 100%;
  width: 100%;
  background: #f7fcfd;
  overflow: hidden;
  box-sizing: border-box;
}

/* ---- Encabezado tipo banner ---- */
.chat-encabezado {
  flex-shrink: 0;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
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
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.texto-online {
  color: #6A9E7D;
  font-weight: 600;
}

.texto-offline {
  color: #7f9ea4;
  font-weight: 600;
}

/* ---- Mensajes ---- */
.contenedor-mensajes {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: rgba(179, 235, 242, 0.28);
}

/* Scrollbar MUY visible para mensajes movido a global */
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
  background: #ffffff;
  flex-shrink: 0;
  z-index: 10;
  border-top: 1px solid rgba(64,109,115,0.1);
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

@media (max-width: 480px) {
  .btn-texto {
    display: none;
  }
  .entrada-mensaje {
    flex-direction: row !important;
    flex-wrap: nowrap !important;
    padding: 8px 10px 16px 10px;
    padding-bottom: calc(env(safe-area-inset-bottom, 16px) + 8px);
    gap: 6px;
  }
  .btn-adjunto, .btn-enviar {
    width: 36px !important;
    height: 36px !important;
    padding: 0 !important;
    justify-content: center !important;
    flex-shrink: 0;
  }
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

/* Adjuntos */
.adjunto-imagen {
  max-width: 280px;
  max-height: 280px;
  border-radius: 8px;
  display: block;
  margin-top: 8px;
}

/* ---- Estilos Mejorados Modal Info Mensaje ---- */
.modal-info-mensaje {
  box-shadow: 0 10px 40px rgba(64, 109, 115, 0.15) !important;
  background: #ffffff !important;
}

.modal-titulo-mejorado {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  border-radius: 24px 24px 0 0 !important;
}

.modal-contenido-mejorado {
  padding: 16px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
}

.info-item {
  display: flex !important;
  flex-direction: column !important;
  gap: 4px !important;
  padding: 10px 12px !important;
  background: rgba(179, 235, 242, 0.15) !important;
  border-radius: 12px !important;
  border-left: 3px solid #406D73 !important;
}

.info-label {
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  color: #406D73 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.02em !important;
}

.info-valor {
  margin: 0 !important;
  font-size: 12px !important;
  color: #2f4a4f !important;
  font-weight: 500 !important;
}

/* ---- Estilos Mejorados Modal Añadir Miembro ---- */
.modal-anadir-miembro {
  box-shadow: 0 10px 40px rgba(64, 109, 115, 0.15) !important;
}

.modal-titulo-anadir {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  border-radius: 24px 24px 0 0 !important;
}

.modal-contenido-anadir {
  padding: 12px !important;
  background: #f7fcfd !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 10px !important;
  border-radius: 0 0 24px 24px !important;
}

.modal-busqueda-anadir {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  background: #ffffff !important;
  border: 1px solid rgba(64, 109, 115, 0.15) !important;
  border-radius: 10px !important;
  padding: 8px 12px !important;
}

.modal-busqueda-anadir input {
  border: none !important;
  background: transparent !important;
  font-size: 12px !important;
  color: #2f4a4f !important;
  flex: 1 !important;
  outline: none !important;
}

.modal-busqueda-anadir input::placeholder {
  color: rgba(64, 109, 115, 0.4) !important;
}

.modal-listado-anadir {
  max-height: 320px !important;
  overflow-y: auto !important;
  border-radius: 10px !important;
  background: #ffffff !important;
  border: 1px solid rgba(64, 109, 115, 0.1) !important;
}

.item-usuario-anadir {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 10px 12px !important;
  cursor: pointer !important;
  transition: background 0.12s !important;
  border-bottom: 1px solid rgba(64, 109, 115, 0.05) !important;
}

.item-usuario-anadir:hover {
  background: rgba(179, 235, 242, 0.15) !important;
}

.item-usuario-anadir:last-child {
  border-bottom: none !important;
}

.icon-plus-anadir {
  transition: transform 0.2s !important;
}

.item-usuario-anadir:hover .icon-plus-anadir {
  transform: scale(1.2) !important;
}

/* ---- Media Queries para Responsividad ---- */
@media (max-width: 768px) {
  .profile-lower {
    padding-left: 90px;
    min-height: 68px;
  }

  .avatar-cuadrado {
    width: 56px;
    height: 56px;
  }

  .entrada-mensaje {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .profile-banner {
    height: 60px;
  }

  .profile-lower {
    padding-left: 76px;
    min-height: 64px;
  }

  .avatar-cuadrado {
    width: 48px;
    height: 48px;
  }

  .entrada-mensaje {
    flex-direction: column;
    gap: 8px;
  }

  .btn-adjunto, .input-mensaje, .btn-enviar {
    width: 100%;
  }
}
</style>
