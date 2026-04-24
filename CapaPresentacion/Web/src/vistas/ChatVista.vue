<template>
  <div class="chat-vista">
    <div class="barra-superior">
      <h2>{{ usuarioActual?.username }}</h2>
      <button class="btn-cerrar-sesion" @click="cerrarSesion">
        Cerrar sesión
      </button>
    </div>

    <div class="contenedor-principal">
      <ListaConversaciones />
      <Chat v-if="conversacionActual" />
      <div v-else class="sin-conversacion">
        <p>Selecciona una conversación para empezar</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacen'
import { servicioApi } from '@/servicios/api'
import Chat from '@/componentes/Chat.vue'
import ListaConversaciones from '@/componentes/ListaConversaciones.vue'

const router = useRouter()
const almacen = useAlmacen()

const usuarioActual = computed(() => almacen.usuarioActual)
const conversacionActual = computed(() => almacen.conversacionActual)

onMounted(async () => {
  // Verificar autenticación
  if (!almacen.estaAutenticado) {
    router.push('/login')
    return
  }

  // Cargar conversaciones
  try {
    almacen.establecerCargando(true)
    const conversaciones = await servicioApi.obtenerConversaciones()
    almacen.establecerConversaciones(conversaciones)
  } catch (error) {
    console.error('Error al cargar conversaciones:', error)
    almacen.establecerError('Error al cargar conversaciones')
  } finally {
    almacen.establecerCargando(false)
  }
})

const cerrarSesion = () => {
  almacen.cerrarSesion()
  router.push('/login')
}
</script>

<style scoped>
.chat-vista {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f9fafb;
}

.barra-superior {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.barra-superior h2 {
  margin: 0;
  font-size: 20px;
}

.btn-cerrar-sesion {
  padding: 8px 16px;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-cerrar-sesion:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.contenedor-principal {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sin-conversacion {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  color: #9ca3af;
  font-size: 16px;
}
</style>
