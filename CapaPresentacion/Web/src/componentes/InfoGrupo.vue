<template>
  <div class="info-grupo">
    <div class="info-encabezado">
      <button class="btn-atras" @click="$emit('volver')">
        <v-icon>mdi-arrow-left</v-icon>
      </button>
      <h2>Información del grupo</h2>
    </div>

    <div class="info-cuerpo">
      <div class="grupo-perfil">
        <div class="avatar-grupo">
          {{ conversacion.nombre?.charAt(0).toUpperCase() || 'G' }}
        </div>
        <h3>{{ conversacion.nombre }}</h3>
        <p class="miembros-count">{{ conversacion.participantes?.length || 0 }} miembros</p>
      </div>

      <div class="seccion-info">
        <h4>Integrantes</h4>
        <div class="lista-miembros">
          <div v-for="miembro in conversacion.participantes" :key="miembro.id" class="item-miembro">
            <img v-if="miembro.fotoUrl" :src="miembro.fotoUrl" class="foto-perfil" />
            <div v-else class="avatar-mini">{{ miembro.username.charAt(0).toUpperCase() }}</div>
            <div class="info-usuario">
              <span class="nombre">{{ miembro.username }}</span>
              <span class="estado">{{ miembro.estado || 'Disponible' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="seccion-placeholder">
        <h4>Opciones adicionales</h4>
        <p class="placeholder-text">Falta implementar: Archivos, Silenciar, Salir del grupo, etc.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  conversacion: {
    type: Object,
    required: true
  }
})

defineEmits(['volver'])
</script>

<style scoped>
.info-grupo {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f0f7f8;
}

.info-encabezado {
  padding: 16px;
  background-color: #406D73;
  color: white;
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-atras {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 20px;
  display: flex;
  align-items: center;
}

.info-cuerpo {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.grupo-perfil {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.avatar-grupo {
  width: 120px;
  height: 120px;
  background-color: #B3EBF2;
  color: #2f4a4f;
  font-size: 48px;
  font-weight: bold;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.miembros-count {
  color: #666;
  font-size: 14px;
}

.seccion-info {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.seccion-info h4, .seccion-placeholder h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #406D73;
}

.lista-miembros {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-miembro {
  display: flex;
  align-items: center;
  gap: 12px;
}

.foto-perfil, .avatar-mini {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-mini {
  background-color: #ced6d9;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  color: #2f4a4f;
}

.info-usuario {
  display: flex;
  flex-direction: column;
}

.info-usuario .nombre {
  font-weight: bold;
}

.info-usuario .estado {
  font-size: 12px;
  color: #888;
}

.seccion-placeholder {
  background: white;
  border-radius: 12px;
  padding: 16px;
  opacity: 0.7;
}

.placeholder-text {
  font-style: italic;
  color: #666;
}
</style>
