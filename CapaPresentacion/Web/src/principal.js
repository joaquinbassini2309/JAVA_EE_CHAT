import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './rutas/enrutador'
import vuetify from './complementos/vuetify.js'
import Aplicacion from './Aplicacion.vue'
import './recursos/estilos.css'

const app = createApp(Aplicacion)

app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
