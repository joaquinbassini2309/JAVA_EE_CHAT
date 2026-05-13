import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  // Use VITE_CONTEXT_PATH to build for a specific context (e.g. '/chat-empresarial/').
  // Fallback to relative paths ('./') so the built SPA works when deployed either
  // at root or under a subpath without requiring absolute references.
  base: process.env.VITE_CONTEXT_PATH || './',

  plugins: [vue()],
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
    outDir: 'dist',
    emptyOutDir: true,
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
        additionalData: '@import "@/recursos/estilos.scss";'
      }
    }
  }
})
