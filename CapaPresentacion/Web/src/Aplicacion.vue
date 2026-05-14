<template>
  <div id="app-root">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'

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

html, body {
  height: 100%;
  width: 100%;
}

body {
  font-family: 'Roboto', sans-serif;
  background-color: #e4f6f9;
  overflow: hidden;
}

#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

#app-root {
  width: 100%;
  height: 100%;
}

/* Responsive font sizes */
@media (max-width: 768px) {
  body {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  body {
    font-size: 12px;
  }
}
</style>
