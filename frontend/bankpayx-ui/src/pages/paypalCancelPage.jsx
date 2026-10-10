import { useNavigate } from "react-router-dom";
import theme from "../theme";

export default function PaypalCancelPage() {
    const navigate = useNavigate();

    return (
        <div className="slide-up" style={{ maxWidth: 440, margin: "0 auto", padding: "80px 32px", textAlign: "center" }}>
            <div style={{ width: 80, height: 80, background: `${theme.errorBg}`, border: `2px solid ${theme.error}44`, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 28px" }}>
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke={theme.error} strokeWidth="2" strokeLinecap="round">
                    <path d="M18 6L6 18M6 6l12 12" />
                </svg>
            </div>
            <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 10 }}>Paiement annulé</h2>
            <p style={{ color: theme.textMuted, marginBottom: 32 }}>
                Vous avez annulé le paiement sur PayPal.
            </p>
            <button className="bpx-btn-gold" onClick={() => navigate("/payments/paypal")}>
                Réessayer
            </button>
        </div>
    );
}