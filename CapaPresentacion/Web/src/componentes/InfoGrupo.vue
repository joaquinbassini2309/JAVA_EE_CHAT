<template>
  <div class="info-grupo">

    <!-- Encabezado con banner + avatar cuadrado (mismo patrón que Chat.vue) -->
    <div class="info-header">
      <div class="profile-banner profile-banner--default" />
      <div class="profile-lower">
        <div class="profile-avatar-wrap">
          <div class="avatar-cuadrado">
            {{ conversacion.nombre?.charAt(0).toUpperCase() || 'G' }}
          </div>
        </div>
        <div class="profile-title-row">
          <button class="btn-atras" @click="$emit('volver')" title="Volver">
            <v-icon size="20" color="#406D73">mdi-arrow-left</v-icon>
          </button>
          <span class="profile-name text-truncate">{{ conversacion.nombre }}</span>
        </div>
        <div class="profile-subtitle">
          {{ conversacion.participantes?.length || 0 }} miembros
        </div>
      </div>
    </div>

    <!-- Cuerpo scrolleable -->
    <div class="info-cuerpo">

      <!-- Sección integrantes -->
      <div class="seccion">
        <div class="seccion-titulo">
          <v-icon size="16" color="#406D73" class="mr-1">mdi-account-group</v-icon>
          Integrantes
        </div>
        <div class="lista-miembros">
          <div
              v-for="miembro in conversacion.participantes"
              :key="miembro.id"
              class="item-miembro"
          >
            <img
                v-if="miembro.fotoUrl"
                :src="miembro.fotoUrl"
                class="avatar-foto"
            />
            <div v-else class="avatar-mini">
              {{ miembro.username.charAt(0).toUpperCase() }}
            </div>
            <div class="info-usuario">
              <span class="nombre">{{ miembro.username }}</span>
              <span class="estado">{{ miembro.estado || 'Disponible' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sección opciones adicionales -->
      <div class="seccion seccion-placeholder">
        <div class="seccion-titulo">
          <v-icon size="16" color="#406D73" class="mr-1">mdi-cog-outline</v-icon>
          Opciones adicionales
        </div>
        <p class="placeholder-text">Falta implementar: Archivos, Silenciar, Salir del grupo, etc.</p>
      </div>

    </div>
  </div>
</template>

<script setup>
defineProps({
  conversacion: {
    type: Object,
    required: true,
  },
})

defineEmits(['volver'])
</script>

<style scoped>
/* ---- Raíz ---- */
.info-grupo {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f7fcfd;
  overflow: hidden;
}

/* ---- Encabezado (igual patrón que Chat.vue) ---- */
.info-header {
  flex-shrink: 0;
}

.profile-banner {
  height: 76px;
  background-color: #B3EBF2;
  background-image:
      linear-gradient(135deg, rgba(64,109,115,0.22) 0%, transparent 55%),
      linear-gradient(225deg, rgba(255,255,255,0.45) 0%, transparent 48%),
      radial-gradient(ellipse 90% 140% at 15% 0%, rgba(64,109,115,0.15), transparent);
  background-size: cover;
}

.profile-lower {
  position: relative;
  background: #f0f7f8;
  padding: 8px 14px 14px;
  padding-left: 100px;
  min-height: 72px;
}

.profile-avatar-wrap {
  position: absolute;
  left: 14px;
  top: -32px;
  z-index: 2;
}

.avatar-cuadrado {
  width: 64px;
  height: 64px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 10px;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 14px rgba(64,109,115,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
}

.profile-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-atras {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  flex-shrink: 0;
  transition: background 0.15s;
}

.btn-atras:hover {
  background: rgba(64,109,115,0.1);
}

.profile-name {
  font-size: 1rem;
  font-weight: 700;
  color: #2f4a4f;
  line-height: 1.25;
  letter-spacing: 0.01em;
}

.profile-subtitle {
  font-size: 12px;
  color: #5a8a94;
  margin-top: 2px;
}

/* ---- Cuerpo ---- */
.info-cuerpo {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ---- Secciones ---- */
.seccion {
  background: #ffffff;
  border-radius: 12px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(64,109,115,0.08);
  border: 1px solid rgba(64,109,115,0.1);
}

.seccion-titulo {
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 12px;
}

/* ---- Lista de miembros ---- */
.lista-miembros {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-miembro {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-foto {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.avatar-mini {
  width: 38px;
  height: 38px;
  min-width: 38px;
  background: #B2C5C8;
  color: #2f4a4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  flex-shrink: 0;
}

.info-usuario {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.info-usuario .nombre {
  font-weight: 600;
  font-size: 14px;
  color: #2f4a4f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-usuario .estado {
  font-size: 12px;
  color: #7f9ea4;
}

/* ---- Placeholder ---- */
.seccion-placeholder {
  opacity: 0.75;
}

.placeholder-text {
  font-style: italic;
  font-size: 13px;
  color: #7f9ea4;
  margin: 0;
}
</style>