# 📦 Guía de Vuetify 3 para el Proyecto

## ✅ Instalación Completada

Vuetify 3 ha sido agregado al proyecto con:
- ✅ Dependencias en `package.json` (vuetify, @mdi/js, sass)
- ✅ Configuración en `src/vuetify.js`
- ✅ Importado en `src/main.js`

## 🚀 Próximos Pasos

### 1. Instalar dependencias
```bash
cd CapaPresentacion/Web
npm install
```

### 2. Componentes Vuetify Principales

#### Formularios y Entrada
```vue
<!-- Campo de texto -->
<v-text-field
  v-model="email"
  label="Correo Electrónico"
  type="email"
  prepend-inner-icon="mdi-email"
  variant="outlined"
/>

<!-- Contraseña -->
<v-text-field
  v-model="password"
  label="Contraseña"
  type="password"
  prepend-inner-icon="mdi-lock"
  variant="outlined"
/>

<!-- Botón -->
<v-btn color="primary" size="large">
  Iniciar Sesión
</v-btn>
```

#### Layout (Contenedores)
```vue
<!-- Container -->
<v-container>
  <v-row>
    <v-col cols="12" md="6">
      Contenido
    </v-col>
  </v-row>
</v-container>

<!-- Card -->
<v-card>
  <v-card-title>Título</v-card-title>
  <v-card-text>Contenido</v-card-text>
  <v-card-actions>
    <v-btn>Acción</v-btn>
  </v-card-actions>
</v-card>
```

#### Listas
```vue
<!-- Lista simple -->
<v-list>
  <v-list-item
    v-for="item in items"
    :key="item.id"
    @click="seleccionar(item)"
  >
    <v-list-item-title>{{ item.nombre }}</v-list-item-title>
  </v-list-item>
</v-list>
```

#### Navegación
```vue
<!-- AppBar -->
<v-app-bar color="primary">
  <v-app-bar-title>Chat Empresarial</v-app-bar-title>
  <v-spacer></v-spacer>
  <v-btn icon @click="cerrarSesion">
    <v-icon>mdi-logout</v-icon>
  </v-btn>
</v-app-bar>

<!-- Navigation Drawer -->
<v-navigation-drawer v-model="drawer">
  <v-list>
    <v-list-item href="/chat">Chat</v-list-item>
  </v-list>
</v-navigation-drawer>
```

## 🎨 Colores Disponibles

El proyecto está configurado con:
- **primary**: #667eea (Azul)
- **secondary**: #764ba2 (Púrpura)
- **error**: #FF5252 (Rojo)
- **success**: #4CAF50 (Verde)
- **warning**: #FFC107 (Amarillo)

```vue
<v-btn color="primary">Botón Principal</v-btn>
<v-btn color="secondary">Botón Secundario</v-btn>
<v-btn color="success">Botón Éxito</v-btn>
```

## 📱 Sistema de Grid

Vuetify usa un sistema de grid de 12 columnas:

```vue
<v-container>
  <!-- Pantallas grandes: 6 columnas, medianas: 8, pequeñas: 12 -->
  <v-row>
    <v-col cols="12" sm="8" md="6" lg="4">
      Contenido responsive
    </v-col>
  </v-row>
</v-container>
```

## 🔧 Actualizaciones de Componentes Necesarias

### LoginVista.vue
```vue
<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card elevation="8">
          <v-card-title class="text-center">
            Chat Empresarial
          </v-card-title>
          <v-card-text>
            <v-form @submit.prevent="iniciarSesion">
              <v-text-field
                v-model="formulario.correo"
                label="Correo"
                type="email"
                prepend-inner-icon="mdi-email"
                variant="outlined"
                class="mb-4"
              />
              <v-text-field
                v-model="formulario.contrasena"
                label="Contraseña"
                type="password"
                prepend-inner-icon="mdi-lock"
                variant="outlined"
                class="mb-4"
              />
              <v-btn
                type="submit"
                color="primary"
                block
                size="large"
                :loading="cargando"
              >
                Iniciar Sesión
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
```

### ChatVista.vue
```vue
<template>
  <v-app>
    <v-app-bar color="primary">
      <v-app-bar-title>Chat Empresarial</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="cerrarSesion">
        <v-icon>mdi-logout</v-icon>
      </v-btn>
    </v-app-bar>

    <v-container fluid class="mt-4">
      <v-row>
        <v-col cols="12" md="4">
          <ListaConversaciones />
        </v-col>
        <v-col cols="12" md="8">
          <Chat />
        </v-col>
      </v-row>
    </v-container>
  </v-app>
</template>
```

### Chat.vue
```vue
<template>
  <v-card>
    <v-card-title>
      Conversación: {{ conversacionActual?.nombre }}
    </v-card-title>

    <v-divider></v-divider>

    <v-card-text class="chat-mensajes" style="height: 400px; overflow-y: auto;">
      <div v-for="msg in mensajes" :key="msg.id">
        <Mensaje :mensaje="msg" />
      </div>
    </v-card-text>

    <v-divider></v-divider>

    <v-card-text class="pa-2">
      <v-form @submit.prevent="enviarMensaje">
        <v-row no-gutters>
          <v-col cols="10">
            <v-text-field
              v-model="textoMensaje"
              placeholder="Escribe un mensaje..."
              variant="outlined"
              dense
              @keyup.enter="enviarMensaje"
            />
          </v-col>
          <v-col cols="2" class="pl-2">
            <v-btn
              icon
              color="primary"
              @click="enviarMensaje"
              :disabled="!textoMensaje"
            >
              <v-icon>mdi-send</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-form>
    </v-card-text>
  </v-card>
</template>
```

## 📚 Recursos Útiles

- **Documentación oficial**: https://vuetifyjs.com/
- **Componentes disponibles**: https://vuetifyjs.com/en/components/all/
- **Material Design Icons**: https://materialdesignicons.com/

## 💡 Tips

1. Usa `v-spacer` para separar elementos en AppBar
2. Los colores se aplican con el atributo `color="primary"`
3. Usa `variant="outlined"` para campos más modernos
4. Aprovecha el grid system (`v-row`, `v-col`) para responsive
5. Los íconos se cargan automáticamente con `<v-icon>mdi-*</v-icon>`

## ✨ Próximos Pasos

1. Reemplazar CSS manual con componentes Vuetify
2. Actualizar LoginVista.vue con v-form y v-card
3. Actualizar ChatVista.vue con v-app-bar y v-container
4. Actualizar Chat.vue con v-card y v-text-field
5. Probar responsividad con `npm run dev`
