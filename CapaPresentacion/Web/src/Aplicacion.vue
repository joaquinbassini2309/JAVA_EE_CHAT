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
/* Forzar barras de scroll visibles en toda la app */
::-webkit-scrollbar {
  width: 10px !important;
  height: 10px !important;
  display: block !important;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05) !important;
}

::-webkit-scrollbar-thumb {
  background: #406D73 !important;
  border-radius: 5px !important;
  border: 1px solid white !important;
}

::-webkit-scrollbar-thumb:hover {
  background: #2f4a4f !important;
}

/* Para Firefox */
* {
  scrollbar-width: thin;
  scrollbar-color: #406D73 rgba(0, 0, 0, 0.05);
}
</style>
