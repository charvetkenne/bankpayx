import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import AuthProvider from './auth/AuthProvider.jsx'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* L'AuthProvider DOIT être au-dessus de tout le reste.
        Il ne dépend pas du Router : le BrowserRouter reste dans AppRouter.jsx */}
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
)