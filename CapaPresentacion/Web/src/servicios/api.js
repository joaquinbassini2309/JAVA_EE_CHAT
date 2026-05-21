// Servicios API para la aplicación Vue.js
import axios from 'axios'

const contextPath = window.location.pathname.includes('/chat-empresarial') 
  ? '/chat-empresarial' 
  : ''

const API_BASE_URL = `${contextPath}/api/v1`

class ServicioAPI {
  constructor() {
    this.cliente = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000
    })

    // Interceptor para agregar token JWT
    this.cliente.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Interceptor para manejar errores
    this.cliente.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Si el 401 viene del intento de login, solo rechazar la promesa para mostrar el error
          if (error.config && error.config.url && error.config.url.includes('/usuarios/login')) {
            return Promise.reject(error)
          }
          // Token expirado o inválido para otras peticiones
          localStorage.removeItem('token')
          localStorage.removeItem('usuario')
          window.location.href = `${contextPath}/login`
        }
        return Promise.reject(error)
      }
    )
  }

  // ========== AUTENTICACIÓN ==========

  async iniciarSesion(email, contraseña) {
    const { data } = await this.cliente.post('/usuarios/login', {
      email: email,
      password: contraseña
    })
    return data
  }

  async iniciarSesionConGoogle(credential) {
    const { data } = await this.cliente.post('/usuarios/login-google', {
      credential: credential
    })
    return data
  }

  async registrarse(nombreUsuario, email, contraseña) {
    const { data } = await this.cliente.post('/usuarios/register', {
      username: nombreUsuario,
      email: email,
      password: contraseña
    })
    return data
  }

  async actualizarPerfil(payload) {
    const { data } = await this.cliente.put('/usuarios/perfil', payload)
    return data
  }

  async obtenerUsuarios() {
    const { data } = await this.cliente.get('/usuarios')
    return data
  }

  // ========== CONVERSACIONES ==========

  async obtenerConversaciones() {
    const { data } = await this.cliente.get('/conversaciones')
    return data
  }

  async crearConversacionPrivada(idOtroUsuario) {
    const { data } = await this.cliente.post('/conversaciones', {
      tipo: 'PRIVADA',
      participanteIds: [idOtroUsuario]
    })
    return data
  }

  async crearGrupo(nombre, participantesIds) {
    const { data } = await this.cliente.post('/conversaciones', {
      tipo: 'GRUPO',
      nombre: nombre,
      participanteIds: participantesIds
    })
    return data
  }

  async obtenerCanalesAvisos() {
    const { data } = await this.cliente.get('/conversaciones/canales')
    return data
  }

  async unirseACanalAvisos(canalId) {
    const { data } = await this.cliente.post(`/conversaciones/canales/${canalId}/unirse`)
    return data
  }

  async obtenerRolEnConversacion(idConversacion) {
    const { data } = await this.cliente.get(`/conversaciones/${idConversacion}/rol`)
    return data
  }

  async crearCanalAvisos(nombre) {
    const { data } = await this.cliente.post('/conversaciones', {
      tipo: 'AVISO',
      nombre: nombre
    })
    return data
  }

  async obtenerConversacion(idConversacion) {
    const { data } = await this.cliente.get(`/conversaciones/${idConversacion}`)
    return data
  }

  async actualizarConversacion(idConversacion, nombre, fotoUrl, imagenBanner) {
    const { data } = await this.cliente.put(`/conversaciones/${idConversacion}`, { nombre, fotoUrl, imagenBanner })
    return data
  }

  async añadirParticipante(idConversacion, idUsuario) {
    const { data } = await this.cliente.post(`/conversaciones/${idConversacion}/participantes`, {
      usuarioId: idUsuario
    })
    return data
  }

  async eliminarParticipante(idConversacion, idParticipante) {
    const { data } = await this.cliente.delete(`/conversaciones/${idConversacion}/participantes/${idParticipante}`)
    return data
  }

  async actualizarRolParticipante(idConversacion, idParticipante, nuevoRol) {
    const { data } = await this.cliente.put(`/conversaciones/${idConversacion}/participantes/${idParticipante}/rol`, {
      rol: nuevoRol
    })
    return data
  }

  // ========== MENSAJES ==========

  async enviarMensaje(nuevoMensaje) {
    const { data } = await this.cliente.post('/mensajes', nuevoMensaje)
    return data
  }

  // Nuevo: subir archivo en Base64
  async uploadFile(filename, contentBase64) {
    const { data } = await this.cliente.post('/archivos/upload', {
      filename: filename,
      contentBase64: contentBase64
    })

    // Asegurar que la URL devuelta sea absoluta para que el frontend pueda mostrar/descargar correctamente
    try {
      // Usar el origin (protocolo + host + puerto) para no duplicar el contexto de aplicación
      const origin = window.location.origin
      if (data && data.url && data.url.startsWith('/')) {
        data.url = origin + data.url
      }
    } catch (e) {
      // si algo falla, seguimos devolviendo lo que venga del servidor
      console.warn('No se pudo normalizar URL de archivo:', e.message)
    }

    return data
  }

  async obtenerMensajes(idConversacion, limite = 6, offset = 0) {
    const { data } = await this.cliente.get(
      `/mensajes/conversacion/${idConversacion}?limite=${limite}&offset=${offset}`
    )
    return data
  }

  async marcarMensajeLeido(idMensaje) {
    const { data } = await this.cliente.post(
      `/mensajes/${idMensaje}/leido`
    )
    return data
  }

  async marcarConversacionLeida(idConversacion) {
    const { data } = await this.cliente.post(
      `/mensajes/conversacion/${idConversacion}/leidos`
    )
    return data
  }

  async eliminarMensaje(idMensaje) {
    const { data } = await this.cliente.delete(`/mensajes/${idMensaje}`)
    return data
  }

  async obtenerInfoMensaje(idMensaje) {
    const { data } = await this.cliente.get(`/mensajes/${idMensaje}/info`)
    return data
  }

  // ========== TAREAS (local, sin tocar backend) ==========
  // Las tareas se guardan en localStorage por usuario bajo la clave `tareas_<userId>`.
  _keyTareas(userId) {
      return `tareas_${userId}`
  }

  async obtenerTareas(userId) {
      if (!userId) return []
      try {
          const raw = localStorage.getItem(this._keyTareas(userId))
          const arr = raw ? JSON.parse(raw) : []
          // Asegurar orden por fecha (más reciente al final)
          arr.sort((a, b) => new Date(a.fechaEnvio) - new Date(b.fechaEnvio))
          return arr
      } catch (e) {
          console.error('Error leyendo tareas locales', e)
          return []
      }
  }

  async crearTarea(userId, titulo, contenido, fechaVencimiento) {
      if (!userId) throw new Error('userId required')
      const key = this._keyTareas(userId)
      const raw = localStorage.getItem(key)
      const arr = raw ? JSON.parse(raw) : []
      const nueva = {
          id: Date.now(),
          conversacionId: `tareas_${userId}`,
          emisorId: userId,
          titulo: titulo || '',
          contenido: contenido || '',
          fechaEnvio: new Date().toISOString(),
          fechaVencimiento: fechaVencimiento || null,
          completada: false
      }
      arr.push(nueva)
      localStorage.setItem(key, JSON.stringify(arr))
      return nueva
  }

  async actualizarTarea(userId, tarea) {
      if (!userId || !tarea || !tarea.id) throw new Error('invalid params')
      const key = this._keyTareas(userId)
      const raw = localStorage.getItem(key)
      const arr = raw ? JSON.parse(raw) : []
      const idx = arr.findIndex(t => t.id === tarea.id)
      if (idx === -1) throw new Error('Tarea no encontrada')
      arr[idx] = { ...arr[idx], ...tarea }
      localStorage.setItem(key, JSON.stringify(arr))
      return arr[idx]
  }

  async eliminarTarea(userId, tareaId) {
      if (!userId || !tareaId) throw new Error('invalid params')
      const key = this._keyTareas(userId)
      const raw = localStorage.getItem(key)
      const arr = raw ? JSON.parse(raw) : []
      const nueva = arr.filter(t => t.id !== tareaId)
      localStorage.setItem(key, JSON.stringify(nueva))
      return true
  }

  // ========== WEBSOCKET ==========

  conectarWebSocket(idConversacion, idUsuario, token) {
    // Aceptar token como string o ref (Pinia)
    const rawToken = (token && token.value) ? token.value : token;
    const encoded = rawToken ? encodeURIComponent(rawToken) : '';
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${window.location.host}${contextPath}/ws/conversacion/${idConversacion}/usuario/${idUsuario}` + (encoded ? `?token=${encoded}` : '')
    console.log('Intentando conectar WebSocket a:', url)

    const ws = new WebSocket(url)

    // Configurar headers manualmente no funciona en WebSocket,
    // así que pasamos el token en la URL (alternativa: usar socket.io)
    ws.onopen = () => {
      console.log('WebSocket conectado')
      // Enviar token en primer mensaje si es necesario
    }

    ws.onerror = (error) => {
      console.error('Error WebSocket:', error)
    }

    ws.onclose = (ev) => {
      console.log('WebSocket cerrado', ev.code, ev.reason)
    }

    return ws
  }
}

export const servicioApi = new ServicioAPI()
