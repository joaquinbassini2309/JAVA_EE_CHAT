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

import { useAlmacen } from '../almacenes/almacen'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Guardia de navegación
router.beforeEach((to, from, next) => {
  const almacen = useAlmacen()
  const autenticado = almacen.estaAutenticado

  if (to.path === '/') {
    next(autenticado ? '/chat' : '/login')
  } else if (to.path === '/chat' && !autenticado) {
    next('/login')
  } else if (to.path === '/login' && autenticado) {
    next('/chat')
  } else {
    next()
  }
})

export default router
