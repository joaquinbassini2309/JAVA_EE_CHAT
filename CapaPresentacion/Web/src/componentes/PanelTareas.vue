<template>
  <div class="panel-tareas">
    <!-- Header -->
    <div class="header-tareas">
      <div class="titulo-row">
        <v-icon color="#406D73" class="mr-2">mdi-checkbox-marked-circle-outline</v-icon>
        <span class="titulo-texto">Mis Tareas</span>
      </div>
      <div class="acciones-row">
        <v-btn icon="mdi-magnify" variant="text" size="small" color="#406D73" @click="mostrarBusqueda = !mostrarBusqueda" />
        <v-btn icon="mdi-plus" variant="flat" size="small" color="#406D73" class="text-white" @click="abrirNuevaTarea" />
        <v-btn icon="mdi-close" variant="text" size="small" color="#406D73" @click="cerrarPanel" />
      </div>
    </div>

    <!-- Busqueda oculta por defecto -->
    <v-expand-transition>
      <div v-if="mostrarBusqueda" class="busqueda-container">
        <input v-model="busqueda" type="text" placeholder="Buscar tarea..." class="input-busqueda" />
      </div>
    </v-expand-transition>

    <!-- Tabs -->
    <div class="tabs-container">
      <div class="tab-item" :class="{ activo: tabActual === 'PENDIENTE' }" @click="tabActual = 'PENDIENTE'">
        <span>Pendientes</span>
        <div class="badge" v-if="tareasPendientes.length">{{ tareasPendientes.length }}</div>
      </div>
      <div class="tab-item" :class="{ activo: tabActual === 'COMPLETADA' }" @click="tabActual = 'COMPLETADA'">
        <span>Completadas</span>
        <div class="badge gris" v-if="tareasCompletadas.length">{{ tareasCompletadas.length }}</div>
      </div>
      <div class="tab-item" :class="{ activo: tabActual === 'ATRASADA' }" @click="tabActual = 'ATRASADA'">
        <span>Atrasadas</span>
        <div class="badge rojo" v-if="tareasAtrasadas.length">{{ tareasAtrasadas.length }}</div>
      </div>
    </div>

    <!-- Lista de tareas -->
    <div class="lista-tareas-scroll">
      <div v-if="tareasMostradas.length === 0" class="empty-state">
        <v-icon size="40" color="#b2c5c8" class="mb-2">mdi-check-all</v-icon>
        <p>No hay tareas aquí</p>
      </div>
      
      <div v-for="tarea in tareasMostradas" :key="tarea.id" class="tarjeta-tarea">
        <div class="tarea-checkbox" @click="toggleEstado(tarea)">
          <v-icon :color="tarea.estado === 'COMPLETADA' ? '#6A9E7D' : (tarea.estado === 'ATRASADA' ? '#e57373' : '#406D73')">
            {{ tarea.estado === 'COMPLETADA' ? 'mdi-check-circle' : 'mdi-circle-outline' }}
          </v-icon>
        </div>
        <div class="tarea-contenido">
          <div class="tarea-titulo" :class="{ 'texto-tachado': tarea.estado === 'COMPLETADA' }">
            {{ tarea.titulo }}
          </div>
          <div class="tarea-desc text-truncate" v-if="tarea.contenido">
            {{ tarea.contenido }}
          </div>
          <div class="tarea-meta">
            <span v-if="tarea.fechaVencimiento" class="meta-item">
              <v-icon size="12" class="mr-1">mdi-calendar</v-icon>
              {{ formatearFecha(tarea.fechaVencimiento) }}
            </span>
            <span class="meta-item">
              <v-icon size="12" class="mr-1">mdi-account</v-icon>
              De: {{ tarea.creadorUsername }}
            </span>
            <span v-if="tarea.asignadoAUsername" class="meta-item">
              <v-icon size="12" class="mr-1">mdi-account-arrow-right</v-icon>
              Para: {{ tarea.asignadoAUsername }}
            </span>
            <v-chip v-if="tarea.grupoNombre" size="x-small" color="primary" variant="tonal" class="ml-1">
              {{ tarea.grupoNombre }}
            </v-chip>
          </div>
        </div>
        <div class="tarea-acciones">
          <v-menu offset-y>
            <template v-slot:activator="{ props }">
              <v-btn icon="mdi-dots-vertical" variant="text" size="small" v-bind="props" />
            </template>
            <v-list density="compact">
              <v-list-item v-if="tarea.estado !== 'COMPLETADA'" @click="cambiarEstado(tarea, 'COMPLETADA')">
                <v-list-item-title>Marcar completada</v-list-item-title>
              </v-list-item>
              <v-list-item v-if="tarea.estado === 'COMPLETADA'" @click="cambiarEstado(tarea, 'PENDIENTE')">
                <v-list-item-title>Desmarcar completada</v-list-item-title>
              </v-list-item>
              <v-list-item @click="eliminarTarea(tarea)" class="text-error">
                <v-list-item-title>Eliminar tarea</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </div>
      </div>
    </div>

    <!-- Modal Nueva Tarea Premium -->
    <v-dialog v-model="modalNuevaTarea" max-width="450">
      <v-card rounded="xl" class="modal-nueva-tarea">
        <v-card-title class="modal-titulo-tarea">
          <v-icon size="18" color="white" class="mr-2">mdi-clipboard-plus-outline</v-icon>
          Nueva Tarea
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="modalNuevaTarea = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-tarea">
          <div class="campo-grupo">
            <label class="label-input">Título de la tarea</label>
            <input v-model="nuevaTarea.titulo" type="text" placeholder="Ej: Redactar reporte mensual" class="input-mejorado" />
          </div>
          <div class="campo-grupo">
            <label class="label-input">Descripción</label>
            <textarea v-model="nuevaTarea.contenido" placeholder="Escribe detalles sobre la tarea..." class="input-mejorado text-area-mejorado" rows="3"></textarea>
          </div>
          
          <div class="d-flex gap-2" style="width: 100%;">
            <div class="campo-grupo select-wrapper">
              <label class="label-input">Grupo (Opcional)</label>
              <v-select v-model="nuevaTarea.grupoId" :items="gruposDisponibles" item-title="nombre" item-value="id" placeholder="Ninguno" variant="outlined" density="compact" hide-details clearable bg-color="var(--surface)"></v-select>
            </div>
            <div class="campo-grupo select-wrapper ml-2">
              <label class="label-input">Asignar a (Opcional)</label>
              <v-select v-model="nuevaTarea.asignadoAId" :items="usuariosSistema" item-title="username" item-value="id" placeholder="Ninguno" variant="outlined" density="compact" hide-details clearable bg-color="var(--surface)"></v-select>
            </div>
          </div>

          <div class="campo-grupo">
            <label class="label-input">Fecha y hora de vencimiento</label>
            <input v-model="nuevaTarea.fechaVencimiento" type="datetime-local" class="input-mejorado" />
          </div>
        </v-card-text>
        <v-card-actions class="modal-acciones-tarea">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="modalNuevaTarea = false">Cancelar</v-btn>
          <v-btn color="primary" variant="flat" :loading="guardando" @click="guardarNuevaTarea">Crear Tarea</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'

const almacen = useAlmacen()
const busqueda = ref('')
const mostrarBusqueda = ref(false)
const tabActual = ref('PENDIENTE') // PENDIENTE, COMPLETADA, ATRASADA
const modalNuevaTarea = ref(false)
const guardando = ref(false)

const nuevaTarea = ref({
  titulo: '',
  contenido: '',
  fechaVencimiento: '',
  grupoId: null,
  asignadoAId: null
})

const usuariosSistema = ref([])

const gruposDisponibles = computed(() => {
  return almacen.conversaciones.filter(c => c.tipo === 'GRUPO')
})

const formatearFecha = (fechaStr) => {
  if (!fechaStr) return ''
  const d = new Date(fechaStr)
  return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const cargarTareas = async () => {
  if (!almacen.usuarioActual) return
  try {
    const tareasData = await servicioApi.obtenerTareas(almacen.usuarioActual.id)
    almacen.establecerTareas(tareasData)
  } catch (error) {
    console.error("Error al cargar tareas:", error)
  }
}

onMounted(async () => {
  cargarTareas()
  try {
    usuariosSistema.value = await servicioApi.obtenerUsuarios()
  } catch (e) {
    console.error("Error al cargar usuarios:", e)
  }
})

watch(() => almacen.usuarioActual, (nuevoUser) => {
  if (nuevoUser) cargarTareas()
})

const tareasPendientes = computed(() => {
  const now = new Date().getTime()
  return almacen.tareas.filter(t => {
    if (t.estado === 'COMPLETADA') return false
    if (t.estado === 'ATRASADA') return false
    if (t.fechaVencimiento) {
      const v = new Date(t.fechaVencimiento).getTime()
      if (now > v) return false // Se considera atrasada
    }
    return true
  })
})

const tareasCompletadas = computed(() => {
  return almacen.tareas.filter(t => t.estado === 'COMPLETADA')
})

const tareasAtrasadas = computed(() => {
  const now = new Date().getTime()
  return almacen.tareas.filter(t => {
    if (t.estado === 'COMPLETADA') return false
    if (t.estado === 'ATRASADA') return true
    if (t.fechaVencimiento) {
      const v = new Date(t.fechaVencimiento).getTime()
      if (now > v) return true // Se considera atrasada dinámicamente
    }
    return false
  })
})

const tareasMostradas = computed(() => {
  let lista = []
  if (tabActual.value === 'PENDIENTE') lista = tareasPendientes.value
  else if (tabActual.value === 'COMPLETADA') lista = tareasCompletadas.value
  else lista = tareasAtrasadas.value

  if (busqueda.value) {
    const b = busqueda.value.toLowerCase()
    lista = lista.filter(t => t.titulo.toLowerCase().includes(b) || (t.contenido && t.contenido.toLowerCase().includes(b)))
  }
  return lista
})

const cerrarPanel = () => {
  almacen.togglePanelTareas()
}

const abrirNuevaTarea = () => {
  nuevaTarea.value = { titulo: '', contenido: '', fechaVencimiento: '', grupoId: null, asignadoAId: null }
  modalNuevaTarea.value = true
}

const guardarNuevaTarea = async () => {
  if (!nuevaTarea.value.titulo) return
  guardando.value = true
  try {
    const t = await servicioApi.crearTarea(
      nuevaTarea.value.titulo,
      nuevaTarea.value.contenido,
      nuevaTarea.value.fechaVencimiento ? nuevaTarea.value.fechaVencimiento + ":00" : null,
      almacen.usuarioActual.id,
      nuevaTarea.value.asignadoAId || null,
      nuevaTarea.value.grupoId || null
    )
    almacen.tareas.push(t)
    modalNuevaTarea.value = false
  } catch (error) {
    console.error("Error creando tarea", error)
  } finally {
    guardando.value = false
  }
}

const toggleEstado = async (tarea) => {
  const nuevoEstado = tarea.estado === 'COMPLETADA' ? 'PENDIENTE' : 'COMPLETADA'
  await cambiarEstado(tarea, nuevoEstado)
}

const cambiarEstado = async (tarea, nuevoEstado) => {
  try {
    const act = await servicioApi.actualizarEstadoTarea(tarea.id, nuevoEstado, almacen.usuarioActual.id)
    const index = almacen.tareas.findIndex(t => t.id === tarea.id)
    if (index !== -1) {
      almacen.tareas[index] = act
    }
  } catch (error) {
    console.error("Error actualizando estado", error)
  }
}

const eliminarTarea = async (tarea) => {
  if (!confirm('¿Seguro que deseas eliminar esta tarea?')) return
  try {
    await servicioApi.eliminarTarea(tarea.id, almacen.usuarioActual.id)
    almacen.tareas = almacen.tareas.filter(t => t.id !== tarea.id)
  } catch (error) {
    console.error("Error eliminando tarea", error)
  }
}
</script>

<style scoped>
.panel-tareas {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--sidebar-bg);
  border-left: 1px solid var(--border-color);
  width: 100%;
}

.header-tareas {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.titulo-row {
  display: flex;
  align-items: center;
}

.titulo-texto {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
}

.acciones-row {
  display: flex;
  gap: 4px;
}

.busqueda-container {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.input-busqueda {
  width: 100%;
  background: var(--input-bg);
  color: var(--text-primary);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  border: none;
  outline: none;
}

.tabs-container {
  display: flex;
  padding: 0 16px;
  border-bottom: 1px solid var(--border-color);
  gap: 16px;
}

.tab-item {
  padding: 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #7b9599;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.tab-item.activo {
  color: #406D73;
}

.tab-item.activo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #406D73;
  border-top-left-radius: 2px;
  border-top-right-radius: 2px;
}

.badge {
  background: #406D73;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 700;
}

.badge.gris { background: #b2c5c8; }
.badge.rojo { background: #e57373; }

.lista-tareas-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #b2c5c8;
  font-size: 14px;
}

.tarjeta-tarea {
  background: var(--surface);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  transition: transform 0.2s;
}

.tarjeta-tarea:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64,109,115,0.08);
}

.tarea-checkbox {
  cursor: pointer;
  padding-top: 2px;
}

.tarea-contenido {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tarea-titulo {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.texto-tachado {
  text-decoration: line-through;
  opacity: 0.6;
}

.tarea-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.tarea-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.meta-item {
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
}

/* Modal Nueva Tarea Premium */
.modal-nueva-tarea {
  box-shadow: 0 16px 48px var(--teal-glow) !important;
  background: var(--surface) !important;
  border-radius: 20px !important;
  overflow: hidden !important;
}

.modal-titulo-tarea {
  background: linear-gradient(135deg, var(--teal) 0%, var(--teal-light) 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-tarea {
  padding: 20px 16px !important;
  background: var(--bg) !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 16px !important;
}

.campo-grupo {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.select-wrapper {
  flex: 1;
}

.label-input {
  font-size: 11px;
  font-weight: 700;
  color: var(--teal);
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.input-mejorado {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--teal-border);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--surface);
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  font-family: inherit;
}

.input-mejorado:focus {
  border-color: var(--teal);
  box-shadow: 0 0 0 3px var(--teal-glow);
}

.text-area-mejorado {
  resize: vertical;
}

.modal-acciones-tarea {
  background: var(--surface) !important;
  border-top: 1px solid var(--border-color) !important;
  padding: 12px 16px !important;
}
</style>
