import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Auth } from "@supabase/auth-ui-react";
import { ThemeSupa } from "@supabase/auth-ui-shared";
import { supabase } from "../services/supabaseClient";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

export default function LoginPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [authView, setAuthView] = useState("sign_in"); // 'sign_in' | 'magic_link'

  useEffect(() => {
    if (user) {
      navigate("/", { replace: true });
    }
  }, [user, navigate]);

  return (
    <div style={{ 
      maxWidth: 420, 
      margin: "60px auto", 
      padding: 32, 
      background: theme.surface, 
      borderRadius: 12, 
      border: `1px solid ${theme.border}`,
      boxShadow: "0 8px 32px rgba(0,0,0,0.2)"
    }}>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, color: theme.text, letterSpacing: "-0.02em", marginBottom: 8 }}>
          Connexion à BankPayX
        </h2>
        <p style={{ fontSize: 13, color: theme.textMuted }}>
          Choisissez votre méthode de connexion préférée
        </p>
      </div>

      {/* Sélecteur de méthode (Toggle) */}
      <div style={{
        display: "flex",
        background: theme.bg,
        borderRadius: 8,
        padding: 4,
        marginBottom: 24,
        border: `1px solid ${theme.border}`
      }}>
        <button
          onClick={() => setAuthView("sign_in")}
          style={{
            flex: 1, padding: "10px 16px", fontSize: 13, fontWeight: 600,
            fontFamily: "'Sora', system-ui, sans-serif", border: "none", borderRadius: 6,
            cursor: "pointer",
            background: authView === "sign_in" ? theme.surface : "transparent",
            color: authView === "sign_in" ? theme.text : theme.textMuted,
            boxShadow: authView === "sign_in" ? "0 2px 8px rgba(0,0,0,0.1)" : "none",
            transition: "all 0.2s ease"
          }}
        >
          🔑 Mot de passe
        </button>
        <button
          onClick={() => setAuthView("magic_link")}
          style={{
            flex: 1, padding: "10px 16px", fontSize: 13, fontWeight: 600,
            fontFamily: "'Sora', system-ui, sans-serif", border: "none", borderRadius: 6,
            cursor: "pointer",
            background: authView === "magic_link" ? theme.surface : "transparent",
            color: authView === "magic_link" ? theme.text : theme.textMuted,
            boxShadow: authView === "magic_link" ? "0 2px 8px rgba(0,0,0,0.1)" : "none",
            transition: "all 0.2s ease"
          }}
        >
          ✨ Magic Link
        </button>
      </div>

      {/* Formulaire Supabase (le style est géré par globalStyle.js) */}
      <Auth
        supabaseClient={supabase}
        view={authView}
        appearance={{ theme: ThemeSupa }}
        providers={[]}
        redirectTo={window.location.origin}
        localization={{
          variables: {
            sign_in: {
              email_label: "Adresse email",
              password_label: "Votre mot de passe",
              email_input_placeholder: "vous@exemple.com",
              password_input_placeholder: "••••••••",
              button_label: "Se connecter",
              loading_button_label: "Connexion en cours...",
              link_text: "Vous n'avez pas de compte ? Inscrivez-vous",
            },
            magic_link: {
              email_input_label: "Adresse email",
              email_input_placeholder: "vous@exemple.com",
              button_label: "Envoyer le lien magique",
              loading_button_label: "Envoi en cours...",
              link_text: "Vous n'avez pas de compte ? Inscrivez-vous",
              confirmation_text: "Consultez votre boîte mail pour le lien de connexion",
            },
            sign_up: {
              email_label: "Adresse email",
              password_label: "Créer un mot de passe",
              email_input_placeholder: "vous@exemple.com",
              password_input_placeholder: "••••••••",
              button_label: "Créer mon compte",
              loading_button_label: "Création en cours...",
              link_text: "Vous avez déjà un compte ? Connectez-vous",
            }
          }
        }}
      />

      <div style={{ 
        marginTop: 20, padding: 12, background: `${theme.gold}08`, borderRadius: 8,
        border: `1px solid ${theme.gold}22`, fontSize: 12, color: theme.textMuted, textAlign: "center"
      }}>
        {authView === "magic_link" 
          ? "✨ Un lien de connexion sécurisé sera envoyé à votre email. Aucun mot de passe à retenir !"
          : "🔒 Connectez-vous avec votre mot de passe. Sécurisé et classique."
        }
      </div>
    </div>
  );
}