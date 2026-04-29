// Servicios API para la aplicación Vue.js
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/chat-empresarial/api/v1'

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
          // Token expirado o inválido
          localStorage.removeItem('token')
          localStorage.removeItem('usuario')
          window.location.href = '/login'
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

  async registrarse(nombreUsuario, email, contraseña) {
    const { data } = await this.cliente.post('/usuarios/register', {
      username: nombreUsuario,
      email: email,
      password: contraseña
    })
    return data
  }

  async actualizarPerfil(fotoUrl, estado) {
    const { data } = await this.cliente.put('/usuarios/perfil', {
      fotoUrl: fotoUrl,
      estado: estado
    })
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

  async obtenerConversacion(idConversacion) {
    const { data } = await this.cliente.get(`/conversaciones/${idConversacion}`)
    return data
  }

  async actualizarConversacion(idConversacion, nombre) {
    const { data } = await this.cliente.put(`/conversaciones/${idConversacion}`, { nombre })
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

  async obtenerMensajes(idConversacion, limite = 50) {
    const { data } = await this.cliente.get(
      `/mensajes/conversacion/${idConversacion}?limite=${limite}`
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

  // ========== WEBSOCKET ==========

  conectarWebSocket(idConversacion, idUsuario, token) {
    const url = `ws://localhost:8080/chat-empresarial/ws/conversacion/${idConversacion}/usuario/${idUsuario}?token=${token}`
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

    return ws
  }
}

export const servicioApi = new ServicioAPI()
