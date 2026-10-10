import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

/**
 * PaymentCallbackPage
 *
 * URL : /payment/callback
 *
 * Stripe redirige ici après qu'une méthode de paiement externe
 * (PayPal, Bancontact, iDEAL, Klarna, 3DS redirect…) a été complétée.
 *
 * Paramètres URL injectés automatiquement par Stripe :
 *   ?payment_intent=pi_xxx
 *   &payment_intent_client_secret=pi_xxx_secret_xxx
 *   &redirect_status=succeeded|failed|canceled
 *
 * Cette page lit ces paramètres et navigue vers SuccessPage ou FailedPage.
 */
export default function PaymentCallbackPage() {
  const navigate       = useNavigate();
  const [params]       = useSearchParams();
  const [message, setMessage] = useState("Vérification du paiement…");

  useEffect(() => {
    const paymentIntentId    = params.get("payment_intent");
    const clientSecret       = params.get("payment_intent_client_secret");
    const redirectStatus     = params.get("redirect_status");

    if (!paymentIntentId || !redirectStatus) {
      // Paramètres absents → retour à l'accueil
      navigate("/");
      return;
    }

    switch (redirectStatus) {

      case "succeeded":
        // Paiement complété avec succès après redirection
        setMessage("Paiement confirmé, redirection…");
        navigate("/success", {
          state: {
            transactionId: paymentIntentId,
            type:          "card",
            // amount et currency ne sont pas dans l'URL Stripe
            // → les récupérer depuis l'historique ou via un appel GET /api/v1/payments
          },
          replace: true,
        });
        break;

      case "failed":
        setMessage("Paiement refusé, redirection…");
        navigate("/failed", {
          state: { transactionId: paymentIntentId, reason: "Payment failed" },
          replace: true,
        });
        break;

      case "canceled":
        setMessage("Paiement annulé, redirection…");
        navigate("/", { replace: true });
        break;

      default:
        setMessage("Statut inconnu, retour à l'accueil…");
        navigate("/", { replace: true });
    }
  }, [params, navigate]);

  return (
    <div style={{
      display:        "flex",
      flexDirection:  "column",
      alignItems:     "center",
      justifyContent: "center",
      minHeight:      "60vh",
      gap:            16,
    }}>
      {/* Spinner simple */}
      <div style={{
        width:        40,
        height:       40,
        border:       "3px solid #e5e7eb",
        borderTop:    "3px solid #6366f1",
        borderRadius: "50%",
        animation:    "spin 0.8s linear infinite",
      }} />
      <p style={{ color: "#6b7280", fontSize: 15 }}>{message}</p>

      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
