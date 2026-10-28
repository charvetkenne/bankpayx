import { useNavigate } from "react-router-dom";
import theme from "../theme";

export default function PaymentPage() {
  const navigate = useNavigate();

  return (
    <div className="slide-up" style={{ maxWidth: 520, margin: "0 auto", padding: "48px 32px" }}>
      <h2 style={{ fontSize: 26, fontWeight: 600, letterSpacing: "-0.02em", marginBottom: 24 }}>
        Choisir un moyen de paiement
      </h2>

      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <button
          onClick={() => navigate("/payments/card")}
          className="bpx-btn-gold"
          style={{ width: "100%", fontSize: 15 }}
        >
          Paiement Carte
        </button>
        <button
          onClick={() => navigate("/payments/paypal")}
          className="bpx-btn-ghost"
          style={{ width: "100%", fontSize: 15, padding: "13px 28px", color: theme.info, borderColor: theme.info + "44" }}
        >
          Paiement PayPal
        </button>
      </div>
    </div>
  );
}
