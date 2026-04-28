<template>
  <div class="contenedor-registro">
    <div class="tarjeta-registro">
      <!-- Panel Izquierdo: Formulario -->
      <div class="panel-formulario">
        <div class="avatar-placeholder">
          <div class="circulo-gris"></div>
        </div>

        <form @submit.prevent="registrarse">
          <div v-if="error" class="alerta-error">
            {{ error }}
          </div>
          <div v-if="exito" class="alerta-exito">
            ¡Registro exitoso!
          </div>

          <div class="grupo-campo">
            <label for="username">Usuario</label>
            <input
              id="username"
              v-model="username"
              type="text"
              required
            />
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
            class="btn-registro"
            :disabled="cargando || exito"
          >
            {{ cargando ? 'Cargando...' : 'Registrarse' }}
          </button>
        </form>

        <div class="pie-registro">
          <p>¿Ya tienes cuenta? <router-link to="/login">Inicia sesión</router-link></p>
        </div>
      </div>

      <!-- Panel Derecho: Bienvenida -->
      <div class="panel-bienvenida">
        <h1>Únete</h1>
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
      router.push('/login')
    }, 1500)
  } catch (err) {
    error.value = err.response?.data?.detalle || 'Error al registrarse'
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
  background-color: #e4f6f9;
}

.tarjeta-registro {
  display: flex;
  background: #f7fcfd;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 800px;
  height: 550px;
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
  width: 64px;
  height: 64px;
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
  gap: 12px;
}

.grupo-campo {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 13px;
  color: #2f4a4f;
  margin-bottom: 4px;
}

input {
  padding: 8px;
  border: 1px solid #B2C5C8;
  border-radius: 4px;
  background: white;
}

.btn-registro {
  margin-top: 12px;
  padding: 10px;
  background-color: #406D73;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.btn-registro:disabled {
  opacity: 0.7;
}

.alerta-error {
  color: #C85A5A;
  font-size: 13px;
  margin-bottom: 8px;
}

.alerta-exito {
  color: #6A9E7D;
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: bold;
}

.pie-registro {
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
