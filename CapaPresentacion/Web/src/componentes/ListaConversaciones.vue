<template>
  <div class="lista-conversaciones">

    <!-- Panel usuario con banner + avatar cuadrado -->
    <div class="panel-usuario">
      <div class="profile-banner profile-banner--default" />
      <div class="profile-lower">
        <div class="profile-avatar-wrap">
          <div class="avatar-cuadrado">
            {{ usuarioActual?.username?.charAt(0).toUpperCase() }}
          </div>
        </div>
        <div class="profile-title-row">
          <span class="profile-name text-truncate">{{ usuarioActual?.username }}</span>
          <div class="acciones-perfil">
            <!-- DESPUÉS -->
            <v-btn icon="grupo" variant="text" color="accent" size="small" density="comfortable" @click="abrirNuevoGrupo" title="Nuevo grupo" />
            <v-btn icon="añadirContacto" variant="text" color="accent" size="small" density="comfortable" @click="abrirNuevaConversacion" title="Nueva conversación" />
          </div>
        </div>
        <div class="profile-subtitle">En línea</div>

        <!-- Buscador -->
        <div class="campo-busqueda">
          <v-icon size="16" color="#406D73" style="opacity:0.6">buscar</v-icon>
          <input v-model="termino" type="text" placeholder="Buscar contactos" />
        </div>
      </div>
    </div>

    <!-- Tabs Ver canales / Lista de tareas -->
    <div class="tabs-row">
      <button class="tab-btn" :class="{ activo: tabActivo === 'canales' }" @click="tabActivo = 'canales'">
        Ver canales de aviso
      </button>
      <button class="tab-btn" :class="{ activo: tabActivo === 'tareas' }" @click="tabActivo = 'tareas'">
        Lista de tareas
      </button>
    </div>

    <!-- Subheader lista -->
    <div class="seccion-titulo">
      LISTA DE USUARIO Y MIEMBROS PARA HABLAR
    </div>

    <!-- Listado conversaciones -->
    <div class="listado">
      <div
          v-for="conversacion in conversacionesFiltradas"
          :key="conversacion.id"
          class="item-conversacion"
          :class="{ activa: esActiva(conversacion) }"
          @click="seleccionarConversacion(conversacion)"
      >
        <div class="avatar-mini-lista">
          {{ conversacion.nombre.charAt(0).toUpperCase() }}
        </div>
        <div class="info-conversacion">
          <span class="nombre">{{ conversacion.nombre }}</span>
          <span class="ultimo-msg">{{ conversacion.ultimoMensaje || '...' }}</span>
        </div>
      </div>
      <div v-if="conversacionesFiltradas.length === 0" class="sin-resultados">
        Sin conversaciones
      </div>
    </div>

    <!-- Modal Nueva Conversación / Grupo -->
    <v-dialog v-model="mostrarModal" max-width="420">
      <v-card rounded="lg">
        <v-card-title class="modal-titulo">
          {{ esGrupo ? 'Nuevo Grupo' : 'Nueva Conversación' }}
          <v-spacer />
          <v-btn icon="cerrar" variant="text" size="small" @click="cerrarModal" />
        </v-card-title>

        <v-card-text class="pa-0">
          <div v-if="esGrupo" class="modal-seccion">
            <input
                v-model="nombreGrupo"
                type="text"
                placeholder="Nombre del grupo..."
                class="input-modal"
            />
          </div>
          <div class="modal-busqueda-input">
            <v-icon size="16" color="#406D73" style="opacity:0.6">buscar</v-icon>
            <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
          </div>
          <div class="modal-listado">
            <div
                v-for="usuario in usuariosFiltrados"
                :key="usuario.id"
                class="item-usuario-modal"
                @click="toggleSeleccion(usuario.id)"
            >
              <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario-modal">
                <span class="nombre">{{ usuario.username }}</span>
                <span class="email">{{ usuario.email }}</span>
              </div>
              <v-checkbox
                  v-if="esGrupo"
                  :model-value="seleccionados.includes(usuario.id)"
                  color="accent"
                  hide-details
                  density="compact"
                  @click.stop
              />
            </div>
          </div>
        </v-card-text>

        <v-card-actions v-if="esGrupo" class="pa-4 pt-2">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              :disabled="!nombreGrupo || seleccionados.length === 0"
              @click="crearGrupo"
          >
            Crear Grupo
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
const tabActivo = ref('canales')
let intervaloRefresco = null

const usuarioActual = computed(() => almacen.usuarioActual)
const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => (c.nombre || 'Chat').toLowerCase().includes(t))
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
const seleccionarConversacion = (conversacion) => almacen.establecerConversacionActual(conversacion)

const cargarConversaciones = async () => {
  try {
    const convs = await servicioApi.obtenerConversaciones()
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
  esGrupo.value = false
  mostrarModal.value = true
  usuarios.value = await servicioApi.obtenerUsuarios()
}

const abrirNuevoGrupo = async () => {
  esGrupo.value = true
  nombreGrupo.value = ''
  seleccionados.value = []
  mostrarModal.value = true
  usuarios.value = await servicioApi.obtenerUsuarios()
}

const toggleSeleccion = (idUsuario) => {
  if (!esGrupo.value) {
    crearChatPrivado(idUsuario)
    return
  }
  const index = seleccionados.value.indexOf(idUsuario)
  if (index === -1) seleccionados.value.push(idUsuario)
  else seleccionados.value.splice(index, 1)
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

const crearGrupo = async () => {
  try {
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
/* ---- Contenedor raíz ---- */
.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

/* ---- Panel usuario (banner + lower) ---- */
.panel-usuario {
  flex-shrink: 0;
  position: relative;
}

.profile-banner {
  height: 76px;
  background-color: #B3EBF2;
  background-image:
      linear-gradient(135deg, rgba(64,109,115,0.22) 0%, transparent 55%),
      linear-gradient(225deg, rgba(255,255,255,0.45) 0%, transparent 48%),
      radial-gradient(ellipse 90% 140% at 15% 0%, rgba(64,109,115,0.15), transparent);
  background-size: cover;
  background-position: center;
}

.profile-banner--default {
  /* ya cubierto arriba */
}

.profile-lower {
  position: relative;
  background: #f7fcfd;
  padding: 10px 14px 14px;
  padding-left: 100px;
  min-height: 86px;
}

.profile-avatar-wrap {
  position: absolute;
  left: 14px;
  top: -34px;
  z-index: 2;
}

.avatar-cuadrado {
  width: 64px;
  height: 64px;
  background: #406D73;
  color: #ffffff;
  border-radius: 10px;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 14px rgba(64,109,115,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
}

.profile-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.profile-name {
  font-size: 1rem;
  font-weight: 700;
  color: #2f4a4f;
  line-height: 1.25;
  letter-spacing: 0.01em;
}

.profile-subtitle {
  font-size: 12px;
  color: #5a8a94;
  margin-top: 2px;
}

.acciones-perfil {
  display: flex;
  gap: 4px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  transition: background 0.15s;
}

.btn-icon:hover {
  background: rgba(64,109,115,0.1);
}

/* ---- Buscador ---- */
.campo-busqueda {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(64,109,115,0.2);
  border-radius: 8px;
  padding: 5px 10px;
}

.campo-busqueda input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #2f4a4f;
  flex: 1;
  outline: none;
}

.campo-busqueda input::placeholder {
  color: rgba(64,109,115,0.45);
}

/* ---- Tabs ---- */
.tabs-row {
  display: flex;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(64,109,115,0.15);
}

.tab-btn {
  flex: 1;
  padding: 9px 4px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #7a9ea4;
  background: none;
  border: none;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.15s, border-color 0.15s;
}

.tab-btn.activo {
  color: #406D73;
  border-bottom-color: #406D73;
}

.tab-btn:hover:not(.activo) {
  color: #406D73;
}

/* ---- Subheader ---- */
.seccion-titulo {
  padding: 8px 16px 6px;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  letter-spacing: 0.06em;
  background: #ffffff;
  flex-shrink: 0;
}

/* ---- Listado ---- */
.listado {
  flex: 1;
  overflow-y: auto;
  background: #ffffff;
}

.item-conversacion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(64,109,115,0.07);
  transition: background 0.12s;
}

.item-conversacion:hover {
  background: #f7fcfd;
}

.item-conversacion.activa {
  background: rgba(179,235,242,0.4);
}

.avatar-mini-lista {
  width: 38px;
  height: 38px;
  min-width: 38px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
}

.info-conversacion {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.info-conversacion .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #2f4a4f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-conversacion .ultimo-msg {
  font-size: 12px;
  color: #7f9ea4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sin-resultados {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: #a0b8bc;
}

/* ---- Modal ---- */
.modal-titulo {
  background: #406D73;
  color: white;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  padding: 14px 16px;
}

.modal-seccion {
  padding: 14px 16px 0;
  background: #f7fcfd;
  border-bottom: 1px solid rgba(64,109,115,0.15);
}

.input-modal {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #B2C5C8;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 14px;
  outline: none;
  color: #2f4a4f;
}

.input-modal:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.1);
}

.modal-busqueda-input {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 12px 16px;
  background: #f0f7f8;
  border: 1px solid rgba(64,109,115,0.15);
  border-radius: 6px;
  padding: 6px 10px;
}

.modal-busqueda-input input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #2f4a4f;
  flex: 1;
  outline: none;
}

.modal-busqueda-input input::placeholder {
  color: rgba(64,109,115,0.4);
}

.modal-listado {
  max-height: 280px;
  overflow-y: auto;
}

.item-usuario-modal {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.12s;
}

.item-usuario-modal:hover {
  background: #f7fcfd;
}

.avatar-mini-modal {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.info-usuario-modal {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.info-usuario-modal .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #2f4a4f;
}

.info-usuario-modal .email {
  font-size: 12px;
  color: #7f9ea4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>