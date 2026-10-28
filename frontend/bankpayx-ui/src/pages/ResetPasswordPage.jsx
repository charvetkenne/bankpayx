import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../services/supabaseClient";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

/**
 * Définir ou changer son mot de passe.
 * On y arrive depuis le lien reçu par e-mail (« Mot de passe oublié »),
 * qui ouvre une session ; un utilisateur déjà connecté peut aussi y accéder.
 */
export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const auth = useAuth();
  const user = auth?.user ?? null;

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!password) {
      setError("Choisissez un mot de passe.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Les deux mots de passe ne correspondent pas.");
      return;
    }

    setSubmitting(true);
    try {
      const { error: updateError } = await supabase.auth.updateUser({ password });
      if (updateError) setError(updateError.message);
      else setDone(true);
    } catch (err) {
      setError(err?.message || "Une erreur est survenue. Réessayez.");
    } finally {
      setSubmitting(false);
    }
  };

  const boxStyle = {
    maxWidth: 420,
    margin: "60px auto",
    padding: 32,
    background: theme.surface,
    borderRadius: 12,
    border: `1px solid ${theme.border}`,
    boxShadow: "0 8px 32px rgba(0,0,0,0.2)",
    color: theme.text,
  };

  // Pas de session : lien invalide, expiré, ou page ouverte directement
  if (!user) {
    return (
      <div style={boxStyle}>
        <h2 style={{ fontSize: 22, fontWeight: 600, marginBottom: 12, textAlign: "center" }}>
          Lien invalide ou expiré
        </h2>
        <p style={{ fontSize: 13, color: theme.textMuted, textAlign: "center", marginBottom: 24 }}>
          Demandez un nouveau lien depuis la page de connexion, avec « Mot de passe oublié ».
        </p>
        <button className="bpx-btn-gold" style={{ width: "100%" }} onClick={() => navigate("/login")}>
          Aller à la connexion
        </button>
      </div>
    );
  }

  if (done) {
    return (
      <div style={boxStyle}>
        <h2 style={{ fontSize: 22, fontWeight: 600, marginBottom: 12, textAlign: "center" }}>
          Mot de passe enregistré
        </h2>
        <p style={{ fontSize: 13, color: theme.textMuted, textAlign: "center", marginBottom: 24 }}>
          Vous pouvez désormais vous connecter avec votre adresse e-mail et ce mot de passe.
        </p>
        <button className="bpx-btn-gold" style={{ width: "100%" }} onClick={() => navigate("/")}>
          Retour à l'accueil
        </button>
      </div>
    );
  }

  return (
    <div style={boxStyle}>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 8 }}>Définir un mot de passe</h2>
        <p style={{ fontSize: 13, opacity: 0.7 }}>{user.email}</p>
      </div>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <div>
          <label className="bpx-label" htmlFor="new-password">Nouveau mot de passe</label>
          <input
            id="new-password"
            className="bpx-input"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <div>
          <label className="bpx-label" htmlFor="confirm-password">Confirmer le mot de passe</label>
          <input
            id="confirm-password"
            className="bpx-input"
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
        </div>

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

        <button
          type="submit"
          className="bpx-btn-gold"
          disabled={submitting}
          style={{ width: "100%", fontSize: 15, marginTop: 6 }}
        >
          {submitting ? "Enregistrement…" : "Enregistrer le mot de passe"}
        </button>
      </form>
    </div>
  );
}