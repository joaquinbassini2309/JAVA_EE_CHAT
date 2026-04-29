<template>
  <div class="contenedor-login">
    <div class="tarjeta-login">
      <!-- Panel Izquierdo: Formulario -->
      <div class="panel-formulario">
        <div class="avatar-placeholder">
          <div class="circulo-gris"></div>
        </div>

        <form @submit.prevent="iniciarSesion">
          <div v-if="error" class="alerta-error">
            {{ error }}
          </div>

          <div class="grupo-campo">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              required
            />
          </div>

          <div class="grupo-campo">
            <label for="contraseña">Contraseña</label>
            <input
              id="contraseña"
              v-model="contraseña"
              type="password"
              required
            />
          </div>

          <button
            type="submit"
            class="btn-iniciar"
            :disabled="cargando"
          >
            {{ cargando ? 'Cargando...' : 'Iniciar Sesión' }}
          </button>
        </form>

        <div class="pie-login">
          <p>¿No tienes una cuenta? <router-link to="/registro">Regístrate</router-link></p>
        </div>
      </div>

      <!-- Panel Derecho: Bienvenida -->
      <div class="panel-bienvenida">
        <h1>Bienvenido</h1>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const router = useRouter()
const almacen = useAlmacen()

const email = ref('')
const contraseña = ref('')
const cargando = ref(false)
const error = ref(null)

const obtenerMensajeError = (err, fallback) => {
  const data = err?.response?.data
  return data?.detalle || data?.mensaje || data?.message || data?.errores?.[0] || fallback
}

const iniciarSesion = async () => {
  if (!email.value || !contraseña.value) {
    error.value = 'Por favor completa todos los campos'
    return
  }

  cargando.value = true
  error.value = null

  try {
    const respuesta = await servicioApi.iniciarSesion(
      email.value,
      contraseña.value
    )

    almacen.establecerToken(respuesta.token)
    almacen.establecerUsuario(respuesta.usuario)
    localStorage.setItem('usuario', JSON.stringify(respuesta.usuario))

    router.push('/chat')
  } catch (err) {
    console.error('Error al iniciar sesión:', err?.response?.status, err?.response?.data || err)
    error.value = obtenerMensajeError(err, 'Error al iniciar sesión')
  } finally {
    cargando.value = false
  }
}
</script>

<style scoped>
.contenedor-login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #e4f6f9; /* background from temaProyecto */
}

.tarjeta-login {
  display: flex;
  background: #f7fcfd; /* surface from temaProyecto */
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 800px;
  height: 500px;
  overflow: hidden;
}

.panel-formulario {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.avatar-placeholder {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.circulo-gris {
  width: 80px;
  height: 80px;
  background-color: #bdc3c7;
  border-radius: 50%;
}

.panel-bienvenida {
  flex: 1;
  background: linear-gradient(135deg, #B3EBF2 0%, #406D73 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.panel-bienvenida h1 {
  font-size: 48px;
  font-weight: bold;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.grupo-campo {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 14px;
  color: #2f4a4f;
  margin-bottom: 4px;
}

input {
  padding: 8px;
  border: 1px solid #B2C5C8;
  border-radius: 4px;
  background: white;
}

.btn-iniciar {
  margin-top: 8px;
  padding: 10px;
  background-color: #406D73;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.alerta-error {
  color: #C85A5A;
  font-size: 13px;
  margin-bottom: 8px;
}

.pie-login {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #2f4a4f;
}

a {
  color: #406D73;
  text-decoration: none;
  font-weight: bold;
}
</style>
