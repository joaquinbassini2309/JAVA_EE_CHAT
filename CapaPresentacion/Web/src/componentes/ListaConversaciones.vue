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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAlmacen } from '@/almacen'

const almacen = useAlmacen()
const termino = ref('')

const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  if (!termino.value) {
    return conversacionesActuales.value
  }

  const terminoLower = termino.value.toLowerCase()
  return conversacionesActuales.value.filter(c =>
    c.nombre.toLowerCase().includes(terminoLower)
  )
})

const esActiva = (conversacion) => {
  return conversacionActual.value?.id === conversacion.id
}

const seleccionarConversacion = (conversacion) => {
  almacen.establecerConversacionActual(conversacion)
}

const abrirNuevaConversacion = () => {
  // Aquí irá el modal para crear nueva conversación
  console.log('Abrir nueva conversación')
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
</style>
