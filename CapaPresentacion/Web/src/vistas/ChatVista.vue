<template>
  <div class="chat-vista">
    <div class="contenedor-principal">
      <ListaConversaciones />
      <div class="area-derecha">
        <Chat v-if="conversacionActual" />
        <div v-else class="sin-conversacion">
          <div class="logo-placeholder"></div>
          <p>Selecciona una conversación para empezar</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import Chat from '@/componentes/Chat.vue'
import ListaConversaciones from '@/componentes/ListaConversaciones.vue'

const router = useRouter()
const almacen = useAlmacen()

const conversacionActual = computed(() => almacen.conversacionActual)

onMounted(async () => {
  if (!almacen.estaAutenticado) {
    router.push('/login')
    return
  }

  try {
    almacen.establecerCargando(true)
    const conversaciones = await servicioApi.obtenerConversaciones()
    almacen.establecerConversaciones(conversaciones)
  } catch (error) {
    console.error('Error al cargar conversaciones:', error)
  } finally {
    almacen.establecerCargando(false)
  }
})
</script>

<style scoped>
.chat-vista {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #e4f6f9;
  padding: 10px;
}

.contenedor-principal {
  display: flex;
  flex: 1;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(64, 109, 115, 0.25);
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
}

.area-derecha {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: #f7fcfd;
}

.sin-conversacion {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  background-color: #f7fcfd;
  color: #406D73;
}

.logo-placeholder {
  width: 100px;
  height: 100px;
  background-color: #B3EBF2;
  border-radius: 50%;
  margin-bottom: 20px;
  opacity: 0.5;
}
</style>
