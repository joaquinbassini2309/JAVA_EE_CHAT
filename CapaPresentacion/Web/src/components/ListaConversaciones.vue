<template>
  <div class="lista-conversaciones">
    <div class="encabezado">
      <h3>Conversaciones</h3>
      <button class="btn-nueva" @click="abrirNuevaConversacion">
        +
      </button>
    </div>

    <div class="campo-busqueda">
      <input
        v-model="termino"
        type="text"
        placeholder="Buscar conversación..."
        class="input-busqueda"
      />
    </div>

    <div class="listado">
      <div
        v-for="conversacion in conversacionesFiltradas"
        :key="conversacion.id"
        class="item-conversacion"
        :class="{ activa: esActiva(conversacion) }"
        @click="seleccionarConversacion(conversacion)"
      >
        <div class="info-conversacion">
          <h4>{{ conversacion.nombre }}</h4>
          <p>{{ conversacion.ultimoMensaje || 'Sin mensajes' }}</p>
        </div>
        <span v-if="conversacion.noLeidos > 0" class="badge-no-leidos">
          {{ conversacion.noLeidos }}
        </span>
      </div>
    </div>

    <!-- Modal Nueva Conversación -->
    <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModal">
      <div class="modal-contenido">
        <div class="modal-encabezado">
          <h3>Iniciar nueva conversación</h3>
          <button class="btn-cerrar" @click="cerrarModal">&times;</button>
        </div>
        <div class="modal-busqueda">
          <input
            v-model="terminoUsuario"
            type="text"
            placeholder="Buscar usuario por nombre o email..."
            class="input-busqueda"
          />
        </div>
        <div class="modal-listado">
          <div
            v-for="usuario in usuariosFiltrados"
            :key="usuario.id"
            class="item-usuario"
            @click="crearChatPrivado(usuario.id)"
          >
            <div class="avatar-mini">
              {{ usuario.username.charAt(0).toUpperCase() }}
            </div>
            <div class="info-usuario">
              <span class="nombre">{{ usuario.username }}</span>
              <span class="email">{{ usuario.email }}</span>
            </div>
          </div>
          <div v-if="usuariosFiltrados.length === 0" class="sin-resultados">
            No se encontraron usuarios
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAlmacen } from '@/stores'
import { servicioApi } from '@/services/api'

const almacen = useAlmacen()
const termino = ref('')
const terminoUsuario = ref('')
const mostrarModal = ref(false)
const usuarios = ref([])

const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value

  // Filtrar por búsqueda si hay término
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => c.nombre.toLowerCase().includes(t))
  }

  // Filtrar para mostrar solo chats con mensajes O el chat seleccionado/nuevo
  return lista.filter(c => 
    c.ultimoMensaje || c.id === conversacionActual.value?.id
  )
})

const usuariosFiltrados = computed(() => {
  const yo = almacen.usuarioActual
  let lista = usuarios.value.filter(u => u.id !== yo?.id)
  
  if (!terminoUsuario.value) {
    return lista
  }

  const t = terminoUsuario.value.toLowerCase()
  return lista.filter(u => 
    u.username.toLowerCase().includes(t) || 
    u.email.toLowerCase().includes(t)
  )
})

const esActiva = (conversacion) => {
  return conversacionActual.value?.id === conversacion.id
}

const seleccionarConversacion = (conversacion) => {
  almacen.establecerConversacionActual(conversacion)
}

const abrirNuevaConversacion = async () => {
  try {
    mostrarModal.value = true
    usuarios.value = await servicioApi.obtenerUsuarios()
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

const cerrarModal = () => {
  mostrarModal.value = false
  terminoUsuario.value = ''
}

const crearChatPrivado = async (idUsuario) => {
  try {
    const nuevaConv = await servicioApi.crearConversacionPrivada(idUsuario)
    
    // Verificar si ya estaba en la lista
    const existe = almacen.conversaciones.find(c => c.id === nuevaConv.id)
    if (!existe) {
      almacen.agregarConversacion(nuevaConv)
    }
    
    almacen.establecerConversacionActual(nuevaConv)
    cerrarModal()
  } catch (error) {
    console.error('Error al crear conversación:', error)
  }
}
</script>

<style scoped>
.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f9fafb;
  border-right: 1px solid #e5e7eb;
  max-width: 350px;
  width: 100%;
}

.encabezado {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.encabezado h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.btn-nueva {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 24px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-nueva:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.campo-busqueda {
  padding: 12px 16px;
}

.input-busqueda {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.input-busqueda:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.listado {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.item-conversacion {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: background-color 0.2s;
}

.item-conversacion:hover {
  background-color: #f3f4f6;
}

.item-conversacion.activa {
  background-color: #ede9fe;
  border-left: 4px solid #667eea;
}

.info-conversacion {
  flex: 1;
}

.info-conversacion h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.info-conversacion p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge-no-leidos {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 6px;
  background-color: #667eea;
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

/* Estilos Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-contenido {
  background: white;
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-encabezado {
  padding: 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-cerrar {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
}

.modal-busqueda {
  padding: 12px;
}

.modal-listado {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.item-usuario {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.item-usuario:hover {
  background-color: #f3f4f6;
}

.avatar-mini {
  width: 32px;
  height: 32px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-size: 14px;
}

.info-usuario {
  display: flex;
  flex-direction: column;
}

.info-usuario .nombre {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.info-usuario .email {
  font-size: 12px;
  color: #6b7280;
}

.sin-resultados {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
}
</style>
