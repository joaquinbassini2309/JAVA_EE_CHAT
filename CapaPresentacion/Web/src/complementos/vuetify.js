import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import {
  mdiAccount,
  mdiAccountPlus,
  mdiClose,
  mdiEmail,
  mdiLock,
  mdiLogout,
  mdiMagnify,
  mdiMenu,
  mdiPaperclip,
  mdiSend,
} from '@mdi/js'
import { defaultThemeName, temaProyecto } from '@/configuracion/temaProyecto.js'

const iconos = {
  defaultSet: 'mdi',
  aliases: {
    usuario: mdiAccount,
    correo: mdiEmail,
    candado: mdiLock,
    enviar: mdiSend,
    menu: mdiMenu,
    cerrar: mdiClose,
    cerrarSesion: mdiLogout,
    buscar: mdiMagnify,
    añadirContacto: mdiAccountPlus,
    adjuntar: mdiPaperclip,
  },
  sets: {
    mdi: {
      component: (props) => ({
        template: `<svg style="width:24px;height:24px" viewBox="0 0 24 24"><path :d="path" /></svg>`,
        props: {
          icon: { type: String, required: true },
        },
        setup(iconProps) {
          return { path: iconProps.icon }
        },
      }),
    },
  },
}

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: defaultThemeName,
    themes: {
      [defaultThemeName]: temaProyecto,
    },
  },
  icons: iconos,
})
