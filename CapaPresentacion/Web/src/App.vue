<template>
  <div id="app-root">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAlmacen } from '@/almacen'

const almacen = useAlmacen()

onMounted(() => {
  // Cargar datos del localStorage al iniciar
  almacen.cargarDelAlmacenamientoLocal()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100vh;
}
</style>
