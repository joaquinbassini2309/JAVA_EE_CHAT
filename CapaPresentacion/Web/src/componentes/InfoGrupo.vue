<template>
  <div class="info-grupo">

    <!-- Encabezado con banner + avatar cuadrado (mismo patrón que Chat.vue) -->
    <div class="info-header">
      <div class="profile-banner profile-banner--default" />
      <div class="profile-lower">
        <div class="profile-avatar-wrap">
          <div class="avatar-cuadrado">
            {{ conversacion.nombre?.charAt(0).toUpperCase() || 'G' }}
          </div>
        </div>
        <div class="profile-title-row">
          <button class="btn-atras" @click="$emit('volver')" title="Volver">
            <v-icon size="20" color="#406D73">mdi-arrow-left</v-icon>
          </button>

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
          <span v-else class="profile-name text-truncate">{{ conversacion.nombre }}</span>

          <v-btn
            v-if="puedeGestionar && !editandoNombre"
            icon="mdi-pencil"
            variant="text"
            size="x-small"
            @click="editandoNombre = true"
            class="ml-1"
          />
        </div>
        <div class="profile-subtitle">
          {{ conversacion.participantes?.length || 0 }} miembros
        </div>
      </div>
    </div>

    <!-- Cuerpo scrolleable -->
    <div class="info-cuerpo">

      <!-- Sección integrantes -->
      <div class="seccion">
        <div class="seccion-titulo">
          <v-icon size="16" color="#406D73" class="mr-1">mdi-account-group</v-icon>
          Integrantes
        </div>
        <div class="lista-miembros">
          <div v-for="miembro in conversacion.participantes" :key="miembro.usuario.id" class="item-miembro">
            <div class="avatar-mini">{{ miembro.usuario.username.charAt(0).toUpperCase() }}</div>

            <div class="info-usuario">
              <span class="nombre">{{ miembro.usuario.username }}</span>
              <div class="roles-wrapper">
                <v-chip v-if="miembro.rol === 'ADMIN'" color="primary" size="x-small" label>Admin</v-chip>
                <v-chip v-if="miembro.rol === 'MODERADOR'" color="secondary" size="x-small" label>Mod</v-chip>
                <v-chip v-if="miembro.rol === 'SILENCIADO'" color="warning" size="x-small" label>Silenciado</v-chip>
              </div>
            </div>

            <!-- Menú de acciones para el Admin -->
            <v-menu v-if="esAdmin && usuarioActual.id !== miembro.usuario.id">
              <template v-slot:activator="{ props }">
                <v-btn icon="mdi-dots-vertical" variant="text" size="small" v-bind="props" />
              </template>
              <v-list>
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'MODERADOR')" v-if="miembro.rol !== 'MODERADOR'">
                  <v-list-item-title>Nombrar Moderador</v-list-item-title>
                </v-list-item>
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'MIEMBRO')" v-if="miembro.rol !== 'MIEMBRO'">
                  <v-list-item-title>Nombrar Miembro</v-list-item-title>
                </v-list-item>
                <v-divider />
                <v-list-item @click="cambiarRol(miembro.usuario.id, 'SILENCIADO')" class="text-warning">
                  <v-list-item-title>Silenciar</v-list-item-title>
                </v-list-item>
                <v-list-item @click="eliminarMiembro(miembro.usuario.id)" class="text-error">
                  <v-list-item-title>Eliminar del grupo</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>
        </div>
      </div>
    </div>
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

const usuarioActual = computed(() => almacen.usuarioActual)

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
      almacen.actualizarNombreConversacion(props.conversacion.id, nuevoNombre.value)
    } catch (error) {
      console.error("Error al actualizar nombre:", error)
      nuevoNombre.value = props.conversacion.nombre
    }
  } else {
    nuevoNombre.value = props.conversacion.nombre
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

async function eliminarMiembro(participanteId) {
  if (confirm('¿Estás seguro de que quieres eliminar a este miembro del grupo?')) {
    try {
      await servicioApi.eliminarParticipante(props.conversacion.id, participanteId)
      almacen.eliminarParticipante(props.conversacion.id, participanteId)
    } catch (error) {
      console.error("Error al eliminar miembro:", error)
    }
  }
}

</script>

<style scoped>
.info-grupo { display: flex; flex-direction: column; height: 100%; background: #f7fcfd; overflow: hidden; }
.info-header { flex-shrink: 0; }
.profile-banner { height: 76px; background-color: #B3EBF2; background-image: linear-gradient(135deg, rgba(64,109,115,0.22) 0%, transparent 55%), linear-gradient(225deg, rgba(255,255,255,0.45) 0%, transparent 48%), radial-gradient(ellipse 90% 140% at 15% 0%, rgba(64,109,115,0.15), transparent); background-size: cover; }
.profile-lower { position: relative; background: #f0f7f8; padding: 8px 14px 14px; padding-left: 100px; min-height: 72px; }
.profile-avatar-wrap { position: absolute; left: 14px; top: -32px; z-index: 2; }
.avatar-cuadrado { width: 64px; height: 64px; background: #B2C5C8; color: #2f4a4f; border-radius: 10px; border: 3px solid #ffffff; box-shadow: 0 4px 14px rgba(64,109,115,0.22); display: flex; align-items: center; justify-content: center; font-size: 26px; font-weight: 700; }
.profile-title-row { display: flex; align-items: center; gap: 8px; }
.btn-atras { background: none; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 6px; flex-shrink: 0; transition: background 0.15s; }
.btn-atras:hover { background: rgba(64,109,115,0.1); }
.profile-name { font-size: 1rem; font-weight: 700; color: #2f4a4f; line-height: 1.25; letter-spacing: 0.01em; }
.input-nombre-grupo { font-size: 1rem; font-weight: 700; color: #2f4a4f; }
.profile-subtitle { font-size: 12px; color: #5a8a94; margin-top: 2px; }
.info-cuerpo { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.seccion { background: #ffffff; border-radius: 12px; padding: 14px 16px; box-shadow: 0 1px 4px rgba(64,109,115,0.08); border: 1px solid rgba(64,109,115,0.1); }
.seccion-titulo { display: flex; align-items: center; font-size: 11px; font-weight: 700; color: #406D73; letter-spacing: 0.06em; text-transform: uppercase; margin-bottom: 12px; }
.lista-miembros { display: flex; flex-direction: column; gap: 2px; }
.item-miembro { display: flex; align-items: center; gap: 12px; padding: 8px; border-radius: 8px; }
.avatar-mini { width: 38px; height: 38px; min-width: 38px; background: #B2C5C8; color: #2f4a4f; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 15px; flex-shrink: 0; }
.info-usuario { display: flex; flex-direction: column; overflow: hidden; flex-grow: 1; }
.info-usuario .nombre { font-weight: 600; font-size: 14px; color: #2f4a4f; }
.roles-wrapper { display: flex; gap: 4px; margin-top: 2px; }
</style>