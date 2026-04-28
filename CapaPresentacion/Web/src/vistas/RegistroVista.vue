<template>
  <div class="login-container">
    <div class="login-panel">
      <div class="login-form">
        <div class="avatar-container">
          <div class="avatar"></div>
        </div>

        <div v-if="error" class="alerta-error">
          {{ error }}
        </div>
         <div v-if="success" class="alerta-success">
          {{ success }}
        </div>

        <form @submit.prevent="registrarUsuario">
          <div class="form-group">
            <label for="username">Nombre de Usuario</label>
            <input
              id="username"
              v-model="username"
              type="text"
              required
            />
          </div>
          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              required
            />
          </div>
          <div class="form-group">
            <label for="password">Contraseña</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
            />
          </div>
          <button
            type="submit"
            class="login-button"
            :disabled="cargando"
          >
            {{ cargando ? 'Registrando...' : 'Registrarse' }}
          </button>
        </form>
        <div class="switch-form">
          <span>¿Ya tienes una cuenta? <router-link to="/login">Inicia sesión</router-link></span>
        </div>
      </div>
      <div class="welcome-panel">
        <h1>Únete a Nosotros</h1>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { servicioApi } from '@/services/api' // Corregido

const router = useRouter()

const username = ref('')
const email = ref('')
const password = ref('')
const cargando = ref(false)
const error = ref(null)
const success = ref(null)

const registrarUsuario = async () => {
  if (!username.value || !email.value || !password.value) {
    error.value = 'Por favor completa todos los campos'
    return
  }

  cargando.value = true
  error.value = null
  success.value = null

  try {
    await servicioApi.registrarse( // Corregido
      username.value,
      email.value,
      password.value,
    )

    success.value = '¡Registro exitoso! Redirigiendo al login...'
    setTimeout(() => {
      router.push('/login')
    }, 2000)

  } catch (err) {
    error.value =
      err.response?.data?.detalle ||
      err.response?.data?.mensaje ||
      'Error en el registro. Inténtalo de nuevo.'
  } finally {
    cargando.value = false
  }
}
</script>

<style scoped>
/* Tus estilos se mantienen intactos */
body { margin: 0; font-family: sans-serif; background-color: #e4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; }
.login-container { display: flex; justify-content: center; align-items: center; width: 100%; height: 100vh; background-color: #e4f6f9; }
.login-panel { display: flex; width: 800px; height: 500px; background-color: #f7fcfd; border-radius: 15px; box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1); overflow: hidden; }
.login-form { width: 40%; padding: 40px; display: flex; flex-direction: column; justify-content: center; box-sizing: border-box; }
.avatar-container { display: flex; justify-content: center; margin-bottom: 20px; }
.avatar { width: 100px; height: 100px; background-color: #B2C5C8; border-radius: 50%; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 5px; color: #2f4a4f; }
.form-group input { width: 100%; padding: 10px; border: 1px solid #B2C5C8; border-radius: 5px; box-sizing: border-box; }
.login-button { width: 100%; padding: 10px; background-color: #406D73; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; transition: background-color 0.3s; }
.login-button:hover:not(:disabled) { background-color: #5A8A94; }
.login-button:disabled { opacity: 0.6; cursor: not-allowed; }
.welcome-panel { width: 60%; display: flex; justify-content: center; align-items: center; background: linear-gradient(to right, #B3EBF2, #406D73); color: white; }
.welcome-panel h1 { font-size: 48px; text-align: center; }
.switch-form { text-align: center; margin-top: 20px; font-size: 14px; }
.switch-form a { color: #406D73; text-decoration: none; font-weight: bold; }
.switch-form a:hover { text-decoration: underline; }
.alerta-error { padding: 12px; margin-bottom: 16px; background-color: #fee2e2; border: 1px solid #fca5a5; border-radius: 6px; color: #991b1b; font-size: 14px; text-align: center; }
.alerta-success { padding: 12px; margin-bottom: 16px; background-color: #dcfce7; border: 1px solid #86efac; border-radius: 6px; color: #166534; font-size: 14px; text-align: center; }
</style>
