<template>
  <div class="componente-chat">
    <div class="encabezado-chat">
      <h2>{{ conversacionActual?.nombre || 'Chat' }}</h2>
      <span class="badge-estado" :class="estadoUsuario">
        {{ estadoUsuario }}
      </span>
    </div>

    <div class="contenedor-mensajes" ref="contenedorMensajes">
      <div
        v-for="mensaje in mensajesFiltrados"
        :key="mensaje.id"
        class="mensaje"
        :class="{ 'propio': esPropio(mensaje) }"
      >
        <Mensaje :mensaje="mensaje" />
      </div>
    </div>

    <div class="entrada-mensaje">
      <textarea
        v-model="contenidoNuevo"
        @keydown.enter="enviarMensaje"
        placeholder="Escribe un mensaje..."
        rows="3"
      ></textarea>
      <button @click="enviarMensaje" class="btn-enviar">
        Enviar
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import Mensaje from './Mensaje.vue'

const almacen = useAlmacen()
const contenidoNuevo = ref('')
const estadoUsuario = ref('ONLINE')
const ws = ref(null)
const contenedorMensajes = ref(null)

const conversacionActual = computed(() => almacen.conversacionActual)
const usuarioActual = computed(() => almacen.usuarioActual)
const mensajes = computed(() => almacen.mensajes)

const mensajesFiltrados = computed(() => {
  return mensajes.value
})

const esPropio = (mensaje) => {
  return mensaje.emisorId === usuarioActual.value?.id
}

const scrollToBottom = async () => {
  await nextTick()
  if (contenedorMensajes.value) {
    contenedorMensajes.value.scrollTop = contenedorMensajes.value.scrollHeight
  }
}

const cargarMensajes = async () => {
  if (!conversacionActual.value) return
  try {
    const mensajesObtenidos = await servicioApi.obtenerMensajes(
      conversacionActual.value.id
    )
    almacen.establecerMensajes(mensajesObtenidos)
    scrollToBottom()
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
  }
}

const conectarWS = () => {
  if (ws.value) {
    ws.value.close()
  }

  if (conversacionActual.value && usuarioActual.value) {
    const token = almacen.token
    ws.value = servicioApi.conectarWebSocket(
      conversacionActual.value.id,
      usuarioActual.value.id,
      token
    )

    ws.value.onmessage = (event) => {
      console.log('WebSocket: mensaje recibido', event.data)
      const respuesta = JSON.parse(event.data)
      if (respuesta.tipo === 'mensaje') {
        almacen.agregarMensaje(respuesta.datos)
        scrollToBottom()
      } else if (respuesta.tipo === 'usuarioConectado' || respuesta.tipo === 'usuarioDesconectado') {
        console.log('Evento de estado:', respuesta)
      }
    }
  }
}

const enviarMensaje = async () => {
  if (!contenidoNuevo.value.trim() || !conversacionActual.value) return

  const texto = contenidoNuevo.value.trim()
  console.log('Enviando mensaje:', texto)
  contenidoNuevo.value = ''

  try {
    // Si el WS está abierto, podemos enviar por ahí para mayor velocidad
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      console.log('Enviando vía WebSocket')
      ws.value.send(JSON.stringify({
        contenido: texto,
        tipoMensaje: 'TEXTO'
      }))
    } else {
      console.log('Enviando vía REST (WS no disponible)')
      // Fallback a REST si el WS no está listo
      const nuevoMensaje = {
        conversacionId: conversacionActual.value.id,
        contenido: texto,
        tipoMensaje: 'TEXTO'
      }
      const m = await servicioApi.enviarMensaje(nuevoMensaje)
      almacen.agregarMensaje(m)
      scrollToBottom()
    }
  } catch (error) {
    console.error('Error al enviar mensaje:', error)
    contenidoNuevo.value = texto // Devolver texto si falla
  }
}

watch(conversacionActual, (nueva, vieja) => {
  if (nueva && nueva.id !== vieja?.id) {
    cargarMensajes()
    conectarWS()
  }
})

onMounted(() => {
  if (conversacionActual.value) {
    cargarMensajes()
    conectarWS()
  }
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.componente-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f7fcfd; /* surface */
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex: 1;
}

.encabezado-chat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #B3EBF2 0%, #406D73 100%);
  color: white;
}

.encabezado-chat h2 {
  margin: 0;
  font-size: 20px;
}

.badge-estado {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.badge-estado.ONLINE {
  background-color: #6A9E7D; /* success */
  color: white;
}

.badge-estado.OFFLINE {
  background-color: #B2C5C8; /* secondary */
  color: white;
}

.contenedor-mensajes {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: #e4f6f9; /* background */
}

.mensaje {
  display: flex;
  gap: 8px;
  animation: fadeIn 0.3s ease-in;
}

.mensaje.propio {
  justify-content: flex-end;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.entrada-mensaje {
  display: flex;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #B2C5C8;
  background: #f7fcfd;
}

textarea {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #B2C5C8;
  border-radius: 6px;
  font-family: inherit;
  font-size: 14px;
  resize: none;
}

textarea:focus {
  outline: none;
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64, 109, 115, 0.1);
}

.btn-enviar {
  padding: 8px 16px;
  background-color: #406D73;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-enviar:hover {
  background-color: #34585d;
}
</style>
