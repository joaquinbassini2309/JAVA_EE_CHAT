<template>
  <div class="lista-conversaciones">
    <!-- Panel Usuario -->
    <div class="panel-usuario">
      <div class="avatar-grande">
        {{ usuarioActual?.username?.charAt(0).toUpperCase() }}
      </div>
      <div class="info-perfil">
        <span class="nombre">{{ usuarioActual?.username }}</span>
        <span class="estado">En línea</span>
      </div>
    </div>

    <!-- Buscador -->
    <div class="campo-busqueda">
      <div class="input-contenedor">
          <span class="lupa">🔍</span>
        <input
          v-model="termino"
          type="text"
          placeholder="Buscar contactos"
        />
      </div>
    </div>

    <div class="tabs-falsas">
      <button class="tab-falsa activa">Ver canales de aviso</button>
      <button class="tab-falsa">Lista de tareas</button>
    </div>

    <div class="seccion-titulo">
      LISTA DE USUARIO Y MIEMBROS PARA HABLAR
    </div>

    <!-- Listado -->
    <div class="listado">
      <div
        v-for="conversacion in conversacionesFiltradas"
        :key="conversacion.id"
        class="item-conversacion"
        :class="{ activa: esActiva(conversacion) }"
        @click="seleccionarConversacion(conversacion)"
      >
        <div class="avatar-mini">
          {{ conversacion.nombre.charAt(0).toUpperCase() }}
        </div>
        <div class="info-conversacion">
          <span class="nombre">{{ conversacion.nombre }}</span>
          <span class="ultimo-msg">{{ conversacion.ultimoMensaje || '...' }}</span>
        </div>
      </div>
    </div>

    <button class="btn-flotante-nueva" @click="abrirNuevaConversacion" title="Nueva conversación">
      +
    </button>
    <button class="btn-flotante-grupo" @click="abrirNuevoGrupo" title="Nuevo grupo">
      👥
    </button>

    <!-- Modal Nueva Conversación / Grupo -->
    <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModal">
      <div class="modal-contenido">
        <div class="modal-encabezado">
          <h3>{{ esGrupo ? 'Nuevo Grupo' : 'Nueva Conversación' }}</h3>
          <button class="btn-cerrar" @click="cerrarModal">&times;</button>
        </div>
        
        <div v-if="esGrupo" class="modal-config-grupo">
          <input
            v-model="nombreGrupo"
            type="text"
            placeholder="Nombre del grupo..."
            class="input-nombre-grupo"
          />
        </div>

        <div class="modal-busqueda">
          <input
            v-model="terminoUsuario"
            type="text"
            placeholder="Buscar usuario..."
          />
        </div>

        <div class="modal-listado">
          <div
            v-for="usuario in usuariosFiltrados"
            :key="usuario.id"
            class="item-usuario"
            @click="toggleSeleccion(usuario.id)"
          >
            <div class="avatar-mini">
              {{ usuario.username.charAt(0).toUpperCase() }}
            </div>
            <div class="info-usuario">
              <span class="nombre">{{ usuario.username }}</span>
              <span class="email">{{ usuario.email }}</span>
            </div>
            <div v-if="esGrupo" class="checkbox-seleccion">
              <input type="checkbox" :checked="seleccionados.includes(usuario.id)" @click.stop />
            </div>
          </div>
        </div>

        <div v-if="esGrupo" class="modal-acciones">
          <button class="btn-crear-grupo" @click="crearGrupo" :disabled="!nombreGrupo || seleccionados.length === 0">
            Crear Grupo
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const almacen = useAlmacen()
const termino = ref('')
const terminoUsuario = ref('')
const mostrarModal = ref(false)
const esGrupo = ref(false)
const nombreGrupo = ref('')
const seleccionados = ref([])
const usuarios = ref([])
let intervaloRefresco = null

const usuarioActual = computed(() => almacen.usuario)
const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value
  console.log('Conversaciones actuales:', lista)
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => {
      const nombre = c.nombre || 'Chat'
      return nombre.toLowerCase().includes(t)
    })
  }
  return lista
})

const usuariosFiltrados = computed(() => {
  const yo = usuarioActual.value
  let lista = usuarios.value.filter(u => String(u.id) !== String(yo?.id))
  if (terminoUsuario.value) {
    const t = terminoUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const esActiva = (conversacion) => conversacionActual.value?.id === conversacion.id

const seleccionarConversacion = (conversacion) => {
  almacen.establecerConversacionActual(conversacion)
}

const cargarConversaciones = async () => {
  try {
    const convs = await servicioApi.obtenerConversaciones()
    console.log('Conversaciones obtenidas del servidor:', convs)
    almacen.establecerConversaciones(convs)
  } catch (error) {
    console.error('Error al refrescar conversaciones:', error)
  }
}

onMounted(() => {
  cargarConversaciones()
  intervaloRefresco = setInterval(cargarConversaciones, 5000)
})

onUnmounted(() => {
  if (intervaloRefresco) clearInterval(intervaloRefresco)
})

const abrirNuevaConversacion = async () => {
  try {
    esGrupo.value = false
    mostrarModal.value = true
    usuarios.value = await servicioApi.obtenerUsuarios()
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

const abrirNuevoGrupo = async () => {
  try {
    esGrupo.value = true
    nombreGrupo.value = ''
    seleccionados.value = []
    mostrarModal.value = true
    usuarios.value = await servicioApi.obtenerUsuarios()
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  }
}

const toggleSeleccion = (idUsuario) => {
  if (!esGrupo.value) {
    crearChatPrivado(idUsuario)
    return
  }
  const index = seleccionados.value.indexOf(idUsuario)
  if (index === -1) {
    seleccionados.value.push(idUsuario)
  } else {
    seleccionados.value.splice(index, 1)
  }
}

const cerrarModal = () => {
  mostrarModal.value = false
  terminoUsuario.value = ''
}

const crearChatPrivado = async (idUsuario) => {
  try {
    if (String(idUsuario) === String(usuarioActual.value?.id)) {
      console.warn('No se puede crear chat privado con el mismo usuario')
      return
    }
    const nuevaConv = await servicioApi.crearConversacionPrivada(idUsuario)
    const existe = almacen.conversaciones.find(c => c.id === nuevaConv.id)
    if (!existe) almacen.agregarConversacion(nuevaConv)
    almacen.establecerConversacionActual(nuevaConv)
    cerrarModal()
  } catch (error) {
    const detalle = error?.response?.data?.detalle || error?.response?.data?.mensaje || error?.message
    console.error('Error al crear conversación:', detalle, error)
  }
}

const crearGrupo = async () => {
  try {
    console.log('Creando grupo con seleccionados:', seleccionados.value)
    const nuevaConv = await servicioApi.crearGrupo(nombreGrupo.value, seleccionados.value)
    almacen.agregarConversacion(nuevaConv)
    almacen.establecerConversacionActual(nuevaConv)
    cerrarModal()
  } catch (error) {
    console.error('Error al crear grupo:', error)
  }
}
</script>

<style scoped>
.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f7fcfd;
  width: 360px;
  position: relative;
  border-right: 1px solid rgba(64, 109, 115, 0.2);
}

.panel-usuario {
  padding: 16px 16px 10px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: linear-gradient(135deg, rgba(179, 235, 242, 0.55) 0%, rgba(247, 252, 253, 1) 90%);
  border-bottom: 1px solid rgba(64, 109, 115, 0.15);
}

.avatar-grande {
  width: 62px;
  height: 62px;
  background: #406D73;
  color: white;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
}

.info-perfil {
  display: flex;
  flex-direction: column;
}

.info-perfil .nombre {
  font-weight: bold;
  color: #2f4a4f;
  font-size: 16px;
}

.info-perfil .estado {
  font-size: 12px;
  color: #5a8a94;
}

.campo-busqueda {
  padding: 12px 16px;
  background: #f7fcfd;
}

.input-contenedor {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 10px;
  padding: 6px 10px;
  border: 1px solid rgba(64, 109, 115, 0.15);
}

.input-contenedor input {
  border: none;
  background: transparent;
  padding: 4px 8px;
  flex: 1;
  font-size: 14px;
}

.input-contenedor input:focus {
  outline: none;
}

.seccion-titulo {
  padding: 10px 16px;
  font-size: 12px;
  font-weight: bold;
  color: #406D73;
  border-top: 1px solid rgba(0,0,0,0.05);
  background: white;
}

.tabs-falsas {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  padding: 0 8px 8px;
  background: #f7fcfd;
}

.tab-falsa {
  border: 1px solid rgba(64, 109, 115, 0.15);
  background: #eef3f5;
  color: #6a7f85;
  font-size: 11px;
  padding: 8px 6px;
}

.tab-falsa.activa {
  background: #d7dde0;
  color: #406D73;
  font-weight: 600;
}

.listado {
  flex: 1;
  overflow-y: auto;
  background: #ffffff;
}

.item-conversacion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(64, 109, 115, 0.08);
  margin: 0 8px;
  border-radius: 10px;
}

.item-conversacion:hover {
  background-color: #f7fcfd;
}

.item-conversacion.activa {
  background-color: rgba(179, 235, 242, 0.65);
}

.avatar-mini {
  width: 40px;
  height: 40px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.info-conversacion {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.info-conversacion .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #2f4a4f;
}

.info-conversacion .ultimo-msg {
  font-size: 12px;
  color: #7f8c8d;
}

.btn-flotante-nueva {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #406D73;
  color: white;
  border: none;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  cursor: pointer;
  z-index: 10;
}

.btn-flotante-grupo {
  position: absolute;
  bottom: 80px;
  right: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #6A9E7D;
  color: white;
  border: none;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  cursor: pointer;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
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
  width: 400px;
  max-height: 80vh;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.info-usuario {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-usuario .nombre {
  font-weight: 600;
  color: #2f4a4f;
}

.info-usuario .email {
  font-size: 12px;
  color: #5a8a94;
  margin-top: 2px;
}

.modal-encabezado {
  padding: 16px;
  background: #406D73;
  color: white;
  display: flex;
  justify-content: space-between;
}

.modal-config-grupo {
  padding: 16px;
  background: #f7fcfd;
  border-bottom: 1px solid #ddd;
}

.input-nombre-grupo {
  width: 100%;
  padding: 10px;
  border: 1px solid #B2C5C8;
  border-radius: 4px;
}

.modal-busqueda {
  padding: 12px;
}

.modal-busqueda input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal-listado {
  flex: 1;
  overflow-y: auto;
}

.item-usuario {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
}

.item-usuario:hover {
  background-color: #f7fcfd;
}

.checkbox-seleccion {
  margin-left: auto;
}

.modal-acciones {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #eee;
}

.btn-crear-grupo {
  background-color: #406D73;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.btn-crear-grupo:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
