// Almacén central con Pinia para gestionar el estado
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAlmacen = defineStore('principal', () => {
  // ===== ESTADO =====

  const usuario = ref(null)
  const token = ref(null)
  const conversaciones = ref([])
  const conversacionActual = ref(null)
  const mensajes = ref([])
  const cargando = ref(false)
  const error = ref(null)

  // ===== GETTERS =====

  const estaAutenticado = computed(() => !!token.value && !!usuario.value)

  const conversacionesOrdenadas = computed(() => {
    return [...conversaciones.value].sort((a, b) => {
      const fechaA = new Date(a.fechaUltimoMensaje || a.fechaCreacion)
      const fechaB = new Date(b.fechaUltimoMensaje || b.fechaCreacion)
      return fechaB - fechaA
    })
  })

  // ===== ACCIONES =====

  function establecerUsuario(nuevoUsuario) {
    usuario.value = nuevoUsuario
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
    // Lógica mejorada del compañero
    mensajes.value = [...mensajes.value, nuevoMensaje]
    
    const index = conversaciones.value.findIndex(c => c.id === nuevoMensaje.conversacionId)
    if (index !== -1) {
      const conv = conversaciones.value[index]
      conv.ultimoMensaje = nuevoMensaje.contenido
      conv.fechaUltimoMensaje = nuevoMensaje.fechaEnvio
      conversaciones.value = [...conversaciones.value]
    }
  }

  function agregarConversacion(conversacion) {
    conversaciones.value.unshift(conversacion)
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
    usuario,
    token,
    conversaciones,
    conversacionActual,
    mensajes,
    cargando,
    error,
    estaAutenticado,
    conversacionesOrdenadas,
    establecerUsuario,
    establecerToken,
    establecerConversaciones,
    establecerConversacionActual,
    establecerMensajes,
    agregarMensaje,
    agregarConversacion,
    cerrarSesion,
    cargarDelAlmacenamientoLocal
  }
})
