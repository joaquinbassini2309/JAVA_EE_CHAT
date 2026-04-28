import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  base: '/chat-empresarial/',

  plugins: [vue()],
  base: './',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/chat-empresarial',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  },
  build: {
    outDir: '../../CapaServicio/src/main/webapp', 
    // Cambiado a false para evitar que se borre el web.xml
    emptyOutDir: false,
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'vuetify': ['vuetify']
        }
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "@/assets/estilos.scss";'
      }
    }
  }
})
