// Servicios API para la aplicación Vue.js
import axios from 'axios'
import { useAlmacen } from '@/almacen'

// Al compilar, la app Vue y la API Java estarán en el mismo dominio.
// Usamos una ruta relativa para que funcione en cualquier entorno (desarrollo, producción).
const API_BASE_URL = '/chat-empresarial/api/v1'

class ServicioAPI {
  constructor() {
    this.cliente = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000
    })

    // Interceptor para agregar token JWT
    this.cliente.interceptors.request.use(
      (config) => {
        const almacen = useAlmacen()
        const token = almacen.token
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Interceptor para manejar errores de autenticación
    this.cliente.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          const almacen = useAlmacen()
          almacen.cerrarSesion() // Usar la acción del almacén para limpiar
          window.location.href = '/chat-empresarial/login' // Redirigir a la página de login
        }
        return Promise.reject(error)
      }
    )
  }

  // ========== AUTENTICACIÓN ==========

  async iniciarSesion(email, password) {
    // El login no debería usar el interceptor de token, pero para este caso es inofensivo
    const { data } = await this.cliente.post('/usuarios/login', {
      email,
      password
    })
    return data
  }

  async registrarUsuario(datosRegistro) {
    const { data } = await this.cliente.post('/usuarios/register', datosRegistro)
    return data
  }

  async actualizarPerfil(fotoUrl, estado) {
    const { data } = await this.cliente.put('/usuarios/perfil', {
      fotoUrl,
      estado
    })
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

  // ========== MENSAJES ==========

  async obtenerMensajes(idConversacion, limite = 50) {
    const { data } = await this.cliente.get(
      `/conversaciones/${idConversacion}/mensajes?limite=${limite}`
    )
    return data
  }

  // ========== USUARIOS ==========
  async obtenerUsuarios() {
    const { data } = await this.cliente.get('/usuarios');
    return data;
  }
}

export const servicioApi = new ServicioAPI()
