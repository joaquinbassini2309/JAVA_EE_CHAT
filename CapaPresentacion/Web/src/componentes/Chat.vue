<template>
  <template v-if="destinatario">
    <div class="pa-3 pb-2 flex-shrink-0">
        <div class="profile-header">
            <div class="profile-banner profile-banner--default"/>
            <div class="profile-lower profile-lower--chat">
                <div class="profile-avatar-wrap">
                    <v-avatar class="profile-avatar-box" color="secondary" size="72" rounded="lg">
                        <span class="initials-on-secondary-avatar text-subtitle-1 font-weight-medium">{{ destinatario.iniciales }}</span>
                    </v-avatar>
                </div>
                <div class="profile-title-row">
                    <div class="profile-name text-truncate">{{ destinatario.nombre }}</div>
                </div>
                <div class="profile-subtitle text-truncate">{{ destinatario.subtitulo }}</div>
            </div>
        </div>
    </div>
    <v-divider opacity="0.2" color="accent" />
    <div ref="messageListRef" class="messages-scroll flex-grow-1 pa-4 messages-stack">
      <div v-for="msg in mensajes" :key="msg.id" class="bubble" :class="msg.emisorId === yoId ? 'bubble-me' : 'bubble-them'">
        {{ msg.contenido }}
      </div>
    </div>
    <v-divider opacity="0.2" color="accent" />
    <div class="pa-3 bg-surface flex-shrink-0">
      <div class="input-bar">
        <v-text-field v-model="newMessage" density="comfortable" variant="outlined" hide-details placeholder="Escribe un mensaje..." color="accent" bg-color="secondary" class="rounded-lg grow" @keyup.enter="enviarMensaje"/>
        <v-btn class="btn-11" color="accent" variant="flat" prepend-icon="mdi-send" @click="enviarMensaje" :disabled="!newMessage.trim()">Enviar mensaje</v-btn>
      </div>
    </div>
  </template>
  <template v-else>
    <div class="d-flex flex-column align-center justify-center h-100">
        <v-icon size="80" color="accent" class="mb-4">mdi-message-text-outline</v-icon>
        <p class="heading-14">Selecciona una conversación para empezar</p>
    </div>
  </template>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';

const props = defineProps({
  destinatario: { type: Object, default: null },
  mensajes: { type: Array, default: () => [] },
  yoId: { type: [Number, null], default: null }
});

const emit = defineEmits(['enviar-mensaje']);

const newMessage = ref('');
const messageListRef = ref(null);

const enviarMensaje = () => {
  if (newMessage.value.trim()) {
    emit('enviar-mensaje', newMessage.value);
    newMessage.value = '';
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  });
};

watch(() => props.mensajes, () => {
  scrollToBottom();
}, { deep: true });
</script>

<style scoped>
/* Estilos exactos del diseño original para el panel de chat */
.messages-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  background: rgba(179, 235, 242, 0.35);
}
.messages-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bubble {
  border-radius: 14px;
  padding: 10px 14px;
  max-width: 78%;
  font-size: 14px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.bubble-them {
  background: #b2c5c8;
  color: #2f4a4f;
  align-self: flex-start;
}
.bubble-me {
  background: rgba(179, 235, 242, 0.95);
  border: 1px solid rgba(64, 109, 115, 0.25);
  color: #2a4d52;
  align-self: flex-end;
}
.input-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}
.input-bar .grow {
  flex: 1 1 220px;
  min-width: 180px;
}
.profile-header {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 4px;
}
.profile-banner {
  height: 76px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
.profile-banner--default {
  background-color: rgb(var(--v-theme-primary));
  background-image:
    linear-gradient(135deg, rgba(var(--v-theme-accent), 0.2) 0%, transparent 55%),
    linear-gradient(225deg, rgba(255, 255, 255, 0.45) 0%, transparent 48%),
    radial-gradient(ellipse 90% 140% at 15% 0%, rgba(var(--v-theme-accent), 0.18), transparent);
}
.profile-lower {
  position: relative;
  background: rgb(var(--v-theme-primary));
  padding: 14px 14px 16px;
  padding-inline-start: 108px;
}
.profile-lower--chat {
  min-height: 72px;
  padding-bottom: 14px;
}
.profile-avatar-wrap {
  position: absolute;
  z-index: 2;
  inset-inline-start: 14px;
  top: -38px;
}
.profile-avatar-box {
  border-radius: 12px;
  border: 3px solid rgb(var(--v-theme-surface));
  box-shadow: 0 4px 14px rgba(var(--v-theme-accent), 0.22);
}
.initials-on-secondary-avatar {
  color: rgb(var(--v-theme-on-secondary));
}
.profile-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.profile-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: rgb(var(--v-theme-on-primary));
  line-height: 1.25;
  letter-spacing: 0.01em;
}
.profile-subtitle {
  font-size: 12px;
  color: rgb(var(--v-theme-accent));
  line-height: 1.4;
  margin-top: 4px;
  opacity: 0.9;
}
.heading-14 {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: 0.01em;
  color: rgb(var(--v-theme-accent));
}
.btn-11 {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: none;
}
:global(.btn-11.v-btn) {
  min-height: 36px;
}
</style>
