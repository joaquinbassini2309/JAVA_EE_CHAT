import { createRouter, createWebHistory } from 'vue-router'

import LoginVista from '../vistas/LoginVista.vue'
import ChatVista from '../vistas/ChatVista.vue'
import RegistroVista from '../vistas/RegistroVista.vue'


const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginVista
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatVista
    // La protección de ruta se ha movido a la propia vista
  },
  {
    path: '/registro',
    name: 'Registro',
    component: RegistroVista
  }
]

const router = createRouter({
  history: createWebHistory('/'),
  routes
})

export default router
