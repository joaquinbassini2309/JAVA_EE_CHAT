import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './rutas/enrutador'
import vuetify from './complementos/vuetify.js'
import Aplicacion from './Aplicacion.vue'
import './recursos/estilos.css'

const app = createApp(Aplicacion)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(vuetify)

// Cargar sesión guardada antes de montar
import { useAlmacen } from './almacenes/almacen'
const almacen = useAlmacen(pinia)
almacen.cargarDelAlmacenamientoLocal()

app.mount('#app')
