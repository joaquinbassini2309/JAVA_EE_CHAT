<template>
  <div class="contenedor-registro">
    <div class="tarjeta-registro">
      <div class="encabezado">
        <h1>Crear Cuenta</h1>
        <p>Regístrate para empezar a chatear</p>
      </div>

      <div v-if="error" class="alerta-error">
        {{ error }}
      </div>

      <div v-if="exito" class="alerta-exito">
        ¡Registro exitoso! Redirigiendo al login...
      </div>

      <form @submit.prevent="registrarse">
        <div class="grupo-campo">
          <label for="username">Nombre de usuario</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="tu_usuario"
            required
          />
        </div>

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
          <label for="password">Contraseña</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button
          type="submit"
          class="btn-registro"
          :disabled="cargando || exito"
        >
          {{ cargando ? 'Registrando...' : 'Registrarse' }}
        </button>
      </form>

      <div class="pie-registro">
        <p>¿Ya tienes cuenta? <router-link to="/">Inicia sesión aquí</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { servicioApi } from '@/services/api'

const router = useRouter()

const username = ref('')
const email = ref('')
const password = ref('')
const cargando = ref(false)
const error = ref(null)
const exito = ref(false)

const registrarse = async () => {
  cargando.value = true
  error.value = null

  try {
    await servicioApi.registrarse(
      username.value,
      email.value,
      password.value
    )
    exito.value = true
    setTimeout(() => {
      router.push('/')
    }, 2000)
  } catch (err) {
    error.value =
      err.response?.data?.detalle ||
      err.response?.data?.mensaje ||
      'Error al registrarse'
  } finally {
    cargando.value = false
  }
}
</script>

<style scoped>
.contenedor-registro {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tarjeta-registro {
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

.alerta-error {
  padding: 12px;
  margin-bottom: 16px;
  background-color: #fee2e2;
  border: 1px solid #fca5a5;
  border-radius: 6px;
  color: #991b1b;
  font-size: 14px;
}

.alerta-exito {
  padding: 12px;
  margin-bottom: 16px;
  background-color: #d1fae5;
  border: 1px solid #6ee7b7;
  border-radius: 6px;
  color: #065f46;
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
}

.btn-registro {
  padding: 12px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}

.pie-registro {
  text-align: center;
  font-size: 14px;
}

a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}
</style>
