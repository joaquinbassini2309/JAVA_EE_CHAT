import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Corregido: Apuntar al directorio del router que funciona
import router from './router'
import vuetify from './plugins/vuetify.js'
import App from './App.vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
