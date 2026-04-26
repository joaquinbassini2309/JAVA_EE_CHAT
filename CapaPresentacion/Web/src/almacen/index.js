// Almacén central con Pinia para gestionar el estado
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAlmacen = defineStore('principal', () => {
  // ===== ESTADO =====

  const usuario = ref(null) // Corregido: Nombre consistente
  const token = ref(null)
  const conversaciones = ref([])
  const conversacionActual = ref(null)
  const mensajes = ref([])
  const cargando = ref(false)
  const error = ref(null)

  // ===== GETTERS =====

  const estaAutenticado = computed(() => !!token.value && !!usuario.value)

  const conversacionesOrdenadas = computed(() => {
    return conversaciones.value.sort((a, b) => {
      return new Date(b.fechaUltimaActividad) - new Date(a.fechaUltimaActividad)
    })
  })

  const usuariosEnLinea = computed(() => {
    return conversaciones.value
      .flatMap(c => c.participantes || [])
      .filter(u => u.estado === 'ONLINE')
  })

  // ===== ACCIONES =====

  function establecerUsuario(nuevoUsuario) {
    usuario.value = nuevoUsuario
    // Corregido: Guardar en localStorage para persistencia
    if (nuevoUsuario) {
      localStorage.setItem('usuario', JSON.stringify(nuevoUsuario))
    } else {
      localStorage.removeItem('usuario')
    }
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
    mensajes.value.push(nuevoMensaje)
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
    usuario.value = null
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
      usuario.value = JSON.parse(usuarioGuardado)
    }
    if (conversacionGuardada) {
      conversacionActual.value = JSON.parse(conversacionGuardada)
    }
  }

  return {
    // Estado
    usuario, // Corregido
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
    establecerCargando,
    establecerError,
    limpiarError,
    cerrarSesion,
    cargarDelAlmacenamientoLocal
  }
})
