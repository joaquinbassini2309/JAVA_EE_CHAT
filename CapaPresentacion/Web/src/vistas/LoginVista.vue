<template>
  <div class="contenedor-login">
    <!-- Botón flotante para alternar tema -->
    <button class="btn-tema" @click="alternarTema" :title="almacen.temaOscuro ? 'Modo Claro' : 'Modo Oscuro'">
      <v-icon size="20" :color="almacen.temaOscuro ? '#e2edef' : '#406D73'">
        {{ almacen.temaOscuro ? 'mdi-weather-sunny' : 'mdi-weather-night' }}
      </v-icon>
    </button>

    <div class="tarjeta-login">
      <!-- Panel Izquierdo: Formulario -->
      <div class="panel-formulario">
        <div class="avatar-placeholder">
          <div class="circulo-gris">
            <v-icon size="40" color="var(--text-muted)">mdi-account</v-icon>
          </div>
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

        <div class="separador">
          <span>o</span>
        </div>

        <div class="contenedor-google">
          <div id="googleBtn"></div>
        </div>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const router = useRouter()
const almacen = useAlmacen()

const email = ref('')
const contraseña = ref('')
const cargando = ref(false)
const error = ref(null)

const alternarTema = () => {
  almacen.establecerTemaOscuro(!almacen.temaOscuro)
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
    error.value = err.response?.data?.detalle || 'Error al iniciar sesión'
  } finally {
    cargando.value = false
  }
}

const manejarRespuestaGoogle = async (response) => {
  cargando.value = true
  error.value = null
  try {
    const respuesta = await servicioApi.iniciarSesionConGoogle(response.credential)
    almacen.establecerToken(respuesta.token)
    almacen.establecerUsuario(respuesta.usuario)
    localStorage.setItem('usuario', JSON.stringify(respuesta.usuario))
    router.push('/chat')
  } catch (err) {
    error.value = err.response?.data?.detalle || 'Error al iniciar sesión con Google'
  } finally {
    cargando.value = false
  }
}

const inicializarGoogle = () => {
  if (window.google) {
    window.google.accounts.id.initialize({
      client_id: '194864765404-658569mqh83fmf7tkr8skp86tmf1224c.apps.googleusercontent.com',
      callback: manejarRespuestaGoogle
    })
    window.google.accounts.id.renderButton(
      document.getElementById('googleBtn'),
      { theme: almacen.temaOscuro ? 'filled_blue' : 'outline', size: 'large' }
    )
  }
}

onMounted(() => {
  if (!window.google) {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = inicializarGoogle
    document.head.appendChild(script)
  } else {
    inicializarGoogle()
  }
})
</script>

<style scoped>
.contenedor-login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--bg);
  position: relative;
  transition: background-color 0.3s ease;
}

.btn-tema {
  position: absolute;
  top: 20px;
  right: 20px;
  background: var(--surface);
  border: 1px solid var(--border-color);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: var(--t-fast);
  z-index: 10;
}

.btn-tema:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-md);
  background: var(--teal-pale);
}

.tarjeta-login {
  display: flex;
  flex-direction: row;
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  width: 90%;
  max-width: 800px;
  height: 500px;
  overflow: hidden;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

@media (max-width: 768px) {
  .tarjeta-login {
    flex-direction: column;
    max-width: 100%;
    height: auto;
    border-radius: 0;
  }
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
  background-color: var(--teal-pale);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
}

.panel-bienvenida {
  flex: 1;
  background: linear-gradient(135deg, var(--teal-light) 0%, var(--teal-dark) 100%);
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
  color: var(--text-secondary);
  margin-bottom: 4px;
}

input {
  padding: 8px;
  border: 1px solid var(--teal-border);
  border-radius: 4px;
  background: var(--surface-2);
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--t-fast);
}

input:focus {
  border-color: var(--teal);
}

.btn-iniciar {
  margin-top: 8px;
  padding: 10px;
  background-color: var(--teal);
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color var(--t-fast);
}

.btn-iniciar:hover {
  background-color: var(--teal-hover);
}

.alerta-error {
  color: var(--red);
  font-size: 13px;
  margin-bottom: 8px;
}

.pie-login {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.separador {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 16px 0;
  color: var(--text-muted);
}

.separador::before,
.separador::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--border-color);
}

.separador:not(:empty)::before {
  margin-right: .5em;
}

.separador:not(:empty)::after {
  margin-left: .5em;
}

.contenedor-google {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}

a {
  color: var(--teal);
  text-decoration: none;
  font-weight: bold;
}

a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .panel-formulario {
    padding: 20px;
  }

  .panel-bienvenida h1 {
    font-size: 32px;
  }

  .tarjeta-login {
    height: auto;
  }

  .panel-bienvenida {
    display: none;
  }

  form {
    gap: 12px;
  }

  .btn-iniciar {
    padding: 12px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .contenedor-login {
    padding: 10px;
  }

  .panel-formulario {
    padding: 15px;
  }

  .circulo-gris {
    width: 60px;
    height: 60px;
  }

  .panel-bienvenida h1 {
    font-size: 24px;
  }

  label {
    font-size: 12px;
  }

  input {
    font-size: 14px;
    padding: 10px;
  }

  .btn-iniciar {
    font-size: 12px;
    padding: 10px;
  }

  .pie-login {
    font-size: 12px;
  }
}
</style>
