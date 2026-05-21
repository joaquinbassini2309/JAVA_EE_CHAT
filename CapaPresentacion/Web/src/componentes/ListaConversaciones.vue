<template>
  <div class="lista-conversaciones">

    <!-- Panel usuario con banner + avatar cuadrado -->
    <div class="panel-usuario">
      <div
          class="profile-banner profile-banner--default"
          :style="usuarioActual?.imagenBanner ? { backgroundImage: `url('${usuarioActual.imagenBanner}')` } : {}"
      />
      <div class="profile-lower">
        <div class="profile-avatar-wrap">
          <v-menu offset-y content-class="menu-perfil-flotante" transition="slide-y-transition">
            <template v-slot:activator="{ props }">
              <div class="avatar-cuadrado cursor-pointer" v-bind="props" v-ripple>
                <img v-if="usuarioActual?.fotoUrl" :src="usuarioActual.fotoUrl" class="avatar-img" />
                <span v-else>{{ usuarioActual?.username?.charAt(0).toUpperCase() }}</span>
              </div>
            </template>
            <v-list class="bg-surface menu-perfil" density="compact">
              <v-list-item @click="abrirEdicionPerfil('username')" prepend-icon="mdi-account-edit">
                <v-list-item-title>Cambiar nombre de usuario</v-list-item-title>
              </v-list-item>
              <v-list-item @click="abrirEdicionPerfil('fotoUrl')" prepend-icon="mdi-camera">
                <v-list-item-title>Cambiar Foto de perfil</v-list-item-title>
              </v-list-item>
              <v-list-item @click="abrirEdicionPerfil('imagenBanner')" prepend-icon="mdi-wallpaper">
                <v-list-item-title>Colocar imagen de banner</v-list-item-title>
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
        </div>
        <div class="profile-title-row">
          <span class="profile-name text-truncate">{{ usuarioActual?.username }}</span>
          <div class="acciones-perfil">
            <v-btn icon="mdi-account-multiple-plus" variant="flat" color="accent" size="small" density="comfortable" @click="abrirNuevoGrupo" title="Nuevo grupo" />
            <v-btn icon="mdi-plus-circle" variant="flat" color="accent" size="small" density="comfortable" @click="abrirNuevaConversacion" title="Nueva conversación" />
            <v-btn icon="mdi-logout" variant="flat" color="error" size="small" density="comfortable" @click="cerrarSesionLocal" title="Cerrar sesión" />
          </div>
        </div>
        <!-- profile subtitle removed -->

        <!-- Buscador y Filtro -->
        <div class="campo-busqueda" style="margin-top: 10px; padding-left: 4px;">
          <v-menu :close-on-content-click="false" location="bottom start">
            <template v-slot:activator="{ props }">
              <button 
                v-bind="props" 
                title="Filtrar conversaciones"
                style="display: flex; align-items: center; justify-content: center; background: none; border: none; cursor: pointer; color: #406D73; padding: 4px; border-radius: 6px; transition: background 0.2s;"
                onmouseover="this.style.background='rgba(64,109,115,0.1)'"
                onmouseout="this.style.background='none'"
              >
                <v-icon size="20">mdi-filter-variant</v-icon>
              </button>
            </template>
            <v-card min-width="220" rounded="2xl" class="border-sm border-opacity-25" style="border-color: #406D73 !important; overflow: hidden; box-shadow: 0 8px 24px rgba(64,109,115,0.15) !important;">
              <v-card-title class="modal-titulo-mejorado px-4 py-3" style="background-color: #406D73; color: white; font-size: 14px;">
                <v-icon size="16" class="mr-2">mdi-filter-variant</v-icon>
                Mostrar en lista
              </v-card-title>
              <v-list density="compact" class="py-2" bg-color="#ffffff" style="overflow-x: hidden;">
                <template v-if="tabActivo === 'chats'">
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosSeleccionados" label="Contactos" value="PRIVADA" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosSeleccionados" label="Grupos" value="GRUPO" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosSeleccionados" label="Canales de aviso" value="AVISO" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosSeleccionados" label="Lista de tareas" value="TAREAS" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                </template>
                <template v-else-if="tabActivo === 'tareas'">
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosTareas" label="Tareas pendientes" value="PENDIENTES" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                  <v-list-item>
                    <v-checkbox-btn v-model="filtrosTareas" label="Tareas completadas" value="COMPLETADAS" color="#406D73" hide-details></v-checkbox-btn>
                  </v-list-item>
                </template>
                <template v-else>
                  <v-list-item>
                    <v-list-item-title class="text-caption text-center text-grey my-2">Solo filtro por texto</v-list-item-title>
                  </v-list-item>
                </template>
              </v-list>
            </v-card>
          </v-menu>
          <div style="width: 1px; height: 16px; background: rgba(64,109,115,0.2); margin: 0 2px;"></div>
          <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
          <input v-model="termino" type="text" placeholder="Buscar contactos" style="flex: 1;" />
        </div>
      </div>
    </div>

    <!-- Tabs Mis Chats / Ver canales / Lista de tareas -->
    <div class="tabs-row">
      <button class="tab-btn" :class="{ activo: tabActivo === 'chats' }" @click="cambiarTab('chats')">
        Mis Chats
      </button>
      <button class="tab-btn" :class="{ activo: tabActivo === 'canales' }" @click="cambiarTab('canales')">
        Ver canales de aviso
      </button>
      <button class="tab-btn" :class="{ activo: tabActivo === 'tareas' }" @click="cambiarTab('tareas')">
        Lista de tareas
      </button>
    </div>

    <!-- Subheader lista -->
    <div class="seccion-titulo text-uppercase">
      {{ tabActivo === 'chats' ? 'LISTA DE USUARIO Y MIEMBROS PARA HABLAR' : tabActivo === 'canales' ? 'CANALES DE AVISOS PÚBLICOS' : 'LISTA DE TAREAS' }}
    </div>

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
            <div class="d-flex align-center">
              <span class="nombre text-truncate mr-1" style="max-width: 140px;">{{ obtenerNombreVisibleConversacion(conversacion, usuarioActual?.id) }}</span>
              <v-chip v-if="conversacion.tipo === 'AVISO'" color="error" size="x-small" density="compact" variant="flat" class="px-1 text-uppercase ml-1" style="font-size: 8px; height: 14px; line-height: 14px;">Aviso</v-chip>
            </div>
            <span class="ultimo-msg text-truncate">
              <template v-if="String(conversacion.id).startsWith('tareas_')">
                {{ tareasPendientesCount === 1 ? '1 tarea pendiente' : (tareasPendientesCount + ' tareas pendientes') }}
              </template>
              <template v-else>
                {{ conversacion.ultimoMensaje || '...' }}
              </template>
            </span>
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

      <!-- Tab Tareas -->
      <template v-else>
        <div class="canal-header-action">
          <button class="btn-crear-canal" @click="abrirModalTarea">
            <v-icon size="16" class="mr-1">mdi-plus-circle-outline</v-icon>
            Crear nueva tarea
          </button>
        </div>

        <div
            v-for="tarea in tareasFiltradas"
            :key="tarea.id"
            class="item-conversacion tarea-item"
        >
          <div class="item-left" style="display:flex;align-items:center;gap:12px;flex:1;min-width:0;">
            <div class="avatar-mini-lista tarea-avatar" :style="tarea.completada ? 'background: #6A9E7D; color: white;' : 'background: rgba(64,109,115,0.12); color: #406D73;'" @click.stop="toggleCompletadaLista(tarea)">
              <v-icon size="18">{{ tarea.completada ? 'mdi-check' : 'mdi-format-list-bulleted' }}</v-icon>
            </div>
            <div class="info-conversacion tarea-info" style="flex: 1; min-width: 0;">
              <div class="nombre text-truncate" :style="tarea.completada ? 'text-decoration: line-through; opacity: 0.6;' : ''">{{ tarea.titulo || tarea.contenido || 'Sin título' }}</div>
              <div class="ultimo-msg text-truncate">{{ tarea.fechaVencimiento ? 'Vence: ' + formatearFecha(tarea.fechaVencimiento) : 'Sin fecha de vencimiento' }}</div>
            </div>
          </div>
        </div>
        <div v-if="tareasFiltradas.length === 0" class="sin-resultados d-flex flex-column align-center justify-center pa-6">
          <v-icon size="36" color="#a0b8bc" class="mb-2">mdi-clipboard-check-outline</v-icon>
          <span>Aún no hay tareas creadas</span>
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

    <!-- Modal Nueva Tarea -->
    <v-dialog v-model="mostrarModalTarea" max-width="420">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">mdi-checkbox-marked-circle-outline</v-icon>
          Nueva Tarea
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModalTarea" />
        </v-card-title>
        <v-card-text class="modal-contenido-conv">
          <div class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-format-title</v-icon>
              Nombre
            </label>
            <input ref="inputNuevaTareaTitulo" v-model.trim="nuevaTareaTitulo" type="text" placeholder="Ej: Revisar el informe" class="input-modal-conv" />
          </div>
          <div class="modal-seccion-mejorada mt-4">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-text</v-icon>
              Contenido
            </label>
            <textarea v-model="nuevaTareaContenido" placeholder="Detalles de la tarea..." class="input-modal-conv" rows="2" style="resize:none; padding:10px; font-family: inherit; width: 100%; border: 1px solid #B2C5C8; border-radius: 8px;"></textarea>
          </div>
          <div class="modal-seccion-mejorada mt-4">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-calendar</v-icon>
              Fecha de vencimiento (Opcional)
            </label>
            <input type="date" v-model="nuevaTareaFecha" class="input-modal-conv" />
          </div>
        </v-card-text>
        <v-card-actions class="modal-acciones-conv" style="justify-content: flex-end;">
          <v-spacer />
          <v-btn color="accent" variant="tonal" rounded="lg" size="small" :disabled="!nuevaTareaTitulo.trim()" :loading="creandoTareaLocal" prepend-icon="mdi-check-circle" @click="crearTareaLocal">
            Crear Tarea
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
import { formatearFecha } from '@/utilidades/formateoFechas'
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
const filtrosTareas = ref(['PENDIENTES', 'COMPLETADAS'])
let intervaloRefresco = null

// Canales de Avisos
const canalesAvisos = ref([])
const uniendoseCanalId = ref(null)
const mostrarModalCanal = ref(false)
const nombreCanal = ref('')
const creandoCanal = ref(false)
const terminoCanalUsuario = ref('')
const seleccionadosCanal = ref([])
const usuariosCanal = ref([])
const tareasCargando = ref(false)

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
    if (String(c.id).startsWith('tareas_')) {
      return filtrosSeleccionados.value.includes('TAREAS')
    }
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

const tareasFiltradas = computed(() => {
  let lista = tareasDelUsuario.value
  
  lista = lista.filter(t => {
    if (t.completada && filtrosTareas.value.includes('COMPLETADAS')) return true;
    if (!t.completada && filtrosTareas.value.includes('PENDIENTES')) return true;
    return false;
  })

  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(tItem => tItem.contenido.toLowerCase().includes(t))
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
    // Limpiar mensajes inmediatamente para no mostrar mensajes de la conversación anterior
    almacen.establecerMensajes([])
    almacen.establecerConversacionActual(conversacion)

    // Si se selecciona la conversación sintética de tareas, asegurarse de cargar las tareas locales
    if (String(conversacion.id).startsWith('tareas_')) {
        try {
            if (!usuarioActual.value) return
            const userId = usuarioActual.value.id
            const tareas = await servicioApi.obtenerTareas(userId)

            const mensajesTareas = tareas.map(t => ({
                id: t.id,
                conversacionId: conversacion.id,
                emisorId: t.emisorId,
                titulo: t.titulo,
                contenido: t.contenido,
                fechaEnvio: t.fechaEnvio,
                tipo: 'TAREA',
                fechaVencimiento: t.fechaVencimiento,
                completada: !!t.completada
            }))

            almacen.establecerMensajes(mensajesTareas)
            // Marcar la pestaña como 'tareas' para mantener coherencia visual
            tabActivo.value = 'tareas'
        } catch (error) {
            console.error('Error cargando tareas al seleccionar conversación:', error)
        }
    } else {
        // Cualquier otra conversación debe deseleccionar la pestaña de tareas
        tabActivo.value = 'chats'
    }
}

const cargarConversaciones = async () => {
    try {
        const convs = await servicioApi.obtenerConversaciones()
        // Preservar cualquier conversación sintética de tareas que exista localmente
        const sinteticas = almacen.conversaciones.filter(c => String(c.id).startsWith('tareas_'))
        // Filtrar duplicados entre lo obtenido del servidor y las sintéticas
        const convsFiltradas = [...convs]
        sinteticas.forEach(s => {
            if (!convsFiltradas.find(c => c.id === s.id)) {
                convsFiltradas.unshift(s)
            }
        })

        almacen.establecerConversaciones(convsFiltradas)
    } catch (error) {
        console.error('Error al refrescar conversaciones:', error)
    }
}

onMounted(() => {
    cargarConversaciones()
    intervaloRefresco = setInterval(cargarConversaciones, 5000)
    // Si la app inicia en pestaña tareas
    if (tabActivo.value === 'tareas') abrirListaTareas()
})

// Nuevo: abrir lista de tareas como conversación conmigo mismo
const abrirListaTareas = async () => {
    if (!usuarioActual.value) return
    tareasCargando.value = true
    try {
        const userId = usuarioActual.value.id
        const tareas = await servicioApi.obtenerTareas(userId)

        // Crear conversacion sintética para tareas
        const convId = `tareas_${userId}`
        const conv = {
            id: convId,
            tipo: 'PRIVADA',
            nombre: 'Lista de tareas',
            participanteIds: [userId],
            ultimoMensaje: tareas.length ? (tareas[tareas.length - 1].contenido || '') : null,
            fechaUltimoMensaje: tareas.length ? tareas[tareas.length - 1].fechaEnvio : null,
            fechaCreacion: new Date().toISOString()
        }

        // Añadir la conversación sintética al almacen si no existe para que aparezca en la lista
        const existe = almacen.conversaciones.find(c => c.id === convId)
        if (!existe) {
            almacen.agregarConversacion(conv)
        } else {
            // Si existe, actualizar sus metadatos relevantes
            existe.ultimoMensaje = conv.ultimoMensaje
            existe.fechaUltimoMensaje = conv.fechaUltimoMensaje
        }

        // Establecer en el almacen
        almacen.establecerConversacionActual(conv)

        // Mapear tareas a mensajes que el componente Chat entiende
        const mensajesTareas = tareas.map(t => ({
            id: t.id,
            conversacionId: convId,
            emisorId: t.emisorId,
            titulo: t.titulo,
            contenido: t.contenido,
            fechaEnvio: t.fechaEnvio,
            tipo: 'TAREA',
            fechaVencimiento: t.fechaVencimiento,
            completada: !!t.completada
        }))

        almacen.establecerMensajes(mensajesTareas)
    } catch (error) {
        console.error('No se pudo cargar lista de tareas:', error)
    } finally {
        tareasCargando.value = false
    }
}

// Cuando el usuario cambia de pestaña
watch(tabActivo, (nuevo) => {
    if (nuevo === 'tareas') {
        abrirListaTareas()
    }
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

const tareasDelUsuario = computed(() => {
  if (tabActivo.value !== 'tareas') return []
  return almacen.mensajes.filter(m => m.tipo === 'TAREA')
})

// Contador de tareas pendientes (para mostrar en la lista de conversaciones)
const tareasPendientesCount = ref(0)

const refrescarContadorTareas = async () => {
  try {
    if (!usuarioActual.value) { tareasPendientesCount.value = 0; return }
    const todas = await servicioApi.obtenerTareas(usuarioActual.value.id)
    tareasPendientesCount.value = todas.filter(t => !t.completada).length
  } catch (e) {
    console.error('Error al obtener tareas para contador:', e)
    tareasPendientesCount.value = 0
  }
}

onMounted(() => {
  // refrescar contador al montar
  refrescarContadorTareas()
})

// Actualizar contador cuando cambian las tareas en el almacen o cambia el usuario
watch(() => almacen.mensajes, () => {
  // Si hay mensajes de tipo TAREA en el almacen, recalcular
  if (almacen.mensajes.some(m => m.tipo === 'TAREA')) refrescarContadorTareas()
}, { deep: true })

watch(usuarioActual, () => {
  refrescarContadorTareas()
})

// Estado para Modal Tarea Local
const mostrarModalTarea = ref(false)
const nuevaTareaTitulo = ref('')
const nuevaTareaContenido = ref('')
const nuevaTareaFecha = ref('')
const creandoTareaLocal = ref(false)

const abrirModalTarea = () => {
  mostrarModalTarea.value = true
  nuevaTareaTitulo.value = ''
  nuevaTareaContenido.value = ''
  nuevaTareaFecha.value = ''
}

const cerrarModalTarea = () => {
  mostrarModalTarea.value = false
  nuevaTareaTitulo.value = ''
  nuevaTareaContenido.value = ''
  nuevaTareaFecha.value = ''
}

const crearTareaLocal = async () => {
  if (!nuevaTareaTitulo.value.trim() || !usuarioActual.value) return
  creandoTareaLocal.value = true
  try {
    const nueva = await servicioApi.crearTarea(
      usuarioActual.value.id,
      nuevaTareaTitulo.value.trim(),
      nuevaTareaContenido.value.trim(),
      nuevaTareaFecha.value || null
    )
    
    const convId = `tareas_${usuarioActual.value.id}`
    const nuevaComoMensaje = {
      id: nueva.id,
      conversacionId: convId,
      emisorId: nueva.emisorId || usuarioActual.value.id,
      titulo: nueva.titulo,
      contenido: nueva.contenido,
      fechaEnvio: nueva.fechaEnvio,
      tipo: 'TAREA',
      fechaVencimiento: nueva.fechaVencimiento || null,
      completada: !!nueva.completada
    }

    almacen.establecerMensajes([...(almacen.mensajes || []), nuevaComoMensaje])
    
    const convIdx = almacen.conversaciones.findIndex(c => String(c.id) === convId)
    if (convIdx !== -1) {
      const conv = almacen.conversaciones[convIdx]
      conv.ultimoMensaje = nueva.contenido
      conv.fechaUltimoMensaje = nueva.fechaEnvio
      almacen.establecerConversaciones([...almacen.conversaciones])
    }

    refrescarContadorTareas()
    cerrarModalTarea()
  } catch (error) {
    console.error('Error al crear tarea:', error)
  } finally {
    creandoTareaLocal.value = false
  }
}

const toggleCompletadaLista = async (tarea) => {
  if (!usuarioActual.value) return
  try {
    const nuevoEstado = !tarea.completada
    tarea.completada = nuevoEstado
    await servicioApi.actualizarEstadoTarea(usuarioActual.value.id, tarea.id, nuevoEstado)
    refrescarContadorTareas()
  } catch (error) {
    console.error('Error al actualizar estado de la tarea:', error)
    tarea.completada = !tarea.completada
  }
}
</script>

<style scoped>
/* ---- Contenedor raíz ---- */
.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0; /* Crucial para scroll */
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
  overflow: hidden;
  transition: transform 0.2s;
}

.avatar-cuadrado:hover {
  transform: scale(1.05);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  min-height: 0; /* Crucial para scroll */
  background: #ffffff;
}

/* Ajustes específicos para items de la lista de tareas: alinear título/contenido a la derecha del avatar */
.tarea-item {
  cursor: pointer;
}
.tarea-item .tarea-avatar {
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tarea-item .tarea-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.tarea-item .tarea-info .nombre {
  font-weight: 600;
}
.tarea-item .tarea-info .ultimo-msg {
  font-size: 12px;
  color: #7f9ea4;
}

/* ---- Botón crear canal dentro del tab ---- */
.canal-header-action {
  padding: 10px 14px 6px;
  border-bottom: 1px solid rgba(64,109,115,0.07);
}

.btn-crear-canal {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 12px;
  background: rgba(64,109,115,0.06);
  border: 1.5px dashed rgba(64,109,115,0.3);
  border-radius: 10px;
  color: #406D73;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.02em;
  transition: background 0.15s, border-color 0.15s;
}

.btn-crear-canal:hover {
  background: rgba(64,109,115,0.12);
  border-color: #406D73;
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
  overflow: hidden;
}

.avatar-img-lista {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  resize: vertical;
}

.label-input {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #406D73;
  margin-bottom: 6px;
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

/* ---- Estilos Mejorados Modal Nueva Conversación/Grupo ---- */
.modal-nueva-conv {
  box-shadow: 0 10px 40px rgba(64, 109, 115, 0.15) !important;
  background: #ffffff !important;
  border-radius: 24px !important;
  overflow: hidden !important;
}

.modal-titulo-conv {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  border-radius: 24px 24px 0 0 !important;
}

.modal-contenido-conv {
  padding: 16px !important;
  background: #f7fcfd !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
  border-radius: 0 0 24px 24px !important;
}

.modal-seccion-mejorada {
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
  padding: 12px !important;
  background: #ffffff !important;
  border-radius: 10px !important;
  border: 1px solid rgba(64, 109, 115, 0.15) !important;
}

.label-input-conv {
  display: flex !important;
  align-items: center !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  color: #406D73 !important;
  letter-spacing: 0.02em !important;
  text-transform: uppercase !important;
}

.input-modal-conv {
  width: 100% !important;
  padding: 10px 12px !important;
  border: 1px solid rgba(64, 109, 115, 0.2) !important;
  border-radius: 8px !important;
  font-size: 12px !important;
  color: #2f4a4f !important;
  background: #ffffff !important;
  outline: none !important;
  transition: all 0.2s !important;
  font-family: inherit !important;
}

.input-modal-conv:focus {
  border-color: #406D73 !important;
  box-shadow: 0 0 0 3px rgba(64, 109, 115, 0.1) !important;
}

.input-modal-conv::placeholder {
  color: rgba(64, 109, 115, 0.4) !important;
}

.modal-busqueda-mejorada {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  background: #ffffff !important;
  border: 1px solid rgba(64, 109, 115, 0.15) !important;
  border-radius: 10px !important;
  padding: 8px 12px !important;
}

.modal-busqueda-mejorada input {
  border: none !important;
  background: transparent !important;
  font-size: 12px !important;
  color: #2f4a4f !important;
  flex: 1 !important;
  outline: none !important;
}

.modal-busqueda-mejorada input::placeholder {
  color: rgba(64, 109, 115, 0.4) !important;
}

.modal-listado-mejorado {
  max-height: 300px !important;
  overflow-y: auto !important;
  border-radius: 10px !important;
  background: #ffffff !important;
  border: 1px solid rgba(64, 109, 115, 0.1) !important;
}

.item-usuario-mejorado {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 10px 12px !important;
  cursor: pointer !important;
  transition: background 0.12s !important;
  border-bottom: 1px solid rgba(64, 109, 115, 0.05) !important;
}

.item-usuario-mejorado:hover {
  background: rgba(179, 235, 242, 0.15) !important;
}

.item-usuario-mejorado:last-child {
  border-bottom: none !important;
}

.modal-acciones-conv {
  padding: 12px 16px !important;
  background: #ffffff !important;
  border-top: 1px solid rgba(64, 109, 115, 0.08) !important;
  border-radius: 0 0 16px 16px !important;
}

.btn-crear-grupo {
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.02em !important;
}

/* ---- Media Queries para Responsividad ---- */
@media (max-width: 768px) {
  .profile-lower {
    padding-left: 80px;
    min-height: 80px;
  }

  .profile-avatar-wrap {
    left: 10px;
    top: -28px;
  }

  .avatar-cuadrado {
    width: 56px;
    height: 56px;
  }

  .profile-banner {
    height: 70px;
  }

  .acciones-perfil {
    gap: 2px;
  }

  .campo-busqueda {
    margin-top: 8px;
  }

  .tabs-row {
    gap: 0;
  }

  .tab-btn {
    font-size: 11px;
  }

  .item-conversacion {
    padding: 8px 12px;
    gap: 10px;
  }

  .avatar-mini-lista {
    width: 36px;
    height: 36px;
  }

  .seccion-titulo {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .profile-lower {
    padding-left: 70px;
    min-height: 76px;
  }

  .profile-avatar-wrap {
    left: 8px;
    top: -24px;
  }

  .avatar-cuadrado {
    width: 48px;
    height: 48px;
    border: 2px solid #ffffff;
  }

  .profile-banner {
    height: 60px;
  }

  .acciones-perfil {
    gap: 2px;
  }

  .campo-busqueda {
    margin-top: 6px;
    gap: 4px;
  }

  .item-conversacion {
    padding: 6px 10px;
    gap: 8px;
  }

  .avatar-mini-lista {
    width: 34px;
    height: 34px;
  }

  .seccion-titulo {
    font-size: 9px;
  }

  .avatar-mini-modal {
    width: 32px;
    height: 32px;
  }

  .item-usuario-modal {
    padding: 8px 12px;
    gap: 10px;
  }
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
