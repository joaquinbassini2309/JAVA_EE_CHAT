import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { defaultThemeName, temaProyecto } from '@/configuracion/temaProyecto.js'

// 1. Importar la fuente de íconos de Material Design Icons
import '@mdi/font/css/materialdesignicons.css'

// 2. (Opcional pero recomendado) Mantener los alias para usar nombres amigables
const iconos = {
  defaultSet: 'mdi',
  aliases: {
    usuario: 'mdi-account',
    correo: 'mdi-email',
    candado: 'mdi-lock',
    enviar: 'mdi-send',
    menu: 'mdi-menu',
    cerrar: 'mdi-close',
    cerrarSesion: 'mdi-logout',
    buscar: 'mdi-magnify',
    añadirContacto: 'mdi-account-plus',
    grupo: 'mdi-account-group',
    adjuntar: 'mdi-paperclip',
    volver: 'mdi-arrow-left',
    opciones: 'mdi-cog-outline',
    crear: 'mdi-plus',
    confirmar: 'mdi-check',
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
  // 3. Usar la nueva configuración de íconos
  icons: iconos,
})
