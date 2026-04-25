import { temaProyecto, defaultThemeName } from './temaProyecto.js';

const { createApp, ref, reactive, onMounted, nextTick, computed } = Vue;
const { createVuetify } = Vuetify;

const vuetify = createVuetify({
  theme: {
    defaultTheme: defaultThemeName,
    themes: {
      temaProyecto,
    },
  },
});

const app = createApp({
  setup() {
    // --- Estado Reactivo ---
    const token = localStorage.getItem('jwt');
    const usuario = JSON.parse(localStorage.getItem('usuario'));
    
    const conversations = ref([]);
    const users = ref([]);
    const activeConversation = reactive({
      id: null,
      nombre: 'Selecciona un chat',
      mensajes: [],
      participantes: [],
    });
    
    const newMessage = ref('');
    const contactSearch = ref('');
    const addUserModal = ref(false);
    const webSocket = ref(null);
    const messageListRef = ref(null);

    // --- Protección de Ruta ---
    if (!token || !usuario) {
      window.location.href = 'InicioSesion.html';
      return;
    }

    // --- Propiedades Computadas ---
    const yo = computed(() => ({
        nombre: usuario.username,
        iniciales: usuario.username.charAt(0).toUpperCase(),
        subtitulo: 'En línea',
    }));

    const destinatario = computed(() => {
        if (!activeConversation.id) {
            return { nombre: 'Chat', iniciales: '?', subtitulo: 'Selecciona una conversación' };
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
        const response = await fetch('/chat-empresarial/api/v1/conversaciones', {
          headers: { 'Authorization': `Bearer ${token}` },
        });
        if (response.ok) {
          conversations.value = await response.json();
        }
      } catch (error) {
        console.error('Error fetching conversations:', error);
      }
    };

    const fetchMessages = async (convId) => {
      try {
        const response = await fetch(`/chat-empresarial/api/v1/conversaciones/${convId}/mensajes`, {
          headers: { 'Authorization': `Bearer ${token}` },
        });
        if (response.ok) {
          activeConversation.mensajes = await response.json();
          scrollToBottom();
        }
      } catch (error) {
        console.error('Error fetching messages:', error);
      }
    };

    const fetchUsers = async () => {
        try {
            const response = await fetch('/chat-empresarial/api/v1/usuarios', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const allUsers = await response.json();
                users.value = allUsers.filter(u => u.id !== usuario.id);
            }
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
      const wsUrl = `${wsProtocol}//${window.location.host}/chat-empresarial/api/v1/websocket/conversacion/${convId}/usuario/${usuario.id}?token=${token}`;
      
      const ws = new WebSocket(wsUrl);

      ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.tipo === 'mensaje' && data.datos.conversacionId === activeConversation.id) {
          activeConversation.mensajes.push(data.datos);
          scrollToBottom();
        }
      };

      ws.onclose = () => console.log('WebSocket disconnected');
      ws.onerror = (error) => console.error('WebSocket error:', error);

      webSocket.value = ws;
    };

    const sendMessage = () => {
      if (newMessage.value.trim() && webSocket.value && webSocket.value.readyState === WebSocket.OPEN) {
        const messagePayload = {
          contenido: newMessage.value,
          tipoMensaje: 'TEXTO',
        };
        webSocket.value.send(JSON.stringify(messagePayload));
        newMessage.value = '';
      }
    };
    
    const openAddUserModal = () => {
        addUserModal.value = true;
        fetchUsers();
    };

    const createPrivateConversation = async (otherUserId) => {
        try {
            const response = await fetch('/chat-empresarial/api/v1/conversaciones', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    tipo: 'PRIVADA',
                    participanteIds: [otherUserId]
                })
            });

            if (response.ok) {
                const newConv = await response.json();
                addUserModal.value = false;
                await fetchConversations();
                selectConversation(newConv);
            } else if (response.status === 409) {
                alert('Ya tienes una conversación con este usuario.');
            } else {
                alert('Error al crear la conversación.');
            }
        } catch (error) {
            console.error('Error creating conversation:', error);
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

    // --- Ciclo de Vida ---
    onMounted(() => {
      fetchConversations();
    });

    return {
      // Estado
      yo,
      destinatario,
      conversations,
      users,
      activeConversation,
      newMessage,
      contactSearch,
      addUserModal,
      messageListRef,
      // Métodos
      selectConversation,
      sendMessage,
      openAddUserModal,
      createPrivateConversation,
    };
  },
  template: `
      <v-app class="chat-app-root">
        <v-main class="chat-main-fill bg-background pa-0">
          <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
            <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
              <v-row no-gutters class="chat-main-row">
                <!-- Columna de la Barra Lateral -->
                <v-col cols="12" md="4" class="sidebar-col">
                  <div class="pa-3 pb-2 flex-shrink-0">
                    <div class="profile-header">
                      <div class="profile-banner profile-banner--default"/>
                      <div class="profile-lower">
                        <div class="profile-avatar-wrap">
                          <v-avatar class="profile-avatar-box" color="accent" size="72" rounded="lg">
                            <span class="text-white text-subtitle-1 font-weight-medium">{{ yo.iniciales }}</span>
                          </v-avatar>
                        </div>
                        <div class="profile-title-row">
                          <div class="profile-name text-truncate">{{ yo.nombre }}</div>
                          <v-btn icon="mdi-account-plus" variant="tonal" color="accent" size="small" density="comfortable" @click="openAddUserModal"/>
                        </div>
                        <div class="profile-subtitle text-truncate">{{ yo.subtitulo }}</div>
                        <div class="profile-actions">
                          <v-text-field v-model="contactSearch" density="compact" variant="solo-filled" flat hide-details placeholder="Buscar contactos" prepend-inner-icon="mdi-magnify" class="profile-search" bg-color="rgba(255,255,255,0.55)" color="accent"/>
                        </div>
                      </div>
                    </div>
                  </div>
                  <v-divider opacity="0.2" color="accent" />
                  <div class="sidebar-list-scroll">
                    <v-list density="comfortable" lines="two" class="bg-transparent py-2">
                      <v-list-subheader class="heading-14 text-uppercase list-subheader-muted">Conversaciones</v-list-subheader>
                      <v-list-item
                        v-for="conv in conversations.filter(c => c.nombre.toLowerCase().includes(contactSearch.toLowerCase()))"
                        :key="conv.id"
                        rounded="lg"
                        class="mx-2 mb-1"
                        :active="activeConversation.id === conv.id"
                        color="accent"
                        @click="selectConversation(conv)"
                      >
                        <template #prepend>
                          <v-avatar color="secondary" size="40" class="text-caption">{{ conv.nombre.charAt(0).toUpperCase() }}</v-avatar>
                        </template>
                        <v-list-item-title class="font-weight-medium">{{ conv.nombre }}</v-list-item-title>
                        <v-list-item-subtitle>Último mensaje...</v-list-item-subtitle>
                      </v-list-item>
                    </v-list>
                  </div>
                </v-col>
                <!-- Columna del Chat -->
                <v-col cols="12" md="8" class="chat-col">
                  <template v-if="activeConversation.id">
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
                      <div v-for="msg in activeConversation.mensajes" :key="msg.id" class="bubble" :class="msg.emisorId === yo.id ? 'bubble-me' : 'bubble-them'">
                        {{ msg.contenido }}
                      </div>
                    </div>
                    <v-divider opacity="0.2" color="accent" />
                    <div class="pa-3 bg-surface flex-shrink-0">
                      <div class="input-bar">
                        <v-text-field v-model="newMessage" density="comfortable" variant="outlined" hide-details placeholder="Escribe un mensaje..." color="accent" bg-color="secondary" class="rounded-lg grow" @keyup.enter="sendMessage"/>
                        <v-btn class="btn-11" color="accent" variant="flat" prepend-icon="mdi-send" @click="sendMessage" :disabled="!newMessage.trim()">Enviar mensaje</v-btn>
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <div class="d-flex flex-column align-center justify-center h-100">
                        <v-icon size="80" color="accent" class="mb-4">mdi-message-text-outline</v-icon>
                        <p class="heading-14">Selecciona una conversación para empezar</p>
                    </div>
                  </template>
                </v-col>
              </v-row>
            </v-sheet>
          </v-container>
        </v-main>
        <!-- Modal para Añadir Usuario -->
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
    `
});

app.use(vuetify);
app.mount('#app');
