import { createRouter, createWebHistory } from 'vue-router'
import LoginVista from './vistas/LoginVista.vue'
import ChatVista from './vistas/ChatVista.vue'
import RegistroVista from './vistas/RegistroVista.vue' // Importar la nueva vista
import { useAlmacen } from './almacen'

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
    path: '/registro', // Añadir la nueva ruta
    name: 'Registro',
    component: RegistroVista
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatVista,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory('/chat-empresarial/'),
  routes
})

router.beforeEach((to, from, next) => {
  const almacen = useAlmacen()
  const estaAutenticado = almacen.token

  if (to.meta.requiresAuth && !estaAutenticado) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/registro') && estaAutenticado) {
    // Si el usuario está autenticado, no debe poder acceder a login/registro
    next('/chat')
  } else {
    next()
  }
})

export default router
