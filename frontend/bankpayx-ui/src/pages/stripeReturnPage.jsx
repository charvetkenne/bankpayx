import { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { getPaymentStatus } from "../services/paymentService";
import theme from "../theme";

const POLL_INTERVAL_MS = 3000;
const POLL_MAX = 20; // 20 × 3s = 1 minute max

const SUCCESS_STATUSES = ["CAPTURED", "AUTHORIZED"];
const FAILURE_STATUSES = ["FAILED", "CANCELLED"];

export default function StripeReturnPage() {
  const navigate = useNavigate();
  const [phase, setPhase] = useState("loading"); // loading | error
  const [error, setError] = useState("");
  const pollRef = useRef(null);
  const countRef = useRef(0);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    // Cas 1 — on a navigué ici nous-mêmes après un 3DS résolu en modal
    const internalTxId = params.get("transactionId");

    // Cas 2 — retour réel depuis Stripe après redirection externe complète
    const storedTxId = sessionStorage.getItem("bpx_pending_tx");

    const transactionId = internalTxId || storedTxId;

    if (!transactionId) {
      setError("Impossible de retrouver la transaction associée à ce paiement.");
      setPhase("error");
      return;
    }

    const poll = async () => {
      countRef.current += 1;

      if (countRef.current > POLL_MAX) {
        clearInterval(pollRef.current);
        setError("Le statut du paiement n'a pas pu être confirmé. Consultez votre historique.");
        setPhase("error");
        return;
      }

      try {
        const data = await getPaymentStatus(transactionId);

        if (SUCCESS_STATUSES.includes(data.status)) {
          clearInterval(pollRef.current);
          sessionStorage.removeItem("bpx_pending_tx");
          navigate("/payments/success", {
            state: {
              type: "card",
              amount: data.amount,
              currency: data.currency,
              status: data.status,
              transactionId: data.transactionId,
            },
          });
          return;
        }

        if (FAILURE_STATUSES.includes(data.status)) {
          clearInterval(pollRef.current);
          sessionStorage.removeItem("bpx_pending_tx");
          setError(data.failureReason || "Le paiement a échoué.");
          setPhase("error");
          return;
        }

        // statut encore PENDING / en cours de confirmation par le webhook
        // → on continue le polling
      } catch {
        // erreur réseau ponctuelle — on retente au prochain cycle
      }
    };

    poll();
    pollRef.current = setInterval(poll, POLL_INTERVAL_MS);

    return () => clearInterval(pollRef.current);
  }, [navigate]);

  if (phase === "error") {
    return (
      <div className="slide-up" style={{ maxWidth: 440, margin: "80px auto", padding: "0 32px", textAlign: "center" }}>
        <div style={{ width: 80, height: 80, background: theme.errorBg, border: `2px solid ${theme.error}44`, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 28px" }}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke={theme.error} strokeWidth="2" strokeLinecap="round">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </div>
        <h2 style={{ fontSize: 22, fontWeight: 600, marginBottom: 10 }}>Paiement non confirmé</h2>
        <p style={{ color: theme.textMuted, fontSize: 14, marginBottom: 28 }}>{error}</p>
        <button className="bpx-btn-gold" onClick={() => navigate("/payments/card")}>
          Réessayer
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 440, margin: "80px auto", padding: "0 32px", textAlign: "center" }}>
      <div style={{ position: "relative", width: 64, height: 64, margin: "0 auto 24px" }}>
        <div style={{
          position: "absolute", inset: 0,
          border: `2px solid ${theme.border}`,
          borderTopColor: theme.gold,
          borderRadius: "50%",
          animation: "spin 1s linear infinite",
        }} />
      </div>
      <p style={{ color: theme.textMuted, fontSize: 14 }}>
        Confirmation du paiement en cours…
      </p>
      <p style={{ color: theme.textDim, fontSize: 12, marginTop: 8 }}>
        Ne fermez pas cette page
      </p>
    </div>
  );
}