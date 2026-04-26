<template>
  <v-app class="chat-app-root">
    <v-main class="chat-main-fill bg-background pa-0">
      <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
        <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
          <v-row no-gutters class="chat-main-row">

            <!-- Componente Barra Lateral -->
            <v-col cols="12" md="4" class="sidebar-col">
              <ListaConversaciones
                :yo="yo"
                :conversaciones="conversations"
                :conversacionActivaId="activeConversation.id"
                @seleccionar-conversacion="selectConversation"
                @abrir-modal-usuario="openAddUserModal"
              />
            </v-col>

            <!-- Componente Panel de Chat -->
            <v-col cols="12" md="8" class="chat-col">
              <Chat
                :destinatario="destinatario"
                :mensajes="activeConversation.mensajes"
                :yoId="yo.id"
                @enviar-mensaje="sendMessage"
              />
            </v-col>

          </v-row>
        </v-sheet>
      </v-container>
    </v-main>

    <!-- Modal para Añadir Usuario (se mantiene aquí) -->
    <v-dialog v-model="addUserModal" max-width="500">
        <v-card>
            <v-card-title class="headline">Iniciar nueva conversación</v-card-title>
            <v-card-text>
                <v-list>
                    <v-list-item v-for="user in users" :key="user.id" :title="user.username" :subtitle="user.email" @click="createPrivateConversation(user.id)">
                        <template v-slot:prepend>
                            <v-avatar color="secondary"><span>{{ user.username.charAt(0).toUpperCase() }}</span></v-avatar>
                        </template>
                    </v-list-item>
                </v-list>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text @click="addUserModal = false">Cerrar</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
  </v-app>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useAlmacen } from '@/almacen';
import { useRouter } from 'vue-router';
import { servicioApi } from '@/servicios/api';
import ListaConversaciones from '@/componentes/ListaConversaciones.vue';
import Chat from '@/componentes/Chat.vue';

const almacen = useAlmacen();
const router = useRouter();

// --- Estado Reactivo ---
const token = computed(() => almacen.token);
const usuario = computed(() => almacen.usuario);

const conversations = ref([]);
const users = ref([]);
const activeConversation = reactive({
  id: null,
  nombre: 'Selecciona un chat',
  mensajes: [],
  participantes: [],
});

const addUserModal = ref(false);
const webSocket = ref(null);

// --- Protección de Ruta ---
if (!token.value || !usuario.value) {
  router.push('/login');
}

// --- Propiedades Computadas para pasar a los hijos ---
const yo = computed(() => {
    if (!usuario.value) return { id: null, nombre: '', iniciales: '', subtitulo: '' };
    return {
        id: usuario.value.id,
        nombre: usuario.value.username,
        iniciales: usuario.value.username.charAt(0).toUpperCase(),
        subtitulo: 'En línea',
    }
});

const destinatario = computed(() => {
    if (!activeConversation.id) {
        return null; // Devuelve null si no hay chat activo
    }
    return {
        nombre: activeConversation.nombre,
        iniciales: activeConversation.nombre.charAt(0).toUpperCase(),
        subtitulo: 'En conversación',
    };
});

// --- Métodos ---
const fetchConversations = async () => {
  try {
    conversations.value = await servicioApi.obtenerConversaciones();
  } catch (error) {
    console.error('Error fetching conversations:', error);
  }
};

const fetchMessages = async (convId) => {
  try {
    activeConversation.mensajes = await servicioApi.obtenerMensajes(convId);
  } catch (error) {
    console.error('Error fetching messages:', error);
  }
};

const fetchUsers = async () => {
    try {
        const allUsers = await servicioApi.obtenerUsuarios();
        // Filtrar para no mostrarse a uno mismo en la lista de nuevos chats
        users.value = allUsers.filter(u => u.id !== usuario.value.id);
    } catch (error) {
        console.error('Error fetching users:', error);
    }
};

const selectConversation = async (conv) => {
  if (activeConversation.id === conv.id) return;

  activeConversation.id = conv.id;
  activeConversation.nombre = conv.nombre;
  activeConversation.mensajes = [];

  await fetchMessages(conv.id);
  connectWebSocket(conv.id);
};

const connectWebSocket = (convId) => {
  if (webSocket.value) {
    webSocket.value.close();
  }

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = `${wsProtocol}//${window.location.host}/chat-empresarial/api/v1/websocket/conversacion/${convId}/usuario/${usuario.value.id}?token=${token.value}`;

  const ws = new WebSocket(wsUrl);

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.tipo === 'mensaje' && data.datos.conversacionId === activeConversation.id) {
      activeConversation.mensajes.push(data.datos);
    }
  };

  ws.onclose = () => console.log('WebSocket disconnected');
  ws.onerror = (error) => console.error('WebSocket error:', error);

  webSocket.value = ws;
};

const sendMessage = (contenido) => {
  if (contenido.trim() && webSocket.value && webSocket.value.readyState === WebSocket.OPEN) {
    const messagePayload = {
      contenido: contenido,
      tipoMensaje: 'TEXTO',
    };
    webSocket.value.send(JSON.stringify(messagePayload));
  }
};

const openAddUserModal = () => {
    addUserModal.value = true;
    fetchUsers();
};

const createPrivateConversation = async (otherUserId) => {
    try {
        const newConv = await servicioApi.crearConversacionPrivada(otherUserId);
        addUserModal.value = false;
        await fetchConversations();
        selectConversation(newConv);
    } catch (error) {
        console.error('Error creating conversation:', error);
        alert('Error al crear la conversación. Es posible que ya exista.');
    }
};

// --- Ciclo de Vida ---
onMounted(() => {
  if (usuario.value) {
    fetchConversations();
  }
});
</script>

<style scoped>
/* Estilos globales de la estructura de la vista */
.chat-app-root, .chat-main-fill, .chat-container-fill, .chat-shell, .chat-main-row {
  height: 100%;
  margin: 0;
  padding: 0;
}
.sidebar-col, .chat-col {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0; /* Clave para que el scroll funcione */
}
@media (max-width: 959px) {
  .chat-main-row {
    flex-direction: column;
    flex-wrap: nowrap;
  }
  .sidebar-col {
    flex: 0 1 auto;
    max-height: min(48vh, 440px);
    border-inline-end: none;
    border-bottom: 1px solid rgba(var(--v-theme-accent), 0.25);
  }
  .chat-col {
    flex: 1 1 auto;
  }
}
</style>
