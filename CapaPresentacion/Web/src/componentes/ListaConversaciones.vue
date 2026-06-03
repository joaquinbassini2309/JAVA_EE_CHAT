<template>
  <div class="lista-conversaciones">

    <!-- Header Compacto de Usuario -->
    <div class="panel-usuario-compact">
      <div class="user-header-row">
        <v-menu offset-y content-class="menu-perfil-flotante" transition="slide-y-transition">
          <template v-slot:activator="{ props }">
            <div class="user-avatar-wrap" v-bind="props" v-ripple>
              <img v-if="usuarioActual?.fotoUrl" :src="usuarioActual.fotoUrl" class="user-avatar-img" />
              <span v-else>{{ usuarioActual?.username?.charAt(0).toUpperCase() }}</span>
            </div>
          </template>
          <v-list class="bg-surface" density="compact">
            <v-list-item @click="abrirEdicionPerfil('username')" prepend-icon="mdi-account-edit">
              <v-list-item-title>Cambiar nombre de usuario</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionPerfil('fotoUrl')" prepend-icon="mdi-camera">
              <v-list-item-title>Cambiar Foto de perfil</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionPerfil('descripcion')" prepend-icon="mdi-card-text-outline">
              <v-list-item-title>Cambiar descripción</v-list-item-title>
            </v-list-item>
            <v-divider class="my-1" />
            <v-list-item @click="cerrarSesionLocal" class="text-error" prepend-icon="mdi-logout">
              <v-list-item-title>Cerrar Sesión</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

        <div class="user-info-col">
          <span class="user-name text-truncate">{{ usuarioActual?.username }}</span>
        </div>

        <div class="user-actions-row">
          <v-hover v-slot="{ isHovering, props }">
            <v-btn v-bind="props" icon="mdi-account-multiple-plus" :variant="isHovering ? 'flat' : 'outlined'" color="#406D73" size="small" @click="abrirNuevoGrupo" title="Nuevo grupo" class="header-action-btn teal-hover-white" rounded />
          </v-hover>
          <v-hover v-slot="{ isHovering, props }">
            <v-btn v-bind="props" icon="mdi-message-plus" :variant="isHovering ? 'flat' : 'outlined'" color="#406D73" size="small" @click="abrirNuevaConversacion" title="Nueva conversación" class="header-action-btn teal-hover-white" rounded />
          </v-hover>
          <v-hover v-slot="{ isHovering, props }">
            <v-btn v-bind="props" icon="mdi-logout" :variant="isHovering ? 'flat' : 'outlined'" color="error" size="small" @click="cerrarSesionLocal" title="Cerrar sesión" class="header-action-btn" rounded />
          </v-hover>
        </div>
      </div>

      <!-- Buscador -->
      <div class="campo-busqueda-modern">
        <v-menu :close-on-content-click="false" location="bottom start">
          <template v-slot:activator="{ props }">
            <button v-bind="props" title="Filtrar conversaciones" class="btn-filtro">
              <v-icon size="20">mdi-filter-variant</v-icon>
            </button>
          </template>
          <v-card min-width="220" rounded="xl" style="overflow: hidden; box-shadow: 0 8px 24px rgba(64,109,115,0.15) !important;">
            <v-card-title style="background: #406D73; color: white; font-size: 13px; padding: 10px 16px; display:flex; align-items:center; gap:6px;">
              <v-icon size="16">mdi-filter-variant</v-icon>
              Mostrar en lista
            </v-card-title>
            <v-list density="compact" class="py-1" bg-color="#ffffff">
              <template v-if="tabActivo === 'chats'">
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Contactos" value="PRIVADA" color="#406D73" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Grupos" value="GRUPO" color="#406D73" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Canales de aviso" value="AVISO" color="#406D73" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Lista de tareas" value="TAREAS" color="#406D73" hide-details /></v-list-item>
              </template>
              <template v-else-if="tabActivo === 'tareas'">
                <v-list-item><v-checkbox-btn v-model="filtrosTareas" label="Tareas pendientes" value="PENDIENTES" color="#406D73" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosTareas" label="Tareas completadas" value="COMPLETADAS" color="#406D73" hide-details /></v-list-item>
              </template>
              <template v-else>
                <v-list-item><v-list-item-title class="text-caption text-grey py-2 px-2">Solo filtro por texto</v-list-item-title></v-list-item>
              </template>
            </v-list>
          </v-card>
        </v-menu>
        <div class="divider-v"></div>
        <v-icon size="17" color="#406D73" style="opacity:0.55;">mdi-magnify</v-icon>
        <input v-model="termino" type="text" placeholder="Buscar..." class="input-busqueda" />
      </div>
    </div>

    <!-- Navegación Vertical -->
    <div class="nav-vertical">
      <button class="nav-item" :class="{ activo: tabActivo === 'chats' }" @click="cambiarTab('chats')">
        <v-icon size="18">mdi-chat-outline</v-icon>
        <span>Mis Chats</span>
      </button>
      <button class="nav-item" :class="{ activo: tabActivo === 'canales' }" @click="cambiarTab('canales')">
        <v-icon size="18">mdi-bullhorn-outline</v-icon>
        <span>Canales de aviso</span>
      </button>
      <button class="nav-item" :class="{ activo: panelTareasAbierto }" @click="togglePanelTareas">
        <v-icon size="18">mdi-checkbox-marked-circle-outline</v-icon>
        <span>Lista de tareas</span>
      </button>
    </div>

    <!-- Subheader según tab -->
    <div v-if="tabActivo === 'chats'" class="seccion-chats-titulo">CHATS RECIENTES</div>
    <div v-else-if="tabActivo === 'canales'" class="seccion-chats-titulo">CANALES DE AVISOS</div>

    <!-- Listado conversaciones -->
    <div class="listado">
      <!-- Tab Chats -->
      <template v-if="tabActivo === 'chats'">
        <div
            v-for="conversacion in conversacionesFiltradas"
            :key="conversacion.id"
            class="item-conversacion"
            :class="{ activa: esActiva(conversacion) }"
            @click="seleccionarConversacion(conversacion)"
        >
          <div class="avatar-mini-lista">
            <img v-if="conversacion.fotoUrl" :src="conversacion.fotoUrl" class="avatar-img-lista" alt="avatar" />
            <span v-else>{{ obtenerNombreVisibleConversacion(conversacion, usuarioActual?.id).charAt(0).toUpperCase() }}</span>
          </div>
          <div class="info-conversacion" style="flex: 1; min-width: 0;">
            <div class="conv-header-row">
              <span class="nombre text-truncate" style="display: flex; align-items: center; gap: 4px;">
                {{ obtenerNombreVisibleConversacion(conversacion, usuarioActual?.id) }}
                <v-icon v-if="esConversacionFavorita(conversacion)" color="#FFC107" size="16" class="ml-1" title="Favorito">
                  mdi-star
                </v-icon>
              </span>
              <span class="conv-time" v-if="conversacion.fechaUltimoMensaje">{{ formatearHora(conversacion.fechaUltimoMensaje) }}</span>
            </div>
            <div class="conv-footer-row">
              <span class="ultimo-msg text-truncate">
                {{ conversacion.ultimoMensaje || '...' }}
              </span>
            </div>
          </div>
        </div>
        <div v-if="conversacionesFiltradas.length === 0" class="sin-resultados">
          Sin conversaciones
        </div>
      </template>

      <!-- Tab Canales -->
      <template v-else-if="tabActivo === 'canales'">
        <!-- Botón crear canal dentro del tab -->
        <div class="canal-header-action">
          <button class="btn-crear-canal" @click="abrirNuevoCanal">
            <v-icon size="16" class="mr-1">mdi-plus-circle-outline</v-icon>
            Crear nuevo canal de avisos
          </button>
        </div>

        <div
            v-for="canal in canalesFiltrados"
            :key="canal.id"
            class="item-conversacion d-flex align-center justify-space-between"
            :class="{ 'cursor-pointer': usuarioPerteneceAlCanal(canal) }"
            @click="usuarioPerteneceAlCanal(canal) ? abrirCanalYaMiembro(canal) : null"
        >
          <div class="d-flex align-center" style="flex: 1; min-width: 0;">
            <div class="avatar-mini-lista mr-3" style="background: rgba(64,109,115,0.12); color: #406D73;">
              <v-icon size="18">mdi-bullhorn</v-icon>
            </div>
            <div class="info-conversacion" style="flex: 1; min-width: 0;">
              <span class="nombre text-truncate">{{ canal.nombre }}</span>
              <span class="ultimo-msg text-truncate">{{ canal.ultimoMensaje || 'Canal de avisos públicos' }}</span>
            </div>
          </div>

          <div class="acciones-canal ml-2">
            <v-chip
                v-if="usuarioPerteneceAlCanal(canal)"
                color="success"
                size="small"
                variant="tonal"
                class="font-weight-bold"
                style="height: 24px;"
                @click.stop="abrirCanalYaMiembro(canal)"
            >
              Miembro
            </v-chip>
            <v-btn
                v-else
                color="accent"
                size="x-small"
                variant="flat"
                class="font-weight-bold px-3"
                rounded="lg"
                height="24"
                :loading="uniendoseCanalId === canal.id"
                @click.stop="unirseACanal(canal)"
            >
              Unirse
            </v-btn>
          </div>
        </div>
        <div v-if="canalesFiltrados.length === 0" class="sin-resultados d-flex flex-column align-center justify-center pa-6">
          <v-icon size="36" color="#a0b8bc" class="mb-2">mdi-bullhorn-outline</v-icon>
          <span>Aún no hay canales de avisos</span>
          <span style="font-size: 11px; color: #a0b8bc; margin-top: 4px;">Sé el primero en crear uno</span>
        </div>
      </template>
    </div>

    <!-- Modal Nueva Conversación / Grupo -->
    <v-dialog v-model="mostrarModal" max-width="420">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">
            {{ esGrupo ? 'mdi-account-multiple-plus' : 'mdi-message-plus' }}
          </v-icon>
          {{ esGrupo ? 'Nuevo Grupo' : 'Nueva Conversación' }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModal" />
        </v-card-title>

        <v-card-text class="modal-contenido-conv">
          <div v-if="esGrupo" class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-account-group</v-icon>
              Nombre del grupo
            </label>
            <input
                v-model="nombreGrupo"
                type="text"
                placeholder="Ej: Proyecto 2024"
                class="input-modal-conv"
            />
          </div>
          <div class="modal-busqueda-mejorada">
            <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
            <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
          </div>
          <div class="modal-listado-mejorado">
            <div
                v-for="usuario in usuariosFiltrados"
                :key="usuario.id"
                class="item-usuario-mejorado"
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

        <v-card-actions v-if="esGrupo" class="modal-acciones-conv">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              size="small"
              :disabled="!nombreGrupo || seleccionados.length === 0"
              prepend-icon="mdi-check-circle"
              @click="crearGrupo"
              class="btn-crear-grupo"
          >
            Crear Grupo
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Nuevo Canal de Avisos -->
    <v-dialog v-model="mostrarModalCanal" max-width="420">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">mdi-bullhorn</v-icon>
          Nuevo Canal de Avisos
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModalCanal" />
        </v-card-title>

        <v-card-text class="modal-contenido-conv">
          <!-- Nombre del canal -->
          <div class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-bullhorn-outline</v-icon>
              Nombre del canal
            </label>
            <input
                v-model="nombreCanal"
                type="text"
                placeholder="Ej: Comunicados Generales"
                class="input-modal-conv"
            />
          </div>

          <!-- Buscar miembros iniciales (opcional) -->
          <div class="modal-busqueda-mejorada">
            <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
            <input v-model="terminoCanalUsuario" type="text" placeholder="Agregar miembros (opcional)..." />
          </div>
          <div class="modal-listado-mejorado">
            <div
                v-for="usuario in usuariosFiltradosCanal"
                :key="usuario.id"
                class="item-usuario-mejorado"
                @click="toggleSeleccionCanal(usuario.id)"
            >
              <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario-modal">
                <span class="nombre">{{ usuario.username }}</span>
                <span class="email">{{ usuario.email }}</span>
              </div>
              <v-checkbox
                  :model-value="seleccionadosCanal.includes(usuario.id)"
                  color="accent"
                  hide-details
                  density="compact"
                  @click.stop
              />
            </div>
          </div>
          <div v-if="seleccionadosCanal.length > 0" class="d-flex flex-wrap gap-1 pt-1">
            <v-chip v-for="id in seleccionadosCanal" :key="id" size="x-small" color="accent" variant="tonal" closable @click:close="toggleSeleccionCanal(id)">
              {{ usuariosCanal.find(u => u.id === id)?.username }}
            </v-chip>
          </div>
        </v-card-text>

        <v-card-actions class="modal-acciones-conv">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              size="small"
              :disabled="!nombreCanal.trim()"
              :loading="creandoCanal"
              prepend-icon="mdi-check-circle"
              @click="crearCanal"
              class="btn-crear-grupo"
          >
            Crear Canal
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Nueva Tarea removido (ahora está en PanelTareas.vue) -->

    <!-- Modal Editar Perfil -->
    <v-dialog v-model="mostrarModalPerfil" max-width="420">
      <v-card rounded="2xl" class="modal-editar-perfil">
        <v-card-title class="modal-titulo-perfil">
          <v-icon size="18" color="white" class="mr-2">
            {{ tipoEdicionPerfil === 'username' ? 'mdi-account' : tipoEdicionPerfil === 'fotoUrl' ? 'mdi-image-account' : tipoEdicionPerfil === 'imagenBanner' ? 'mdi-image-area' : tipoEdicionPerfil === 'descripcion' ? 'mdi-text-account' : 'mdi-account-edit' }}
          </v-icon>
          {{ tituloModalPerfil }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalPerfil = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-perfil">
          <div v-if="tipoEdicionPerfil === 'username'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-account-circle</v-icon>
              Nuevo nombre de usuario
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="Ej: JuanPerez" />
          </div>
          <div v-else-if="tipoEdicionPerfil === 'descripcion'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-text</v-icon>
              Nueva descripción
            </label>
            <textarea v-model="valorEdicionPerfil" class="input-modal-mejorado" placeholder="Cuenta algo sobre ti..." rows="3"></textarea>
          </div>
          <div v-else-if="tipoEdicionPerfil === 'fotoUrl'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-link</v-icon>
              URL de la foto de perfil
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="https://ejemplo.com/mifoto.jpg" />
          </div>
          <div v-else-if="tipoEdicionPerfil === 'imagenBanner'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-link</v-icon>
              URL de la imagen de banner
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="https://ejemplo.com/mibanner.jpg" />
          </div>
        </v-card-text>
        <v-card-actions class="modal-acciones-perfil">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              prepend-icon="mdi-content-save"
              size="small"
              @click="guardarPerfil"
              :loading="guardandoPerfil"
              class="btn-guardar-perfil"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
import { formatearFecha, formatearHora } from '@/utilidades/formateoFechas'
import { useRouter } from 'vue-router'

const almacen = useAlmacen()
const router = useRouter()
const termino = ref('')
const terminoUsuario = ref('')
const mostrarModal = ref(false)
const esGrupo = ref(false)
const nombreGrupo = ref('')
const seleccionados = ref([])
const usuarios = ref([])
const tabActivo = ref('chats')
const filtrosSeleccionados = ref(['PRIVADA', 'GRUPO', 'AVISO'])
let intervaloRefresco = null

const panelTareasAbierto = computed(() => almacen.panelTareasAbierto)

const togglePanelTareas = () => {
  almacen.togglePanelTareas()
}

const cambiarTab = (tab) => {
  tabActivo.value = tab
  if (tab === 'canales') {
    cargarCanalesAvisos()
  }
}

const cargarCanalesAvisos = async () => {
  try {
    canalesAvisos.value = await servicioApi.obtenerCanalesAvisos()
  } catch (error) {
    console.error('Error al cargar canales de aviso:', error)
  }
}

const usuarioPerteneceAlCanal = (canal) => {
  return almacen.conversaciones.some(c => c.id === canal.id)
}

const abrirCanalYaMiembro = (canal) => {
  const conv = almacen.conversaciones.find(c => c.id === canal.id)
  if (conv) {
    almacen.establecerConversacionActual(conv)
    tabActivo.value = 'chats'
  }
}

const unirseACanal = async (canal) => {
  uniendoseCanalId.value = canal.id
  try {
    await servicioApi.unirseACanalAvisos(canal.id)
    await cargarConversaciones()
    
    const conv = almacen.conversaciones.find(c => c.id === canal.id)
    if (conv) {
      almacen.establecerConversacionActual(conv)
    }
    tabActivo.value = 'chats'
  } catch (error) {
    console.error('Error al unirse al canal de avisos:', error)
  } finally {
    uniendoseCanalId.value = null
  }
}

const abrirNuevoCanal = async () => {
  nombreCanal.value = ''
  terminoCanalUsuario.value = ''
  seleccionadosCanal.value = []
  mostrarModalCanal.value = true
  // Cargar lista de usuarios para agregar como miembros iniciales
  try {
    usuariosCanal.value = await servicioApi.obtenerUsuarios()
  } catch (e) {
    console.error('Error al cargar usuarios para canal:', e)
  }
}

const cerrarModalCanal = () => {
  mostrarModalCanal.value = false
  nombreCanal.value = ''
  terminoCanalUsuario.value = ''
  seleccionadosCanal.value = []
}

const toggleSeleccionCanal = (idUsuario) => {
  const index = seleccionadosCanal.value.indexOf(idUsuario)
  if (index === -1) seleccionadosCanal.value.push(idUsuario)
  else seleccionadosCanal.value.splice(index, 1)
}

const crearCanal = async () => {
  if (!nombreCanal.value.trim()) return
  creandoCanal.value = true
  try {
    const nuevoCanal = await servicioApi.crearCanalAvisos(nombreCanal.value.trim())
    
    // Agregar miembros seleccionados al canal uno a uno
    for (const idUsuario of seleccionadosCanal.value) {
      try {
        await servicioApi.añadirParticipante(nuevoCanal.id, idUsuario)
      } catch (e) {
        console.error('Error al agregar miembro al canal:', e)
      }
    }

    await cargarConversaciones()
    const canalActualizado = almacen.conversaciones.find(c => c.id === nuevoCanal.id) || nuevoCanal
    almacen.establecerConversacionActual(canalActualizado)

    cerrarModalCanal()
    tabActivo.value = 'chats'
  } catch (error) {
    console.error('Error al crear canal de avisos:', error)
  } finally {
    creandoCanal.value = false
  }
}

// Edición de perfil
const mostrarModalPerfil = ref(false)
const tipoEdicionPerfil = ref('')
const valorEdicionPerfil = ref('')
const tituloModalPerfil = ref('')
const guardandoPerfil = ref(false)

const usuarioActual = computed(() => almacen.usuarioActual)
const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value.filter(c => {
    return filtrosSeleccionados.value.includes(c.tipo)
  })
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => obtenerNombreVisibleConversacion(c, usuarioActual.value?.id).toLowerCase().includes(t))
  }
  return lista
})

const canalesFiltrados = computed(() => {
  let lista = canalesAvisos.value
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

const usuariosFiltradosCanal = computed(() => {
  const yo = almacen.usuarioActual
  let lista = usuariosCanal.value.filter(u => u.id !== yo?.id)
  if (terminoCanalUsuario.value) {
    const t = terminoCanalUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const esActiva = (conversacion) => conversacionActual.value?.id === conversacion.id
const seleccionarConversacion = async (conversacion) => {
    if (conversacionActual.value?.id === conversacion.id) return;

    // Limpiar mensajes inmediatamente para no mostrar mensajes de la conversación anterior
    almacen.establecerMensajes([])
    almacen.establecerConversacionActual(conversacion)
}

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

    // Enviar mensaje automático de bienvenida
    try {
      const mensajeAutomatico = await servicioApi.enviarMensaje({
        conversacionId: nuevaConv.id,
        contenido: 'Bienvenido al grupo',
        tipoMensaje: 'TEXTO'
      })
      almacen.agregarMensaje(mensajeAutomatico)
    } catch (error) {
      console.error('Error al enviar mensaje automático:', error)
    }

    cerrarModal()
  } catch (error) {
    console.error('Error al crear grupo:', error)
  }
}

// Logica perfil
const abrirEdicionPerfil = (tipo) => {
  tipoEdicionPerfil.value = tipo
  valorEdicionPerfil.value = usuarioActual.value[tipo] || ''

  if (tipo === 'username') tituloModalPerfil.value = 'Cambiar nombre de usuario'
  else if (tipo === 'fotoUrl') tituloModalPerfil.value = 'Cambiar foto de perfil'
  else if (tipo === 'imagenBanner') tituloModalPerfil.value = 'Colocar imagen de banner'
  else if (tipo === 'descripcion') tituloModalPerfil.value = 'Cambiar descripción'

  mostrarModalPerfil.value = true
}

const guardarPerfil = async () => {
  guardandoPerfil.value = true
  try {
    const data = {}
    data[tipoEdicionPerfil.value] = valorEdicionPerfil.value

    // El servicio espera un DTO con fotoUrl, estado, username, descripcion, imagenBanner
    // Mantenemos los datos actuales por defecto
    const payload = {
      username: usuarioActual.value.username,
      fotoUrl: usuarioActual.value.fotoUrl,
      descripcion: usuarioActual.value.descripcion,
      imagenBanner: usuarioActual.value.imagenBanner,
      estado: usuarioActual.value.estado,
      ...data
    }

    const usrActualizado = await servicioApi.actualizarPerfil(payload)
    almacen.establecerUsuario(usrActualizado)
    localStorage.setItem('usuario', JSON.stringify(usrActualizado))
    mostrarModalPerfil.value = false
  } catch(error) {
    console.error('Error actualizando perfil', error)
  } finally {
    guardandoPerfil.value = false
  }
}

const cerrarSesionLocal = () => {
  almacen.cerrarSesion()
  const contextPath = window.location.pathname.includes('/chat-empresarial') 
    ? '/chat-empresarial' 
    : ''
  window.location.href = `${contextPath}/login`
}

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

const esConversacionFavorita = (conversacion) => {
  if (conversacion.tipo !== 'PRIVADA') return false
  const yo = almacen.usuarioActual
  if (!yo || !yo.favoritos) return false
  const favIds = yo.favoritos.split(',').filter(x => x).map(Number)
  
  let otroId = null
  if (conversacion.participantes) {
    const otro = conversacion.participantes.find(p => p.usuario && p.usuario.id !== yo.id)
    if (otro && otro.usuario) otroId = otro.usuario.id
  }
  if (otroId === null && conversacion.participanteIds) {
    otroId = conversacion.participanteIds.find(id => id !== yo.id)
  }
  return otroId !== null && favIds.includes(otroId)
}
</script>

<style scoped>
/* ==================================================
   ListaConversaciones.vue — Premium SaaS Sidebar 2026
   ================================================== */

.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

/* ===== HEADER COMPACTO ===== */
.panel-usuario-compact {
  flex-shrink: 0;
  padding: 14px 16px 12px;
  background: #ffffff;
  border-bottom: 1px solid rgba(64,109,115,0.08);
}

.user-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-wrap {
  width: 42px;
  height: 42px;
  min-width: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64,109,115,0.2);
  transition: transform .15s, box-shadow .15s;
}

.user-avatar-wrap:hover {
  transform: scale(1.06);
  box-shadow: 0 4px 14px rgba(64,109,115,0.28);
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info-col {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a2e31;
  letter-spacing: -0.01em;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-actions-row {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.header-action-btn {
  transition: transform .15s ease !important;
}
.header-action-btn:hover {
  transform: scale(1.12) !important;
}

.teal-hover-white.v-btn--variant-flat {
  color: #ffffff !important;
}
.teal-hover-white.v-btn--variant-flat .v-icon,
.teal-hover-white.v-btn--variant-flat i {
  color: #ffffff !important;
}

/* Buscador */
.campo-busqueda-modern {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  background: #f4f8f9;
  border: 1px solid rgba(64,109,115,0.1);
  border-radius: 12px;
  padding: 6px 12px;
  transition: border-color .2s, box-shadow .2s;
}

.campo-busqueda-modern:focus-within {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.07);
}

.btn-filtro {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: #406D73;
  padding: 3px;
  border-radius: 6px;
  transition: background .15s;
  flex-shrink: 0;
}

.btn-filtro:hover { background: rgba(64,109,115,0.08); }

.divider-v {
  width: 1px;
  height: 16px;
  background: rgba(64,109,115,0.15);
  margin: 0 4px;
  flex-shrink: 0;
}

.input-busqueda {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #1a2e31;
  flex: 1;
  outline: none;
  min-width: 0;
}

.input-busqueda::placeholder { color: rgba(64,109,115,0.4); }

/* ===== NAVEGACIÓN VERTICAL ===== */
.nav-vertical {
  display: flex;
  flex-direction: column;
  padding: 8px 12px;
  gap: 4px;
  background: #ffffff;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #406D73;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 10px;
  transition: background .18s, color .18s;
}

.nav-item.activo {
  background: rgba(64,109,115,0.08);
}

.nav-item:hover:not(.activo) {
  background: rgba(64,109,115,0.04);
}

/* Subheader */
.seccion-chats-titulo {
  padding: 12px 16px 8px;
  font-size: 10px;
  font-weight: 800;
  color: #5a8a94;
  letter-spacing: 0.08em;
  background: #ffffff;
  flex-shrink: 0;
  text-transform: uppercase;
}

/* ===== LISTADO ===== */
.listado {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  background: #ffffff;
}

.item-conversacion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(64,109,115,0.04);
  transition: background .15s;
  position: relative;
}

.item-conversacion:hover { background: rgba(64,109,115,0.04); }

.item-conversacion.activa {
  background: rgba(64,109,115,0.08);
}

.item-conversacion.activa::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #406D73;
}

.avatar-mini-lista {
  width: 44px;
  height: 44px;
  min-width: 44px;
  background: linear-gradient(135deg, #B2C5C8, #9fb3b6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img-lista { width: 100%; height: 100%; object-fit: cover; }

.info-conversacion {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 2px;
}

.conv-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conv-footer-row {
  display: flex;
  align-items: center;
}

.info-conversacion .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #1a2e31;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.01em;
}

.info-conversacion .conv-time {
  font-size: 11px;
  color: #5a8a94;
  font-weight: 600;
  margin-left: 8px;
  flex-shrink: 0;
}

.info-conversacion .ultimo-msg {
  font-size: 12px;
  color: #7f9ea4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

/* Tareas */
.tarea-item { cursor: pointer; }

.tarea-item .tarea-avatar {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: transform .15s;
}

.tarea-item .tarea-avatar:hover { transform: scale(1.08); }
.tarea-item .tarea-info { display: flex; flex-direction: column; justify-content: center; }
.tarea-item .tarea-info .nombre { font-weight: 600; }
.tarea-item .tarea-info .ultimo-msg { font-size: 12px; color: #7f9ea4; }

/* Crear canal/tarea */
.canal-header-action { padding: 10px 16px 8px; }

.btn-crear-canal {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 14px;
  background: rgba(64,109,115,0.04);
  border: 1.5px dashed rgba(64,109,115,0.2);
  border-radius: 12px;
  color: #406D73;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.01em;
  transition: background .15s, border-color .15s;
}

.btn-crear-canal:hover {
  background: rgba(64,109,115,0.08);
  border-color: #406D73;
}

.sin-resultados {
  padding: 32px 16px;
  text-align: center;
  font-size: 13px;
  color: #a0b8bc;
}

/* ===== MODALES ===== */
.modal-nueva-conv {
  box-shadow: 0 16px 48px rgba(64,109,115,0.18) !important;
  background: #ffffff !important;
  border-radius: 20px !important;
  overflow: hidden !important;
}

.modal-titulo-conv {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-conv {
  padding: 16px !important;
  background: #f8fbfc !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
}

.modal-seccion-mejorada {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(64,109,115,0.1);
}

.label-input-conv {
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.input-modal-conv {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid rgba(64,109,115,0.15);
  border-radius: 10px;
  font-size: 13px;
  color: #1a2e31;
  background: #ffffff;
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  font-family: inherit;
  box-sizing: border-box;
}

.input-modal-conv:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.08);
}

.input-modal-conv::placeholder { color: rgba(64,109,115,0.4); }

.modal-busqueda-mejorada {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  border: 1px solid rgba(64,109,115,0.1);
  border-radius: 12px;
  padding: 8px 14px;
}

.modal-busqueda-mejorada input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #1a2e31;
  flex: 1;
  outline: none;
}

.modal-busqueda-mejorada input::placeholder { color: rgba(64,109,115,0.4); }

.modal-listado-mejorado {
  max-height: 300px;
  overflow-y: auto;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid rgba(64,109,115,0.08);
}

.item-usuario-mejorado {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background .12s;
  border-bottom: 1px solid rgba(64,109,115,0.04);
}

.item-usuario-mejorado:hover { background: rgba(179,235,242,0.12); }
.item-usuario-mejorado:last-child { border-bottom: none; }

.avatar-mini-modal {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: linear-gradient(135deg, #B2C5C8, #9fb3b6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.info-usuario-modal { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.info-usuario-modal .nombre { font-weight: 600; font-size: 14px; color: #1a2e31; }
.info-usuario-modal .email { font-size: 12px; color: #7f9ea4; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.modal-acciones-conv {
  padding: 12px 16px !important;
  background: #ffffff !important;
  border-top: 1px solid rgba(64,109,115,0.06) !important;
}

.btn-crear-grupo { font-size: 12px !important; font-weight: 600 !important; letter-spacing: 0.01em !important; }

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .panel-usuario-compact { padding: 10px 12px; }
  .user-avatar-wrap { width: 38px; height: 38px; min-width: 38px; font-size: 15px; }
  .tab-btn { font-size: 11px; }
  .item-conversacion { padding: 8px 12px; }
  .avatar-mini-lista { width: 40px; height: 40px; min-width: 40px; }
}

@media (max-width: 480px) {
  .panel-usuario-compact { padding: 8px 10px; }
  .user-avatar-wrap { width: 36px; height: 36px; min-width: 36px; }
  .item-conversacion { padding: 6px 10px; gap: 10px; }
  .avatar-mini-lista { width: 38px; height: 38px; min-width: 38px; }
  .seccion-titulo { font-size: 9px; }
}
</style>

<style>
/* Estilos globales para el menú flotante del perfil */
.menu-perfil-flotante {
  border-radius: 20px !important;
  overflow: hidden !important;
  box-shadow: 0 8px 32px rgba(64, 109, 115, 0.18) !important;
  border: 1px solid rgba(64, 109, 115, 0.1) !important;
  min-width: 220px !important;
}

.menu-perfil-flotante .v-list {
  padding: 8px 0 !important;
  background-color: #ffffff !important;
}

.menu-perfil-flotante .v-list-item {
  min-height: 36px !important;
  padding: 0 12px !important;
  transition: background 0.2s;
}

.menu-perfil-flotante .v-list-item:hover {
  background-color: #f0f7f8 !important;
}

.menu-perfil-flotante .v-list-item-title {
  font-size: 12px !important;
  font-weight: 500 !important;
  color: #2f4a4f !important;
}

.menu-perfil-flotante .text-error .v-list-item-title {
  color: #e57373 !important;
  font-weight: 600 !important;
}

.menu-perfil-flotante .v-list-item__prepend {
  margin-right: 10px !important;
}

.menu-perfil-flotante .v-icon {
  font-size: 16px !important;
  color: #5a8a94;
}

.menu-perfil-flotante .text-error .v-icon {
  color: #e57373 !important;
}

.menu-perfil-flotante .v-divider {
  margin: 4px 0 !important;
  opacity: 0.2 !important;
}

/* Scrollbar para la lista movido a global */
/* ---- Estilos Mejorados Modal Editar Perfil ---- */
.modal-editar-perfil {
  box-shadow: 0 10px 40px rgba(64, 109, 115, 0.15) !important;
  background: #ffffff !important;
  border-radius: 24px !important;
  overflow: hidden !important;
}

.modal-titulo-perfil {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  border-radius: 24px 24px 0 0 !important;
}

.modal-contenido-perfil {
  padding: 16px !important;
  background: #f7fcfd !important;
  border-radius: 0 0 24px 24px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
}

.campo-edicion {
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
}

.label-input-mejorado {
  display: flex !important;
  align-items: center !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  color: #406D73 !important;
  letter-spacing: 0.02em !important;
  text-transform: uppercase !important;
  margin-bottom: 4px !important;
}

.input-modal-mejorado {
  width: 100% !important;
  padding: 10px 12px !important;
  border: 1px solid rgba(64, 109, 115, 0.2) !important;
  border-radius: 12px !important;
  font-size: 12px !important;
  color: #2f4a4f !important;
  background: #ffffff !important;
  outline: none !important;
  transition: all 0.2s !important;
  resize: vertical !important;
  font-family: inherit !important;
}

.input-modal-mejorado:focus {
  border-color: #406D73 !important;
  box-shadow: 0 0 0 3px rgba(64, 109, 115, 0.1) !important;
}

.input-modal-mejorado::placeholder {
  color: rgba(64, 109, 115, 0.4) !important;
}

.modal-acciones-perfil {
  padding: 12px 16px !important;
  background: #ffffff !important;
  border-top: 1px solid rgba(64, 109, 115, 0.08) !important;
  border-radius: 0 0 16px 16px !important;
}

.btn-guardar-perfil {
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.02em !important;
}
</style>

<style>
/* Estilos globales para los menús flotantes para que no tengan barras de desplazamiento y se vean mejor */
.menu-perfil-flotante, .menu-mensaje-flotante {
  border-radius: 12px !important;
  overflow: hidden !important;
  box-shadow: 0 8px 24px rgba(64, 109, 115, 0.15) !important;
  border: 1px solid rgba(64, 109, 115, 0.08) !important;
}
.menu-perfil-flotante .v-list, .menu-mensaje-flotante .v-list {
  padding: 4px 0 !important;
  overflow: hidden !important; /* quita el scrollbar */
}
.menu-perfil-flotante .v-list-item-title, .menu-mensaje-flotante .v-list-item-title {
  font-size: 13px !important;
  font-weight: 600 !important;
  color: #2f4a4f !important;
}
.menu-mensaje-flotante .v-list-item-title {
  font-size: 12px !important;
}
.menu-perfil-flotante .v-list-item__prepend > .v-icon, .menu-mensaje-flotante .v-list-item__prepend > .v-icon {
  font-size: 18px !important;
  color: #406D73 !important;
  opacity: 0.8 !important;
  margin-inline-end: 12px !important;
}
.menu-perfil-flotante .text-error .v-list-item-title,
.menu-perfil-flotante .text-error .v-icon,
.menu-perfil-flotante .text-error .v-list-item__prepend > .v-icon,
.menu-mensaje-flotante .text-error .v-list-item-title,
.menu-mensaje-flotante .text-error .v-icon,
.menu-mensaje-flotante .text-error .v-list-item__prepend > .v-icon {
  color: #d32f2f !important;
}
.menu-perfil-flotante .v-list-item, .menu-mensaje-flotante .v-list-item {
  min-height: 36px !important;
  padding: 4px 16px !important;
}
</style>
