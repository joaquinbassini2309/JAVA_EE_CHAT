import { createRouter, createWebHistory } from 'vue-router'
import LoginVista from '../views/LoginVista.vue'
import ChatVista from '../views/ChatVista.vue'
import RegistroVista from '../views/RegistroVista.vue'

const routes = [
  {
    path: '/',
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
  history: createWebHistory(),
  routes
})

export default router
