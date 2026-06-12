<template>
  <div class="contenedor-registro">
    <!-- Botón flotante para alternar tema -->
    <button class="btn-tema" @click="alternarTema" :title="almacen.temaOscuro ? 'Modo Claro' : 'Modo Oscuro'">
      <v-icon size="20" :color="almacen.temaOscuro ? '#e2edef' : '#406D73'">
        {{ almacen.temaOscuro ? 'mdi-weather-sunny' : 'mdi-weather-night' }}
      </v-icon>
    </button>

    <div class="tarjeta-registro">
      <!-- Panel Izquierdo: Formulario -->
      <div class="panel-formulario">
        <div class="avatar-placeholder">
          <div class="circulo-gris">
            <v-icon size="32" color="var(--text-muted)">mdi-account-plus</v-icon>
          </div>
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
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const router = useRouter()
const almacen = useAlmacen()

const username = ref('')
const email = ref('')
const password = ref('')
const cargando = ref(false)
const error = ref(null)
const exito = ref(false)

const alternarTema = () => {
  almacen.establecerTemaOscuro(!almacen.temaOscuro)
}

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

.tarjeta-registro {
  display: flex;
  flex-direction: row;
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  width: 90%;
  max-width: 800px;
  height: 550px;
  overflow: hidden;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

@media (max-width: 768px) {
  .tarjeta-registro {
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
  width: 64px;
  height: 64px;
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
  gap: 12px;
}

.grupo-campo {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 13px;
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

.btn-registro {
  margin-top: 12px;
  padding: 10px;
  background-color: var(--teal);
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color var(--t-fast);
}

.btn-registro:hover {
  background-color: var(--teal-hover);
}

.btn-registro:disabled {
  opacity: 0.7;
}

.alerta-error {
  color: var(--red);
  font-size: 13px;
  margin-bottom: 8px;
}

.alerta-exito {
  color: var(--green);
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: bold;
}

.pie-registro {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
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
    font-size: 36px;
  }

  .tarjeta-registro {
    height: auto;
  }

  .panel-bienvenida {
    display: none;
  }

  form {
    gap: 10px;
  }

  .btn-registro {
    padding: 12px;
    font-size: 14px;
  }

  .circulo-gris {
    width: 56px;
    height: 56px;
  }
}

@media (max-width: 480px) {
  .contenedor-registro {
    padding: 10px;
  }

  .panel-formulario {
    padding: 15px;
  }

  .circulo-gris {
    width: 50px;
    height: 50px;
  }

  .avatar-placeholder {
    margin-bottom: 16px;
  }

  .panel-bienvenida h1 {
    font-size: 28px;
  }

  .panel-bienvenida {
    padding: 25px 15px;
    min-height: 180px;
  }

  label {
    font-size: 12px;
    margin-bottom: 3px;
  }

  input {
    font-size: 14px;
    padding: 9px;
  }

  .btn-registro {
    margin-top: 10px;
    padding: 10px;
    font-size: 13px;
  }

  .pie-registro {
    margin-top: 12px;
    font-size: 12px;
  }

  form {
    gap: 8px;
  }

  .grupo-campo {
    gap: 2px;
  }
}
</style>
