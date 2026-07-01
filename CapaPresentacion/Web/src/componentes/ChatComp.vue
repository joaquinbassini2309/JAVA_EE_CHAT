<template>
  <div class="componente-chat" style="display: flex; flex-direction: column; height: 100%; width: 100%; overflow: hidden; background: var(--bg);">
    <template v-if="!mostrandoInfo">

      <!-- Cabecera del chat -->
      <ChatCabecera
        :conversacionActual="conversacionActual"
        :destinatario="destinatario"
        :otroUsuario="otroUsuario"
        :esGrupo="esGrupo"
        :esAviso="esAviso"
        :esFavorito="esFavorito"
        :rolUsuario="rolUsuario"
        @click-info="mostrandoInfo = true"
        @toggle-favorito="toggleFavorito"
        @abrir-modal-anadir="abrirModalAñadir"
        @cerrar-conversacion="cerrarConversacion"
      />

      <!-- Barra de Mensaje Fijado -->
      <div v-if="conversacionActual?.mensajeFijado" class="barra-mensaje-fijado" @click="irAlMensaje(conversacionActual.mensajeFijado.id)">
        <v-icon size="16" color="#406D73" class="fijado-icono mr-2">mdi-pin</v-icon>
        <div class="fijado-cuerpo">
          <div class="fijado-titulo">Mensaje fijado</div>
          <div class="fijado-texto">{{ conversacionActual.mensajeFijado.contenido || 'Archivo adjunto' }}</div>
        </div>
        <button class="fijado-btn-cerrar" @click.stop="desfijarMensajeActual" title="Desfijar mensaje">
          <v-icon size="14">mdi-close</v-icon>
        </button>
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
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-message-text</v-icon>
                Contenido
              </div>
              <p class="info-valor">{{ mensajeParaInfo.contenido }}</p>
            </div>
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-account</v-icon>
                Enviado por
              </div>
              <p class="info-valor">{{ esPropio(mensajeParaInfo) ? 'Tú' : (mensajeParaInfo.emisorNombre || 'Desconocido') }}</p>
            </div>
            <div class="info-item">
              <div class="info-label">
                <v-icon size="14" color="#406D73">mdi-calendar-outline</v-icon>
                Fecha
              </div>
              <p class="info-valor">{{ formatearFecha(mensajeParaInfo.fechaEnvio) }}</p>
            </div>
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

      <!-- Modal Crear Tarea -->
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

      <!-- Lista de Mensajes -->
      <ChatMensajesLista
        ref="mensajesListaRef"
        :mensajesFiltrados="mensajesFiltrados"
        :todosCargados="todosCargados"
        :cargandoAnteriores="cargandoAnteriores"
        :estiloWallpaper="estiloWallpaper"
        :usuarioActual="usuarioActual"
        @cargar-mas-mensajes="cargarMasMensajes"
        @ver-info="mostrarInfoMensaje"
        @eliminar="eliminarMensaje"
        @fijar="fijarMensaje"
      />

      <v-divider style="opacity:0.2; border-color:#406D73;" />

      <!-- Barra de entrada de texto -->
      <ChatEntradaTexto
        :conversacionActual="conversacionActual"
        :usuarioActual="usuarioActual"
        :esGrupo="esGrupo"
        :esAviso="esAviso"
        :rolUsuario="rolUsuario"
        :subiendoArchivo="subiendoArchivo"
        @enviar-mensaje="manejarEnviarMensaje"
        @subir-archivo="manejarSubirArchivo"
      />

    </template>

    <InfoGrupo v-else :conversacion="conversacionActual" @volver="mostrandoInfo = false" />
    
    <!-- Modal de error de subida (API REST) -->
    <v-dialog v-model="mostrarErrorSubida" max-width="400">
      <v-card rounded="xl">
        <v-card-title class="d-flex align-center pa-4 pb-0" style="color: #406D73;">
          <v-icon color="#406D73" class="mr-2">mdi-alert-circle</v-icon>
          Error al enviar
        </v-card-title>
        <v-card-text class="pa-4 pt-2 text-body-1">
          No se pudo enviar el archivo. Puede que exceda el tamaño soportado por el servidor.<br><br>
          <small style="color: #406D73; font-weight: bold;">{{ mensajeErrorSubida }}</small>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn style="background-color: #406D73; color: white;" variant="flat" rounded="pill" @click="mostrarErrorSubida = false">Entendido</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
import { formatearFecha } from '@/utilidades/formateoFechas'

import ChatCabecera from './ChatCabecera.vue'
import ChatMensajesLista from './ChatMensajesLista.vue'
import ChatEntradaTexto from './ChatEntradaTexto.vue'
import InfoGrupo from './InfoGrupo.vue'

const almacen = useAlmacen()
const ws = ref(null)
const mensajesListaRef = ref(null)
const mostrarModalAñadir = ref(false)
const mostrandoInfo = ref(false)
const terminoUsuario = ref('')
const usuariosDisponibles = ref([])
const mostrarModalInfo = ref(false)
const mensajeParaInfo = ref(null)

const mostrarModalFecha = ref(false)
const fechaVencInput = ref(null)
const textoPendienteTitulo = ref('')
const textoPendienteContenido = ref('')
const creandoTarea = ref(false)

const subiendoArchivo = ref(false)
const mostrarErrorSubida = ref(false)
const mensajeErrorSubida = ref('')
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
      '--chat-wallpaper-url': `url('${url}')`,
      '--chat-wallpaper-size': 'cover',
      '--chat-wallpaper-repeat': 'no-repeat',
      '--chat-wallpaper-opacity': '0.38'
    }
  }
  return {}
})

// Determinar el rol del usuario en la conversacion.
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

// Alternar el estado de favorito de un usuario.
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

const scrollToBottom = () => {
  if (mensajesListaRef.value) {
    mensajesListaRef.value.scrollToBottom()
  }
}

// Cargar mensajes iniciales.
const cargarMensajes = async () => {
  if (!conversacionActual.value) return

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
      conv.mencionesSinLeer = 0
    }
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
  }
}

// Cargar mensajes mas antiguos.
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

// Abrir modal para añadir un miembro al grupo.
const abrirModalAñadir = async () => {
  try {
    usuariosDisponibles.value = await servicioApi.obtenerUsuarios()
    mostrarModalAñadir.value = true
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

// Añadir un miembro al grupo.
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

// Conectar al socket de mensajes de la conversacion.
const conectarWS = () => {
  if (ws.value) ws.value.close()
  if (conversacionActual.value && usuarioActual.value) {
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
        
        if (typeof Notification !== 'undefined' && Notification.permission === 'granted') {
          if (!document.hasFocus() && msg.emisorId !== usuarioActual.value?.id) {
            const username = almacen.usuarioActual?.username
            const esMencion = username && (msg.contenido.includes(`@${username}`) || msg.contenido.includes('@todos'))
            
            const titulo = esMencion 
              ? `🔔 ¡Fuiste mencionado en ${destinatario.value.nombre || 'un grupo'}!` 
              : (destinatario.value.nombre || 'Nuevo mensaje')
            
            new Notification(titulo, {
              body: msg.contenido,
              icon: conversacionActual.value?.fotoUrl || null
            })
          }
        }

        if (document.hasFocus()) {
          servicioApi.marcarConversacionLeida(conversacionActual.value.id)
        }
      } else if (respuesta.tipo === 'mensaje_resaltado') {
        almacen.actualizarMensaje(respuesta.datos)
      } else if (respuesta.tipo === 'mensaje_eliminado') {
        almacen.actualizarMensaje(respuesta.datos)
      } else if (respuesta.tipo === 'mensajes_leidos') {
        const datos = respuesta.datos
        if (datos.usuarioId !== usuarioActual.value?.id) {
          almacen.mensajes.forEach(m => {
            if (m.emisorId === usuarioActual.value?.id) {
              m.leido = true
            }
          })
        }
      } else if (respuesta.tipo === 'mensajeFijado') {
        almacen.actualizarMensajeFijado(conversacionActual.value.id, respuesta.datos)
      } else if (respuesta.tipo === 'mensajeDesfijado') {
        almacen.actualizarMensajeFijado(conversacionActual.value.id, null)
      } else if (respuesta.tipo === 'nuevaTarea') {
        const t = respuesta.datos
        if (!almacen.tareas.find(x => x.id === t.id)) {
          almacen.tareas.push(t)
        }
      } else if (respuesta.tipo === 'tareaActualizada') {
        const index = almacen.tareas.findIndex(t => t.id === respuesta.datos.id)
        if (index !== -1) {
          almacen.tareas[index] = respuesta.datos
        }
      } else if (respuesta.tipo === 'tareaEliminada') {
        almacen.tareas = almacen.tareas.filter(t => t.id !== respuesta.datos)
      }
    }
  }
}

// Manejar el envio de mensaje de texto.
const manejarEnviarMensaje = async (texto) => {
  if (!conversacionActual.value) return
  
  try {
    if (String(conversacionActual.value.id).startsWith('tareas_')) {
      const lineas = texto.split('\n')
      textoPendienteTitulo.value = lineas[0] || ''
      textoPendienteContenido.value = lineas.slice(1).join('\n') || ''
      fechaVencInput.value = null
      mostrarModalFecha.value = true
      return
    }

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
  }
}

// Manejar la subida de un archivo.
const manejarSubirArchivo = async ({ nombre, base64, tipo }) => {
  if (!conversacionActual.value) return
  try {
    subiendoArchivo.value = true
    const resp = await servicioApi.uploadFile(nombre, base64)
    const urlAdjunto = resp.url

    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify({
        contenido: nombre,
        tipoMensaje: tipo,
        urlAdjunto: urlAdjunto
      }))
    } else {
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
    console.error('Error al subir archivo:', err)
    mensajeErrorSubida.value = err.response?.data?.message || err.message || 'Ocurrió un error inesperado al subir el archivo.'
    mostrarErrorSubida.value = true
  } finally {
    subiendoArchivo.value = false
  }
}

// Mostrar informacion detallada del mensaje.
const mostrarInfoMensaje = (mensaje) => {
  mensajeParaInfo.value = mensaje
  mostrarModalInfo.value = true
}

// Eliminar un mensaje.
const eliminarMensaje = async (mensaje) => {
  try {
    if (String(conversacionActual.value.id).startsWith('tareas_')) {
      await servicioApi.eliminarTarea(usuarioActual.value.id, mensaje.id)
      const nuevos = almacen.mensajes.filter(m => m.id !== mensaje.id)
      almacen.establecerMensajes(nuevos)
      const convId = conversacionActual.value.id
      const convIdx = almacen.conversaciones.findIndex(c => c.id === convId)
      if (convIdx !== -1) {
        const conv = almacen.conversaciones[convIdx]
        const ultimo = nuevos.length ? (nuevos[nuevos.length - 1].contenido || '') : null
        conv.ultimoMensaje = ultimo
        conv.fechaUltimoMensaje = nuevos.length ? nuevos[nuevos.length - 1].fechaEnvio : null
        almacen.establecerConversaciones([...almacen.conversaciones])
      }
      return
    }

    await servicioApi.eliminarMensaje(mensaje.id)
    mensaje.eliminado = true
    mensaje.contenido = 'Mensaje eliminado'
    mensaje.urlAdjunto = null
    mensaje.tipo = 'TEXTO'
    mensaje.tipoMensaje = 'TEXTO'
  } catch (error) {
    console.error('Error al eliminar mensaje:', error)
  }
}

// Fijar un mensaje en la conversacion.
const fijarMensaje = async (mensaje) => {
  if (!conversacionActual.value) return
  try {
    const dtMensaje = await servicioApi.fijarMensaje(conversacionActual.value.id, mensaje.id)
    almacen.actualizarMensajeFijado(conversacionActual.value.id, dtMensaje)
  } catch (error) {
    console.error('Error al fijar mensaje:', error)
  }
}

// Desfijar el mensaje actual de la conversacion.
const desfijarMensajeActual = async () => {
  if (!conversacionActual.value) return
  try {
    await servicioApi.desfijarMensaje(conversacionActual.value.id)
    almacen.actualizarMensajeFijado(conversacionActual.value.id, null)
  } catch (error) {
    console.error('Error al desfijar mensaje:', error)
  }
}

// Hacer scroll y resaltar un mensaje por su ID.
const irAlMensaje = async (mensajeId) => {
  if (!mensajeId) return
  
  let el = document.getElementById(`msg-${mensajeId}`)
  const yaCargado = mensajes.value.some(m => m.id === mensajeId)
  
  if (!yaCargado) {
    try {
      cargandoMas.value = true
      let encontrado = false
      let limiteIntentos = 5
      while (!encontrado && !todosCargados.value && limiteIntentos > 0) {
        offsetMensajes.value += 6
        const anteriores = await servicioApi.obtenerMensajes(conversacionActual.value.id, 6, offsetMensajes.value)
        if (anteriores.length > 0) {
          almacen.establecerMensajes([...anteriores, ...mensajes.value])
          if (anteriores.length < 6) {
            todosCargados.value = true
          }
          if (anteriores.some(m => m.id === mensajeId)) {
            encontrado = true
          }
        } else {
          todosCargados.value = true
        }
        limiteIntentos--
      }
    } catch (e) {
      console.error('Error al intentar cargar mensaje para scroll:', e)
    } finally {
      cargandoMas.value = false
    }
  }

  await nextTick()
  el = document.getElementById(`msg-${mensajeId}`)
  
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('mensaje-resaltado-temp')
    setTimeout(() => {
      el.classList.remove('mensaje-resaltado-temp')
    }, 2000)
  } else {
    console.warn(`No se encontró el elemento con ID msg-${mensajeId}`)
  }
}

// Cerrar la ventana del chat.
const cerrarConversacion = () => {
  if (window.innerWidth < 960) {
    if (window.history.state && window.history.state.appNav === 'chat') {
      window.history.back()
    } else {
      almacen.establecerConversacionActual(null)
    }
  } else {
    almacen.establecerConversacionActual(null)
  }
}

watch(conversacionActual, (nueva, vieja) => {
  if (nueva && nueva.id !== vieja?.id) {
    almacen.establecerMensajes([])
    determinarRolUsuario()
    cargarMensajes()
    conectarWS()
  }
})

// Manejar cuando la ventana del navegador gana foco.
const handleWindowFocus = () => {
  if (conversacionActual.value) {
    servicioApi.marcarConversacionLeida(conversacionActual.value.id)
    const conv = almacen.conversaciones.find(c => c.id === conversacionActual.value.id)
    if (conv) {
      conv.noLeidos = 0
      conv.mencionesSinLeer = 0
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

const cerrarModalAñadir = () => {
  mostrarModalAñadir.value = false
  terminoUsuario.value = ''
}

// Cancelar la creacion de tarea.
const cancelarCrearTarea = () => {
  mostrarModalFecha.value = false
  textoPendienteTitulo.value = ''
  textoPendienteContenido.value = ''
  fechaVencInput.value = null
}

// Confirmar la creacion de una nueva tarea.
const confirmarCrearTarea = async () => {
  if (!textoPendienteTitulo.value || !usuarioActual.value) return
  creandoTarea.value = true
  try {
    const nueva = await servicioApi.crearTarea(
        usuarioActual.value.id,
        textoPendienteTitulo.value,
        textoPendienteContenido.value,
        fechaVencInput.value || null
    )

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
.componente-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: var(--bg);
  overflow: hidden;
  box-sizing: border-box;
  font-family: 'Inter', system-ui, sans-serif;
}

.barra-mensaje-fijado {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  min-height: 58px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(64, 109, 115, 0.12);
  cursor: pointer;
  z-index: 9;
  flex-shrink: 0;
  animation: slideDown 0.25s cubic-bezier(0.16, 1, 0.3, 1) both;
  transition: background-color 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.barra-mensaje-fijado:hover {
  background: rgba(240, 247, 248, 0.98);
}

.fijado-cuerpo {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.fijado-titulo {
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  line-height: 1.2;
}

.fijado-texto {
  font-size: 13px;
  color: #2f4a4f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  margin-top: 2px;
}

.fijado-icono {
  transform: rotate(45deg);
  transition: transform 0.2s ease;
}

.barra-mensaje-fijado:hover .fijado-icono {
  transform: rotate(15deg) scale(1.1);
}

.fijado-btn-cerrar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--teal);
  background-color: transparent;
  color: var(--teal);
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease, border-color 0.2s;
  box-sizing: border-box;
  margin-left: 8px;
}

.fijado-btn-cerrar:hover {
  background-color: var(--teal);
  color: #ffffff;
  transform: scale(1.15);
}

.fijado-btn-cerrar .v-icon,
.fijado-btn-cerrar i {
  color: inherit !important;
}

.fijado-btn-cerrar:hover .v-icon,
.fijado-btn-cerrar:hover i {
  color: #ffffff !important;
}

/* Modales */
.modal-titulo {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  padding: 14px 16px;
}

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
  background: var(--teal-pale) !important;
  border-radius: 12px !important;
  border: 1px solid var(--border-color) !important;
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
  color: var(--text-primary) !important;
  font-weight: 500 !important;
}

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
  background: var(--bg) !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 10px !important;
}

.modal-busqueda-anadir {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  background: var(--input-bg) !important;
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
  color: var(--text-primary) !important;
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
  background: var(--surface) !important;
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
  color: var(--text-primary);
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes highlightFlash {
  0% {
    background-color: transparent;
  }
  20% {
    background-color: rgba(64, 109, 115, 0.25);
    box-shadow: 0 0 12px rgba(64, 109, 115, 0.4);
  }
  100% {
    background-color: transparent;
  }
}

.mensaje-resaltado-temp {
  animation: highlightFlash 2s cubic-bezier(0.25, 1, 0.5, 1);
  border-radius: 12px;
}

@media (max-width: 480px) {
  .barra-mensaje-fijado { padding: 8px 12px; min-height: 56px; }
  .modal-titulo-anadir { padding: 10px 12px !important; }
}
</style>
