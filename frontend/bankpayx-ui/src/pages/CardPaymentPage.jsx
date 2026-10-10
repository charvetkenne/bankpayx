import { loadStripe } from "@stripe/stripe-js";
import { Elements } from "@stripe/react-stripe-js";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PaymentForm from "../components/PaymentForm";
import { payByCard } from "../services/paymentService";
import theme from "../theme";

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLIC_KEY);

export default function CardPaymentPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  // Appelle le backend et retourne la réponse brute —
  // c'est PaymentForm qui décide de la suite selon requiresAction/redirectUrl
  const handleSubmit = async (payload) => {
    setLoading(true);
    try {
      return await payByCard(payload);
    } finally {
      setLoading(false);
    }
  };

  // Appelé uniquement quand le résultat est définitif ou doit être
  // confirmé via la page de polling (webhook = source de vérité)
  const handleResult = (result) => {
    if (result.pending) {
      // 3DS résolu en modal sans quitter la page —
      // on route vers la même page que le retour Stripe externe
      navigate(`/payment/callback?transactionId=${result.transactionId}`);
      return;
    }

    const successStatuses = ["CAPTURED", "AUTHORIZED"];
    if (successStatuses.includes(result.status)) {
      navigate("/payments/success", {
        state: {
          type: "card",
          amount: result.amount,
          currency: result.currency,
          status: result.status,
          transactionId: result.transactionId,
        },
      });
    } else {
      navigate("/payment/callback?transactionId=" + result.transactionId);
    }
  };

  return (
    <div className="slide-up" style={{ maxWidth: 520, margin: "0 auto", padding: "48px 32px" }}>
      <div style={{ marginBottom: 32 }}>
        <p style={{ fontSize: 12, color: theme.textMuted, letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: 8 }}>
          Paiement sécurisé
        </p>
        <h2 style={{ fontSize: 26, fontWeight: 600, letterSpacing: "-0.02em" }}>
          Carte bancaire
        </h2>
      </div>

      <div className="bpx-card">
        <Elements stripe={stripePromise}>
          <PaymentForm onSubmit={handleSubmit} onResult={handleResult} loading={loading} />
        </Elements>
      </div>
    </div>
  );
}


// // import { loadStripe } from "@stripe/stripe-js";
// // import { Elements } from "@stripe/react-stripe-js";
// // import { useState } from "react";
// // import { useNavigate } from "react-router-dom";
// // import PaymentForm from "../components/PaymentForm";
// // import { payByCard } from "../services/paymentService";
// // import theme from "../theme";

// // const stripePromise = loadStripe(
// //   import.meta.env.VITE_STRIPE_PUBLIC_KEY
// // );

// // export default function CardPaymentPage() {

// //   const navigate = useNavigate();
// //   const [loading, setLoading] = useState(false);

// //   /**
// //    * Appelé par PaymentForm
// //    * Retourne la réponse backend au lieu de naviguer
// //    */
// //   const handleSubmit = async (payload) => {

// //     setLoading(true);

// //     try {

// //       const data = await payByCard(payload);

// //       console.log(
// //         "Réponse backend paiement :",
// //         data
// //       );

// //       return data;

// //     } finally {

// //       setLoading(false);

// //     }
// //   };

// //   /**
// //    * Appelé par PaymentForm
// //    * uniquement quand le paiement est réellement terminé
// //    */
// //   const handleSuccess = (result) => {

// //     navigate("/payments/success", {
// //       state: {
// //         type: result.type,
// //         amount: result.amount,
// //         currency: result.currency,
// //         status: result.status,
// //         transactionId: result.transactionId,
// //       },
// //     });

// //   };

// //   /**
// //    * Appelé par PaymentForm
// //    */
// //   const handleError = (error) => {

// //     console.error(
// //       "Erreur paiement :",
// //       error
// //     );

// //   };

// //   return (
// //     <div
// //       className="slide-up"
// //       style={{
// //         maxWidth: 520,
// //         margin: "0 auto",
// //         padding: "48px 32px",
// //       }}
// //     >
// //       <div style={{ marginBottom: 32 }}>
// //         <p
// //           style={{
// //             fontSize: 12,
// //             color: theme.textMuted,
// //             letterSpacing: "0.1em",
// //             textTransform: "uppercase",
// //             marginBottom: 8,
// //           }}
// //         >
// //           Paiement sécurisé
// //         </p>

// //         <h2
// //           style={{
// //             fontSize: 26,
// //             fontWeight: 600,
// //             letterSpacing: "-0.02em",
// //           }}
// //         >
// //           Carte bancaire
// //         </h2>
// //       </div>

// //       <div className="bpx-card">
// //         <Elements stripe={stripePromise}>
// //           <PaymentForm
// //             onSubmit={handleSubmit}
// //             onSuccess={handleSuccess}
// //             onError={handleError}
// //             loading={loading}
// //           />
// //         </Elements>
// //       </div>
// //     </div>
// //   );
// // }

// import {loadStripe} from "@stripe/stripe-js"
// import {Elements} from "@stripe/react-stripe-js";
// import { useState } from "react";
// import { useNavigate } from "react-router-dom";
// import PaymentForm from "../components/PaymentForm";
// import { payByCard } from "../services/paymentService";
// import theme from "../theme";

//  console.log(import.meta.env.VITE_STRIPE_PUBLIC_KEY);
// const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLIC_KEY);
// export default function CardPaymentPage() {
  
//   const navigate = useNavigate();
//   const [loading, setLoading] = useState(false);

//   const handleSubmit = async (payload) => {
//     setLoading(true);
//     try {
//       const data = await payByCard(payload);
//       navigate("/payments/success", {
//         state: {
//           type: "card",
//           amount: payload.amount,
//           currency: payload.currency,
//           status: data.status,
//           transactionId: data.transactionId,
//         },
//       });
//     } catch (e) {
//       // L'erreur est gérée dans PaymentForm via le re-throw
//       throw e;
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div className="slide-up" style={{ maxWidth: 520, margin: "0 auto", padding: "48px 32px" }}>
//       <div style={{ marginBottom: 32 }}>
//         <p style={{ fontSize: 12, color: theme.textMuted, letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: 8 }}>
//           Paiement sécurisé
//         </p>
//         <h2 style={{ fontSize: 26, fontWeight: 600, letterSpacing: "-0.02em" }}>
//           Carte bancaire
//         </h2>
//       </div>

//       <div className="bpx-card">
//         <Elements stripe={stripePromise}>
//           <PaymentForm onSubmit={handleSubmit} loading={loading} />
//         </Elements>
//       </div>
//     </div>
//   );
// }
