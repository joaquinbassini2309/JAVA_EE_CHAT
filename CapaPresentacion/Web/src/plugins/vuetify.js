import { createVuetify } from 'CapaPresentacion/Web/src/plugins/vuetify.js'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { mdiAccount, mdiEmail, mdiLock, mdiSend, mdiMenu, mdiClose, mdiLogout } from '@mdi/js'

// Tema personalizado con colores del proyecto
const tema = {
  dark: false,
  colors: {
    primary: '#667eea',
    secondary: '#764ba2',
    accent: '#82B1FF',
    error: '#FF5252',
    warning: '#FFC107',
    info: '#2196F3',
    success: '#4CAF50',
    background: '#f5f5f5',
    surface: '#ffffff'
  }
}

// Configuración de iconos
const iconos = {
  defaultSet: 'mdi',
  aliases: {
    usuario: mdiAccount,
    correo: mdiEmail,
    candado: mdiLock,
    enviar: mdiSend,
    menu: mdiMenu,
    cerrar: mdiClose,
    cerrarSesion: mdiLogout
  },
  sets: {
    mdi: {
      component: (props) => ({
        template: `<svg style="width:24px;height:24px" viewBox="0 0 24 24"><path :d="path" /></svg>`,
        props: ['path'],
        setup() {
          return {
            path: props.icon
          }
        }
      })
    }
  }
}

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'tema',
    themes: {
      tema
    }
  },
  icons: iconos
})
