import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { capturePaypalOrder } from "../services/paymentService";
import theme from "../theme";

export default function PaypalSuccessPage() {
    const navigate = useNavigate();
    const [status, setStatus] = useState("loading"); // loading | success | error
    const [error, setError]   = useState("");

    useEffect(() => {
        const capture = async () => {
            // PayPal met l'orderId dans ?token= dans l'URL de retour
            const params  = new URLSearchParams(window.location.search);
            const orderId = params.get("token");

            if (!orderId) {
                setError("Identifiant de commande PayPal introuvable dans l'URL.");
                setStatus("error");
                return;
            }

            try {
                await capturePaypalOrder(orderId);
                setStatus("success");
            } catch (e) {
                setError(
                    e.response?.status === 400
                        ? "Capture échouée — commande invalide ou déjà capturée."
                        : "Erreur lors de la confirmation du paiement."
                );
                setStatus("error");
            }
        };

        capture();
    }, []);

    if (status === "loading") {
        return (
            <div style={{ maxWidth: 440, margin: "80px auto", padding: "0 32px", textAlign: "center" }}>
                <div style={{ width: 48, height: 48, border: `2px solid ${theme.border}`, borderTopColor: theme.info, borderRadius: "50%", animation: "spin 1s linear infinite", margin: "0 auto 24px" }} />
                <p style={{ color: theme.textMuted }}>Confirmation du paiement PayPal…</p>
            </div>
        );
    }

    if (status === "error") {
        return (
            <div style={{ maxWidth: 440, margin: "80px auto", padding: "0 32px", textAlign: "center" }}>
                <p style={{ color: theme.error, fontSize: 15, marginBottom: 24 }}>{error}</p>
                <button className="bpx-btn-ghost" onClick={() => navigate("/payments/paypal")}>
                    Réessayer
                </button>
            </div>
        );
    }

    return (
        <div className="slide-up" style={{ maxWidth: 440, margin: "0 auto", padding: "80px 32px", textAlign: "center" }}>
            <div style={{ width: 80, height: 80, background: `${theme.success}18`, border: `2px solid ${theme.success}44`, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 28px" }}>
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke={theme.success} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 6L9 17l-5-5" />
                </svg>
            </div>
            <h2 style={{ fontSize: 24, fontWeight: 600, marginBottom: 10 }}>Paiement confirmé</h2>
            <p style={{ color: theme.textMuted, marginBottom: 32 }}>
                Votre paiement PayPal a bien été capturé.
            </p>
            <button className="bpx-btn-gold" onClick={() => navigate("/")}>
                Retour à l'accueil
            </button>
        </div>
    );
}