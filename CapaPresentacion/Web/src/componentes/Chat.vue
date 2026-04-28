<template>
  <template v-if="conversacionActiva">
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
      <Mensaje
        v-for="msg in mensajes"
        :key="msg.id"
        :mensaje="msg"
      />
    </div>
    <v-divider opacity="0.2" color="accent" />
    <div class="pa-3 bg-surface flex-shrink-0">
      <div class="input-bar">
        <v-text-field v-model="newMessage" density="comfortable" variant="outlined" hide-details placeholder="Escribe un mensaje..." color="accent" bg-color="secondary" class="rounded-lg grow" @keyup.enter="enviarMensaje"/>
        <v-btn class="btn-11" color="accent" variant="flat" prepend-icon="mdi-send" @click="enviarMensaje" :disabled="!newMessage.trim()">Enviar mensaje</v-btn>
      </div>
    </div>
  </template>
</template>

<script setup>
import { ref, computed, watch, onUnmounted, nextTick } from 'vue';
import { useAlmacen } from '@/almacen'; // Corregido
import Mensaje from './Mensaje.vue';

const almacen = useAlmacen();

const conversacionActiva = computed(() => almacen.conversacionActual);
const mensajes = computed(() => almacen.mensajes);
const usuario = computed(() => almacen.usuario);
const token = computed(() => almacen.token);

const newMessage = ref('');
const webSocket = ref(null);
const messageListRef = ref(null);

const destinatario = computed(() => {
    if (!conversacionActiva.value) return null;
    return {
        nombre: conversacionActiva.value.nombre,
        iniciales: conversacionActiva.value.nombre.charAt(0).toUpperCase(),
        subtitulo: 'En conversación',
    };
});

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
};

const connectWebSocket = (convId) => {
  if (webSocket.value) webSocket.value.close();

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = `${wsProtocol}//${window.location.host}/chat-empresarial/api/v1/websocket/conversacion/${convId}/usuario/${usuario.value.id}?token=${token.value}`;

  const ws = new WebSocket(wsUrl);

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.tipo === 'mensaje') {
      almacen.agregarMensaje(data.datos);
    }
  };

  ws.onclose = () => console.log('WebSocket disconnected');
  ws.onerror = (error) => console.error('WebSocket error:', error);

  webSocket.value = ws;
};

const enviarMensaje = () => {
  if (newMessage.value.trim() && webSocket.value?.readyState === WebSocket.OPEN) {
    const messagePayload = {
      contenido: newMessage.value,
      tipoMensaje: 'TEXTO',
    };
    webSocket.value.send(JSON.stringify(messagePayload));
    newMessage.value = '';
  }
};

watch(conversacionActiva, (nuevaConv) => {
  if (nuevaConv?.id) {
    connectWebSocket(nuevaConv.id);
  } else {
    if (webSocket.value) webSocket.value.close();
  }
}, { immediate: true });

watch(mensajes, () => {
  scrollToBottom();
}, { deep: true });

onUnmounted(() => {
  if (webSocket.value) webSocket.value.close();
});
</script>

<style scoped>
/* Tus estilos se mantienen intactos */
.messages-scroll { flex: 1 1 auto; min-height: 0; overflow-y: auto; background: rgba(179, 235, 242, 0.35); }
.messages-stack { display: flex; flex-direction: column; gap: 12px; }
.input-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.input-bar .grow { flex: 1 1 220px; min-width: 180px; }
.profile-header { position: relative; border-radius: 12px; overflow: hidden; margin-bottom: 4px; }
.profile-banner { height: 76px; background-size: cover; background-position: center; background-repeat: no-repeat; }
.profile-banner--default { background-color: rgb(var(--v-theme-primary)); background-image: linear-gradient(135deg, rgba(var(--v-theme-accent), 0.2) 0%, transparent 55%), linear-gradient(225deg, rgba(255, 255, 255, 0.45) 0%, transparent 48%), radial-gradient(ellipse 90% 140% at 15% 0%, rgba(var(--v-theme-accent), 0.18), transparent); }
.profile-lower { position: relative; background: rgb(var(--v-theme-primary)); padding: 14px 14px 16px; padding-inline-start: 108px; }
.profile-lower--chat { min-height: 72px; padding-bottom: 14px; }
.profile-avatar-wrap { position: absolute; z-index: 2; inset-inline-start: 14px; top: -38px; }
.profile-avatar-box { border-radius: 12px; border: 3px solid rgb(var(--v-theme-surface)); box-shadow: 0 4px 14px rgba(var(--v-theme-accent), 0.22); }
.initials-on-secondary-avatar { color: rgb(var(--v-theme-on-secondary)); }
.profile-title-row { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.profile-name { font-size: 1.15rem; font-weight: 700; color: rgb(var(--v-theme-on-primary)); line-height: 1.25; letter-spacing: 0.01em; }
.profile-subtitle { font-size: 12px; color: rgb(var(--v-theme-accent)); line-height: 1.4; margin-top: 4px; opacity: 0.9; }
.heading-14 { font-size: 14px; font-weight: 500; line-height: 1.3; letter-spacing: 0.01em; color: rgb(var(--v-theme-accent)); }
.btn-11 { font-size: 11px; font-weight: 600; letter-spacing: 0.04em; text-transform: none; }
:global(.btn-11.v-btn) { min-height: 36px; }
</style>
