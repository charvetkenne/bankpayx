import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../services/supabaseClient";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

/**
 * Page de connexion Supabase (formulaire maison, sans @supabase/auth-ui-react).
 * Deux méthodes : mot de passe ou lien magique (e-mail).
 */
export default function LoginPage() {
  const navigate = useNavigate();
  const auth = useAuth();
  const user = auth?.user ?? null;

  const [mode, setMode] = useState("password"); // 'password' | 'magic_link'
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");

  // Déjà connecté (ou connexion réussie) → retour à l'accueil
  useEffect(() => {
    if (user) navigate("/", { replace: true });
  }, [user, navigate]);

  const switchMode = (next) => {
    setMode(next);
    setError("");
    setInfo("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setInfo("");

    const cleanEmail = email.trim();
    if (!cleanEmail) {
      setError("Saisissez votre adresse e-mail.");
      return;
    }

    setSubmitting(true);
    try {
      if (mode === "password") {
        if (!password) {
          setError("Saisissez votre mot de passe.");
          return;
        }
        const { error: signInError } = await supabase.auth.signInWithPassword({
          email: cleanEmail,
          password,
        });
        if (signInError) {
          setError(signInError.message);
        }
        // Succès : onAuthStateChange met à jour le contexte, l'effet ci-dessus redirige.
      } else {
        const { error: otpError } = await supabase.auth.signInWithOtp({
          email: cleanEmail,
          options: { emailRedirectTo: window.location.origin },
        });
        if (otpError) {
          setError(otpError.message);
        } else {
          setInfo("Lien de connexion envoyé. Consultez votre boîte mail.");
        }
      }
    } catch (err) {
      setError(err?.message || "Une erreur est survenue. Réessayez.");
    } finally {
      setSubmitting(false);
    }
  };

  const tabStyle = (active) => ({
    flex: 1,
    padding: 10,
    background: active ? theme.surface : "transparent",
    color: theme.text,
    border: "none",
    borderRadius: 6,
    cursor: "pointer",
  });

  return (
    <div
      style={{
        maxWidth: 420,
        margin: "60px auto",
        padding: 32,
        background: theme.surface || "#1a2440",
        borderRadius: 12,
        border: `1px solid ${theme.border || "#2a3550"}`,
        boxShadow: "0 8px 32px rgba(0,0,0,0.2)",
        color: theme.text || "#ffffff",
      }}
    >
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 8 }}>Connexion à BankPayX</h2>
        <p style={{ fontSize: 13, opacity: 0.7 }}>Choisissez votre méthode de connexion</p>
      </div>

      {/* Sélecteur de méthode */}
      <div
        style={{
          display: "flex",
          background: theme.bg || "#0f172a",
          borderRadius: 8,
          padding: 4,
          marginBottom: 24,
          border: `1px solid ${theme.border || "#2a3550"}`,
        }}
      >
        <button type="button" onClick={() => switchMode("password")} style={tabStyle(mode === "password")}>
          🔑 Mot de passe
        </button>
        <button type="button" onClick={() => switchMode("magic_link")} style={tabStyle(mode === "magic_link")}>
          ✨ Magic Link
        </button>
      </div>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <div>
          <label className="bpx-label" htmlFor="login-email">Adresse e-mail</label>
          <input
            id="login-email"
            className="bpx-input"
            type="email"
            autoComplete="email"
            placeholder="vous@exemple.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        {mode === "password" && (
          <div>
            <label className="bpx-label" htmlFor="login-password">Mot de passe</label>
            <input
              id="login-password"
              className="bpx-input"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
        )}

        {error && (
          <div
            role="alert"
            style={{
              background: theme.errorBg,
              border: `1px solid ${theme.error}33`,
              borderRadius: 8,
              padding: "12px 16px",
              fontSize: 13,
              color: theme.error,
            }}
          >
            {error}
          </div>
        )}

        {info && (
          <div
            role="status"
            style={{
              border: `1px solid ${theme.success}33`,
              borderRadius: 8,
              padding: "12px 16px",
              fontSize: 13,
              color: theme.success,
            }}
          >
            {info}
          </div>
        )}

        <button
          type="submit"
          className="bpx-btn-gold"
          disabled={submitting}
          style={{ width: "100%", fontSize: 15, marginTop: 6 }}
        >
          {submitting
            ? "Connexion en cours…"
            : mode === "password"
            ? "Se connecter"
            : "Envoyer le lien de connexion"}
        </button>
      </form>
    </div>
  );
}

// import { useEffect, useState } from "react";
// import { useNavigate } from "react-router-dom";
// import { Auth } from "@supabase/auth-ui-react";
// import { ThemeSupa } from "@supabase/auth-ui-shared";
// import { supabase } from "../services/supabaseClient";
// import { useAuth } from "../auth/AuthProvider";
// import theme from "../theme";

// export default function LoginPage() {
//   const navigate = useNavigate();
//   const authContext = useAuth();
//   const [authView, setAuthView] = useState("sign_in"); // 'sign_in' | 'magic_link'

//    // DEBUG : Affiche l'état de l'authentification
//   console.log("🔍 [LoginPage] Rendu. authContext:", authContext);
//   console.log("🔍 [LoginPage] User actuel:", authContext?.user);

//   const user = authContext?.user;

//   useEffect(() => {
//     if (user) {
//       navigate("/", { replace: true });
//     }
//   }, [user, navigate]);

//  // Si le contexte est undefined, on affiche une erreur au lieu d'une page blanche
//   if (!authContext) {
//     return (
//       <div style={{ padding: 40, color: "red" }}>
//         ERREUR : AuthProvider n'est pas configuré correctement dans main.jsx
//       </div>
//     );
//   }

// return (
//     <div style={{ 
//       maxWidth: 420, 
//       margin: "60px auto", 
//       padding: 32, 
//       background: theme.surface || "#1a2440", 
//       borderRadius: 12, 
//       border: `1px solid ${theme.border || "#2a3550"}`,
//       boxShadow: "0 8px 32px rgba(0,0,0,0.2)",
//       color: theme.text || "#ffffff" // Force la couleur du texte au cas où
//     }}>
//       <div style={{ textAlign: "center", marginBottom: 28 }}>
//         <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 8 }}>
//           Connexion à BankPayX
//         </h2>
//         <p style={{ fontSize: 13, opacity: 0.7 }}>
//           Choisissez votre méthode de connexion
//         </p>
//       </div>

//       {/* Sélecteur de méthode */}
//       <div style={{
//         display: "flex",
//         background: theme.bg || "#0f172a",
//         borderRadius: 8,
//         padding: 4,
//         marginBottom: 24,
//         border: `1px solid ${theme.border || "#2a3550"}`
//       }}>
//         <button onClick={() => setAuthView("sign_in")} style={{ flex: 1, padding: 10, background: authView === "sign_in" ? theme.surface : "transparent", color: theme.text, border: "none", borderRadius: 6, cursor: "pointer" }}>
//           🔑 Mot de passe
//         </button>
//         <button onClick={() => setAuthView("magic_link")} style={{ flex: 1, padding: 10, background: authView === "magic_link" ? theme.surface : "transparent", color: theme.text, border: "none", borderRadius: 6, cursor: "pointer" }}>
//           ✨ Magic Link
//         </button>
//       </div>

//       {/* Formulaire Supabase */}
//       <Auth
//         supabaseClient={supabase}
//         view={authView}
//         appearance={{ theme: ThemeSupa }}
//         providers={[]}
//         redirectTo={window.location.origin}
//       />
//     </div>
//   );
// }