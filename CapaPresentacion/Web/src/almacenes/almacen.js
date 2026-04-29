// Almacén central con Pinia para gestionar el estado
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAlmacen = defineStore('principal', () => {
  // ===== ESTADO =====

  const usuarioActual = ref(null)
  const token = ref(null)
  const conversaciones = ref([])
  const conversacionActual = ref(null)
  const mensajes = ref([])
  const cargando = ref(false)
  const error = ref(null)

  // ===== GETTERS =====

  const estaAutenticado = computed(() => !!token.value && !!usuarioActual.value)

  const conversacionesOrdenadas = computed(() => {
    return [...conversaciones.value].sort((a, b) => {
      const fechaA = new Date(a.fechaUltimoMensaje || a.fechaCreacion)
      const fechaB = new Date(b.fechaUltimoMensaje || b.fechaCreacion)
      return fechaB - fechaA
    })
  })

  const usuariosEnLinea = computed(() => {
    return conversaciones.value
      .flatMap(c => c.participantes || [])
      .filter(u => u.estado === 'ONLINE')
  })

  // ===== ACCIONES =====

  function establecerUsuario(usuario) {
    usuarioActual.value = usuario
  }

  function establecerToken(nuevoToken) {
    token.value = nuevoToken
    if (nuevoToken) {
      localStorage.setItem('token', nuevoToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function establecerConversaciones(nuevasConversaciones) {
    conversaciones.value = nuevasConversaciones
  }

  function establecerConversacionActual(conversacion) {
    conversacionActual.value = conversacion
    if (conversacion) {
      localStorage.setItem('conversacionActual', JSON.stringify(conversacion))
    }
  }

  function establecerMensajes(nuevosMensajes) {
    mensajes.value = nuevosMensajes
  }

  function agregarMensaje(nuevoMensaje) {
    mensajes.value = [...mensajes.value, nuevoMensaje]
    
    // Actualizar el último mensaje en la lista de conversaciones
    const index = conversaciones.value.findIndex(c => c.id === nuevoMensaje.conversacionId)
    if (index !== -1) {
      const conv = conversaciones.value[index]
      conv.ultimoMensaje = nuevoMensaje.contenido
      conv.fechaUltimoMensaje = nuevoMensaje.fechaEnvio
      // Forzar actualización de la lista
      conversaciones.value = [...conversaciones.value]
    }
  }

  function agregarConversacion(conversacion) {
    conversaciones.value.unshift(conversacion)
  }

  function actualizarEstadoUsuario(idUsuario, nuevoEstado) {
    conversaciones.value.forEach(conv => {
      if (conv.participantes) {
        const participante = conv.participantes.find(p => p.id === idUsuario)
        if (participante) {
          participante.estado = nuevoEstado
        }
      }
    })
  }

  function actualizarNombreConversacion(conversacionId, nombre) {
    const conv = conversaciones.value.find(c => c.id === conversacionId)
    if (conv) {
      conv.nombre = nombre
    }
    if (conversacionActual.value?.id === conversacionId) {
      conversacionActual.value.nombre = nombre
    }
  }

  function actualizarRolParticipante(conversacionId, participanteId, nuevoRol) {
    const conv = conversaciones.value.find(c => c.id === conversacionId)
    if (conv?.participantes) {
      const participante = conv.participantes.find(p => p.id === participanteId)
      if (participante) {
        participante.rol = nuevoRol
      }
    }
    if (conversacionActual.value?.id === conversacionId) {
      const participante = conversacionActual.value.participantes.find(p => p.id === participanteId)
      if (participante) {
        participante.rol = nuevoRol
      }
    }
  }

  function eliminarParticipante(conversacionId, participanteId) {
    const conv = conversaciones.value.find(c => c.id === conversacionId)
    if (conv?.participantes) {
      conv.participantes = conv.participantes.filter(p => p.id !== participanteId)
    }
    if (conversacionActual.value?.id === conversacionId) {
      conversacionActual.value.participantes = conversacionActual.value.participantes.filter(p => p.id !== participanteId)
    }
  }

  function establecerCargando(valor) {
    cargando.value = valor
  }

  function establecerError(mensaje) {
    error.value = mensaje
  }

  function limpiarError() {
    error.value = null
  }

  function cerrarSesion() {
    usuarioActual.value = null
    token.value = null
    conversaciones.value = []
    conversacionActual.value = null
    mensajes.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
    localStorage.removeItem('conversacionActual')
  }

  function cargarDelAlmacenamientoLocal() {
    const tokenGuardado = localStorage.getItem('token')
    const usuarioGuardado = localStorage.getItem('usuario')
    const conversacionGuardada = localStorage.getItem('conversacionActual')

    if (tokenGuardado) {
      token.value = tokenGuardado
    }
    if (usuarioGuardado) {
      usuarioActual.value = JSON.parse(usuarioGuardado)
    }
    if (conversacionGuardada) {
      conversacionActual.value = JSON.parse(conversacionGuardada)
    }
  }

  return {
    // Estado
    usuarioActual,
    token,
    conversaciones,
    conversacionActual,
    mensajes,
    cargando,
    error,

    // Getters
    estaAutenticado,
    conversacionesOrdenadas,
    usuariosEnLinea,

    // Acciones
    establecerUsuario,
    establecerToken,
    establecerConversaciones,
    establecerConversacionActual,
    establecerMensajes,
    agregarMensaje,
    agregarConversacion,
    actualizarEstadoUsuario,
    actualizarNombreConversacion,
    actualizarRolParticipante,
    eliminarParticipante,
    establecerCargando,
    establecerError,
    limpiarError,
    cerrarSesion,
    cargarDelAlmacenamientoLocal
  }
})
