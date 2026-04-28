import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { mdi } from 'vuetify/iconsets/mdi-svg'
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
  mdiAccountGroup,
  mdiArrowLeft,
  mdiCogOutline,
  mdiPlus,
  mdiCheck,
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
    grupo: mdiAccountGroup,
    adjuntar: mdiPaperclip,
    volver: mdiArrowLeft,
    opciones: mdiCogOutline,
    crear: mdiPlus,
    confirmar: mdiCheck,
  },
  sets: {
    mdi,
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
