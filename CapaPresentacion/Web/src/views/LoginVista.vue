<template>
  <div class="contenedor-login">
    <div class="tarjeta-login">
      <div class="encabezado">
        <h1>Chat Empresarial</h1>
        <p>Inicia sesión para continuar</p>
      </div>

      <div v-if="error" class="alerta-error">
        {{ error }}
      </div>

      <form @submit.prevent="iniciarSesion">
        <div class="grupo-campo">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="usuario@ejemplo.com"
            required
          />
        </div>

        <div class="grupo-campo">
          <label for="contraseña">Contraseña</label>
          <input
            id="contraseña"
            v-model="contraseña"
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button
          type="submit"
          class="btn-iniciar"
          :disabled="cargando"
        >
          {{ cargando ? 'Iniciando sesión...' : 'Iniciar sesión' }}
        </button>
      </form>

      <div class="pie-login">
        <p>¿No tienes cuenta? <router-link to="/registro">Regístrate aquí</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/stores'
import { servicioApi } from '@/services/api'

const router = useRouter()
const almacen = useAlmacen()

const email = ref('')
const contraseña = ref('')
const cargando = ref(false)
const error = ref(null)

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

    // Guardar token y usuario
    almacen.establecerToken(respuesta.token)
    almacen.establecerUsuario(respuesta.usuario)
    localStorage.setItem('usuario', JSON.stringify(respuesta.usuario))

    // Redirigir al chat
    router.push('/chat')
  } catch (err) {
    error.value =
      err.response?.data?.detalle ||
      err.response?.data?.mensaje ||
      'Error al iniciar sesión'
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tarjeta-login {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 400px;
}

.encabezado {
  text-align: center;
  margin-bottom: 30px;
}

.encabezado h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #111827;
}

.encabezado p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.alerta-error {
  padding: 12px;
  margin-bottom: 16px;
  background-color: #fee2e2;
  border: 1px solid #fca5a5;
  border-radius: 6px;
  color: #991b1b;
  font-size: 14px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.grupo-campo {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-iniciar {
  padding: 12px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-iniciar:hover:not(:disabled) {
  background-color: #5568d3;
}

.btn-iniciar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pie-login {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

a:hover {
  text-decoration: underline;
}
</style>
