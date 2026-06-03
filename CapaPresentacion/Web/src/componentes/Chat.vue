<template>
  <div class="componente-chat" style="display: grid; grid-template-rows: auto minmax(0, 1fr) auto; height: 100%; width: 100%; overflow: hidden; background: #f8f9fa;">
    <template v-if="!mostrandoInfo">

      <!-- Header Compacto Estilo Telegram/Slack -->
      <div class="chat-header-compact" @click="mostrandoInfo = true">
        <div class="header-left">
          <div class="header-avatar">
            <img v-if="conversacionActual?.fotoUrl" :src="conversacionActual.fotoUrl" class="header-avatar-img" alt="avatar" />
            <span v-else>{{ destinatario.iniciales }}</span>
            <span v-if="!esGrupo && otroUsuario?.estado === 'ONLINE'" class="header-dot"></span>
          </div>
          <div class="header-info">
            <div style="display: flex; align-items: center; gap: 6px;">
              <span class="header-name">{{ destinatario.nombre }}</span>
              <v-btn
                v-if="!esGrupo && otroUsuario"
                icon
                variant="text"
                density="compact"
                size="small"
                @click.stop="toggleFavorito"
                title="Favorito"
              >
                <v-icon :color="esFavorito ? '#FFC107' : '#FFD54F'" size="20">
                  {{ esFavorito ? 'mdi-star' : 'mdi-star-outline' }}
                </v-icon>
              </v-btn>
            </div>
            <span class="header-status">
              <template v-if="!esGrupo && otroUsuario">
                <span :class="otroUsuario.estado === 'ONLINE' ? 'status-online' : 'status-offline'">
                  {{ otroUsuario.estado === 'ONLINE' ? '● En línea' : '● Desconectado' }}
                </span>
              </template>
              <template v-else>
                <span class="status-group">● Grupo de conversación</span>
              </template>
            </span>
          </div>
        </div>
        <div class="header-actions" @click.stop>
          <v-hover v-slot="{ isHovering, props }">
            <v-btn
              v-bind="props"
              v-if="!esAviso || rolUsuario === 'ADMIN'"
              icon="mdi-account-plus"
              :variant="isHovering ? 'flat' : 'outlined'"
              color="#406D73"
              size="small"
              @click="abrirModalAñadir"
              title="Añadir miembro"
              class="header-btn teal-hover-white"
            />
          </v-hover>
          <v-hover v-slot="{ isHovering, props }">
            <v-btn
              v-bind="props"
              icon="mdi-close"
              :variant="isHovering ? 'flat' : 'outlined'"
              color="error"
              size="small"
              @click="cerrarConversacion"
              title="Cerrar conversación"
              class="header-btn"
            />
          </v-hover>
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

      <!-- Modal Crear Tarea(fecha más amigable) -->
      <v-dialog v-model="mostrarModalFecha" max-width="480">
        <v-card rounded="lg">
          <v-card-title class="modal-titulo">Nueva tarea</v-card-title>
          <v-card-text>
            <div style="display:flex;flex-direction:column;gap:10px;padding:8px 4px;">
              <div style="display:flex;flex-direction:column;gap:6px;">
                <label style="font-weight:700;color:#2f4a4f">Nombre</label>
                <input type="text" v-model="textoPendienteTitulo" style="padding:8px;border-radius:8px;border:1px solid #B2C5C8;background:#fff;" />
              </div>
              <div style="display:flex;flex-direction:column;gap:6px;">
                <label style="font-weight:700;color:#2f4a4f">Contenido</label>
                <textarea v-model="textoPendienteContenido" style="padding:8px;border-radius:8px;border:1px solid #B2C5C8;background:#fff;" rows="3"></textarea>
              </div>
              <div style="display:flex;flex-direction:column;gap:6px;">
                <label style="font-weight:700;color:#2f4a4f">Fecha de vencimiento (opcional)</label>
                <input type="date" v-model="fechaVencInput" style="padding:8px;border-radius:8px;border:1px solid #B2C5C8;background:#fff;" />
              </div>
            </div>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn text @click="cancelarCrearTarea">Cancelar</v-btn>
            <v-btn color="accent" variant="flat" @click="confirmarCrearTarea" :loading="creandoTarea" :disabled="!textoPendienteTitulo.trim()">Crear</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Área de mensajes -->
      <div ref="contenedorMensajes" class="contenedor-mensajes" :style="estiloWallpaper">
        <!-- Cifrado estático al inicio -->
        <div class="mensaje-sistema-container">
          <div class="mensaje-sistema">
            <v-icon size="12" color="#406D73" class="mr-1" style="opacity: 0.8;">mdi-lock</v-icon>
            Los mensajes están cifrados de extremo a extremo
          </div>
        </div>

        <!-- Botón Cargar Más -->
        <div v-if="!todosCargados && mensajesFiltrados.length > 0" class="d-flex justify-center my-3">
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
            v-for="(mensaje, index) in mensajesFiltrados"
            :key="mensaje.id"
            class="mensaje-agrupador"
        >
          <!-- Date Header -->
          <div v-if="mostrarFechaHeader(index)" class="fecha-header-container">
            <div class="fecha-header">{{ formatearSoloFecha(mensaje.fechaEnvio) }}</div>
          </div>
          
          <div
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
      </div>

      <v-divider style="opacity:0.2; border-color:#406D73;" />

      <!-- Barra de entrada o Banner de Solo Lectura -->
      <div v-if="esAviso && rolUsuario !== 'ADMIN'" class="read-only-banner d-flex align-center justify-center pa-4 text-subtitle-2 font-weight-bold" style="background: rgba(64,109,115,0.08); border-top: 1px solid rgba(0,0,0,0.1); color: #406D73; gap: 8px;">
        <v-icon size="18" color="#406D73">mdi-lock</v-icon>
        Solo los administradores pueden enviar mensajes en este canal.
      </div>
      <div v-else class="entrada-mensaje">
        <input
            ref="fileInput"
            type="file"
            accept="image/*,application/pdf"
            @change="handleFileSelected"
            style="display:none"
        />
        <v-hover v-slot="{ isHovering, props }">
          <v-btn
            v-bind="props"
            icon="mdi-paperclip"
            :variant="isHovering ? 'flat' : 'text'"
            color="#406D73"
            class="btn-adjunto teal-hover-white"
            @click="seleccionarArchivo"
            title="Adjuntar archivo o imagen"
            :loading="subiendoArchivo"
          ></v-btn>
        </v-hover>
        <textarea
            v-model="contenidoNuevo"
            class="input-mensaje"
            :placeholder="String(conversacionActual?.id).startsWith('tareas_') ? '✏️  Escribe una nueva tarea...' : '✉️  Escribe un mensaje...'"
            @keydown="handleKeydown"
            rows="1"
            style="resize: none; overflow-y: hidden;"
        ></textarea>
        <v-btn
          variant="flat"
          :color="contenidoNuevo.trim() ? '#5A8A94' : '#e0f2f1'"
          height="38"
          class="btn-enviar-pill"
          :class="!contenidoNuevo.trim() ? 'btn-enviar-inactivo' : ''"
          @click="enviarMensaje"
        >
          <v-icon size="18" :color="contenidoNuevo.trim() ? 'white' : '#8aa8ae'" class="mr-1">mdi-send</v-icon>
          <span :style="{ color: contenidoNuevo.trim() ? 'white' : '#8aa8ae', fontWeight: 600, textTransform: 'none', fontSize: '14px', letterSpacing: '0.02em' }">Enviar</span>
        </v-btn>
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
import { formatearFecha, formatearSoloFecha } from '@/utilidades/formateoFechas'

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

// Modal fecha y estado de creación de tarea
const mostrarModalFecha = ref(false)
const fechaVencInput = ref(null)
const textoPendienteTitulo = ref('')
const textoPendienteContenido = ref('')
const creandoTarea = ref(false)

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

const estiloWallpaper = computed(() => {
  const url = conversacionActual.value?.imagenBanner || usuarioActual.value?.imagenBanner
  if (url) {
    return {
      backgroundImage: `url('${url}')`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }
  }
  return {}
})

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
  if (esGrupo.value || esAviso.value) return null
  const participante = conversacionActual.value?.participantes?.find(p => p.usuario?.id !== usuarioActual.value?.id)
  return participante?.usuario
})

const esFavorito = computed(() => {
  if (!otroUsuario.value || !usuarioActual.value) return false
  const favIds = usuarioActual.value.favoritos
    ? usuarioActual.value.favoritos.split(',').filter(x => x).map(Number)
    : []
  return favIds.includes(otroUsuario.value.id)
})

const togglingFavorito = ref(false)
const toggleFavorito = async () => {
  if (!otroUsuario.value || !usuarioActual.value || togglingFavorito.value) return
  togglingFavorito.value = true
  try {
    const favIds = usuarioActual.value.favoritos
      ? usuarioActual.value.favoritos.split(',').filter(x => x).map(Number)
      : []
    const index = favIds.indexOf(otroUsuario.value.id)
    if (index !== -1) {
      favIds.splice(index, 1)
    } else {
      if (favIds.length >= 3) {
        alert('Se podrá tener hasta un máximo de 3 contactos en favoritos.')
        togglingFavorito.value = false
        return
      }
      favIds.push(otroUsuario.value.id)
    }

    const nuevosFavoritos = favIds.join(',')
    const payload = {
      username: usuarioActual.value.username,
      fotoUrl: usuarioActual.value.fotoUrl,
      descripcion: usuarioActual.value.descripcion,
      imagenBanner: usuarioActual.value.imagenBanner,
      estado: usuarioActual.value.estado,
      favoritos: nuevosFavoritos
    }

    const usrActualizado = await servicioApi.actualizarPerfil(payload)
    almacen.establecerUsuario(usrActualizado)
    localStorage.setItem('usuario', JSON.stringify(usrActualizado))
  } catch (error) {
    console.error('Error al actualizar favoritos:', error)
  } finally {
    togglingFavorito.value = false
  }
}

const usuariosFiltrados = computed(() => {
  const participantes = conversacionActual.value?.participanteIds || []
  let lista = usuariosDisponibles.value.filter(u => !participantes.includes(u.id))
  if (terminoUsuario.value) {
    const t = terminoUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const mensajesFiltrados = computed(() => {
  return mensajes.value.filter(m => m.contenido !== 'Los mensajes están cifrados de extremo a extremo')
})

const esPropio = (mensaje) => mensaje.emisorId === usuarioActual.value?.id

const scrollToBottom = async () => {
  await nextTick()
  if (contenedorMensajes.value) {
    contenedorMensajes.value.scrollTop = contenedorMensajes.value.scrollHeight
  }
}

const cargarMensajes = async () => {
  if (!conversacionActual.value) return

  // Si es la conversación de tareas local, no llamar al backend
  if (String(conversacionActual.value.id).startsWith('tareas_')) {
    try {
      if (!usuarioActual.value) return
      const userId = usuarioActual.value.id
      const tareas = await servicioApi.obtenerTareas(userId)

      const mensajesTareas = tareas.map(t => ({
        id: t.id,
        conversacionId: conversacionActual.value.id,
        emisorId: t.emisorId,
        contenido: t.contenido,
        fechaEnvio: t.fechaEnvio,
        tipo: 'TAREA',
        fechaVencimiento: t.fechaVencimiento,
        completada: !!t.completada
      }))

      almacen.establecerMensajes(mensajesTareas)
      await nextTick()
      scrollToBottom()
    } catch (e) {
      console.error('Error cargando tareas en Chat:', e)
    }
    return
  }

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
    const conv = almacen.conversaciones.find(c => c.id === conversacionActual.value.id)
    if (conv) {
      conv.noLeidos = 0
    }
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

  // Tamaño máximo consistente con backend (15 MB)
  const MAX_BYTES = 15 * 1024 * 1024
  if (f.size > MAX_BYTES) {
    console.error('Archivo demasiado grande')
    alert('El archivo supera el tamaño máximo permitido (15 MB).')
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
    // No conectar WebSocket para la conversación de tareas local
    if (String(conversacionActual.value.id).startsWith('tareas_')) return

    const token = almacen.token
    ws.value = servicioApi.conectarWebSocket(
        conversacionActual.value.id,
        usuarioActual.value.id,
        token
    )
    ws.value.onmessage = (event) => {
      const respuesta = JSON.parse(event.data)
      if (respuesta.tipo === 'mensaje') {
        const msg = respuesta.datos
        almacen.agregarMensaje(msg)
        scrollToBottom()
        
        // Si el chat está abierto pero la ventana no tiene el foco, disparar notificación nativa
        if (typeof Notification !== 'undefined' && Notification.permission === 'granted') {
          if (!document.hasFocus() && msg.emisorId !== usuarioActual.value?.id) {
            const titulo = destinatario.value.nombre || 'Nuevo mensaje'
            new Notification(titulo, {
              body: msg.contenido,
              icon: conversacionActual.value?.fotoUrl || null
            })
          }
        }

        // Si la pestaña está enfocada, marcar inmediatamente como leída
        if (document.hasFocus()) {
          servicioApi.marcarConversacionLeida(conversacionActual.value.id)
        }
      } else if (respuesta.tipo === 'mensaje_resaltado') {
        almacen.actualizarMensaje(respuesta.datos)
      }
    }
  }
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    enviarMensaje()
  }
}

const mostrarFechaHeader = (index) => {
  if (index === 0) return true;
  const msgActual = mensajesFiltrados.value[index];
  const msgAnterior = mensajesFiltrados.value[index - 1];
  
  if (!msgActual || !msgAnterior) return false;
  
  const fechaActual = formatearSoloFecha(msgActual.fechaEnvio);
  const fechaAnterior = formatearSoloFecha(msgAnterior.fechaEnvio);
  
  return fechaActual !== fechaAnterior;
}

const enviarMensaje = async () => {
  if (!contenidoNuevo.value.trim() || !conversacionActual.value) return
  const texto = contenidoNuevo.value.trim()
  contenidoNuevo.value = ''
  
  try {
    // Si estamos en la conversación de tareas local, crear tarea local
    if (String(conversacionActual.value.id).startsWith('tareas_')) {
      // Abrir modal para seleccionar fecha y confirmar la tarea
      const lineas = texto.split('\n')
      textoPendienteTitulo.value = lineas[0] || ''
      textoPendienteContenido.value = lineas.slice(1).join('\n') || ''
      fechaVencInput.value = null
      mostrarModalFecha.value = true
      return
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
    // Si es tarea local, eliminar usando servicio local
    if (String(conversacionActual.value.id).startsWith('tareas_')) {
      await servicioApi.eliminarTarea(usuarioActual.value.id, mensaje.id)
      // Remover del almacen
      const nuevos = almacen.mensajes.filter(m => m.id !== mensaje.id)
      almacen.establecerMensajes(nuevos)
      // Actualizar metadatos de la conversación sintética (último mensaje)
      const convId = conversacionActual.value.id
      const convIdx = almacen.conversaciones.findIndex(c => c.id === convId)
      if (convIdx !== -1) {
        const conv = almacen.conversaciones[convIdx]
        const ultimo = nuevos.length ? (nuevos[nuevos.length - 1].contenido || '') : null
        conv.ultimoMensaje = ultimo
        conv.fechaUltimoMensaje = nuevos.length ? nuevos[nuevos.length - 1].fechaEnvio : null
        // Forzar actualización de la lista
        almacen.establecerConversaciones([...almacen.conversaciones])
      }
      return
    }

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
    // Limpiar mensajes inmediatamente para evitar mostrar mensajes de la conversación previa
    almacen.establecerMensajes([])
    determinarRolUsuario()
    cargarMensajes()
    conectarWS()
  }
})

const handleWindowFocus = () => {
  if (conversacionActual.value) {
    servicioApi.marcarConversacionLeida(conversacionActual.value.id)
    const conv = almacen.conversaciones.find(c => c.id === conversacionActual.value.id)
    if (conv) {
      conv.noLeidos = 0
    }
  }
}

onMounted(() => {
  window.addEventListener('focus', handleWindowFocus)
  if (conversacionActual.value) {
    determinarRolUsuario()
    cargarMensajes()
    conectarWS()
  }
})

onUnmounted(() => {
  window.removeEventListener('focus', handleWindowFocus)
  if (ws.value) ws.value.close()
})

const cancelarCrearTarea = () => {
  mostrarModalFecha.value = false
  textoPendienteTitulo.value = ''
  textoPendienteContenido.value = ''
  fechaVencInput.value = null
}

const confirmarCrearTarea = async () => {
  if (!textoPendienteTitulo.value || !usuarioActual.value) return
  creandoTarea.value = true
  try {
    // Crear tarea en el "servicio local"
    const nueva = await servicioApi.crearTarea(
        usuarioActual.value.id,
        textoPendienteTitulo.value,
        textoPendienteContenido.value,
        fechaVencInput.value || null
    )

    // Añadir a mensajes del almacen
    const nuevaComoMensaje = {
      id: nueva.id,
      conversacionId: nueva.conversacionId || `tareas_${usuarioActual.value.id}`,
      emisorId: nueva.emisorId || usuarioActual.value.id,
      titulo: nueva.titulo,
      contenido: nueva.contenido,
      fechaEnvio: nueva.fechaEnvio,
      tipo: 'TAREA',
      fechaVencimiento: nueva.fechaVencimiento || null,
      completada: !!nueva.completada
    }

    const listaMensajes = [...(almacen.mensajes || []), nuevaComoMensaje]
    almacen.establecerMensajes(listaMensajes)

    // Actualizar / crear conversación sintética de tareas
    const convId = `tareas_${usuarioActual.value.id}`
    const idx = almacen.conversaciones.findIndex(c => String(c.id) === convId)
    if (idx === -1) {
      almacen.agregarConversacion({
        id: convId,
        nombre: 'Tareas',
        tipo: 'TAREAS',
        ultimoMensaje: nueva.contenido,
        fechaUltimoMensaje: nueva.fechaEnvio
      })
    } else {
      const conv = almacen.conversaciones[idx]
      conv.ultimoMensaje = nueva.contenido
      conv.fechaUltimoMensaje = nueva.fechaEnvio
      almacen.establecerConversaciones([...almacen.conversaciones])
    }

    mostrarModalFecha.value = false
    // Limpiar los campos correctos (nombre y contenido de la tarea)
    textoPendienteTitulo.value = ''
    textoPendienteContenido.value = ''
    fechaVencInput.value = null

    await scrollToBottom()
  } catch (e) {
    console.error('Error creando tarea:', e)
    alert('No se pudo crear la tarea. Intenta nuevamente.')
  } finally {
    creandoTarea.value = false
  }
}
</script>

<style scoped>
/* ==================================================
   Chat.vue — Premium SaaS Styles 2026
   ================================================== */

/* ---- Raíz ---- */
.componente-chat {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  height: 100%;
  width: 100%;
  background: #f0f5f7;
  overflow: hidden;
  box-sizing: border-box;
  font-family: 'Inter', system-ui, sans-serif;
}

/* ===================================================
   HEADER COMPACTO (estilo Telegram/Slack)
   =================================================== */
.chat-header-compact {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #ffffff;
  border-bottom: 1px solid rgba(64,109,115,0.1);
  cursor: pointer;
  flex-shrink: 0;
  z-index: 10;
  min-height: 62px;
  transition: background .18s ease;
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}

.chat-header-compact:hover {
  background: rgba(179, 235, 242, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.header-avatar {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: visible;
  box-shadow: 0 2px 8px rgba(64,109,115,0.28);
}

.header-avatar-img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.header-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #6A9E7D;
  border: 2.5px solid #ffffff;
  box-shadow: 0 0 0 1px rgba(106,158,125,0.3);
}

.header-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 1px;
}

.header-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2e31;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.header-status {
  font-size: 12px;
  line-height: 1.4;
}

.status-online  { color: #6A9E7D; font-weight: 500; }
.status-offline { color: #8aa5ab; font-weight: 400; }
.status-group   { color: #8aa5ab; font-weight: 400; }

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-btn {
  transition: transform .15s ease !important;
}
.header-btn:hover {
  transform: scale(1.08) !important;
}

.teal-hover-white.v-btn--variant-flat {
  color: #ffffff !important;
}
.teal-hover-white.v-btn--variant-flat .v-icon,
.teal-hover-white.v-btn--variant-flat i {
  color: #ffffff !important;
}

/* ===================================================
   ÁREA DE MENSAJES
   =================================================== */
.contenedor-mensajes {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  /* Patrón muy sutil como WhatsApp */
  background-color: #e8f1f3;
  background-image:
    radial-gradient(circle at 1px 1px, rgba(64,109,115,0.06) 1px, transparent 0);
  background-size: 24px 24px;
}

.mensaje-agrupador {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.mensaje-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
  align-items: flex-start;
}

.mensaje-wrap.propio {
  align-items: flex-end;
}

/* ===================================================
   BANNERS Y SEPARADORES
   =================================================== */
.read-only-banner {
  background: rgba(64,109,115,0.05) !important;
  border-top: 1px solid rgba(64,109,115,0.08) !important;
  color: #406D73 !important;
}

/* ===================================================
   BARRA DE ENTRADA (estilo Telegram)
   =================================================== */
.entrada-mensaje {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #ffffff;
  flex-shrink: 0;
  z-index: 10;
  border-top: 1px solid rgba(64,109,115,0.08);
  min-height: 60px;
}

.btn-adjunto-icon {
  flex-shrink: 0;
  opacity: 0.65;
  transition: opacity .15s ease, transform .15s ease !important;
}
.btn-adjunto-icon:hover {
  opacity: 1;
  transform: rotate(-15deg) scale(1.1) !important;
}

.input-mensaje {
  flex: 1;
  padding: 10px 16px;
  border: 1.5px solid rgba(64,109,115,0.15);
  border-radius: 24px;
  font-size: 14px;
  font-family: 'Inter', system-ui, sans-serif;
  color: #1a2e31;
  background: #f7fbfc;
  outline: none;
  min-width: 0;
  max-height: 120px;
  line-height: 1.45;
  transition: border-color .18s ease, box-shadow .18s ease, background .18s ease;
}

.input-mensaje::placeholder {
  color: rgba(64,109,115,0.38);
  font-weight: 400;
}

.input-mensaje:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.12);
  background: #ffffff;
}

.btn-enviar-pill {
  flex-shrink: 0;
  border-radius: 20px !important;
  padding: 0 16px !important;
  transition: transform .15s ease, box-shadow .15s ease, background-color .15s !important;
}
.btn-enviar-pill:not(.btn-enviar-inactivo):hover {
  transform: scale(1.04) !important;
  box-shadow: 0 4px 12px rgba(64,109,115,0.35) !important;
}
.btn-enviar-pill:not(.btn-enviar-inactivo):active {
  transform: scale(0.98) !important;
}
.btn-enviar-inactivo {
  cursor: default !important;
  box-shadow: none !important;
}
.btn-enviar-inactivo:hover > .v-btn__overlay {
  opacity: 0 !important;
}

/* Mensaje Sistema (Pill de Cifrado) */
.mensaje-sistema-container {
  width: 100%;
  text-align: center;
  display: flex;
  justify-content: center;
  margin: 12px 0 20px;
}
.mensaje-sistema {
  font-size: 11px;
  font-weight: 600;
  color: #406D73;
  background: #f7fcfd;
  border: 1px solid rgba(64,109,115,0.15);
  padding: 6px 14px;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.01em;
}

/* Date header */
.fecha-header-container {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 16px 0 12px;
}
.fecha-header {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(4px);
  color: #5a8a94;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  letter-spacing: 0.02em;
}

/* ===================================================
   MODALES
   =================================================== */

/* Modal Crear Tarea */
.modal-titulo {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  padding: 14px 16px;
}

/* Modal Info Mensaje */
.modal-info-mensaje {
  box-shadow: 0 16px 48px rgba(64,109,115,0.18) !important;
}

.modal-titulo-mejorado {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-mejorado {
  padding: 16px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 10px !important;
}

.info-item {
  display: flex !important;
  flex-direction: column !important;
  gap: 4px !important;
  padding: 10px 14px !important;
  background: rgba(179,235,242,0.12) !important;
  border-radius: 12px !important;
  border-left: 3px solid #406D73 !important;
}

.info-label {
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
  font-size: 10px !important;
  font-weight: 700 !important;
  color: #406D73 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.05em !important;
}

.info-valor {
  margin: 0 !important;
  font-size: 13px !important;
  color: #1a2e31 !important;
  font-weight: 500 !important;
}

/* Modal Añadir Miembro */
.modal-anadir-miembro {
  box-shadow: 0 16px 48px rgba(64,109,115,0.18) !important;
}

.modal-titulo-anadir {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-anadir {
  padding: 14px !important;
  background: #f7fbfc !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 10px !important;
}

.modal-busqueda-anadir {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  background: #ffffff !important;
  border: 1.5px solid rgba(64,109,115,0.15) !important;
  border-radius: 12px !important;
  padding: 8px 12px !important;
  transition: border-color .15s, box-shadow .15s !important;
}

.modal-busqueda-anadir:focus-within {
  border-color: #406D73 !important;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.1) !important;
}

.modal-busqueda-anadir input {
  border: none !important;
  background: transparent !important;
  font-size: 13px !important;
  font-family: 'Inter', system-ui, sans-serif !important;
  color: #1a2e31 !important;
  flex: 1 !important;
  outline: none !important;
}

.modal-busqueda-anadir input::placeholder {
  color: rgba(64,109,115,0.38) !important;
}

.modal-listado-anadir {
  max-height: 300px !important;
  overflow-y: auto !important;
  border-radius: 12px !important;
  background: #ffffff !important;
  border: 1px solid rgba(64,109,115,0.1) !important;
}

.item-usuario-anadir {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 10px 12px !important;
  cursor: pointer !important;
  transition: background .15s !important;
  border-bottom: 1px solid rgba(64,109,115,0.05) !important;
}

.item-usuario-anadir:hover {
  background: rgba(179,235,242,0.2) !important;
}

.item-usuario-anadir:last-child {
  border-bottom: none !important;
}

.icon-plus-anadir {
  transition: transform .2s ease !important;
}

.item-usuario-anadir:hover .icon-plus-anadir {
  transform: scale(1.25) !important;
}

.avatar-mini-modal {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: linear-gradient(135deg, #406D73, #5a8a94);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.info-usuario-modal { flex: 1; }
.info-usuario-modal .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #1a2e31;
}

/* ===================================================
   RESPONSIVE
   =================================================== */
@media (max-width: 480px) {
  .chat-header-compact { padding: 8px 12px; min-height: 56px; }
  .header-avatar { width: 38px; height: 38px; font-size: 15px; }
  .header-name { font-size: 14px; }
  .entrada-mensaje { padding: 8px 10px; gap: 6px; }
  .input-mensaje { padding: 8px 14px; font-size: 14px; }
  .contenedor-mensajes { padding: 14px 10px 8px; }
}
</style>

