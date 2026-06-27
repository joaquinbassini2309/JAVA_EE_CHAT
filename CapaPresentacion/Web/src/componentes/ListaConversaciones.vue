<template>
  <div class="lista-conversaciones">

    <!-- Header Compacto de Usuario -->
    <div class="panel-usuario-compact">
      <div class="user-header-row">
        <v-menu offset-y content-class="menu-perfil-flotante" transition="slide-y-transition">
          <template v-slot:activator="{ props }">
            <div class="user-avatar-wrap" v-bind="props" v-ripple>
              <img v-if="usuarioActual?.fotoUrl" :src="usuarioActual.fotoUrl" class="user-avatar-img" />
              <span v-else>{{ usuarioActual?.username?.charAt(0).toUpperCase() }}</span>
            </div>
          </template>
          <v-list class="bg-surface" density="compact">
            <v-list-item @click="abrirEdicionPerfil('username')" prepend-icon="mdi-account-edit">
              <v-list-item-title>Cambiar nombre de usuario</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionPerfil('fotoUrl')" prepend-icon="mdi-camera">
              <v-list-item-title>Cambiar Foto de perfil</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionPerfil('imagenBanner')" prepend-icon="mdi-image-area">
              <v-list-item-title>Fondo de pantalla del chat</v-list-item-title>
            </v-list-item>
            <v-list-item @click="abrirEdicionPerfil('descripcion')" prepend-icon="mdi-card-text-outline">
              <v-list-item-title>Cambiar descripción</v-list-item-title>
            </v-list-item>
            <v-divider class="my-1" />
            <v-list-item @click="alternarTema" :prepend-icon="almacen.temaOscuro ? 'mdi-weather-sunny' : 'mdi-weather-night'">
              <v-list-item-title>{{ almacen.temaOscuro ? 'Modo Claro' : 'Modo Oscuro' }}</v-list-item-title>
            </v-list-item>
            <v-divider class="my-1" />
            <v-list-item @click="cerrarSesionLocal" class="text-error" prepend-icon="mdi-logout">
              <v-list-item-title>Cerrar Sesión</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

        <div class="user-info-col">
          <span class="user-name text-truncate">{{ usuarioActual?.username }}</span>
        </div>

        <div class="user-actions-row">
          <button
            @click="abrirNuevoGrupo"
            title="Nuevo grupo"
            class="btn-cabecera-accion"
          >
            <v-icon size="17">mdi-account-group</v-icon>
          </button>
          <button
            @click="abrirNuevaConversacion"
            title="Nueva conversación"
            class="btn-cabecera-accion"
          >
            <v-icon size="17">mdi-chat-plus</v-icon>
          </button>
          <button
            @click="cerrarSesionLocal"
            title="Cerrar sesión"
            class="btn-cabecera-accion btn-logout"
          >
            <v-icon size="17">mdi-logout</v-icon>
          </button>
        </div>
      </div>

      <!-- Buscador -->
      <div class="campo-busqueda-modern">
        <v-menu :close-on-content-click="false" location="bottom start">
          <template v-slot:activator="{ props }">
            <button v-bind="props" title="Filtrar conversaciones" class="btn-filtro">
              <v-icon size="20">mdi-filter-variant</v-icon>
            </button>
          </template>
          <v-card class="menu-filtro-card" min-width="220" rounded="xl" style="overflow: hidden; box-shadow: 0 8px 24px rgba(64,109,115,0.15) !important;">
            <v-card-title class="menu-filtro-title">
              <v-icon size="16">mdi-filter-variant</v-icon>
              Mostrar en lista
            </v-card-title>
            <v-list density="compact" class="py-1 menu-filtro-list">
              <template v-if="tabActivo === 'chats'">
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Contactos" value="PRIVADA" color="primary" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Grupos" value="GRUPO" color="primary" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Canales de aviso" value="AVISO" color="primary" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosSeleccionados" label="Lista de tareas" value="TAREAS" color="primary" hide-details /></v-list-item>
              </template>
              <template v-else-if="tabActivo === 'tareas'">
                <v-list-item><v-checkbox-btn v-model="filtrosTareas" label="Tareas pendientes" value="PENDIENTES" color="primary" hide-details /></v-list-item>
                <v-list-item><v-checkbox-btn v-model="filtrosTareas" label="Tareas completadas" value="COMPLETADAS" color="primary" hide-details /></v-list-item>
              </template>
              <template v-else>
                <v-list-item><v-list-item-title class="text-caption text-grey py-2 px-2">Solo filtro por texto</v-list-item-title></v-list-item>
              </template>
            </v-list>
          </v-card>
        </v-menu>
        <div class="divider-v"></div>
        <v-icon size="17" color="#406D73" style="opacity:0.55;">mdi-magnify</v-icon>
        <input v-model="termino" type="text" placeholder="Buscar..." class="input-busqueda" />
      </div>
    </div>

    <!-- Navegación Vertical -->
    <div class="nav-vertical">
      <button class="nav-item" :class="{ activo: tabActivo === 'chats' }" @click="cambiarTab('chats')">
        <v-icon size="18">mdi-chat-outline</v-icon>
        <span>Mis Chats</span>
      </button>
      <button class="nav-item" :class="{ activo: tabActivo === 'canales' }" @click="cambiarTab('canales')">
        <v-icon size="18">mdi-bullhorn-outline</v-icon>
        <span>Canales de aviso</span>
      </button>
      <button class="nav-item" :class="{ activo: panelTareasAbierto }" @click="togglePanelTareas">
        <v-icon size="18">mdi-checkbox-marked-circle-outline</v-icon>
        <span>Lista de tareas</span>
      </button>
    </div>

    <!-- Carpetas de Filtrado de Conversaciones -->
    <div v-if="tabActivo === 'chats'" class="carpetas-conversaciones-container">
      <div class="carpetas-scroll-row">
        <button
          class="carpeta-pill"
          :class="{ activa: grupoFiltroActivo === null }"
          @click="grupoFiltroActivo = null"
        >
          Todos
        </button>
        <button
          v-for="grupo in gruposDeConversacion"
          :key="grupo"
          class="carpeta-pill"
          :class="{ activa: grupoFiltroActivo === grupo }"
          @click="grupoFiltroActivo = grupo"
        >
          <v-icon size="12" class="mr-1" style="opacity:0.8; color: inherit;">mdi-folder-outline</v-icon>
          <span>{{ grupo }}</span>
          <v-btn
            icon="mdi-close"
            variant="text"
            density="compact"
            size="x-small"
            color="white"
            class="ml-1 btn-eliminar-carpeta"
            @click.stop="eliminarGrupoConversacion(grupo)"
          />
        </button>
        <button class="carpeta-pill btn-crear-carpeta" @click="abrirCrearGrupoConversacion">
          <v-icon size="12" class="mr-1">mdi-plus</v-icon>
          Agrupar Chats
        </button>
      </div>
    </div>

    <!-- Subheader según tab -->
    <div v-if="tabActivo === 'chats'" class="seccion-chats-titulo">CHATS RECIENTES</div>
    <div v-else-if="tabActivo === 'canales'" class="seccion-chats-titulo">CANALES DE AVISOS</div>

    <!-- Listado conversaciones -->
    <div class="listado">
      <!-- Tab Chats -->
      <template v-if="tabActivo === 'chats'">
        <div
            v-for="conversacion in conversacionesFiltradas"
            :key="conversacion.id"
            class="item-conversacion"
            :class="{ activa: esActiva(conversacion) }"
            @click="seleccionarConversacion(conversacion)"
        >
          <div class="avatar-mini-lista">
            <img v-if="conversacion.fotoUrl" :src="conversacion.fotoUrl" class="avatar-img-lista" alt="avatar" />
            <span v-else>{{ obtenerNombreVisibleConversacion(conversacion, usuarioActual?.id).charAt(0).toUpperCase() }}</span>
          </div>
          <div class="info-conversacion" style="flex: 1; min-width: 0;">
            <div class="conv-header-row" style="display: flex; justify-content: space-between; align-items: center;">
              <span class="nombre text-truncate" style="display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0;">
                {{ obtenerNombreVisibleConversacion(conversacion, usuarioActual?.id) }}
                <v-icon v-if="esConversacionFavorita(conversacion)" color="#FFC107" size="16" class="ml-1" title="Favorito">
                  mdi-star
                </v-icon>
                <v-chip v-if="obtenerGrupoDeConversacion(conversacion.id)" size="x-small" color="teal-lighten-4" variant="tonal" class="ml-1 px-1 font-weight-bold" style="height: 16px; font-size: 9px; color: #406D73 !important;">
                  {{ obtenerGrupoDeConversacion(conversacion.id) }}
                </v-chip>
              </span>
              <span class="conv-time" v-if="conversacion.fechaUltimoMensaje" style="margin-left: 6px;">{{ formatearHora(conversacion.fechaUltimoMensaje) }}</span>
              
              <!-- Menú de Agrupar Conversación -->
              <v-menu location="bottom end" transition="scale-transition" :close-on-content-click="true" content-class="menu-agrupar-premium">
                <template v-slot:activator="{ props }">
                  <button
                    v-bind="props"
                    class="ml-1 btn-opcion-carpeta"
                    @click.stop
                    title="Asociar a grupo/carpeta"
                  >
                    <v-icon size="17">mdi-folder-move-outline</v-icon>
                  </button>
                </template>
                <v-card class="tarjeta-menu-agrupar" elevation="8">
                  <div class="menu-agrupar-header">
                    <v-icon size="18" color="primary" class="mr-2">mdi-folder-star</v-icon>
                    <span>Organizar Chat</span>
                  </div>
                  <div class="menu-agrupar-content">
                    <div v-for="grupo in gruposDeConversacion" :key="grupo" class="menu-agrupar-item" :class="{'item-activo': obtenerGrupoDeConversacion(conversacion.id) === grupo}" @click="asignarConversacionAGrupo(conversacion.id, grupo)">
                      <v-icon size="16" class="mr-2">{{ obtenerGrupoDeConversacion(conversacion.id) === grupo ? 'mdi-folder-check' : 'mdi-folder-outline' }}</v-icon>
                      <span class="text-truncate" style="max-width: 150px;">{{ grupo }}</span>
                    </div>
                    
                    <div class="menu-agrupar-item item-nuevo mt-1" @click="abrirCrearGrupoConConversacion(conversacion.id)">
                      <v-icon size="16" class="mr-2">mdi-plus-circle-outline</v-icon>
                      <span>Nueva carpeta</span>
                    </div>

                    <template v-if="obtenerGrupoDeConversacion(conversacion.id)">
                      <v-divider class="my-1 border-opacity-50"></v-divider>
                      <div class="menu-agrupar-item item-quitar" @click="removerConversacionDeGrupo(conversacion.id)">
                        <v-icon size="16" class="mr-2">mdi-folder-remove-outline</v-icon>
                        <span>Quitar de carpeta</span>
                      </div>
                    </template>
                  </div>
                </v-card>
              </v-menu>
            </div>
            <div class="conv-footer-row" style="display: flex; justify-content: space-between; align-items: center;">
              <span class="ultimo-msg text-truncate" style="flex: 1; min-width: 0;">
                {{ conversacion.ultimoMensaje || '...' }}
              </span>
              <span v-if="conversacion.mencionesSinLeer > 0" class="badge-mencion-no-leida">
                @
              </span>
              <span v-if="conversacion.noLeidos > 0" class="badge-no-leidos">
                {{ conversacion.noLeidos }}
              </span>
            </div>
          </div>
        </div>
        <div v-if="conversacionesFiltradas.length === 0" class="sin-resultados">
          Sin conversaciones
        </div>
      </template>

      <!-- Tab Canales -->
      <template v-else-if="tabActivo === 'canales'">
        <!-- Botón crear canal dentro del tab -->
        <div class="canal-header-action">
          <button class="btn-crear-canal" @click="abrirNuevoCanal">
            <v-icon size="16" class="mr-1">mdi-plus-circle-outline</v-icon>
            Crear nuevo canal de avisos
          </button>
        </div>

        <div
            v-for="canal in canalesFiltrados"
            :key="canal.id"
            class="item-conversacion d-flex align-center justify-space-between"
            :class="{ 'cursor-pointer': usuarioPerteneceAlCanal(canal), activa: esActiva(canal) }"
            @click="usuarioPerteneceAlCanal(canal) ? abrirCanalYaMiembro(canal) : null"
        >
          <div class="d-flex align-center" style="flex: 1; min-width: 0;">
            <div class="avatar-mini-lista mr-3" style="background: rgba(64,109,115,0.12); color: #406D73;">
              <v-icon size="18">mdi-bullhorn</v-icon>
            </div>
            <div class="info-conversacion" style="flex: 1; min-width: 0;">
              <span class="nombre text-truncate">{{ canal.nombre }}</span>
              <span class="ultimo-msg text-truncate">{{ canal.ultimoMensaje || 'Canal de avisos públicos' }}</span>
            </div>
          </div>

          <div class="acciones-canal ml-2">
            <v-chip
                v-if="usuarioPerteneceAlCanal(canal)"
                color="success"
                size="small"
                variant="tonal"
                class="font-weight-bold"
                style="height: 24px;"
                @click.stop="abrirCanalYaMiembro(canal)"
            >
              Miembro
            </v-chip>
            <v-btn
                v-else
                color="accent"
                size="x-small"
                variant="flat"
                class="font-weight-bold px-3"
                rounded="lg"
                height="24"
                :loading="uniendoseCanalId === canal.id"
                @click.stop="unirseACanal(canal)"
            >
              Unirse
            </v-btn>
          </div>
        </div>
        <div v-if="canalesFiltrados.length === 0" class="sin-resultados d-flex flex-column align-center justify-center pa-6">
          <v-icon size="36" color="#a0b8bc" class="mb-2">mdi-bullhorn-outline</v-icon>
          <span>Aún no hay canales de avisos</span>
          <span style="font-size: 11px; color: #a0b8bc; margin-top: 4px;">Sé el primero en crear uno</span>
        </div>
      </template>
    </div>

    <!-- Modal Nueva Conversación / Grupo -->
    <v-dialog v-model="mostrarModal" max-width="420">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">
            {{ esGrupo ? 'mdi-account-multiple-plus' : 'mdi-message-plus' }}
          </v-icon>
          {{ esGrupo ? 'Nuevo Grupo' : 'Nueva Conversación' }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModal" />
        </v-card-title>

        <v-card-text class="modal-contenido-conv">
          <div v-if="esGrupo" class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-account-group</v-icon>
              Nombre del grupo
            </label>
            <input
                v-model="nombreGrupo"
                type="text"
                placeholder="Ej: Proyecto 2024"
                class="input-modal-conv"
            />
          </div>
          <div class="modal-busqueda-mejorada">
            <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
            <input v-model="terminoUsuario" type="text" placeholder="Buscar usuario..." />
          </div>
          <div class="modal-listado-mejorado">
            <div
                v-for="usuario in usuariosFiltrados"
                :key="usuario.id"
                class="item-usuario-mejorado"
                @click="toggleSeleccion(usuario.id)"
            >
              <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario-modal">
                <span class="nombre">{{ usuario.username }}</span>
                <span class="email">{{ usuario.email }}</span>
              </div>
              <v-checkbox
                  v-if="esGrupo"
                  :model-value="seleccionados.includes(usuario.id)"
                  color="accent"
                  hide-details
                  density="compact"
              />
            </div>
          </div>
          <div v-if="seleccionados.length > 0" class="d-flex flex-wrap gap-1 pt-1">
            <v-chip v-for="id in seleccionados" :key="id" size="x-small" color="accent" variant="tonal" closable @click:close="toggleSeleccion(id)">
              {{ usuarios.find(u => u.id === id)?.username }}
            </v-chip>
          </div>
        </v-card-text>

        <v-card-actions v-if="esGrupo" class="modal-acciones-conv">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              size="small"
              :disabled="!nombreGrupo.trim() || seleccionados.length === 0"
              prepend-icon="mdi-check-circle"
              @click="crearGrupo"
              class="btn-crear-grupo"
          >
            Crear Grupo
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Nuevo Canal de Avisos -->
    <v-dialog v-model="mostrarModalCanal" max-width="420">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">mdi-bullhorn</v-icon>
          Nuevo Canal de Avisos
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="cerrarModalCanal" />
        </v-card-title>

        <v-card-text class="modal-contenido-conv">
          <!-- Nombre del canal -->
          <div class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-bullhorn-outline</v-icon>
              Nombre del canal
            </label>
            <input
                v-model="nombreCanal"
                type="text"
                placeholder="Ej: Comunicados Generales"
                class="input-modal-conv"
            />
          </div>

          <!-- Buscar miembros iniciales (opcional) -->
          <div class="modal-busqueda-mejorada">
            <v-icon size="16" color="#406D73" style="opacity:0.6">mdi-magnify</v-icon>
            <input v-model="terminoCanalUsuario" type="text" placeholder="Agregar miembros (opcional)..." />
          </div>
          <div class="modal-listado-mejorado">
            <div
                v-for="usuario in usuariosFiltradosCanal"
                :key="usuario.id"
                class="item-usuario-mejorado"
                @click="toggleSeleccionCanal(usuario.id)"
            >
              <div class="avatar-mini-modal">{{ usuario.username.charAt(0).toUpperCase() }}</div>
              <div class="info-usuario-modal">
                <span class="nombre">{{ usuario.username }}</span>
                <span class="email">{{ usuario.email }}</span>
              </div>
              <v-checkbox
                  :model-value="seleccionadosCanal.includes(usuario.id)"
                  color="accent"
                  hide-details
                  density="compact"
              />
            </div>
          </div>
          <div v-if="seleccionadosCanal.length > 0" class="d-flex flex-wrap gap-1 pt-1">
            <v-chip v-for="id in seleccionadosCanal" :key="id" size="x-small" color="accent" variant="tonal" closable @click:close="toggleSeleccionCanal(id)">
              {{ usuariosCanal.find(u => u.id === id)?.username }}
            </v-chip>
          </div>
        </v-card-text>

        <v-card-actions class="modal-acciones-conv">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              size="small"
              :disabled="!nombreCanal.trim()"
              :loading="creandoCanal"
              prepend-icon="mdi-check-circle"
              @click="crearCanal"
              class="btn-crear-grupo"
          >
            Crear Canal
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Nueva Tarea removido (ahora está en PanelTareas.vue) -->

    <!-- Modal Editar Perfil -->
    <v-dialog v-model="mostrarModalPerfil" max-width="420">
      <v-card rounded="2xl" class="modal-editar-perfil">
        <v-card-title class="modal-titulo-perfil">
          <v-icon size="18" color="white" class="mr-2">
            {{ tipoEdicionPerfil === 'username' ? 'mdi-account' : tipoEdicionPerfil === 'fotoUrl' ? 'mdi-image-account' : tipoEdicionPerfil === 'imagenBanner' ? 'mdi-image-area' : tipoEdicionPerfil === 'descripcion' ? 'mdi-text-account' : 'mdi-account-edit' }}
          </v-icon>
          {{ tituloModalPerfil }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalPerfil = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-perfil">
          <div v-if="tipoEdicionPerfil === 'username'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-account-circle</v-icon>
              Nuevo nombre de usuario
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="Ej: JuanPerez" />
          </div>
          <div v-else-if="tipoEdicionPerfil === 'descripcion'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-text</v-icon>
              Nueva descripción
            </label>
            <textarea v-model="valorEdicionPerfil" class="input-modal-mejorado" placeholder="Cuenta algo sobre ti..." rows="3"></textarea>
          </div>
          <div v-else-if="tipoEdicionPerfil === 'fotoUrl'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-link</v-icon>
              URL de la foto de perfil
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="https://ejemplo.com/mifoto.jpg" />

            <div class="o-separador-text my-2 text-center text-caption text-grey" style="font-size: 11px; letter-spacing: 0.05em; font-weight: bold; opacity: 0.7;">O SUBIR UNA IMAGEN</div>

            <v-btn
              color="#406D73"
              variant="outlined"
              prepend-icon="mdi-upload"
              size="small"
              block
              :loading="subiendoWallpaper"
              @click="seleccionarWallpaper"
              class="text-none font-weight-bold"
              style="border-radius: 10px;"
            >
              Seleccionar archivo
            </v-btn>
            <input
              ref="wallpaperInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleWallpaperSelected"
            />
          </div>
          <div v-else-if="tipoEdicionPerfil === 'imagenBanner'" class="campo-edicion">
            <label class="label-input-mejorado">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-link</v-icon>
              URL de la imagen de fondo
            </label>
            <input v-model="valorEdicionPerfil" type="text" class="input-modal-mejorado" placeholder="https://ejemplo.com/mifondo.jpg" />

            <div class="o-separador-text my-2 text-center text-caption text-grey" style="font-size: 11px; letter-spacing: 0.05em; font-weight: bold; opacity: 0.7;">O SUBIR UNA IMAGEN</div>

            <v-btn
              color="#406D73"
              variant="outlined"
              prepend-icon="mdi-upload"
              size="small"
              block
              :loading="subiendoWallpaper"
              @click="seleccionarWallpaper"
              class="text-none font-weight-bold"
              style="border-radius: 10px;"
            >
              Seleccionar archivo
            </v-btn>
            <input
              ref="wallpaperInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleWallpaperSelected"
            />
          </div>
        </v-card-text>
        <v-card-actions class="modal-acciones-perfil">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              prepend-icon="mdi-content-save"
              size="small"
              @click="guardarPerfil"
              :loading="guardandoPerfil"
              class="btn-guardar-perfil"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Modal Crear Carpeta / Grupo de Conversaciones -->
    <v-dialog v-model="mostrarModalCrearGrupo" max-width="380">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv">
          <v-icon size="18" color="white" class="mr-2">mdi-folder-plus</v-icon>
          Nuevo Grupo de Chats
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalCrearGrupo = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-conv">
          <div class="modal-seccion-mejorada">
            <label class="label-input-conv">
              <v-icon size="14" color="#406D73" class="mr-1">mdi-folder-outline</v-icon>
              Nombre del grupo de chats
            </label>
            <input
                v-model="nombreNuevoGrupo"
                type="text"
                placeholder="Ej: Marketing, Soporte, Personal..."
                class="input-modal-conv"
                @keyup.enter="crearGrupoConversacion"
                autofocus
            />
          </div>
        </v-card-text>
        <v-card-actions class="modal-acciones-conv">
          <v-spacer />
          <v-btn
              color="accent"
              variant="flat"
              rounded="lg"
              size="small"
              :disabled="!nombreNuevoGrupo.trim()"
              prepend-icon="mdi-check-circle"
              @click="crearGrupoConversacion"
              class="btn-crear-grupo"
          >
            Crear Grupo
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!-- Modal Confirmar Eliminación de Carpeta / Grupo de Conversaciones -->
    <v-dialog v-model="mostrarModalConfirmarEliminarGrupo" max-width="380">
      <v-card rounded="2xl" class="modal-nueva-conv">
        <v-card-title class="modal-titulo-conv" style="background: linear-gradient(135deg, #d32f2f 0%, #e57373 100%) !important;">
          <v-icon size="18" color="white" class="mr-2">mdi-alert-circle-outline</v-icon>
          Eliminar Grupo de Chats
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" color="white" @click="mostrarModalConfirmarEliminarGrupo = false" />
        </v-card-title>
        <v-card-text class="modal-contenido-conv" style="padding: 20px 16px !important; color: var(--text-primary);">
          ¿Estás seguro de que deseas eliminar el grupo <strong>{{ grupoAEliminar }}</strong>? Las conversaciones no se borrarán.
        </v-card-text>
        <v-card-actions class="modal-acciones-conv">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="mostrarModalConfirmarEliminarGrupo = false">Cancelar</v-btn>
          <v-btn color="error" variant="flat" @click="confirmarEliminarGrupoConversacion">Eliminar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useAlmacen } from '@/almacenes/almacen'
import { servicioApi } from '@/servicios/api'
import { obtenerNombreVisibleConversacion } from '@/utilidades/helpers'
import { formatearFecha, formatearHora } from '@/utilidades/formateoFechas'
import { useRouter } from 'vue-router'

const almacen = useAlmacen()
const router = useRouter()

const subiendoWallpaper = ref(false)
const wallpaperInput = ref(null)

// Variables para Canales de Aviso
const canalesAvisos = ref([])
const uniendoseCanalId = ref(null)
const nombreCanal = ref('')
const terminoCanalUsuario = ref('')
const seleccionadosCanal = ref([])
const mostrarModalCanal = ref(false)
const usuariosCanal = ref([])
const creandoCanal = ref(false)

const seleccionarWallpaper = () => {
  if (wallpaperInput.value) wallpaperInput.value.click()
}

const handleWallpaperSelected = async (event) => {
  const f = event.target.files && event.target.files[0]
  if (!f) return

  const MAX_BYTES = 15 * 1024 * 1024
  if (f.size > MAX_BYTES) {
    alert('El archivo supera el tamaño máximo permitido (15 MB).')
    return
  }

  try {
    subiendoWallpaper.value = true
    const reader = new FileReader()
    reader.onload = async (e) => {
      try {
        const base64 = e.target.result
        const resp = await servicioApi.uploadFile(f.name, base64)
        valorEdicionPerfil.value = resp.url
      } catch (err) {
        console.error('Error al subir wallpaper:', err)
        alert('Error al subir la imagen. Intenta de nuevo.')
      } finally {
        subiendoWallpaper.value = false
        if (wallpaperInput.value) wallpaperInput.value.value = null
      }
    }
    reader.readAsDataURL(f)
  } catch (err) {
    console.error('Error al procesar archivo de wallpaper:', err)
    subiendoWallpaper.value = false
  }
}
const termino = ref('')
const terminoUsuario = ref('')
const mostrarModal = ref(false)
const esGrupo = ref(false)
const nombreGrupo = ref('')
const seleccionados = ref([])
const usuarios = ref([])
const tabActivo = ref('chats')
const filtrosSeleccionados = ref(['PRIVADA', 'GRUPO', 'AVISO'])
let intervaloRefresco = null

// Agrupación de conversaciones (Carpetas)
const gruposDeConversacion = ref([])
const mapaConversacionesGrupos = ref({})
const grupoFiltroActivo = ref(null)
const mostrarModalCrearGrupo = ref(false)
const nombreNuevoGrupo = ref('')
const conversacionIdParaNuevoGrupo = ref(null)
const mostrarModalConfirmarEliminarGrupo = ref(false)
const grupoAEliminar = ref('')

const obtenerLlaveGrupos = () => {
  const userId = almacen.usuarioActual?.id || 'invitado'
  return `chat_nombres_grupos_${userId}`
}

const obtenerLlaveMapa = () => {
  const userId = almacen.usuarioActual?.id || 'invitado'
  return `chat_mapa_grupos_${userId}`
}

const cargarGruposLocalStorage = () => {
  const llaveGrupos = obtenerLlaveGrupos()
  const llaveMapa = obtenerLlaveMapa()
  
  try {
    const grupos = localStorage.getItem(llaveGrupos)
    if (grupos) {
      gruposDeConversacion.value = JSON.parse(grupos)
    } else {
      gruposDeConversacion.value = []
    }
  } catch (e) {
    console.error('Error al cargar grupos de localStorage:', e)
    gruposDeConversacion.value = []
  }

  try {
    const mapa = localStorage.getItem(llaveMapa)
    if (mapa) {
      mapaConversacionesGrupos.value = JSON.parse(mapa)
    } else {
      mapaConversacionesGrupos.value = {}
    }
  } catch (e) {
    console.error('Error al cargar mapa de grupos de localStorage:', e)
    mapaConversacionesGrupos.value = {}
  }
}

const guardarGruposLocalStorage = () => {
  const llaveGrupos = obtenerLlaveGrupos()
  const llaveMapa = obtenerLlaveMapa()
  
  try {
    localStorage.setItem(llaveGrupos, JSON.stringify(gruposDeConversacion.value))
    localStorage.setItem(llaveMapa, JSON.stringify(mapaConversacionesGrupos.value))
  } catch (e) {
    console.error('Error al guardar grupos en localStorage:', e)
  }
}

const asignarConversacionAGrupo = (conversacionId, grupo) => {
  mapaConversacionesGrupos.value[conversacionId] = grupo
  guardarGruposLocalStorage()
}

const removerConversacionDeGrupo = (conversacionId) => {
  delete mapaConversacionesGrupos.value[conversacionId]
  guardarGruposLocalStorage()
}

const obtenerGrupoDeConversacion = (conversacionId) => {
  return mapaConversacionesGrupos.value[conversacionId] || null
}

const abrirCrearGrupoConConversacion = (conversacionId) => {
  conversacionIdParaNuevoGrupo.value = conversacionId
  nombreNuevoGrupo.value = ''
  mostrarModalCrearGrupo.value = true
}

const abrirCrearGrupoConversacion = () => {
  conversacionIdParaNuevoGrupo.value = null
  nombreNuevoGrupo.value = ''
  mostrarModalCrearGrupo.value = true
}

const crearGrupoConversacion = () => {
  const nombre = nombreNuevoGrupo.value.trim()
  if (!nombre) return
  
  if (!gruposDeConversacion.value.includes(nombre)) {
    gruposDeConversacion.value.push(nombre)
  }
  
  if (conversacionIdParaNuevoGrupo.value !== null) {
    mapaConversacionesGrupos.value[conversacionIdParaNuevoGrupo.value] = nombre
  }
  
  guardarGruposLocalStorage()
  mostrarModalCrearGrupo.value = false
  nombreNuevoGrupo.value = ''
  conversacionIdParaNuevoGrupo.value = null
}

const eliminarGrupoConversacion = (grupo) => {
  grupoAEliminar.value = grupo
  mostrarModalConfirmarEliminarGrupo.value = true
}

const confirmarEliminarGrupoConversacion = () => {
  const grupo = grupoAEliminar.value
  if (!grupo) return
  
  gruposDeConversacion.value = gruposDeConversacion.value.filter(g => g !== grupo)
  
  Object.keys(mapaConversacionesGrupos.value).forEach(id => {
    if (mapaConversacionesGrupos.value[id] === grupo) {
      delete mapaConversacionesGrupos.value[id]
    }
  })
  
  guardarGruposLocalStorage()
  
  if (grupoFiltroActivo.value === grupo) {
    grupoFiltroActivo.value = null
  }
  
  mostrarModalConfirmarEliminarGrupo.value = false
  grupoAEliminar.value = ''
}

watch(() => almacen.usuarioActual, () => {
  cargarGruposLocalStorage()
  grupoFiltroActivo.value = null
})

const panelTareasAbierto = computed(() => almacen.panelTareasAbierto)

const togglePanelTareas = () => {
  almacen.togglePanelTareas()
}

const cambiarTab = (tab) => {
  tabActivo.value = tab
  if (tab === 'canales') {
    cargarCanalesAvisos()
  }
}

const cargarCanalesAvisos = async () => {
  try {
    canalesAvisos.value = await servicioApi.obtenerCanalesAvisos()
  } catch (error) {
    console.error('Error al cargar canales de aviso:', error)
  }
}

const usuarioPerteneceAlCanal = (canal) => {
  return almacen.conversaciones.some(c => c.id === canal.id)
}

const abrirCanalYaMiembro = (canal) => {
  const conv = almacen.conversaciones.find(c => c.id === canal.id) || canal
  almacen.establecerConversacionActual(conv)
}

const unirseACanal = async (canal) => {
  uniendoseCanalId.value = canal.id
  try {
    await servicioApi.unirseACanalAvisos(canal.id)
    await cargarConversaciones()
    
    const conv = almacen.conversaciones.find(c => c.id === canal.id) || canal
    almacen.establecerConversacionActual(conv)
  } catch (error) {
    console.error('Error al unirse al canal de avisos:', error)
  } finally {
    uniendoseCanalId.value = null
  }
}

const abrirNuevoCanal = async () => {
  nombreCanal.value = ''
  terminoCanalUsuario.value = ''
  seleccionadosCanal.value = []
  mostrarModalCanal.value = true
  // Cargar lista de usuarios para agregar como miembros iniciales
  try {
    usuariosCanal.value = await servicioApi.obtenerUsuarios()
  } catch (e) {
    console.error('Error al cargar usuarios para canal:', e)
  }
}

const cerrarModalCanal = () => {
  mostrarModalCanal.value = false
  nombreCanal.value = ''
  terminoCanalUsuario.value = ''
  seleccionadosCanal.value = []
}

const toggleSeleccionCanal = (idUsuario) => {
  const index = seleccionadosCanal.value.indexOf(idUsuario)
  if (index === -1) seleccionadosCanal.value.push(idUsuario)
  else seleccionadosCanal.value.splice(index, 1)
}

const crearCanal = async () => {
  if (!nombreCanal.value.trim()) return
  creandoCanal.value = true
  try {
    const nuevoCanal = await servicioApi.crearCanalAvisos(nombreCanal.value.trim())
    
    // Agregar miembros seleccionados al canal uno a uno
    for (const idUsuario of seleccionadosCanal.value) {
      try {
        await servicioApi.añadirParticipante(nuevoCanal.id, idUsuario)
      } catch (e) {
        console.error('Error al agregar miembro al canal:', e)
      }
    }

    await cargarConversaciones()
    const canalActualizado = almacen.conversaciones.find(c => c.id === nuevoCanal.id) || nuevoCanal
    almacen.establecerConversacionActual(canalActualizado)

    cerrarModalCanal()
    tabActivo.value = 'chats'
  } catch (error) {
    console.error('Error al crear canal de avisos:', error)
  } finally {
    creandoCanal.value = false
  }
}

// Edición de perfil
const mostrarModalPerfil = ref(false)
const tipoEdicionPerfil = ref('')
const valorEdicionPerfil = ref('')
const tituloModalPerfil = ref('')
const guardandoPerfil = ref(false)

const usuarioActual = computed(() => almacen.usuarioActual)
const conversacionesActuales = computed(() => almacen.conversacionesOrdenadas)
const conversacionActual = computed(() => almacen.conversacionActual)

const conversacionesFiltradas = computed(() => {
  let lista = conversacionesActuales.value.filter(c => {
    return filtrosSeleccionados.value.includes(c.tipo)
  })
  
  // Filtrar por grupo de conversación (carpeta) si está activo
  if (grupoFiltroActivo.value !== null) {
    lista = lista.filter(c => mapaConversacionesGrupos.value[c.id] === grupoFiltroActivo.value)
  }

  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => obtenerNombreVisibleConversacion(c, usuarioActual.value?.id).toLowerCase().includes(t))
  }
  return lista
})

const canalesFiltrados = computed(() => {
  let lista = canalesAvisos.value
  if (termino.value) {
    const t = termino.value.toLowerCase()
    lista = lista.filter(c => c.nombre.toLowerCase().includes(t))
  }
  return lista
})

const usuariosFiltrados = computed(() => {
  const yo = almacen.usuarioActual
  let lista = usuarios.value.filter(u => u.id !== yo?.id)
  if (terminoUsuario.value) {
    const t = terminoUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const usuariosFiltradosCanal = computed(() => {
  const yo = almacen.usuarioActual
  let lista = usuariosCanal.value.filter(u => u.id !== yo?.id)
  if (terminoCanalUsuario.value) {
    const t = terminoCanalUsuario.value.toLowerCase()
    lista = lista.filter(u => u.username.toLowerCase().includes(t))
  }
  return lista
})

const esActiva = (conversacion) => conversacionActual.value?.id === conversacion.id
const seleccionarConversacion = async (conversacion) => {
    if (conversacionActual.value?.id === conversacion.id) return;

    // Limpiar mensajes inmediatamente para no mostrar mensajes de la conversación anterior
    almacen.establecerMensajes([])
    almacen.establecerConversacionActual(conversacion)
}

onUnmounted(() => {
  if (intervaloRefresco) clearInterval(intervaloRefresco)
})

const abrirNuevaConversacion = async () => {
  esGrupo.value = false
  mostrarModal.value = true
  usuarios.value = await servicioApi.obtenerUsuarios()
}

const abrirNuevoGrupo = async () => {
  esGrupo.value = true
  nombreGrupo.value = ''
  seleccionados.value = []
  mostrarModal.value = true
  usuarios.value = await servicioApi.obtenerUsuarios()
}

const toggleSeleccion = (idUsuario) => {
  if (!esGrupo.value) {
    crearChatPrivado(idUsuario)
    return
  }
  const index = seleccionados.value.indexOf(idUsuario)
  if (index === -1) {
    seleccionados.value = [...seleccionados.value, idUsuario]
  } else {
    seleccionados.value = seleccionados.value.filter(id => id !== idUsuario)
  }
}

const cerrarModal = () => {
  mostrarModal.value = false
  terminoUsuario.value = ''
}

const crearChatPrivado = async (idUsuario) => {
  try {
    const nuevaConv = await servicioApi.crearConversacionPrivada(idUsuario)
    const existe = almacen.conversaciones.find(c => c.id === nuevaConv.id)
    if (!existe) almacen.agregarConversacion(nuevaConv)
    almacen.establecerConversacionActual(nuevaConv)

    cerrarModal()
  } catch (error) {
    console.error('Error al crear conversación:', error)
  }
}

const crearGrupo = async () => {
  try {
    const nuevaConv = await servicioApi.crearGrupo(nombreGrupo.value, seleccionados.value)
    almacen.agregarConversacion(nuevaConv)
    almacen.establecerConversacionActual(nuevaConv)

    // Enviar mensaje automático de bienvenida
    try {
      const mensajeAutomatico = await servicioApi.enviarMensaje({
        conversacionId: nuevaConv.id,
        contenido: 'Bienvenido al grupo',
        tipoMensaje: 'TEXTO'
      })
      almacen.agregarMensaje(mensajeAutomatico)
    } catch (error) {
      console.error('Error al enviar mensaje automático:', error)
    }

    cerrarModal()
  } catch (error) {
    console.error('Error al crear grupo:', error)
  }
}

// Logica perfil
const abrirEdicionPerfil = (tipo) => {
  tipoEdicionPerfil.value = tipo
  valorEdicionPerfil.value = usuarioActual.value[tipo] || ''

  if (tipo === 'username') tituloModalPerfil.value = 'Cambiar nombre de usuario'
  else if (tipo === 'fotoUrl') tituloModalPerfil.value = 'Cambiar foto de perfil'
  else if (tipo === 'imagenBanner') tituloModalPerfil.value = 'Cambiar fondo de pantalla del chat'
  else if (tipo === 'descripcion') tituloModalPerfil.value = 'Cambiar descripción'

  mostrarModalPerfil.value = true
}

const guardarPerfil = async () => {
  guardandoPerfil.value = true
  try {
    const data = {}
    data[tipoEdicionPerfil.value] = valorEdicionPerfil.value

    // El servicio espera un DTO con fotoUrl, estado, username, descripcion, imagenBanner
    // Mantenemos los datos actuales por defecto
    const payload = {
      username: usuarioActual.value.username,
      fotoUrl: usuarioActual.value.fotoUrl,
      descripcion: usuarioActual.value.descripcion,
      imagenBanner: usuarioActual.value.imagenBanner,
      estado: usuarioActual.value.estado,
      ...data
    }

    const usrActualizado = await servicioApi.actualizarPerfil(payload)
    almacen.establecerUsuario(usrActualizado)
    localStorage.setItem('usuario', JSON.stringify(usrActualizado))
    mostrarModalPerfil.value = false
  } catch(error) {
    console.error('Error actualizando perfil', error)
  } finally {
    guardandoPerfil.value = false
  }
}

const alternarTema = () => {
  almacen.establecerTemaOscuro(!almacen.temaOscuro)
}

const cerrarSesionLocal = () => {
  almacen.cerrarSesion()
  const contextPath = window.location.pathname.includes('/chat-empresarial') 
    ? '/chat-empresarial' 
    : ''
  window.location.href = `${contextPath}/login`
}

const verificarNuevosMensajes = (nuevasConvs) => {
  if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return

  const yoId = almacen.usuarioActual?.id
  if (!yoId) return

  nuevasConvs.forEach(conv => {
    const oldConv = almacen.conversaciones.find(c => c.id === conv.id)
    const esNuevo = !oldConv || (conv.fechaUltimoMensaje && conv.fechaUltimoMensaje !== oldConv.fechaUltimoMensaje)
    
    if (esNuevo) {
      const emisorEsYo = conv.ultimoMensajeEmisorId === yoId
      const esConversacionActiva = almacen.conversacionActual && almacen.conversacionActual.id === conv.id
      const chatFocado = esConversacionActiva && document.hasFocus()

      if (!emisorEsYo && !chatFocado && conv.ultimoMensaje && conv.ultimoMensaje !== '...' && conv.ultimoMensaje !== 'Los mensajes están cifrados de extremo a extremo') {
        const username = almacen.usuarioActual?.username
        const esMencion = username && (conv.ultimoMensaje.includes(`@${username}`) || conv.ultimoMensaje.includes('@todos'))
        
        const titulo = esMencion
          ? `🔔 ¡Fuiste mencionado en ${obtenerNombreVisibleConversacion(conv, yoId) || 'un grupo'}!`
          : (obtenerNombreVisibleConversacion(conv, yoId) || 'Nuevo mensaje')
        const cuerpo = conv.ultimoMensaje

        try {
          new Notification(titulo, {
            body: cuerpo,
            icon: conv.fotoUrl || null
          })
        } catch (e) {
          console.error('Error al mostrar notificación:', e)
        }
      }
    }
  })
}

const esPrimeraCarga = ref(true)

const cargarConversaciones = async () => {
  try {
      const convs = await servicioApi.obtenerConversaciones()
      if (!esPrimeraCarga.value) {
          verificarNuevosMensajes(convs)
      } else {
          esPrimeraCarga.value = false
      }
      almacen.establecerConversaciones(convs)
  } catch (error) {
      console.error('Error al refrescar conversaciones:', error)
  }
}

onMounted(() => {
  cargarGruposLocalStorage()
  cargarConversaciones()
  intervaloRefresco = setInterval(cargarConversaciones, 5000)

  // Solicitar permisos de notificación nativa
  if (typeof Notification !== 'undefined' && Notification.permission === 'default') {
    Notification.requestPermission()
  }
})

const esConversacionFavorita = (conversacion) => {
  if (conversacion.tipo !== 'PRIVADA') return false
  const yo = almacen.usuarioActual
  if (!yo || !yo.favoritos) return false
  const favIds = yo.favoritos.split(',').filter(x => x).map(Number)
  
  let otroId = null
  if (conversacion.participantes) {
    const otro = conversacion.participantes.find(p => p.usuario && p.usuario.id !== yo.id)
    if (otro && otro.usuario) otroId = otro.usuario.id
  }
  if (otroId === null && conversacion.participanteIds) {
    otroId = conversacion.participanteIds.find(id => id !== yo.id)
  }
  return otroId !== null && favIds.includes(otroId)
}
</script>

<style scoped>
/* ==================================================
   ListaConversaciones.vue — Premium SaaS Sidebar 2026
   ================================================== */

.lista-conversaciones {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--sidebar-bg);
  position: relative;
  overflow: hidden;
}

/* ===== HEADER COMPACTO ===== */
.panel-usuario-compact {
  flex-shrink: 0;
  padding: 14px 16px 12px;
  background: var(--sidebar-bg);
  border-bottom: 1px solid rgba(64,109,115,0.08);
}

.user-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-wrap {
  width: 42px;
  height: 42px;
  min-width: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64,109,115,0.2);
  transition: transform .15s, box-shadow .15s;
}

.user-avatar-wrap:hover {
  transform: scale(1.06);
  box-shadow: 0 4px 14px rgba(64,109,115,0.28);
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info-col {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-actions-row {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn-cabecera-accion {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--teal);
  background-color: var(--surface);
  color: var(--teal);
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease, border-color 0.2s;
  box-sizing: border-box;
}

.btn-cabecera-accion:hover {
  background-color: var(--teal);
  color: #ffffff;
  transform: scale(1.12);
}

.btn-cabecera-accion .v-icon,
.btn-cabecera-accion i {
  color: inherit !important;
}

.btn-cabecera-accion:hover .v-icon,
.btn-cabecera-accion:hover i {
  color: #ffffff !important;
}

.btn-cabecera-accion.btn-logout {
  border-color: #d32f2f;
  color: #d32f2f;
}

.btn-cabecera-accion.btn-logout:hover {
  background-color: #d32f2f;
  color: #ffffff;
}

.btn-cabecera-accion.btn-logout:hover .v-icon,
.btn-cabecera-accion.btn-logout:hover i {
  color: #ffffff !important;
}

/* Buscador */
.campo-busqueda-modern {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  background: var(--input-bg);
  border: 1px solid rgba(64,109,115,0.1);
  border-radius: 12px;
  padding: 6px 12px;
  transition: border-color .2s, box-shadow .2s;
}

.campo-busqueda-modern:focus-within {
  border-color: var(--teal);
  box-shadow: 0 0 0 3px var(--teal-glow);
}

.btn-filtro {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid transparent;
  background-color: transparent;
  color: var(--teal);
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease;
  box-sizing: border-box;
  flex-shrink: 0;
}

.btn-filtro:hover {
  background-color: var(--teal);
  color: #ffffff;
  transform: scale(1.08);
}

.btn-filtro .v-icon,
.btn-filtro i {
  color: inherit !important;
}

.btn-filtro:hover .v-icon,
.btn-filtro:hover i {
  color: #ffffff !important;
}

.menu-filtro-card {
  background: var(--surface) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color);
}
.menu-filtro-title {
  background: var(--teal) !important;
  color: white !important;
  font-size: 13px;
  padding: 10px 16px;
  display: flex !important;
  align-items: center;
  gap: 6px;
}
.menu-filtro-list {
  background: var(--surface) !important;
  color: var(--text-primary) !important;
}

.divider-v {
  width: 1px;
  height: 16px;
  background: rgba(64,109,115,0.15);
  margin: 0 4px;
  flex-shrink: 0;
}

.input-busqueda {
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  flex: 1;
  outline: none;
  min-width: 0;
}

.input-busqueda::placeholder { color: rgba(64,109,115,0.4); }

/* ===== NAVEGACIÓN VERTICAL ===== */
.nav-vertical {
  display: flex;
  flex-direction: column;
  padding: 8px 12px;
  gap: 4px;
  background: var(--sidebar-bg);
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #406D73;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 10px;
  transition: background .18s, color .18s;
}

.nav-item.activo {
  background: rgba(64,109,115,0.08);
}

.nav-item:hover:not(.activo) {
  background: rgba(64,109,115,0.04);
}

/* Subheader */
.seccion-chats-titulo {
  padding: 12px 16px 8px;
  font-size: 10px;
  font-weight: 800;
  color: #5a8a94;
  letter-spacing: 0.08em;
  background: var(--sidebar-bg);
  flex-shrink: 0;
  text-transform: uppercase;
}

/* ===== LISTADO ===== */
.listado {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  background: var(--sidebar-bg);
}

.item-conversacion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(64,109,115,0.04);
  transition: background .15s;
  position: relative;
}

.item-conversacion:hover { background: rgba(64,109,115,0.04); }

.item-conversacion.activa {
  background: rgba(64,109,115,0.08);
}

.item-conversacion.activa::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #406D73;
}

.avatar-mini-lista {
  width: 44px;
  height: 44px;
  min-width: 44px;
  background: linear-gradient(135deg, #B2C5C8, #9fb3b6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img-lista { width: 100%; height: 100%; object-fit: cover; }

.info-conversacion {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 2px;
}

.conv-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conv-footer-row {
  display: flex;
  align-items: center;
}

.info-conversacion .nombre {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.01em;
}

.info-conversacion .conv-time {
  font-size: 11px;
  color: #5a8a94;
  font-weight: 600;
  margin-left: 8px;
  flex-shrink: 0;
}

.info-conversacion .ultimo-msg {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

/* Tareas */
.tarea-item { cursor: pointer; }

.tarea-item .tarea-avatar {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: transform .15s;
}

.tarea-item .tarea-avatar:hover { transform: scale(1.08); }
.tarea-item .tarea-info { display: flex; flex-direction: column; justify-content: center; }
.tarea-item .tarea-info .nombre { font-weight: 600; }
.tarea-item .tarea-info .ultimo-msg { font-size: 12px; color: #7f9ea4; }

/* Crear canal/tarea */
.canal-header-action { padding: 10px 16px 8px; }

.btn-crear-canal {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 14px;
  background: rgba(64,109,115,0.04);
  border: 1.5px dashed rgba(64,109,115,0.2);
  border-radius: 12px;
  color: #406D73;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.01em;
  transition: background .15s, border-color .15s;
}

.btn-crear-canal:hover {
  background: rgba(64,109,115,0.08);
  border-color: #406D73;
}

.sin-resultados {
  padding: 32px 16px;
  text-align: center;
  font-size: 13px;
  color: #a0b8bc;
}

/* ===== MODALES ===== */
.modal-nueva-conv {
  box-shadow: 0 16px 48px rgba(64,109,115,0.18) !important;
  background: var(--surface) !important;
  border-radius: 20px !important;
  overflow: hidden !important;
}

.dialog-crear-grupo .v-card {
  background: var(--surface) !important;
  border-radius: 16px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5) !important;
}

.menu-agrupar-premium {
  border-radius: 12px !important;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4) !important;
}

.tarjeta-menu-agrupar {
  background: var(--surface) !important;
  color: var(--text-primary) !important;
  min-width: 220px;
}

.menu-agrupar-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: rgba(var(--v-theme-primary), 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  font-weight: 600;
  font-size: 0.8rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: var(--primary);
}

.menu-agrupar-content {
  padding: 6px;
}

.menu-agrupar-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s ease;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.menu-agrupar-item:hover {
  background: rgba(128, 128, 128, 0.15);
  transform: translateX(2px);
}

.item-activo {
  background: rgba(var(--v-theme-primary), 0.15) !important;
  color: var(--primary) !important;
}

.item-nuevo {
  color: #4CAF50;
}

.item-nuevo:hover {
  background: rgba(76, 175, 80, 0.1);
  color: #81C784;
}

.item-quitar {
  color: #F44336;
}

.item-quitar:hover {
  background: rgba(244, 67, 54, 0.1);
  color: #E57373;
}

.modal-titulo-conv {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 14px 16px !important;
}

.modal-contenido-conv {
  padding: 16px !important;
  background: var(--bg) !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
}

.modal-seccion-mejorada {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--surface);
  border-radius: 12px;
  border: 1px solid rgba(64,109,115,0.1);
}

.label-input-conv {
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #406D73;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.input-modal-conv {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid rgba(64,109,115,0.15);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--input-bg);
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  font-family: inherit;
  box-sizing: border-box;
}

.input-modal-conv:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.08);
}

.input-modal-conv::placeholder { color: rgba(64,109,115,0.4); }

.modal-busqueda-mejorada {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--input-bg);
  border: 1px solid rgba(64,109,115,0.1);
  border-radius: 12px;
  padding: 8px 14px;
}

.modal-busqueda-mejorada input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  flex: 1;
  outline: none;
}

.modal-busqueda-mejorada input::placeholder { color: rgba(64,109,115,0.4); }

.modal-listado-mejorado {
  max-height: 300px;
  overflow-y: auto;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid rgba(64,109,115,0.08);
}

.item-usuario-mejorado {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background .12s;
  border-bottom: 1px solid rgba(64,109,115,0.04);
}

.item-usuario-mejorado:hover { background: rgba(179,235,242,0.12); }
.item-usuario-mejorado:last-child { border-bottom: none; }

.avatar-mini-modal {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: linear-gradient(135deg, #B2C5C8, #9fb3b6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.info-usuario-modal { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.info-usuario-modal .nombre { font-weight: 600; font-size: 14px; color: var(--text-primary); }
.info-usuario-modal .email { font-size: 12px; color: var(--text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.modal-acciones-conv {
  padding: 12px 16px !important;
  background: var(--surface) !important;
  border-top: 1px solid rgba(64,109,115,0.06) !important;
}

.btn-crear-grupo { font-size: 12px !important; font-weight: 600 !important; letter-spacing: 0.01em !important; }

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .panel-usuario-compact { padding: 10px 12px; }
  .user-avatar-wrap { width: 38px; height: 38px; min-width: 38px; font-size: 15px; }
  .tab-btn { font-size: 11px; }
  .item-conversacion { padding: 8px 12px; }
  .avatar-mini-lista { width: 40px; height: 40px; min-width: 40px; }
}

@media (max-width: 480px) {
  .panel-usuario-compact { padding: 8px 10px; }
  .user-avatar-wrap { width: 36px; height: 36px; min-width: 36px; }
  .item-conversacion { padding: 6px 10px; gap: 10px; }
  .avatar-mini-lista { width: 38px; height: 38px; min-width: 38px; }
  .seccion-titulo { font-size: 9px; }
}
</style>

<style>
/* Estilos globales para el menú flotante del perfil */
.menu-perfil-flotante {
  border-radius: 20px !important;
  overflow: hidden !important;
  box-shadow: 0 8px 32px rgba(64, 109, 115, 0.18) !important;
  border: 1px solid rgba(64, 109, 115, 0.1) !important;
  min-width: 220px !important;
}

.menu-perfil-flotante .v-list {
  padding: 8px 0 !important;
  background-color: var(--surface) !important;
}

.menu-perfil-flotante .v-list-item {
  min-height: 36px !important;
  padding: 0 12px !important;
  transition: background 0.2s;
}

.menu-perfil-flotante .v-list-item:hover {
  background-color: var(--teal-pale) !important;
}

.menu-perfil-flotante .v-list-item-title {
  font-size: 12px !important;
  font-weight: 500 !important;
  color: var(--text-primary) !important;
}

.menu-perfil-flotante .text-error .v-list-item-title {
  color: #e57373 !important;
  font-weight: 600 !important;
}

.menu-perfil-flotante .v-list-item__prepend {
  margin-right: 10px !important;
}

.menu-perfil-flotante .v-icon {
  font-size: 16px !important;
  color: #5a8a94;
}

.menu-perfil-flotante .text-error .v-icon {
  color: #e57373 !important;
}

.menu-perfil-flotante .v-divider {
  margin: 4px 0 !important;
  opacity: 0.2 !important;
}

/* Scrollbar para la lista movido a global */
/* ---- Estilos Mejorados Modal Editar Perfil ---- */
.modal-editar-perfil {
  box-shadow: 0 10px 40px rgba(64, 109, 115, 0.15) !important;
  background: var(--surface) !important;
  border-radius: 24px !important;
  overflow: hidden !important;
}

.modal-titulo-perfil {
  background: linear-gradient(135deg, #406D73 0%, #5a8a94 100%) !important;
  color: white !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  border-radius: 24px 24px 0 0 !important;
}

.modal-contenido-perfil {
  padding: 16px !important;
  background: var(--bg) !important;
  border-radius: 0 0 24px 24px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
}

.campo-edicion {
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
}

.label-input-mejorado {
  display: flex !important;
  align-items: center !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  color: #406D73 !important;
  letter-spacing: 0.02em !important;
  text-transform: uppercase !important;
  margin-bottom: 4px !important;
}

.input-modal-mejorado {
  width: 100% !important;
  padding: 10px 12px !important;
  border: 1px solid rgba(64, 109, 115, 0.2) !important;
  border-radius: 12px !important;
  font-size: 12px !important;
  color: var(--text-primary) !important;
  background: var(--input-bg) !important;
  outline: none !important;
  transition: all 0.2s !important;
  resize: vertical !important;
  font-family: inherit !important;
}

.input-modal-mejorado:focus {
  border-color: #406D73 !important;
  box-shadow: 0 0 0 3px rgba(64, 109, 115, 0.1) !important;
}

.input-modal-mejorado::placeholder {
  color: rgba(64, 109, 115, 0.4) !important;
}

.modal-acciones-perfil {
  padding: 12px 16px !important;
  background: var(--surface) !important;
  border-top: 1px solid rgba(64, 109, 115, 0.08) !important;
  border-radius: 0 0 16px 16px !important;
}

.btn-guardar-perfil {
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.02em !important;
}

.badge-no-leidos {
  background-color: #00bcd4 !important;
  color: white !important;
  font-size: 10px !important;
  font-weight: 700 !important;
  border-radius: 50% !important;
  min-width: 18px !important;
  height: 18px !important;
  padding: 0 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin-left: 8px !important;
  flex-shrink: 0 !important;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1) !important;
  line-height: 1 !important;
}

.badge-mencion-no-leida {
  background-color: #00bcd4 !important;
  color: white !important;
  font-size: 10px !important;
  font-weight: 700 !important;
  border-radius: 50% !important;
  min-width: 18px !important;
  height: 18px !important;
  padding: 0 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin-left: 8px !important;
  flex-shrink: 0 !important;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1) !important;
  line-height: 1 !important;
}

/* ===== CARPETAS DE CONVERSACIONES ===== */
.carpetas-conversaciones-container {
  padding: 10px 16px;
  background: var(--sidebar-bg);
  border-bottom: 1px solid rgba(64,109,115,0.06);
  flex-shrink: 0;
}

.carpetas-scroll-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 2px 0 6px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(64, 109, 115, 0.25) rgba(64, 109, 115, 0.05);
}

.carpetas-scroll-row::-webkit-scrollbar {
  height: 4px;
}

.carpetas-scroll-row::-webkit-scrollbar-track {
  background: rgba(64, 109, 115, 0.05);
  border-radius: 10px;
}

.carpetas-scroll-row::-webkit-scrollbar-thumb {
  background: rgba(64, 109, 115, 0.25);
  border-radius: 10px;
}

.carpetas-scroll-row::-webkit-scrollbar-thumb:hover {
  background: rgba(64, 109, 115, 0.45);
}

.carpeta-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: var(--bg);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 600;
  border-radius: 50px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.carpeta-pill:hover {
  background: #e0f2f1;
  color: #406D73;
}

.carpeta-pill.activa {
  background: #406D73;
  color: #ffffff;
  box-shadow: 0 2px 6px rgba(64,109,115,0.25);
}

.btn-eliminar-carpeta {
  margin-right: -6px;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.btn-eliminar-carpeta:hover {
  opacity: 1 !important;
  color: #ff5252 !important;
}

.btn-crear-carpeta {
  background: transparent;
  color: #406D73;
  border: 1px dashed rgba(64, 109, 115, 0.4);
}

.btn-crear-carpeta:hover {
  background: rgba(64,109,115,0.06);
  border-style: solid;
}

.btn-opcion-carpeta {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid var(--teal);
  background-color: var(--surface);
  color: var(--teal);
  cursor: pointer;
  outline: none;
  opacity: 0;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease, border-color 0.2s, opacity 0.2s ease;
  box-sizing: border-box;
  margin-left: 4px;
}

.item-conversacion:hover .btn-opcion-carpeta {
  opacity: 0.8;
}

.btn-opcion-carpeta:hover {
  opacity: 1 !important;
  background-color: var(--teal) !important;
  color: #ffffff !important;
  transform: scale(1.12);
}

.btn-opcion-carpeta .v-icon,
.btn-opcion-carpeta i {
  color: inherit !important;
}

.btn-opcion-carpeta:hover .v-icon,
.btn-opcion-carpeta:hover i {
  color: #ffffff !important;
}
</style>

<style>
/* Estilos globales para los menús flotantes para que no tengan barras de desplazamiento y se vean mejor */
.menu-perfil-flotante, .menu-mensaje-flotante {
  border-radius: 12px !important;
  overflow: hidden !important;
  box-shadow: 0 8px 24px rgba(64, 109, 115, 0.15) !important;
  border: 1px solid rgba(64, 109, 115, 0.08) !important;
}
.menu-perfil-flotante .v-list, .menu-mensaje-flotante .v-list {
  padding: 4px 0 !important;
  overflow: hidden !important; /* quita el scrollbar */
}
.menu-perfil-flotante .v-list-item-title, .menu-mensaje-flotante .v-list-item-title {
  font-size: 13px !important;
  font-weight: 600 !important;
  color: var(--text-primary) !important;
}
.menu-mensaje-flotante .v-list-item-title {
  font-size: 12px !important;
}
.menu-perfil-flotante .v-list-item__prepend > .v-icon, .menu-mensaje-flotante .v-list-item__prepend > .v-icon {
  font-size: 18px !important;
  color: #406D73 !important;
  opacity: 0.8 !important;
  margin-inline-end: 12px !important;
}
.menu-perfil-flotante .text-error .v-list-item-title,
.menu-perfil-flotante .text-error .v-icon,
.menu-perfil-flotante .text-error .v-list-item__prepend > .v-icon,
.menu-mensaje-flotante .text-error .v-list-item-title,
.menu-mensaje-flotante .text-error .v-icon,
.menu-mensaje-flotante .text-error .v-list-item__prepend > .v-icon {
  color: #d32f2f !important;
}
.menu-perfil-flotante .v-list-item, .menu-mensaje-flotante .v-list-item {
  min-height: 36px !important;
  padding: 4px 16px !important;
}
</style>
