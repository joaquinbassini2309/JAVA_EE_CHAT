// Servicios API para la aplicación Vue.js
import axios from 'axios'
import { useAlmacen } from '@/almacen' // Apuntará al almacén correcto

// Usar una ruta relativa para que funcione en cualquier entorno
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
        if (almacen.token) {
          config.headers.Authorization = `Bearer ${almacen.token}`
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
          almacen.cerrarSesion()
          window.location.href = '/chat-empresarial/login'
        }
        return Promise.reject(error)
      }
    )
  }

  // ... (todos los métodos de la API como estaban, son correctos)
  async iniciarSesion(email, password) {
    const { data } = await this.cliente.post('/usuarios/login', { email, password });
    return data;
  }
  async registrarse(username, email, password) {
    const { data } = await this.cliente.post('/usuarios/register', { username, email, password });
    return data;
  }
  async obtenerUsuarios() {
    const { data } = await this.cliente.get('/usuarios');
    return data;
  }
  async obtenerConversaciones() {
    const { data } = await this.cliente.get('/conversaciones');
    return data;
  }
  async crearConversacionPrivada(idOtroUsuario) {
    const { data } = await this.cliente.post('/conversaciones', { tipo: 'PRIVADA', participanteIds: [idOtroUsuario] });
    return data;
  }
  async obtenerMensajes(idConversacion) {
    const { data } = await this.cliente.get(`/conversaciones/${idConversacion}/mensajes`);
    return data;
  }
}

export const servicioApi = new ServicioAPI()
