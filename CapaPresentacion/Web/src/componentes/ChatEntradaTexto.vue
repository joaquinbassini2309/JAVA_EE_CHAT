<template>
  <!-- Barra de entrada o Banner de Solo Lectura -->
  <div v-if="esAviso && rolUsuario !== 'ADMIN'" class="read-only-banner d-flex align-center justify-center pa-4 text-subtitle-2 font-weight-bold">
    <v-icon size="18" color="#406D73" class="mr-2">mdi-lock</v-icon>
    Solo los administradores pueden enviar mensajes en este canal.
  </div>
  <div v-else class="entrada-mensaje" style="position: relative;">
    <!-- Lista de sugerencias de mención -->
    <div v-if="mostrarSugerenciasMencion && sugerenciasFiltradas.length > 0" class="sugerencias-mencion-card">
      <div
        v-for="(sug, index) in sugerenciasFiltradas"
        :key="sug.esTodos ? 'todos' : sug.id"
        class="item-sugerencia"
        :class="{ activo: index === indiceSugerenciaMencion }"
        @click="seleccionarMencion(sug)"
      >
        <div v-if="sug.esTodos" class="avatar-mini-mencion todos">
          <v-icon size="14" color="white">mdi-account-group</v-icon>
        </div>
        <div v-else class="avatar-mini-mencion">
          {{ sug.username.charAt(0).toUpperCase() }}
        </div>
        <div class="info-sugerencia">
          <span class="username">{{ sug.esTodos ? '@todos' : '@' + sug.username }}</span>
          <span class="rol">{{ sug.esTodos ? 'Notificar a todos en el grupo' : (sug.email || 'Miembro') }}</span>
        </div>
      </div>
    </div>

    <input
        ref="fileInput"
        type="file"
        accept="image/*,application/pdf"
        @change="handleFileSelected"
        style="display:none"
    />
    <!-- Boton Adjuntar -->
    <button
      class="btn-accion-chat"
      @click="seleccionarArchivo"
      title="Adjuntar archivo o imagen"
      :disabled="subiendoArchivo"
    >
      <v-icon size="20">mdi-paperclip</v-icon>
    </button>
    <!-- Boton Emoji -->
    <v-menu :close-on-content-click="false" location="top start" transition="slide-y-transition">
      <template v-slot:activator="{ props }">
        <button
          v-bind="props"
          class="btn-accion-chat mr-1"
          title="Insertar emoji"
        >
          <v-icon size="20">mdi-emoticon-outline</v-icon>
        </button>
      </template>
      <v-card class="emoji-picker-card" rounded="xl" width="300">
        <div class="emoji-picker-grid">
          <button
            v-for="emoji in listaEmojis"
            :key="emoji"
            class="btn-emoji-item"
            @click="insertarEmoji(emoji)"
          >
            {{ emoji }}
          </button>
        </div>
      </v-card>
    </v-menu>
    <!-- Boton Mencionar -->
    <button
      v-if="esGrupo || esAviso"
      class="btn-accion-chat mr-1"
      @click="insertarArroba"
      title="Mencionar a alguien"
    >
      <v-icon size="20">mdi-at</v-icon>
    </button>

    <textarea
        ref="textareaMensaje"
        v-model="contenidoNuevo"
        class="input-mensaje"
        :placeholder="String(conversacionActual?.id).startsWith('tareas_') ? '✏️  Escribe una nueva tarea...' : '✉️  Escribe un mensaje...'"
        @keydown="handleKeydown"
        @input="handleTextareaInput"
        rows="1"
        style="resize: none; overflow-y: hidden;"
    ></textarea>
    <v-btn
      variant="flat"
      :color="btnEnviarColor"
      height="38"
      class="btn-enviar-pill"
      :class="!contenidoNuevo.trim() ? 'btn-enviar-inactivo' : ''"
      :ripple="false"
      @mouseenter="hoverEnviar = true"
      @mouseleave="hoverEnviar = false"
      @click="enviarMensaje"
    >
      <v-icon size="18" :color="contenidoNuevo.trim() ? 'white' : '#8aa8ae'" class="mr-1">mdi-send</v-icon>
      <span :style="{ color: contenidoNuevo.trim() ? 'white' : '#8aa8ae', fontWeight: 600, textTransform: 'none', fontSize: '14px', letterSpacing: '0.02em' }">Enviar</span>
    </v-btn>

    <!-- Modal de error de archivo -->
    <v-dialog v-model="mostrarErrorArchivo" max-width="400">
      <v-card rounded="xl">
        <v-card-title class="d-flex align-center pa-4 pb-0" style="color: #406D73;">
          <v-icon color="#406D73" class="mr-2">mdi-alert-circle</v-icon>
          Archivo demasiado grande
        </v-card-title>
        <v-card-text class="pa-4 pt-2 text-body-1">
          El archivo seleccionado supera el límite máximo permitido de 15 MB. Por favor, selecciona un archivo más ligero.
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn style="background-color: #406D73; color: white;" variant="flat" rounded="pill" @click="mostrarErrorArchivo = false">Entendido</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'

const props = defineProps({
  conversacionActual: {
    type: Object,
    default: null
  },
  usuarioActual: {
    type: Object,
    default: null
  },
  esGrupo: {
    type: Boolean,
    default: false
  },
  esAviso: {
    type: Boolean,
    default: false
  },
  rolUsuario: {
    type: String,
    default: 'MIEMBRO'
  },
  subiendoArchivo: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['enviar-mensaje', 'subir-archivo'])

const contenidoNuevo = ref('')
const fileInput = ref(null)
const textareaMensaje = ref(null)
const mostrarErrorArchivo = ref(false)

const mostrarSugerenciasMencion = ref(false)
const indiceSugerenciaMencion = ref(0)
const busquedaMencion = ref('')
const posicionArroba = ref(-1)

const hoverEnviar = ref(false)

const btnEnviarColor = computed(() => {
  if (contenidoNuevo.value.trim()) {
    return hoverEnviar.value ? '#3d6b72' : '#5A8A94'
  }
  return '#e0f2f1'
})

const miembrosGrupo = computed(() => {
  if (!props.conversacionActual || (props.conversacionActual.tipo !== 'GRUPO' && props.conversacionActual.tipo !== 'AVISO')) return []
  return props.conversacionActual.participantes
    ? props.conversacionActual.participantes.map(p => p.usuario).filter(u => u && u.id !== props.usuarioActual?.id)
    : []
})

const sugerenciasFiltradas = computed(() => {
  const lista = []
  lista.push({ username: 'todos', esTodos: true })
  lista.push(...miembrosGrupo.value)
  
  if (!busquedaMencion.value) {
    return lista
  }
  
  const q = busquedaMencion.value.toLowerCase()
  return lista.filter(item => item.username.toLowerCase().includes(q))
})

const listaEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
  '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
  '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '💀',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '🖐️', '✋',
  '👋', '👏', '🙏', '🙌', '👐', '🤝', '✍️', '💅', '🤳', '💪',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
  '🔥', '✨', '🎉', '🎈', '🚀', '💡', '📌', '📅', '📝', '💬'
]

// Insertar un emoji seleccionado.
const insertarEmoji = (emoji) => {
  contenidoNuevo.value += emoji
}

// Abrir el selector de archivos.
const seleccionarArchivo = () => {
  if (fileInput.value) fileInput.value.click()
}

// Manejar seleccion de archivos.
const handleFileSelected = (event) => {
  const f = event.target.files && event.target.files[0]
  if (!f) return

  const MAX_BYTES = 15 * 1024 * 1024
  if (f.size > MAX_BYTES) {
    console.error('Archivo demasiado grande')
    mostrarErrorArchivo.value = true
    if (fileInput.value) fileInput.value.value = null
    return
  }

  try {
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64 = e.target.result
      const nombre = f.name
      const tipo = f.type.startsWith('image/') ? 'IMAGEN' : 'DOCUMENTO'
      emit('subir-archivo', { nombre, base64, tipo })
      if (fileInput.value) fileInput.value.value = null
    }
    reader.readAsDataURL(f)
  } catch (err) {
    console.error('Error procesando archivo:', err)
  }
}

// Manejar la entrada del teclado en el textarea.
const handleKeydown = (e) => {
  if (mostrarSugerenciasMencion.value && sugerenciasFiltradas.value.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      indiceSugerenciaMencion.value = (indiceSugerenciaMencion.value + 1) % sugerenciasFiltradas.value.length
      return
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      indiceSugerenciaMencion.value = (indiceSugerenciaMencion.value - 1 + sugerenciasFiltradas.value.length) % sugerenciasFiltradas.value.length
      return
    }
    if (e.key === 'Enter') {
      e.preventDefault()
      const sug = sugerenciasFiltradas.value[indiceSugerenciaMencion.value]
      if (sug) seleccionarMencion(sug)
      return
    }
    if (e.key === 'Escape') {
      e.preventDefault()
      mostrarSugerenciasMencion.value = false
      return
    }
  }

  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    enviarMensaje()
  }
}

// Manejar la escritura en el textarea para detectar menciones.
const handleTextareaInput = (e) => {
  const el = e.target
  const val = el.value
  const selStart = el.selectionStart || 0
  
  const textBeforeCursor = val.slice(0, selStart)
  const lastAtIdx = textBeforeCursor.lastIndexOf('@')
  
  if (lastAtIdx !== -1) {
    const charBeforeAt = lastAtIdx > 0 ? textBeforeCursor.charAt(lastAtIdx - 1) : ' '
    if (charBeforeAt === ' ' || charBeforeAt === '\n') {
      const query = textBeforeCursor.slice(lastAtIdx + 1)
      if (!query.includes(' ')) {
        mostrarSugerenciasMencion.value = true
        busquedaMencion.value = query
        posicionArroba.value = lastAtIdx
        
        nextTick(() => {
          if (indiceSugerenciaMencion.value >= sugerenciasFiltradas.value.length) {
            indiceSugerenciaMencion.value = 0
          }
        })
        return
      }
    }
  }
  
  mostrarSugerenciasMencion.value = false
  posicionArroba.value = -1
  busquedaMencion.value = ''
}

// Insertar un arroba en la posicion actual del cursor.
const insertarArroba = () => {
  if (!props.conversacionActual || (props.conversacionActual.tipo !== 'GRUPO' && props.conversacionActual.tipo !== 'AVISO')) return
  
  const el = textareaMensaje.value
  if (!el) return
  
  const selStart = el.selectionStart || 0
  const val = contenidoNuevo.value
  
  contenidoNuevo.value = val.slice(0, selStart) + '@' + val.slice(selStart)
  
  nextTick(() => {
    el.focus()
    el.setSelectionRange(selStart + 1, selStart + 1)
    
    mostrarSugerenciasMencion.value = true
    busquedaMencion.value = ''
    posicionArroba.value = selStart
    indiceSugerenciaMencion.value = 0
  })
}

// Seleccionar un usuario para la mencion.
const seleccionarMencion = (sug) => {
  const el = textareaMensaje.value
  if (!el || posicionArroba.value === -1) return
  
  const val = contenidoNuevo.value
  const selStart = el.selectionStart || 0
  
  const usernameMencion = sug.esTodos ? 'todos' : sug.username
  const textoMencion = `@${usernameMencion} `
  
  const nuevoValor = val.slice(0, posicionArroba.value) + textoMencion + val.slice(selStart)
  contenidoNuevo.value = nuevoValor
  
  const arrobaPos = posicionArroba.value
  
  mostrarSugerenciasMencion.value = false
  posicionArroba.value = -1
  busquedaMencion.value = ''
  
  nextTick(() => {
    el.focus()
    const nuevaPos = arrobaPos + textoMencion.length
    el.setSelectionRange(nuevaPos, nuevaPos)
  })
}

// Enviar el mensaje actual.
const enviarMensaje = () => {
  if (!contenidoNuevo.value.trim()) return
  emit('enviar-mensaje', contenidoNuevo.value.trim())
  contenidoNuevo.value = ''
}
</script>

<style scoped>
.entrada-mensaje {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--surface);
  flex-shrink: 0;
  z-index: 10;
  border-top: 1px solid rgba(64,109,115,0.08);
  min-height: 60px;
}

.read-only-banner {
  background: rgba(64,109,115,0.05) !important;
  border-top: 1px solid rgba(64,109,115,0.08) !important;
  color: #406D73 !important;
  padding: 16px;
  font-weight: bold;
}

.input-mensaje {
  flex: 1;
  padding: 10px 16px;
  border: 1.5px solid rgba(64,109,115,0.15);
  border-radius: 24px;
  font-size: 14px;
  font-family: 'Inter', system-ui, sans-serif;
  color: var(--text-primary);
  background: var(--input-bg);
  outline: none;
  min-width: 0;
  max-height: 120px;
  line-height: 1.45;
  transition: border-color .18s ease, box-shadow .18s ease, background .18s ease;
}

.input-mensaje::placeholder {
  color: rgba(64,109,115,0.38);
  font-weight: 400;
}

.input-mensaje:focus {
  border-color: #406D73;
  box-shadow: 0 0 0 3px rgba(64,109,115,0.12);
  background: var(--surface);
}

.btn-enviar-pill {
  flex-shrink: 0;
  border-radius: 20px !important;
  padding: 0 16px !important;
  transition: transform .15s ease, box-shadow .15s ease, background-color .15s !important;
}

.btn-enviar-inactivo {
  cursor: default !important;
  box-shadow: none !important;
}

.btn-accion-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: transparent;
  color: var(--teal);
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s, color 0.2s, transform 0.15s ease;
  box-sizing: border-box;
  flex-shrink: 0;
}

.btn-accion-chat:hover {
  background-color: var(--teal);
  color: #ffffff;
  transform: scale(1.08);
}

/* Sugerencias mención */
.sugerencias-mencion-card {
  position: absolute;
  bottom: 100%;
  left: 14px;
  right: 14px;
  background: var(--surface);
  border: 1px solid rgba(64,109,115,0.15);
  border-radius: 12px;
  box-shadow: 0 -4px 24px rgba(0,0,0,0.12);
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  padding: 6px 0;
}

.item-sugerencia {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.item-sugerencia:hover,
.item-sugerencia.activo {
  background-color: rgba(64,109,115,0.06);
}

.avatar-mini-mencion {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #5a8a94;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.avatar-mini-mencion.todos {
  background: #6a9e7d;
}

.info-sugerencia {
  display: flex;
  flex-direction: column;
}

.info-sugerencia .username {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.info-sugerencia .rol {
  font-size: 11px;
  color: var(--text-secondary);
}

/* Picker emoji */
.emoji-picker-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.btn-emoji-item {
  font-size: 20px;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.1s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-emoji-item:hover {
  transform: scale(1.2);
}
</style>
