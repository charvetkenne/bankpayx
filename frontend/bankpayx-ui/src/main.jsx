import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
     {/* L'AuthProvider DOIT être au-dessus de tout le reste */}
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
)
