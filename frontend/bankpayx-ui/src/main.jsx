import { Component, StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

/**
 * main.jsx de diagnostic : transforme toute erreur invisible (page blanche)
 * en message lisible directement dans la page.
 * - AuthProvider est déjà monté dans App.jsx : ne pas le dupliquer ici.
 * - App est chargé par import() dynamique pour intercepter aussi les erreurs
 *   levées à l'import d'un module (ex. supabaseClient.js sans variables d'env).
 */

function showFatal(title, error) {
  const detail = error?.stack || error?.message || String(error)
  console.error(title, error)

  let box = document.getElementById('bpx-fatal')
  if (!box) {
    box = document.createElement('pre')
    box.id = 'bpx-fatal'
    box.style.cssText =
      'position:fixed;inset:0;z-index:99999;margin:0;padding:24px;overflow:auto;' +
      'background:#1a0b0b;color:#ffb4b4;font:13px/1.5 monospace;white-space:pre-wrap;'
    document.body.appendChild(box)
  }
  box.textContent += `\n[${title}]\n${detail}\n`
}

window.addEventListener('error', (e) => showFatal('Erreur JavaScript', e.error || e.message))
window.addEventListener('unhandledrejection', (e) => showFatal('Promesse rejetée', e.reason))

class ErrorBoundary extends Component {
  state = { error: null }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    console.error('Erreur de rendu React :', error, info?.componentStack)
  }

  render() {
    if (this.state.error) {
      return (
        <pre style={{ padding: 24, color: '#ffb4b4', background: '#1a0b0b', whiteSpace: 'pre-wrap', minHeight: '100vh', margin: 0 }}>
          {`Erreur de rendu React\n\n${this.state.error.stack || this.state.error}`}
        </pre>
      )
    }
    return this.props.children
  }
}

import('./App.jsx')
  .then(({ default: App }) => {
    createRoot(document.getElementById('root')).render(
      <StrictMode>
        <ErrorBoundary>
          <App />
        </ErrorBoundary>
      </StrictMode>,
    )
  })
  .catch((err) => showFatal("Échec du chargement de l'application (erreur à l'import d'un module)", err))