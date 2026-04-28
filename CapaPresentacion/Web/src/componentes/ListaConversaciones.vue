<template>
  <div class="pa-3 pb-2 flex-shrink-0">
    <div class="profile-header">
      <div class="profile-banner profile-banner--default"/>
      <div class="profile-lower">
        <div class="profile-avatar-wrap">
          <v-avatar class="profile-avatar-box" color="accent" size="72" rounded="lg">
            <span class="text-white text-subtitle-1 font-weight-medium">{{ yo.iniciales }}</span>
          </v-avatar>
        </div>
        <div class="profile-title-row">
          <div class="profile-name text-truncate">{{ yo.nombre }}</div>
          <v-btn icon="mdi-account-plus" variant="tonal" color="accent" size="small" density="comfortable" @click="abrirModalUsuario"/>
        </div>
        <div class="profile-subtitle text-truncate">{{ yo.subtitulo }}</div>
        <div class="profile-actions">
          <v-text-field v-model="contactSearch" density="compact" variant="solo-filled" flat hide-details placeholder="Buscar contactos" prepend-inner-icon="mdi-magnify" class="profile-search" bg-color="rgba(255,255,255,0.55)" color="accent"/>
        </div>
      </div>
    </div>
  </div>
  <v-divider opacity="0.2" color="accent" />
  <div class="sidebar-list-scroll">
    <v-list density="comfortable" lines="two" class="bg-transparent py-2">
      <v-list-subheader class="heading-14 text-uppercase list-subheader-muted">Conversaciones</v-list-subheader>
      <v-list-item
        v-for="conv in conversacionesFiltradas"
        :key="conv.id"
        rounded="lg"
        class="mx-2 mb-1"
        :active="conversacionActiva?.id === conv.id"
        color="accent"
        @click="seleccionarConversacion(conv)"
      >
        <template #prepend>
          <v-avatar color="secondary" size="40" class="text-caption">{{ conv.nombre.charAt(0).toUpperCase() }}</v-avatar>
        </template>
        <v-list-item-title class="font-weight-medium">{{ conv.nombre }}</v-list-item-title>
        <v-list-item-subtitle>Último mensaje...</v-list-item-subtitle>
      </v-list-item>
    </v-list>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useAlmacen } from '@/almacen'; // Corregido
import { servicioApi } from '@/services/api'; // Corregido

const almacen = useAlmacen();

const conversaciones = computed(() => almacen.conversacionesOrdenadas);
const conversacionActiva = computed(() => almacen.conversacionActual);
const usuario = computed(() => almacen.usuario);

const contactSearch = ref('');

const yo = computed(() => {
    if (!usuario.value) return { id: null, nombre: '', iniciales: '', subtitulo: '' };
    return {
        id: usuario.value.id,
        nombre: usuario.value.username,
        iniciales: usuario.value.username.charAt(0).toUpperCase(),
        subtitulo: 'En línea',
    }
});

const conversacionesFiltradas = computed(() => {
  if (!contactSearch.value) {
    return conversaciones.value;
  }
  const termino = contactSearch.value.toLowerCase();
  return conversaciones.value.filter(c => c.nombre.toLowerCase().includes(termino));
});

const seleccionarConversacion = async (conv) => {
  if (almacen.conversacionActual?.id === conv.id) return;

  almacen.establecerConversacionActual(conv);

  try {
    const mensajes = await servicioApi.obtenerMensajes(conv.id);
    almacen.establecerMensajes(mensajes);
  } catch (error) {
    console.error("Error al cargar mensajes:", error);
  }
};

const abrirModalUsuario = () => {
  console.log("Abrir modal para añadir usuario");
};
</script>

<style scoped>
/* Tus estilos se mantienen intactos */
.sidebar-list-scroll { flex: 1 1 auto; min-height: 0; overflow-y: auto; }
.profile-header { position: relative; border-radius: 12px; overflow: hidden; margin-bottom: 4px; }
.profile-banner { height: 76px; background-size: cover; background-position: center; background-repeat: no-repeat; }
.profile-banner--default { background-color: rgb(var(--v-theme-primary)); background-image: linear-gradient(135deg, rgba(var(--v-theme-accent), 0.2) 0%, transparent 55%), linear-gradient(225deg, rgba(255, 255, 255, 0.45) 0%, transparent 48%), radial-gradient(ellipse 90% 140% at 15% 0%, rgba(var(--v-theme-accent), 0.18), transparent); }
.profile-lower { position: relative; background: rgb(var(--v-theme-primary)); padding: 14px 14px 16px; padding-inline-start: 108px; min-height: 88px; }
.profile-avatar-wrap { position: absolute; z-index: 2; inset-inline-start: 14px; top: -38px; }
.profile-avatar-box { border-radius: 12px; border: 3px solid rgb(var(--v-theme-surface)); box-shadow: 0 4px 14px rgba(var(--v-theme-accent), 0.22); }
.profile-title-row { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.profile-name { font-size: 1.15rem; font-weight: 700; color: rgb(var(--v-theme-on-primary)); line-height: 1.25; letter-spacing: 0.01em; }
.profile-subtitle { font-size: 12px; color: rgb(var(--v-theme-accent)); line-height: 1.4; margin-top: 4px; opacity: 0.9; }
.profile-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-top: 12px; }
.heading-14 { font-size: 14px; font-weight: 500; line-height: 1.3; letter-spacing: 0.01em; color: rgb(var(--v-theme-accent)); }
:global(.profile-search .v-field__input::placeholder) { color: rgba(var(--v-theme-accent), 0.45); opacity: 1; }
:global(.profile-search .v-icon) { opacity: 0.75; }
</style>
