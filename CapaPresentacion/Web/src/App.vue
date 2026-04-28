<template>
  <div id="app-root">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAlmacen } from '@/stores'

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
  font-family: 'Roboto', sans-serif;
  background-color: #e4f6f9; /* background */
}

#app {
  width: 100%;
  height: 100vh;
}
</style>
