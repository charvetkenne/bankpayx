import { createContext, useContext, useEffect, useState } from "react";
import keycloak from "./keycloak";
import theme from "../theme";

const AuthContext = createContext(null);

export function useAuth() {
  return useContext(AuthContext);
}

export default function AuthProvider({ children }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    keycloak
      .init({
        onLoad: "login-required",
        pkceMethod: "S256",
      })
      .then((auth) => {
         console.log("TOKEN PARSED:", keycloak.tokenParsed);
        setAuthenticated(auth);
        setLoading(false);

        // Rafraîchissement automatique du token toutes les 5 minutes
        if (auth) {
          setInterval(() => {
            keycloak
              .updateToken(300)
              .catch(() => keycloak.logout());
          }, 60_000);
        }
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          background: theme.bg,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: 24,
          fontFamily: "Sora, system-ui, sans-serif",
        }}
      >
        <div style={{ position: "relative", width: 64, height: 64 }}>
          <div
            style={{
              position: "absolute",
              inset: 0,
              border: `2px solid ${theme.border}`,
              borderTopColor: theme.gold,
              borderRadius: "50%",
              animation: "spin 1s linear infinite",
            }}
          />
        </div>
        <div style={{ textAlign: "center" }}>
          <p style={{ color: theme.textMuted, fontSize: 14 }}>
            Authentification Keycloak
          </p>
          <p style={{ color: theme.textDim, fontSize: 12, marginTop: 4 }}>
            OAuth2 PKCE · realm:{" "}
            {import.meta.env.VITE_KEYCLOAK_REALM || "bankpayx"}
          </p>
        </div>
        <style>{`@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
      </div>
    );
  }

  if (!authenticated) return null;

  return (
    <AuthContext.Provider value={keycloak}>
      {children}
    </AuthContext.Provider>
  );
}
