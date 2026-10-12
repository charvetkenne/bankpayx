import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Auth } from "@supabase/auth-ui-react";
import { ThemeSupa } from "@supabase/auth-ui-shared";
import { supabase } from "../services/supabaseClient";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

export default function LoginPage() {
  const navigate = useNavigate();
  const authContext = useAuth();
  const [authView, setAuthView] = useState("sign_in"); // 'sign_in' | 'magic_link'

   // DEBUG : Affiche l'état de l'authentification
  console.log("🔍 [LoginPage] Rendu. authContext:", authContext);
  console.log("🔍 [LoginPage] User actuel:", authContext?.user);

  const user = authContext?.user;

  useEffect(() => {
    if (user) {
      navigate("/", { replace: true });
    }
  }, [user, navigate]);

 // Si le contexte est undefined, on affiche une erreur au lieu d'une page blanche
  if (!authContext) {
    return (
      <div style={{ padding: 40, color: "red" }}>
        ERREUR : AuthProvider n'est pas configuré correctement dans main.jsx
      </div>
    );
  }

return (
    <div style={{ 
      maxWidth: 420, 
      margin: "60px auto", 
      padding: 32, 
      background: theme.surface || "#1a2440", 
      borderRadius: 12, 
      border: `1px solid ${theme.border || "#2a3550"}`,
      boxShadow: "0 8px 32px rgba(0,0,0,0.2)",
      color: theme.text || "#ffffff" // Force la couleur du texte au cas où
    }}>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 8 }}>
          Connexion à BankPayX
        </h2>
        <p style={{ fontSize: 13, opacity: 0.7 }}>
          Choisissez votre méthode de connexion
        </p>
      </div>

      {/* Sélecteur de méthode */}
      <div style={{
        display: "flex",
        background: theme.bg || "#0f172a",
        borderRadius: 8,
        padding: 4,
        marginBottom: 24,
        border: `1px solid ${theme.border || "#2a3550"}`
      }}>
        <button onClick={() => setAuthView("sign_in")} style={{ flex: 1, padding: 10, background: authView === "sign_in" ? theme.surface : "transparent", color: theme.text, border: "none", borderRadius: 6, cursor: "pointer" }}>
          🔑 Mot de passe
        </button>
        <button onClick={() => setAuthView("magic_link")} style={{ flex: 1, padding: 10, background: authView === "magic_link" ? theme.surface : "transparent", color: theme.text, border: "none", borderRadius: 6, cursor: "pointer" }}>
          ✨ Magic Link
        </button>
      </div>

      {/* Formulaire Supabase */}
      <Auth
        supabaseClient={supabase}
        view={authView}
        appearance={{ theme: ThemeSupa }}
        providers={[]}
        redirectTo={window.location.origin}
      />
    </div>
  );
}