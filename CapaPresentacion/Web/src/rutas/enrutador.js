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
    name: 'login',
    component: LoginVista
  },
  {
    path: '/chat',
    name: 'chat',
    component: ChatVista
  },
  {
    path: '/registro',
    name: 'registro',
    component: RegistroVista
  }
]

const router = createRouter({
  history: createWebHistory('/chat-empresarial/'),
  routes
})

export default router
