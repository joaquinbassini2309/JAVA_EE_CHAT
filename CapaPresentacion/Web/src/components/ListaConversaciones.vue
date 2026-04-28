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

    <div class="seccion-titulo">
      CONVERSACIONES
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

    <button class="btn-flotante-nueva" @click="abrirNuevaConversacion">
      +
    </button>

    <!-- Modal Nueva Conversación -->
    <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModal">
      <div class="modal-contenido">
        <div class="modal-encabezado">
          <h3>Nueva Conversación</h3>
          <button class="btn-cerrar" @click="cerrarModal">&times;</button>
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

const usuarioActual = computed(() => almacen.usuarioActual)
const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => c.nombre.toLowerCase().includes(t))
  }
  return lista
})

const usuariosFiltrados = computed(() => {
  const yo = almacen.usuarioActual
  let lista = usuarios.value.filter(u => u.id !== yo?.id)
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
    const existe = almacen.conversaciones.find(c => c.id === nuevaConv.id)
    if (!existe) almacen.agregarConversacion(nuevaConv)
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
  background: #B3EBF2; /* primary (light cyan) as background in sidebar */
  width: 320px;
  position: relative;
}

.panel-usuario {
  padding: 24px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(to bottom, #f7fcfd 0%, #B3EBF2 100%);
}

.avatar-grande {
  width: 56px;
  height: 56px;
  background: #406D73;
  color: white;
  border-radius: 4px;
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
  padding: 0 16px 16px 16px;
}

.input-contenedor {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 4px;
  padding: 4px 8px;
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
  padding: 16px;
  font-size: 12px;
  font-weight: bold;
  color: #406D73;
  border-top: 1px solid rgba(0,0,0,0.05);
  background: white;
}

.listado {
  flex: 1;
  overflow-y: auto;
  background: white;
}

.item-conversacion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.item-conversacion:hover {
  background-color: #f7fcfd;
}

.item-conversacion.activa {
  background-color: #B2C5C8;
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
  border-radius: 8px;
  overflow: hidden;
}

.modal-encabezado {
  padding: 16px;
  background: #406D73;
  color: white;
  display: flex;
  justify-content: space-between;
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
</style>
