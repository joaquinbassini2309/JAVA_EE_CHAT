<template>
  <div class="info-grupo">

    <!-- Header Compacto Estilo Telegram/Slack -->
    <div class="info-header-compact">
      <button class="btn-atras" @click="$emit('volver')" title="Volver">
        <v-icon size="20" color="#406D73">mdi-arrow-left</v-icon>
      </button>

      <div class="header-avatar-wrap">
        <div class="header-avatar-info">
          <template v-if="!esGrupoOCanal && otroUsuario?.fotoUrl">
            <img :src="otroUsuario.fotoUrl" class="header-avatar-img" />
          </template>
          <template v-else-if="esAviso">
            <v-icon size="24" color="white">mdi-bullhorn</v-icon>
          </template>
          <template v-else-if="conversacion.fotoUrl">
            <img :src="conversacion.fotoUrl" class="header-avatar-img" />
          </template>
          <template v-else>
            {{ conversacion.nombre?.charAt(0).toUpperCase() || 'G' }}
          </template>
        </div>
      </div>

      <div class="header-info-content">
        <!-- Título editable -->
        <v-text-field
            v-if="editandoNombre"
            v-model="nuevoNombre"
            variant="underlined"
            density="compact"
            autofocus
            hide-details
            @keyup.enter="guardarNuevoNombre"
            @blur="guardarNuevoNombre"
            class="input-nombre-grupo"
        />
        <span v-else class="info-name text-truncate">{{ conversacion.nombre }}</span>
        
        <div class="info-subtitle">
          {{ esAviso ? '📣 Canal de Avisos' : '' }}{{ conversacion.participantes?.length || 0 }} miembros
        </div>
      </div>

      <div class="header-actions">
        <v-btn
            v-if="puedeGestionar && !editandoNombre && !esAviso"
            icon="mdi-pencil"
            variant="text"
            color="#406D73"
            size="small"
            @click="editandoNombre = true"
            class="header-btn"
        />
        <v-menu v-if="puedeGestionar" content-class="menu-perfil-flotante" transition="scale-transition">
          <template v-slot:activator="{ props }">
            <v-btn icon="mdi-dots-vertical" color="#406D73" variant="text" size="small" class="header-btn" v-bind="props" />
          </template>
          <v-list>
            <v-list-item @click="editandoNombre = true" prepend-icon="mdi-pencil">
              <v-list-item-title>Cambiar nombre</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionGrupo('fotoUrl')" prepend-icon="mdi-camera">
              <v-list-item-title>Cambiar foto de grupo</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </div>

    <!-- Cuerpo scrolleable -->
    <div class="info-cuerpo">

      <!-- Sección integrantes (grupos y canales de avisos) -->
      <div v-if="esGrupoOCanal" class="seccion">
        <div class="seccion-titulo">
          <v-icon size="16" color="#406D73" class="mr-1">{{ esAviso ? 'mdi-bullhorn' : 'mdi-account-group' }}</v-icon>
          {{ esAviso ? 'Miembros del Canal' : 'Integrantes' }}
        </div>
        <div class="lista-miembros">
          <div v-for="miembro in conversacion.participantes" :key="miembro.usuario.id" class="item-miembro">
            <div class="avatar-mini">
              <img v-if="miembro.usuario.fotoUrl" :src="miembro.usuario.fotoUrl" alt="avatar" />
              <span v-else>{{ miembro.usuario.username.charAt(0).toUpperCase() }}</span>
            </div>

            <div class="info-usuario">
              <span class="nombre">{{ miembro.usuario.username }}</span>
              <div class="roles-wrapper">
                <v-chip v-if="miembro.rol === 'ADMIN'" color="primary" size="x-small" label>Admin</v-chip>
                <v-chip v-if="miembro.rol === 'MODERADOR'" color="secondary" size="x-small" label>Mod</v-chip>
                <v-chip v-if="miembro.rol === 'SILENCIADO'" color="warning" size="x-small" label>Silenciado</v-chip>
              </div>
            </div>

            <!-- Menú de acciones para el Admin -->
            <v-menu v-if="esAdmin && usuarioActual.id !== miembro.usuario.id" content-class="menu-perfil-flotante" transition="scale-transition">
              <template v-slot:activator="{ props }">
                <v-btn icon="mdi-dots-vertical" variant="text" size="small" v-bind="props" />
              </template>
              <v-list>
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'ADMIN')" v-if="miembro.rol !== 'ADMIN'">
                  <v-list-item-title>Nombrar Administrador</v-list-item-title>
                </v-list-item>
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'MODERADOR')" v-if="miembro.rol !== 'MODERADOR'">
                  <v-list-item-title>Nombrar Moderador</v-list-item-title>
                </v-list-item>
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'MIEMBRO')" v-if="miembro.rol !== 'MIEMBRO'">
                  <v-list-item-title>Nombrar Miembro</v-list-item-title>
                </v-list-item>
                <v-divider />
                <v-list-item @click="cambiarRol(miembro.usuario.id, miembro.rol === 'SILENCIADO' ? 'MIEMBRO' : 'SILENCIADO')" :class="miembro.rol === 'SILENCIADO' ? 'text-success' : 'text-warning'">
                  <v-list-item-title>{{ miembro.rol === 'SILENCIADO' ? 'Desilenciar' : 'Silenciar' }}</v-list-item-title>
                </v-list-item>
                <v-list-item @click="abrirConfirmacionEliminar(miembro.usuario)" class="text-error">
                  <v-list-item-title>Eliminar del grupo</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>
        </div>
      </div>

      <!-- Sección de perfil de usuario (solo chat privado) -->
      <div v-else-if="!esGrupoOCanal && otroUsuario" class="seccion-perfil">
        <div class="seccion">
          <div class="seccion-titulo">
            <v-icon size="16" color="#406D73" class="mr-1">mdi-account-details</v-icon>
            Descripción
          </div>
          <p class="descripcion-usuario">{{ otroUsuario.descripcion || 'Sin descripción' }}</p>
        </div>

        <div class="seccion" style="margin-top: 16px;">
          <div class="seccion-titulo">
            <v-icon size="16" color="#406D73" class="mr-1">mdi-account-group</v-icon>
            Grupos en común
          </div>
          <div class="lista-miembros" v-if="gruposEnComun.length > 0">
            <div v-for="grupo in gruposEnComun" :key="grupo.id" class="item-miembro">
              <div class="avatar-mini">{{ grupo.nombre.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario">
                <span class="nombre">{{ grupo.nombre }}</span>
                <span class="email" style="font-size: 11px; color: #999;">{{ grupo.participantes?.length || 0 }} miembros</span>
              </div>
            </div>
          </div>
          <p v-else class="descripcion-usuario">No hay grupos en común</p>
        </div>
      </div>
    </div>

    <!-- Modal Editar Info de Grupo -->
    <v-dialog v-model="mostrarModalEdicion" max-width="420">
      <v-card rounded="2xl" class="modal-editar-perfil">
        <v-card-title class="modal-titulo-perfil">
          <v-icon size="18" color="white" class="mr-2">
            {{ tipoEdicion === 'fotoUrl' ? 'mdi-camera' : 'mdi-wallpaper' }}
          </v-icon>
          {{ tituloModal }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalEdicion = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-perfil">
          <div class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-link</v-icon>
              {{ tipoEdicion === 'fotoUrl' ? 'URL de la foto' : 'URL del banner' }}
            </label>
            <input v-model="valorEdicion" type="text" class="input-modal-mejorado" placeholder="https://ejemplo.com/imagen.jpg" />
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
              @click="guardarInfoGrupo"
              :loading="guardandoInfo"
              class="btn-guardar-perfil"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Confirmar Eliminación -->
    <v-dialog v-model="mostrarModalEliminar" max-width="400">
      <v-card rounded="2xl">
        <v-card-title style="font-size: 1rem; font-weight: 700; color: #d32f2f;">
          Eliminar Usuario
        </v-card-title>
        <v-card-text>
          ¿Estás seguro de que deseas eliminar a <strong>{{ usuarioAEliminar?.username }}</strong> del grupo?
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="mostrarModalEliminar = false">Cancelar</v-btn>
          <v-btn color="error" variant="flat" @click="confirmarEliminarMiembro" :loading="eliminandoMiembro">Aceptar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const props = defineProps({
  conversacion: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['volver'])
const almacen = useAlmacen()

const editandoNombre = ref(false)
const nuevoNombre = ref(props.conversacion.nombre)

// Modal Edicion Grupo
const mostrarModalEdicion = ref(false)
const tipoEdicion = ref('')
const valorEdicion = ref('')
const tituloModal = ref('')
const guardandoInfo = ref(false)

// Confirmacion de eliminacion
const mostrarModalEliminar = ref(false)
const usuarioAEliminar = ref(null)
const eliminandoMiembro = ref(false)

const usuarioActual = computed(() => almacen.usuarioActual)

const esGrupo = computed(() => props.conversacion.tipo === 'GRUPO')
const esAviso = computed(() => props.conversacion.tipo === 'AVISO')
const esGrupoOCanal = computed(() => esGrupo.value || esAviso.value)

const otroUsuario = computed(() => {
  if (esGrupoOCanal.value) return null
  const participante = props.conversacion.participantes?.find(p => p.usuario.id !== usuarioActual.value?.id)
  return participante?.usuario
})

const gruposEnComun = computed(() => {
  const convs = almacen.conversaciones || [];
  return convs.filter(c =>
      c.tipo === 'GRUPO' &&
      c.participantes?.some(p => p.usuario.id === usuarioActual.value?.id) &&
      c.participantes?.some(p => p.usuario.id === otroUsuario.value?.id)
  );
})

const rolUsuarioActual = computed(() => {
  const participante = props.conversacion.participantes?.find(p => p.usuario.id === usuarioActual.value?.id)
  return participante?.rol
})

const esAdmin = computed(() => rolUsuarioActual.value === 'ADMIN')
const puedeGestionar = computed(() => esAdmin.value || rolUsuarioActual.value === 'MODERADOR')

async function guardarNuevoNombre() {
  if (!editandoNombre.value) return
  editandoNombre.value = false
  if (nuevoNombre.value.trim() && nuevoNombre.value !== props.conversacion.nombre) {
    try {
      await servicioApi.actualizarConversacion(props.conversacion.id, nuevoNombre.value)
      almacen.actualizarInfoConversacion(props.conversacion.id, nuevoNombre.value)
    } catch (error) {
      console.error("Error al actualizar nombre:", error)
      nuevoNombre.value = props.conversacion.nombre
    }
  } else {
    nuevoNombre.value = props.conversacion.nombre
  }
}

const abrirEdicionGrupo = (tipo) => {
  tipoEdicion.value = tipo
  valorEdicion.value = props.conversacion[tipo] || ''
  tituloModal.value = tipo === 'fotoUrl' ? 'Cambiar foto de grupo' : 'Cambiar banner del grupo'
  mostrarModalEdicion.value = true
}

const guardarInfoGrupo = async () => {
  if (!valorEdicion.value.trim() && !props.conversacion[tipoEdicion.value]) {
    mostrarModalEdicion.value = false
    return
  }

  guardandoInfo.value = true
  try {
    const payload = {
      nombre: props.conversacion.nombre,
      fotoUrl: props.conversacion.fotoUrl,
      imagenBanner: props.conversacion.imagenBanner
    }
    payload[tipoEdicion.value] = valorEdicion.value

    await servicioApi.actualizarConversacion(props.conversacion.id, payload.nombre, payload.fotoUrl, payload.imagenBanner)
    almacen.actualizarInfoConversacion(props.conversacion.id, payload.nombre, payload.fotoUrl, payload.imagenBanner)
    mostrarModalEdicion.value = false
  } catch (error) {
    console.error("Error al actualizar info:", error)
  } finally {
    guardandoInfo.value = false
  }
}

async function cambiarRol(participanteId, nuevoRol) {
  try {
    await servicioApi.actualizarRolParticipante(props.conversacion.id, participanteId, nuevoRol)
    almacen.actualizarRolParticipante(props.conversacion.id, participanteId, nuevoRol)
  } catch (error) {
    console.error("Error al cambiar rol:", error)
  }
}

const abrirConfirmacionEliminar = (usuario) => {
  usuarioAEliminar.value = usuario
  mostrarModalEliminar.value = true
}

const confirmarEliminarMiembro = async () => {
  if (!usuarioAEliminar.value) return
  eliminandoMiembro.value = true
  try {
    await servicioApi.eliminarParticipante(props.conversacion.id, usuarioAEliminar.value.id)
    almacen.eliminarParticipante(props.conversacion.id, usuarioAEliminar.value.id)
    mostrarModalEliminar.value = false
  } catch (error) {
    console.error("Error al eliminar miembro:", error)
  } finally {
    eliminandoMiembro.value = false
    usuarioAEliminar.value = null
  }
}

</script>

<style scoped>
/* ==================================================
   InfoGrupo.vue — Premium SaaS Styles 2026
   ================================================== */

.info-grupo {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  overflow: hidden;
  border-left: 1px solid rgba(64,109,115,0.08);
}

/* ===================================================
   HEADER COMPACTO
   =================================================== */
.info-header-compact {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid rgba(64,109,115,0.1);
  flex-shrink: 0;
  gap: 12px;
  min-height: 62px;
}

.btn-atras {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: background .15s, transform .15s;
}

.btn-atras:hover {
  background: rgba(64,109,115,0.08);
  transform: translateX(-2px);
}

.header-avatar-wrap {
  flex-shrink: 0;
}

.header-avatar-info {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(64,109,115,0.2);
}

.header-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-info-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.info-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a2e31;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.input-nombre-grupo {
  font-size: 15px;
  font-weight: 700;
  color: #1a2e31;
}

.info-subtitle {
  font-size: 12px;
  color: #5a8a94;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.header-btn {
  transition: transform .15s ease, background .15s ease !important;
}
.header-btn:hover {
  transform: scale(1.08) !important;
}

/* ===================================================
   CUERPO SCROLLEABLE
   =================================================== */
.info-cuerpo {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fbfc;
}

/* ===================================================
   SECCIONES Y TARJETAS
   =================================================== */
.seccion {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(64,109,115,0.06), 0 4px 12px rgba(64,109,115,0.03);
  border: 1px solid rgba(64,109,115,0.08);
}

.seccion-titulo {
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 14px;
}

.descripcion-usuario {
  font-size: 13px;
  color: #2f4a4f;
  line-height: 1.6;
  margin: 0;
}

/* ===================================================
   LISTA DE MIEMBROS
   =================================================== */
.lista-miembros {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-miembro {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  transition: background .15s;
}

.item-miembro:hover {
  background: rgba(179, 235, 242, 0.15);
}

.avatar-mini {
  width: 40px;
  height: 40px;
  min-width: 40px;
  background: linear-gradient(135deg, #B2C5C8, #9fb3b6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-mini img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-usuario {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-grow: 1;
}

.info-usuario .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #1a2e31;
  letter-spacing: -0.01em;
}

.roles-wrapper {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

/* ===================================================
   MODALES (Heredan de estilos.css globales pero ajustes finos aquí)
   =================================================== */
.modal-editar-perfil {
  box-shadow: 0 16px 48px rgba(64,109,115,0.18) !important;
}

.modal-titulo-perfil {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-perfil {
  padding: 20px 16px !important;
  background: #f7fbfc !important;
}

.campo-edicion {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label-input-mejorado {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 600;
  color: #2f4a4f;
}

.input-modal-mejorado {
  padding: 10px 14px;
  border: 1.5px solid rgba(64,109,115,0.2);
  border-radius: 10px;
  font-size: 13px;
  color: #1a2e31;
  background: #ffffff;
  outline: none;
  transition: border-color .15s, box-shadow .15s;
}

.input-modal-mejorado:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.1);
}

.modal-acciones-perfil {
  background: #ffffff !important;
  border-top: 1px solid rgba(64,109,115,0.08) !important;
  padding: 12px 16px !important;
}

/* ===================================================
   RESPONSIVE
   =================================================== */
@media (max-width: 480px) {
  .info-header-compact {
    padding: 10px 12px;
    min-height: 56px;
  }
  .header-avatar-info {
    width: 38px;
    height: 38px;
    font-size: 15px;
  }
  .info-cuerpo {
    padding: 12px;
  }
  .seccion {
    padding: 12px;
  }
}
</style>

