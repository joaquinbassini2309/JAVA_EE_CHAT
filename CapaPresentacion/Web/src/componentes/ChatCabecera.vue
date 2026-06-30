<template>
  <div class="chat-header-compact" @click="$emit('click-info')">
    <div class="header-left">
      <div class="header-avatar">
        <img v-if="conversacionActual?.fotoUrl" :src="conversacionActual.fotoUrl" class="header-avatar-img" alt="avatar" />
        <span v-else>{{ destinatario.iniciales }}</span>
        <span v-if="!esGrupo && otroUsuario?.estado === 'ONLINE'" class="header-dot"></span>
      </div>
      <div class="header-info">
        <div style="display: flex; align-items: center; gap: 6px;">
          <span class="header-name">{{ destinatario.nombre }}</span>
          <v-btn
            v-if="!esGrupo && otroUsuario"
            icon
            variant="text"
            density="compact"
            size="small"
            @click.stop="$emit('toggle-favorito')"
            title="Favorito"
          >
            <v-icon :color="esFavorito ? '#FFC107' : '#FFD54F'" size="20">
              {{ esFavorito ? 'mdi-star' : 'mdi-star-outline' }}
            </v-icon>
          </v-btn>
        </div>
        <span class="header-status">
          <template v-if="!esGrupo && otroUsuario">
            <span :class="otroUsuario.estado === 'ONLINE' ? 'status-online' : 'status-offline'">
              {{ otroUsuario.estado === 'ONLINE' ? '● En línea' : '● Desconectado' }}
            </span>
          </template>
          <template v-else>
            <span class="status-group">● Grupo de conversación</span>
          </template>
        </span>
      </div>
    </div>
    <div class="header-actions" @click.stop>
      <button
        v-if="!esAviso || rolUsuario === 'ADMIN'"
        @click="$emit('abrir-modal-anadir')"
        title="Añadir miembro"
        class="btn-cabecera-accion mr-1"
      >
        <v-icon size="17">mdi-account-plus</v-icon>
      </button>
      <button
        @click="$emit('cerrar-conversacion')"
        title="Cerrar conversación"
        class="btn-cabecera-accion btn-close-chat"
      >
        <v-icon size="17">mdi-close</v-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  conversacionActual: {
    type: Object,
    default: null
  },
  destinatario: {
    type: Object,
    required: true
  },
  otroUsuario: {
    type: Object,
    default: null
  },
  esGrupo: {
    type: Boolean,
    default: false
  },
  esAviso: {
    type: Boolean,
    default: false
  },
  esFavorito: {
    type: Boolean,
    default: false
  },
  rolUsuario: {
    type: String,
    default: 'MIEMBRO'
  }
})

defineEmits(['click-info', 'toggle-favorito', 'abrir-modal-anadir', 'cerrar-conversacion'])
</script>

<style scoped>
.chat-header-compact {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--surface);
  border-bottom: 1px solid rgba(64,109,115,0.1);
  cursor: pointer;
  flex-shrink: 0;
  z-index: 10;
  min-height: 62px;
  transition: background .18s ease;
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}

.chat-header-compact:hover {
  background: rgba(179, 235, 242, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.header-avatar {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: visible;
  box-shadow: 0 2px 8px rgba(64,109,115,0.28);
}

.header-avatar-img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.header-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #6A9E7D;
  border: 2.5px solid #ffffff;
  box-shadow: 0 0 0 1px rgba(106,158,125,0.3);
}

.header-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 1px;
}

.header-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2e31;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.header-status {
  font-size: 12px;
  line-height: 1.4;
}

.status-online  { color: #6A9E7D; font-weight: 500; }
.status-offline { color: #8aa5ab; font-weight: 400; }
.status-group   { color: #8aa5ab; font-weight: 400; }

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.btn-cabecera-accion {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--teal);
  background-color: var(--surface);
  color: var(--teal);
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease, border-color 0.2s;
  box-sizing: border-box;
}

.btn-cabecera-accion:hover {
  background-color: var(--teal);
  color: #ffffff;
  transform: scale(1.12);
}

.btn-cabecera-accion .v-icon,
.btn-cabecera-accion i {
  color: inherit !important;
}

.btn-cabecera-accion:hover .v-icon,
.btn-cabecera-accion:hover i {
  color: #ffffff !important;
}

.btn-cabecera-accion.btn-close-chat {
  border-color: #d32f2f;
  color: #d32f2f;
}

.btn-cabecera-accion.btn-close-chat:hover {
  background-color: #d32f2f;
  color: #ffffff;
}

.btn-cabecera-accion.btn-close-chat:hover .v-icon,
.btn-cabecera-accion.btn-close-chat:hover i {
  color: #ffffff !important;
}
</style>
