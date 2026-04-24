/**
 * Demo estática: Vue y Vuetify vienen como globales desde el HTML.
 * Con `npm run dev`, se importa el tema desde `src/config/temaProyecto.js`.
 * Fuera de Vite (p. ej. file://), se usa el fallback (mantener alineado con ese archivo).
 */
const TEMA_VISTAPRINCIPAL_FALLBACK = {
  defaultThemeName: 'temaProyecto',
  temaProyecto: {
    dark: false,
    colors: {
      primary: '#B3EBF2',
      secondary: '#B2C5C8',
      accent: '#406D73',
      error: '#C85A5A',
      warning: '#D4A574',
      info: '#5A8A94',
      success: '#6A9E7D',
      background: '#e4f6f9',
      surface: '#f7fcfd',
      'on-primary': '#2a4d52',
      'on-secondary': '#2f4a4f',
      'on-surface': '#2f4a4f',
    },
  },
};

async function iniciarVistaPrincipal() {
  const { createApp } = window.Vue;
  const { createVuetify } = window.Vuetify;

  let temaMod = null;
  try {
    temaMod = await import('/src/config/temaProyecto.js');
  } catch {
    temaMod = null;
  }

  const defaultThemeName = temaMod?.defaultThemeName ?? TEMA_VISTAPRINCIPAL_FALLBACK.defaultThemeName;
  const temaProyecto = temaMod?.temaProyecto ?? TEMA_VISTAPRINCIPAL_FALLBACK.temaProyecto;

  const vuetify = createVuetify({
    theme: {
      defaultTheme: defaultThemeName,
      themes: {
        [defaultThemeName]: temaProyecto,
      },
    },
  });

  createApp({
    data() {
      return {
        busqueda: '',
        pestañaLista: 'canales',
        yo: {
          nombre: 'Tu nombre',
          iniciales: 'TN',
          subtitulo: 'Aquí irá la descripción del usuario',
          fotoPerfil: null,
          bannerUrl: null,
        },
        destinatario: {
          nombre: 'Nombre del usuario con el que estás hablando',
          iniciales: 'US',
          subtitulo: 'En conversación',
          fotoPerfil: null,
          bannerUrl: null,
        },
        contactos: [
          { nombre: 'Ana López', detalle: 'Equipo ventas', iniciales: 'AL' },
          { nombre: 'Carlos Ruiz', detalle: 'Soporte', iniciales: 'CR' },
          { nombre: 'Equipo RR.HH.', detalle: 'Canal interno', iniciales: 'RH' },
          { nombre: 'María Fernández', detalle: 'En línea', iniciales: 'MF' },
        ],
        mensajes: [
          { mio: false, texto: 'Buenos días, ¿tienes un momento para revisar el aviso?' },
          { mio: true, texto: 'Sí, en10 minutos lo veo y te escribo.' },
          { mio: false, texto: 'Perfecto, gracias.' },
        ],
        textoMensaje: '',
      };
    },
    methods: {
      estiloBanner(entidad) {
        const url = entidad && entidad.bannerUrl;
        if (!url) return {};
        return { backgroundImage: `url("${url}")` };
      },
      enviar() {
        const t = (this.textoMensaje || '').trim();
        if (!t) return;
        this.mensajes.push({ mio: true, texto: t });
        this.textoMensaje = '';
        this.$nextTick(() => {
          const el = this.$refs.areaMensajes;
          if (el) el.scrollTop = el.scrollHeight;
        });
      },
      adjuntar() {
        alert('Aquí iría la selección de archivos adjuntos (demo).');
      },
      añadirContactoOGrupo() {
        alert('Aquí se abriría el flujo para añadir contactos o grupos (demo).');
      },
    },
    template: `
      <v-app class="chat-app-root">
        <v-main class="chat-main-fill bg-background pa-0">
          <v-container fluid class="chat-container-fill pa-2 pa-sm-3 pa-md-4">
            <v-sheet class="chat-shell bg-surface d-flex flex-column flex-grow-1" elevation="0">
              <v-row no-gutters class="chat-main-row">
                <v-col cols="12" md="4" class="sidebar-col">
                  <div class="pa-3 pb-2 flex-shrink-0">
                    <div class="profile-header">
                      <div
                        class="profile-banner"
                        :class="{ 'profile-banner--default': !yo.bannerUrl }"
                        :style="estiloBanner(yo)"
                      />
                      <div class="profile-lower">
                        <div class="profile-avatar-wrap">
                          <v-avatar
                            class="profile-avatar-box"
                            color="accent"
                            size="72"
                            rounded="lg"
                          >
                            <v-img v-if="yo.fotoPerfil" :src="yo.fotoPerfil" cover alt="" />
                            <span v-else class="text-white text-subtitle-1 font-weight-medium">{{ yo.iniciales }}</span>
                          </v-avatar>
                        </div>
                        <div class="profile-title-row">
                          <div class="profile-name text-truncate">{{ yo.nombre }}</div>
                          <v-btn
                            icon="mdi-account-plus"
                            variant="tonal"
                            color="accent"
                            size="small"
                            density="comfortable"
                            class="flex-shrink-0"
                            aria-label="Añadir contacto o grupo"
                            @click="añadirContactoOGrupo"
                          />
                        </div>
                        <div class="profile-subtitle text-truncate">{{ yo.subtitulo }}</div>
                        <div class="profile-actions">
                          <v-text-field
                            v-model="busqueda"
                            density="compact"
                            variant="solo-filled"
                            flat
                            hide-details
                            placeholder="Buscar contactos"
                            prepend-inner-icon="mdi-magnify"
                            class="profile-search"
                            bg-color="rgba(255,255,255,0.55)"
                            color="accent"
                          />
                        </div>
                      </div>
                    </div>

                    <v-btn-toggle
                      v-model="pestañaLista"
                      mandatory
                      divided
                      color="accent"
                      variant="outlined"
                      class="mt-3 w-100"
                      density="comfortable"
                    >
                      <v-btn value="canales" class="flex-grow-1 text-caption">
                        Ver canales de aviso
                      </v-btn>
                      <v-btn value="tareas" class="flex-grow-1 text-caption">
                        Lista de tareas
                      </v-btn>
                    </v-btn-toggle>
                  </div>

                  <v-divider opacity="0.2" color="accent" />

                  <div class="sidebar-list-scroll">
                    <v-list density="comfortable" lines="two" class="bg-transparent py-2">
                      <v-list-subheader class="heading-14 text-uppercase list-subheader-muted">
                        Lista de usuario y miembros para hablar
                      </v-list-subheader>
                      <v-list-item
                        v-for="(c, i) in contactos"
                        :key="i"
                        rounded="lg"
                        class="mx-2 mb-1"
                        :active="i === 0"
                        color="accent"
                      >
                        <template #prepend>
                          <v-avatar :color="i % 2 === 0 ? 'secondary' : 'primary'" size="40" class="text-caption">
                            {{ c.iniciales }}
                          </v-avatar>
                        </template>
                        <v-list-item-title class="font-weight-medium">{{ c.nombre }}</v-list-item-title>
                        <v-list-item-subtitle>{{ c.detalle }}</v-list-item-subtitle>
                      </v-list-item>
                    </v-list>
                  </div>
                </v-col>

                <v-col cols="12" md="8" class="chat-col">
                  <div class="pa-3 pb-2 flex-shrink-0">
                    <div class="profile-header">
                      <div
                        class="profile-banner"
                        :class="{ 'profile-banner--default': !destinatario.bannerUrl }"
                        :style="estiloBanner(destinatario)"
                      />
                      <div class="profile-lower profile-lower--chat">
                        <div class="profile-avatar-wrap">
                          <v-avatar
                            class="profile-avatar-box"
                            color="secondary"
                            size="72"
                            rounded="lg"
                          >
                            <v-img v-if="destinatario.fotoPerfil" :src="destinatario.fotoPerfil" cover alt="" />
                            <span v-else class="initials-on-secondary-avatar text-subtitle-1 font-weight-medium">{{ destinatario.iniciales }}</span>
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

                  <div ref="areaMensajes" class="messages-scroll flex-grow-1 pa-4 messages-stack">
                    <div
                      v-for="(m, j) in mensajes"
                      :key="j"
                      class="bubble"
                      :class="m.mio ? 'bubble-me' : 'bubble-them'"
                    >
                      {{ m.texto }}
                    </div>
                  </div>

                  <v-divider opacity="0.2" color="accent" />

                  <div class="pa-3 bg-surface flex-shrink-0">
                    <div class="input-bar">
                      <v-btn
                        class="btn-11"
                        color="accent"
                        variant="tonal"
                        prepend-icon="mdi-paperclip"
                        @click="adjuntar"
                      >
                        Archivos adjunto
                      </v-btn>
                      <v-text-field
                        v-model="textoMensaje"
                        density="comfortable"
                        variant="outlined"
                        hide-details
                        placeholder="Barra de escribir mensajes"
                        color="accent"
                        bg-color="secondary"
                        class="rounded-lg grow"
                        @keyup.enter="enviar"
                      />
                      <v-btn
                        class="btn-11"
                        color="accent"
                        variant="flat"
                        prepend-icon="mdi-send"
                        @click="enviar"
                      >
                        Enviar mensaje
                      </v-btn>
                    </div>
                  </div>
                </v-col>
              </v-row>
            </v-sheet>
          </v-container>
        </v-main>
      </v-app>
    `,
  })
    .use(vuetify)
    .mount('#app');
}

iniciarVistaPrincipal();
