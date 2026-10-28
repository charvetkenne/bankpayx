import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    // Force une seule copie de React/ReactDOM dans le bundle
    // (évite « Cannot read properties of null (reading 'useState') » si une
    // bibliothèque résout une autre copie de React).
    dedupe: ['react', 'react-dom'],
  },
})