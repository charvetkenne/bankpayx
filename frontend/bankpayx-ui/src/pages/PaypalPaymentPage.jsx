import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPaypalOrder } from "../services/paymentService";
import theme from "../theme";

export default function PaypalPaymentPage() {
  const navigate = useNavigate();
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    setError("");
    if (!amount || isNaN(Number(amount))) return setError("Montant invalide.");

    setLoading(true);
    try {
     const data = await createPaypalOrder({
                amount:      Number(amount),
                currency:    "EUR",           // majuscules obligatoires
                merchantId:  "merchant_1",
                description: "Commande via BankPayX",
                returnUrl:   "http://localhost:5173/paypal/success",
                cancelUrl:   "http://localhost:5173/paypal/cancel",
            });

            // Redirige vers la page d'approbation PayPal
            window.location.href = data.approveUrl;
    } catch (e) {
      setError(
        e.response?.status === 400
                    ? "Paramètres invalides — vérifiez le montant et la devise."
                    : "Impossible de joindre le service PayPal."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="slide-up" style={{ maxWidth: 440, margin: "0 auto", padding: "48px 32px" }}>
      <div style={{ marginBottom: 32 }}>
        <p style={{ fontSize: 12, color: theme.textMuted, letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: 8 }}>
          Paiement sécurisé
        </p>
        <h2 style={{ fontSize: 26, fontWeight: 600, letterSpacing: "-0.02em" }}>PayPal</h2>
      </div>

      <div className="bpx-card" style={{ marginBottom: 16 }}>
        <div style={{ background: "#0E1C3A", borderRadius: 10, padding: 24, marginBottom: 24, textAlign: "center", border: `1px solid ${theme.info}33` }}>
          <div style={{ width: 56, height: 56, background: `${theme.info}18`, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 14px" }}>
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke={theme.info} strokeWidth="1.5" strokeLinecap="round">
              <path d="M7 11c0 3.866 2.686 7 6 7h1c3.314 0 6-3.134 6-7S17.314 4 14 4h-1C9.686 4 7 7.134 7 11Z" />
              <path d="M3 15c0 2.761 1.791 5 4 5h1c2.209 0 4-2.239 4-5s-1.791-5-4-5H7C4.791 10 3 12.239 3 15Z" />
            </svg>
          </div>
          <p style={{ color: theme.info, fontWeight: 600, fontSize: 15 }}>PayPal Express</p>
          <p style={{ color: theme.textMuted, fontSize: 12, marginTop: 4 }}>Redirection sécurisée vers PayPal pour le paiement</p>
        </div>

        <div>
          <label className="bpx-label">Montant (EUR)</label>
          <input
            className="bpx-input"
            placeholder="0.00"
            value={amount}
            onChange={(e) => setAmount(e.target.value.replace(/[^0-9.]/g, ""))}
          />
        </div>
      </div>

      {error && (
        <div style={{ background: theme.errorBg, border: `1px solid ${theme.error}33`, borderRadius: 8, padding: "12px 16px", marginBottom: 14, fontSize: 13, color: theme.error }}>
          {error}
        </div>
      )}

      <button
        onClick={submit}
        disabled={loading}
        style={{
          width: "100%", background: loading ? `${theme.info}44` : `linear-gradient(135deg, ${theme.info}, #6CB4F0)`,
          border: "none", borderRadius: 8, padding: "13px 28px", fontSize: 15, fontWeight: 600,
          color: "#fff", cursor: loading ? "not-allowed" : "pointer",
          fontFamily: "Sora, system-ui, sans-serif", transition: "opacity 0.2s",
        }}
      >
        {loading ? "Redirection…" : `Payer avec PayPal${amount ? ` · ${amount} EUR` : ""}`}
      </button>

      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 8, marginTop: 14 }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke={theme.textDim} strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" /></svg>
        <span style={{ fontSize: 11, color: theme.textDim }}>JWT Bearer · PayPal Webhooks</span>
      </div>
    </div>
  );
}
