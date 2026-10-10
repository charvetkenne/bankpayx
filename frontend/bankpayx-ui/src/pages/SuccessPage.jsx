import { useNavigate, useLocation } from "react-router-dom";
import theme from "../theme";

export default function SuccessPage() {
  const navigate = useNavigate();
  const { state } = useLocation();

  if (!state) {
    navigate("/");
    return null;
  }

  return (
    <div className="slide-up" style={{ maxWidth: 440, margin: "0 auto", padding: "80px 32px", textAlign: "center" }}>
      <div style={{ width: 80, height: 80, background: `${theme.success}18`, border: `2px solid ${theme.success}44`, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 28px" }}>
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke={theme.success} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M20 6L9 17l-5-5" />
        </svg>
      </div>

      <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 10, color: theme.text }}>
        Paiement confirmé
      </h2>
      <p style={{ color: theme.textMuted, fontSize: 15, marginBottom: 32 }}>
        Votre paiement de{" "}
        <span style={{ color: theme.gold, fontWeight: 600 }}>
          {state.amount} {state.currency}
        </span>{" "}
        a été traité via{" "}
        <span style={{ color: theme.text }}>
          {state.type === "card" ? "Stripe" : "PayPal"}
        </span>.
      </p>

      <div style={{ background: theme.surface, border: `1px solid ${theme.border}`, borderRadius: 10, padding: "14px 20px", marginBottom: 28, fontSize: 12, color: theme.textMuted, fontFamily: "JetBrains Mono, monospace" }}>
        {state.transactionId || `TXN-${Math.random().toString(36).slice(2, 10).toUpperCase()}`}
        {" · "}
        {new Date().toLocaleString("fr-FR")}
      </div>

      <button className="bpx-btn-gold" onClick={() => navigate("/")}>
        Nouveau paiement
      </button>
    </div>
  );
}
