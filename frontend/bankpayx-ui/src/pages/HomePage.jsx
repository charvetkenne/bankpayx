import { useNavigate } from "react-router-dom";
import { getCurrentUser } from "../services/authService";
import theme from "../theme";

export default function HomePage() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  return (
    <div className="slide-up" style={{ maxWidth: 800, margin: "0 auto", padding: "60px 32px" }}>
      <div style={{ marginBottom: 48 }}>
        <p style={{ color: theme.gold, fontSize: 13, fontWeight: 500, letterSpacing: "0.12em", textTransform: "uppercase", marginBottom: 12 }}>
          Bonjour
        </p>
        <h1 style={{ fontSize: 38, fontWeight: 600, letterSpacing: "-0.03em", lineHeight: 1.1, marginBottom: 12 }}>
          {user.firstName}{" "}
          <span style={{ color: theme.textMuted, fontWeight: 300 }}>{user.lastName}</span>
        </h1>
        <p style={{ color: theme.textMuted, fontSize: 15 }}>
          Quel paiement souhaitez-vous effectuer aujourd'hui ?
        </p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginBottom: 32 }}>
        {/* Carte bancaire */}
        <button
          onClick={() => navigate("/payments/card")}
          style={{ background: theme.surface, border: `1px solid ${theme.border}`, borderRadius: 14, padding: 28, cursor: "pointer", textAlign: "left", transition: "all 0.2s" }}
          onMouseEnter={(e) => { e.currentTarget.style.borderColor = theme.gold; e.currentTarget.style.background = `${theme.gold}08`; }}
          onMouseLeave={(e) => { e.currentTarget.style.borderColor = theme.border; e.currentTarget.style.background = theme.surface; }}
        >
          <div style={{ width: 44, height: 44, background: `${theme.gold}18`, borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 18 }}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={theme.gold} strokeWidth="1.5" strokeLinecap="round">
              <rect x="2" y="5" width="20" height="14" rx="3" />
              <path d="M2 10h20" />
            </svg>
          </div>
          <div style={{ fontSize: 16, fontWeight: 600, marginBottom: 6, color: theme.text }}>Carte bancaire</div>
          <div style={{ fontSize: 13, color: theme.textMuted, lineHeight: 1.5 }}>Visa, Mastercard via Stripe</div>
        </button>

        {/* PayPal */}
        <button
          onClick={() => navigate("/payments/paypal")}
          style={{ background: theme.surface, border: `1px solid ${theme.border}`, borderRadius: 14, padding: 28, cursor: "pointer", textAlign: "left", transition: "all 0.2s" }}
          onMouseEnter={(e) => { e.currentTarget.style.borderColor = theme.info; e.currentTarget.style.background = `${theme.info}08`; }}
          onMouseLeave={(e) => { e.currentTarget.style.borderColor = theme.border; e.currentTarget.style.background = theme.surface; }}
        >
          <div style={{ width: 44, height: 44, background: `${theme.info}18`, borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 18 }}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={theme.info} strokeWidth="1.5" strokeLinecap="round">
              <path d="M7 11c0 3.866 2.686 7 6 7h1c3.314 0 6-3.134 6-7S17.314 4 14 4h-1C9.686 4 7 7.134 7 11Z" />
              <path d="M3 15c0 2.761 1.791 5 4 5h1c2.209 0 4-2.239 4-5s-1.791-5-4-5H7C4.791 10 3 12.239 3 15Z" />
            </svg>
          </div>
          <div style={{ fontSize: 16, fontWeight: 600, marginBottom: 6, color: theme.text }}>PayPal</div>
          <div style={{ fontSize: 13, color: theme.textMuted, lineHeight: 1.5 }}>Paiement express PayPal</div>
        </button>
      </div>

      {/* Statut session */}
      <div style={{ background: theme.surfaceHigh, border: `1px solid ${theme.border}`, borderRadius: 10, padding: "14px 18px", display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ width: 8, height: 8, borderRadius: "50%", background: theme.success, flexShrink: 0 }} className="pulse" />
        <div style={{ fontSize: 12, color: theme.textMuted }}>
          <span style={{ color: theme.text, fontWeight: 500 }}>Session sécurisée · </span>
          OAuth2 PKCE · JWT actif ·{" "}
          <span style={{ fontFamily: "JetBrains Mono, monospace", fontSize: 11 }}>
            realm: {import.meta.env.VITE_KEYCLOAK_REALM || "bankpayx"}
          </span>
          {" · "}Rôles : <span style={{ color: theme.gold }}>{user.roles.slice(0, 3).join(", ")}</span>
        </div>
      </div>
    </div>
  );
}
