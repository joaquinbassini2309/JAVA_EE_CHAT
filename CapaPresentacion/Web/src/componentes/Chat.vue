<template>
  <div class="componente-chat">
    <template v-if="!mostrandoInfo">
      <div class="encabezado-chat">
        <div class="info-encabezado clickable" @click="mostrandoInfo = true">
          <h2>{{ conversacionActual?.nombre || 'Chat' }}</h2>
          <button v-if="esGrupo" class="btn-añadir-miembro" @click.stop="abrirModalAñadir" title="Añadir miembro">
            +
          </button>
        </div>
        <span class="badge-estado" :class="estadoUsuario">
          {{ estadoUsuario }}
        </span>
      </div>

      <!-- Modal Añadir Miembro (se mantiene igual) -->
      <div v-if="mostrarModalAñadir" class="modal-overlay" @click.self="cerrarModalAñadir">
        <div class="modal-contenido">
          <div class="modal-encabezado">
            <h3>Añadir al Grupo</h3>
            <button class="btn-cerrar" @click="cerrarModalAñadir">&times;</button>
          </div>
          <div class="modal-busqueda">
            <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
          </div>
          <div class="modal-listado">
            <div v-for="usuario in usuariosFiltrados" :key="usuario.id" class="item-usuario" @click="añadirMiembro(usuario.id)">
              <div class="avatar-mini">{{ usuario.username.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario">
                <span class="nombre">{{ usuario.username }}</span>
              </div>
              <div class="accion-añadir">+</div>
            </div>
          </div>
        </div>
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
    </template>
    
    <InfoGrupo v-else :conversacion="conversacionActual" @volver="mostrandoInfo = false" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import Mensaje from './Mensaje.vue'
import InfoGrupo from './InfoGrupo.vue'

const almacen = useAlmacen()
const contenidoNuevo = ref('')
const estadoUsuario = ref('ONLINE')
const ws = ref(null)
const contenedorMensajes = ref(null)
const mostrarModalAñadir = ref(false)
const mostrandoInfo = ref(false)
const terminoUsuario = ref('')
const usuariosDisponibles = ref([])

const conversacionActual = computed(() => almacen.conversacionActual)
const usuarioActual = computed(() => almacen.usuarioActual)
const mensajes = computed(() => almacen.mensajes)
const esGrupo = computed(() => conversacionActual.value?.tipo === 'GRUPO')

const usuariosFiltrados = computed(() => {
  const participantes = conversacionActual.value?.participanteIds || []
  let lista = usuariosDisponibles.value.filter(u => !participantes.includes(u.id))
  if (terminoUsuario.value) {
    const t = terminoUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

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
    // Marcar como leídos
    await servicioApi.marcarConversacionLeida(conversacionActual.value.id)
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
  }
}

const abrirModalAñadir = async () => {
  try {
    usuariosDisponibles.value = await servicioApi.obtenerUsuarios()
    mostrarModalAñadir.value = true
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

const cerrarModalAñadir = () => {
  mostrarModalAñadir.value = false
  terminoUsuario.value = ''
}

const añadirMiembro = async (idUsuario) => {
  try {
    await servicioApi.añadirParticipante(conversacionActual.value.id, idUsuario)
    
    // Actualizar localmente para que desaparezca de la lista
    if (conversacionActual.value.participanteIds) {
      conversacionActual.value.participanteIds.push(idUsuario)
    }
    
    cerrarModalAñadir()
  } catch (error) {
    console.error('Error al añadir miembro:', error)
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

.info-encabezado {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-encabezado.clickable {
  cursor: pointer;
}

.info-encabezado.clickable:hover h2 {
  text-decoration: underline;
}

.btn-añadir-miembro {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid white;
  background: transparent;
  color: white;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-añadir-miembro:hover {
  background: rgba(255,255,255,0.2);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-contenido {
  background: white;
  width: 350px;
  border-radius: 8px;
  overflow: hidden;
  color: #2f4a4f;
}

.modal-encabezado {
  padding: 12px 16px;
  background: #406D73;
  color: white;
  display: flex;
  justify-content: space-between;
}

.btn-cerrar {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
}

.modal-busqueda {
  padding: 12px;
}

.modal-busqueda input {
  width: 100%;
  padding: 8px;
  border: 1px solid #B2C5C8;
  border-radius: 4px;
}

.modal-listado {
  max-height: 300px;
  overflow-y: auto;
}

.item-usuario {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  gap: 12px;
}

.item-usuario:hover {
  background: #f7fcfd;
}

.accion-añadir {
  margin-left: auto;
  color: #406D73;
  font-weight: bold;
  font-size: 18px;
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
