import { useState } from "react";
import {
  CardElement,
  useStripe,
  useElements,
} from "@stripe/react-stripe-js";

import theme from "../theme";

export default function PaymentForm({ onSubmit, onResult, loading }) {
  const stripe = useStripe();
  const elements = useElements();

  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("EUR");
  const [cardHolder, setCardHolder] = useState("");
  const [error, setError] = useState("");
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async () => {
    setError("");

    if (!stripe || !elements) return;

    if (!amount || isNaN(Number(amount))) {
      setError("Montant invalide.");
      return;
    }

    const cardElement = elements.getElement(CardElement);
    if (!cardElement) {
      setError("Champ carte introuvable.");
      return;
    }

    // Étape 1 — tokenisation côté Stripe, jamais de données brutes
    const { paymentMethod, error: stripeError } =
      await stripe.createPaymentMethod({
        type: "card",
        card: cardElement,
        billing_details: { name: cardHolder },
      });

    if (stripeError) {
      setError(stripeError.message);
      return;
    }

    try {
      // Étape 2 — le backend crée et confirme le PaymentIntent
      const data = await onSubmit({
        paymentMethodId: paymentMethod.id,
        amount: Number(amount),
        currency,
        merchantId: "merchant_1",
        description: "Paiement carte",
      });

      // Étape 3 — le backend a-t-il besoin que le frontend prenne le relais ?
      if (data.requiresAction && data.clientSecret) {
        setProcessing(true);

        // Persisté avant tout risque de redirection complète,
        // pour que la page de retour puisse retrouver la transaction
        sessionStorage.setItem("bpx_pending_tx", data.transactionId);

        const { error: actionError } = await stripe.handleNextAction({
          clientSecret: data.clientSecret,
        });

        // Si on arrive ici, soit le 3DS s'est résolu en modal (pas de
        // redirection complète), soit une erreur est survenue.
        // Si une vraie redirection a eu lieu, le navigateur a déjà quitté
        // la page — ce code ne s'exécute jamais dans ce cas.
        setProcessing(false);

        if (actionError) {
          setError(actionError.message);
          return;
        }

        // Le statut final n'est connu de manière fiable que via le webhook
        // Stripe côté backend → on route vers la page qui interroge le statut
        onResult({ transactionId: data.transactionId, pending: true });
        return;
      }

      if (data.redirectUrl) {
        sessionStorage.setItem("bpx_pending_tx", data.transactionId);
        window.location.href = data.redirectUrl;
        return;
      }

      // Aucune action supplémentaire — résultat déjà définitif
      onResult(data);

    } catch (e) {
      setError("Erreur lors du paiement.");
    }
  };

  return (
    <div>
      {/* Prévisualisation carte */}
      <div
        style={{
          background: `linear-gradient(135deg, ${theme.surfaceHigh}, #1a2440)`,
          borderRadius: 10,
          padding: 20,
          marginBottom: 24,
          position: "relative",
          overflow: "hidden",
          border: `1px solid ${theme.borderLight}`,
        }}
      >
        <div
          style={{
            position: "absolute",
            top: -20,
            right: -20,
            width: 100,
            height: 100,
            borderRadius: "50%",
            background: `${theme.gold}12`,
          }}
        />

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            marginBottom: 20,
          }}
        >
          <svg width="36" height="28" viewBox="0 0 36 28" fill="none">
            <rect width="36" height="28" rx="4" fill={`${theme.gold}22`} />
            <rect
              x="4"
              y="10"
              width="14"
              height="8"
              rx="2"
              fill={theme.gold}
              opacity="0.7"
            />
          </svg>

          <span
            style={{
              fontSize: 13,
              fontWeight: 600,
              color: theme.gold,
              fontFamily: "JetBrains Mono, monospace",
            }}
          >
            STRIPE
          </span>
        </div>

        <p
          style={{
            fontFamily: "JetBrains Mono, monospace",
            fontSize: 17,
            letterSpacing: "0.15em",
            color: theme.text,
            marginBottom: 12,
          }}
        >
          •••• •••• •••• ••••
        </p>

        <div style={{ display: "flex", gap: 32, fontSize: 12 }}>
          <div>
            <div
              style={{
                color: theme.textDim,
                marginBottom: 2,
                fontSize: 10,
                letterSpacing: "0.08em",
                textTransform: "uppercase",
              }}
            >
              Titulaire
            </div>

            <div
              style={{
                fontFamily: "JetBrains Mono, monospace",
                color: cardHolder ? theme.text : theme.textDim,
              }}
            >
              {cardHolder.toUpperCase() || "NOM PRÉNOM"}
            </div>
          </div>
        </div>
      </div>

      {/* Champs */}
      <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <div>
          <label className="bpx-label">Carte bancaire</label>

          <div
            style={{
              border: `1px solid ${theme.border}`,
              borderRadius: 8,
              padding: "14px 16px",
              background: theme.surface,
            }}
          >
            <CardElement
              options={{
                hidePostalCode: true,
                disableLink: true,
                style: {
                  base: {
                    color: theme.text,
                    fontSize: "15px",
                    "::placeholder": { color: theme.textDim },
                  },
                  invalid: { color: theme.error },
                },
              }}
            />
          </div>
        </div>

        <div>
          <label className="bpx-label">Titulaire</label>
          <input
            className="bpx-input"
            placeholder="Jean Dupont"
            value={cardHolder}
            onChange={(e) => setCardHolder(e.target.value)}
          />
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 12 }}>
          <div>
            <label className="bpx-label">Montant</label>
            <input
              className="bpx-input"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value.replace(/[^0-9.]/g, ""))}
            />
          </div>

          <div>
            <label className="bpx-label">Devise</label>
            <select
              className="bpx-input"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              style={{ cursor: "pointer" }}
            >
              <option value="EUR">EUR €</option>
              <option value="USD">USD $</option>
              <option value="GBP">GBP £</option>
              <option value="XAF">XAF</option>
            </select>
          </div>
        </div>
      </div>

      {error && (
        <div
          style={{
            background: theme.errorBg,
            border: `1px solid ${theme.error}33`,
            borderRadius: 8,
            padding: "12px 16px",
            marginTop: 14,
            fontSize: 13,
            color: theme.error,
          }}
        >
          {error}
        </div>
      )}

      <button
        className="bpx-btn-gold"
        onClick={handleSubmit}
        disabled={loading || processing || !stripe}
        style={{ width: "100%", fontSize: 15, marginTop: 20 }}
      >
        {processing
          ? "Authentification en cours…"
          : loading
          ? "Traitement en cours…"
          : `Payer ${amount ? `${amount} ${currency}` : ""}`}
      </button>

      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: 8,
          marginTop: 14,
        }}
      >
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke={theme.textDim} strokeWidth="2">
          <rect x="3" y="11" width="18" height="11" rx="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
        <span style={{ fontSize: 11, color: theme.textDim }}>
          Chiffré TLS · Stripe PCI-DSS
        </span>
      </div>
    </div>
  );
}

// // import { useState } from "react";
// // import {
// //   CardElement,
// //   useStripe,
// //   useElements,
// // } from "@stripe/react-stripe-js";

// // import theme from "../theme";

// // export default function PaymentForm({
// //   onSubmit,
// //   onSuccess,
// //   onError,
// //   loading,
// // }) {
// //   const stripe = useStripe();
// //   const elements = useElements();

// //   const [amount, setAmount] = useState("");
// //   const [currency, setCurrency] = useState("EUR");
// //   const [cardHolder, setCardHolder] = useState("");
// //   const [error, setError] = useState("");
// //   const [processing, setProcessing] = useState(false);

// //   const handleSubmit = async () => {
// //     setError("");
// //     setProcessing(true);

// //     try {
// //       if (!stripe || !elements) {
// //         setError("Stripe n'est pas encore chargé.");
// //         return;
// //       }

// //       if (!amount || isNaN(Number(amount))) {
// //         setError("Montant invalide.");
// //         return;
// //       }

// //       const cardElement = elements.getElement(CardElement);

// //       if (!cardElement) {
// //         setError("Champ carte introuvable.");
// //         return;
// //       }

// //       const { paymentMethod, error: stripeError } =
// //         await stripe.createPaymentMethod({
// //           type: "card",
// //           card: cardElement,
// //           billing_details: {
// //             name: cardHolder,
// //           },
// //         });

// //       if (stripeError) {
// //         setError(stripeError.message);
// //         return;
// //       }

// //       const response = await onSubmit({
// //         paymentMethodId: paymentMethod.id,
// //         amount: Number(amount),
// //         currency,
// //         merchantId: "merchant_1",
// //         description: "Paiement carte",
// //       });

// //       // Paiement terminé directement
// //       if (!response?.requiresAction) {
// //         if (onSuccess) {
// //           onSuccess({
// //             transactionId: response?.transactionId,
// //             amount: response?.amount,
// //             currency: response?.currency,
// //             status: response?.status,
// //             type: "card",
// //           });
// //         }
// //         return;
// //       }

// //       // Redirection externe (PayPal, Klarna, etc.)
// //       if (response?.redirectUrl) {
// //         window.location.href = response.redirectUrl;
// //         return;
// //       }

// //       // 3DS / OTP Stripe
// //       if (response?.clientSecret) {
// //         const {
// //           error: actionError,
// //           paymentIntent,
// //         } = await stripe.handleNextAction({
// //           clientSecret: response.clientSecret,
// //         });

// //         if (actionError) {
// //           setError(
// //             actionError.message ||
// //               "Échec de l'authentification."
// //           );

// //           if (onError) {
// //             onError(actionError);
// //           }

// //           return;
// //         }

// //         if (
// //           paymentIntent?.status === "requires_capture" ||
// //           paymentIntent?.status === "succeeded"
// //         ) {
// //           if (onSuccess) {
// //             onSuccess({
// //               transactionId: response.transactionId,
// //               amount: response.amount,
// //               currency: response.currency,
// //               status: paymentIntent.status,
// //               type: "card",
// //             });
// //           }
// //         } else {
// //           setError(
// //             "Authentification échouée ou paiement refusé."
// //           );

// //           if (onError) {
// //             onError({
// //               message: "Authentication failed",
// //             });
// //           }
// //         }
// //       }
// //     } catch (e) {
// //       setError("Erreur lors du paiement.");

// //       if (onError) {
// //         onError(e);
// //       }
// //     } finally {
// //       setProcessing(false);
// //     }
// //   };

// //   return (
// //     <div>
// //       {/* Prévisualisation carte */}
// //       <div
// //         style={{
// //           background: `linear-gradient(135deg, ${theme.surfaceHigh}, #1a2440)`,
// //           borderRadius: 10,
// //           padding: 20,
// //           marginBottom: 24,
// //           position: "relative",
// //           overflow: "hidden",
// //           border: `1px solid ${theme.borderLight}`,
// //         }}
// //       >
// //         <div
// //           style={{
// //             position: "absolute",
// //             top: -20,
// //             right: -20,
// //             width: 100,
// //             height: 100,
// //             borderRadius: "50%",
// //             background: `${theme.gold}12`,
// //           }}
// //         />

// //         <div
// //           style={{
// //             display: "flex",
// //             justifyContent: "space-between",
// //             alignItems: "flex-start",
// //             marginBottom: 20,
// //           }}
// //         >
// //           <svg width="36" height="28" viewBox="0 0 36 28" fill="none">
// //             <rect width="36" height="28" rx="4" fill={`${theme.gold}22`} />
// //             <rect
// //               x="4"
// //               y="10"
// //               width="14"
// //               height="8"
// //               rx="2"
// //               fill={theme.gold}
// //               opacity="0.7"
// //             />
// //           </svg>

// //           <span
// //             style={{
// //               fontSize: 13,
// //               fontWeight: 600,
// //               color: theme.gold,
// //               fontFamily: "JetBrains Mono, monospace",
// //             }}
// //           >
// //             STRIPE
// //           </span>
// //         </div>

// //         <p
// //           style={{
// //             fontFamily: "JetBrains Mono, monospace",
// //             fontSize: 17,
// //             letterSpacing: "0.15em",
// //             color: theme.text,
// //             marginBottom: 12,
// //           }}
// //         >
// //           •••• •••• •••• ••••
// //         </p>

// //         <div
// //           style={{
// //             display: "flex",
// //             gap: 32,
// //             fontSize: 12,
// //           }}
// //         >
// //           <div>
// //             <div
// //               style={{
// //                 color: theme.textDim,
// //                 marginBottom: 2,
// //                 fontSize: 10,
// //                 letterSpacing: "0.08em",
// //                 textTransform: "uppercase",
// //               }}
// //             >
// //               Titulaire
// //             </div>

// //             <div
// //               style={{
// //                 fontFamily: "JetBrains Mono, monospace",
// //                 color: cardHolder
// //                   ? theme.text
// //                   : theme.textDim,
// //               }}
// //             >
// //               {cardHolder.toUpperCase() || "NOM PRÉNOM"}
// //             </div>
// //           </div>
// //         </div>
// //       </div>

// //       {/* Champs */}
// //       <div
// //         style={{
// //           display: "flex",
// //           flexDirection: "column",
// //           gap: 14,
// //         }}
// //       >
// //         <div>
// //           <label className="bpx-label">
// //             Carte bancaire
// //           </label>

// //           <div
// //             style={{
// //               border: `1px solid ${theme.border}`,
// //               borderRadius: 8,
// //               padding: "14px 16px",
// //               background: theme.surface,
// //             }}
// //           >
// //             <CardElement
// //               options={{
// //                 hidePostalCode: true,
// //                 disableLink: true,
// //                 style: {
// //                   base: {
// //                     color: theme.text,
// //                     fontSize: "15px",
// //                     "::placeholder": {
// //                       color: theme.textDim,
// //                     },
// //                   },
// //                   invalid: {
// //                     color: theme.error,
// //                   },
// //                 },
// //               }}
// //             />
// //           </div>
// //         </div>

// //         <div>
// //           <label className="bpx-label">
// //             Titulaire
// //           </label>

// //           <input
// //             className="bpx-input"
// //             placeholder="Jean Dupont"
// //             value={cardHolder}
// //             onChange={(e) =>
// //               setCardHolder(e.target.value)
// //             }
// //           />
// //         </div>

// //         <div
// //           style={{
// //             display: "grid",
// //             gridTemplateColumns: "2fr 1fr",
// //             gap: 12,
// //           }}
// //         >
// //           <div>
// //             <label className="bpx-label">
// //               Montant
// //             </label>

// //             <input
// //               className="bpx-input"
// //               placeholder="0.00"
// //               value={amount}
// //               onChange={(e) =>
// //                 setAmount(
// //                   e.target.value.replace(/[^0-9.]/g, "")
// //                 )
// //               }
// //             />
// //           </div>

// //           <div>
// //             <label className="bpx-label">
// //               Devise
// //             </label>

// //             <select
// //               className="bpx-input"
// //               value={currency}
// //               onChange={(e) =>
// //                 setCurrency(e.target.value)
// //               }
// //               style={{ cursor: "pointer" }}
// //             >
// //               <option value="EUR">EUR €</option>
// //               <option value="USD">USD $</option>
// //               <option value="GBP">GBP £</option>
// //               <option value="XAF">XAF</option>
// //             </select>
// //           </div>
// //         </div>
// //       </div>

// //       {error && (
// //         <div
// //           style={{
// //             background: theme.errorBg,
// //             border: `1px solid ${theme.error}33`,
// //             borderRadius: 8,
// //             padding: "12px 16px",
// //             marginTop: 14,
// //             fontSize: 13,
// //             color: theme.error,
// //           }}
// //         >
// //           {error}
// //         </div>
// //       )}

// //       <button
// //         className="bpx-btn-gold"
// //         onClick={handleSubmit}
// //         disabled={
// //           loading ||
// //           processing ||
// //           !stripe
// //         }
// //         style={{
// //           width: "100%",
// //           fontSize: 15,
// //           marginTop: 20,
// //         }}
// //       >
// //         {loading || processing
// //           ? "Traitement en cours…"
// //           : `Payer ${
// //               amount
// //                 ? `${amount} ${currency}`
// //                 : ""
// //             }`}
// //       </button>

// //       <div
// //         style={{
// //           display: "flex",
// //           alignItems: "center",
// //           justifyContent: "center",
// //           gap: 8,
// //           marginTop: 14,
// //         }}
// //       >
// //         <svg
// //           width="13"
// //           height="13"
// //           viewBox="0 0 24 24"
// //           fill="none"
// //           stroke={theme.textDim}
// //           strokeWidth="2"
// //         >
// //           <rect
// //             x="3"
// //             y="11"
// //             width="18"
// //             height="11"
// //             rx="2"
// //           />
// //           <path d="M7 11V7a5 5 0 0 1 10 0v4" />
// //         </svg>

// //         <span
// //           style={{
// //             fontSize: 11,
// //             color: theme.textDim,
// //           }}
// //         >
// //           Chiffré TLS · Stripe PCI-DSS
// //         </span>
// //       </div>
// //     </div>
// //   );
// // }










// import { useState } from "react";
// import {
//   CardElement,
//   useStripe,
//   useElements,
// } from "@stripe/react-stripe-js";

// import theme from "../theme";

// export default function PaymentForm({ onSubmit, loading }) {
//   const stripe = useStripe();
//   const elements = useElements();

//   const [amount, setAmount] = useState("");
//   const [currency, setCurrency] = useState("EUR");
//   const [cardHolder, setCardHolder] = useState("");
//   const [error, setError] = useState("");

//   const handleSubmit = async () => {
//     setError("");

//     // Stripe pas encore chargé
//     if (!stripe || !elements) {
//       return;
//     }

//     // Validation montant
//     if (!amount || isNaN(Number(amount))) {
//       setError("Montant invalide.");
//       return;
//     }

//     // Récupération du composant carte Stripe
//     const cardElement = elements.getElement(CardElement);

//     if (!cardElement) {
//       setError("Champ carte introuvable.");
//       return;
//     }

//     // Création sécurisée PaymentMethod Stripe
//     const { paymentMethod, error: stripeError } =
//       await stripe.createPaymentMethod({
//         type: "card",
//         card: cardElement,
//         billing_details: {
//           name: cardHolder,
//         },
//       });

//     // Erreur Stripe
//     if (stripeError) {
//       setError(stripeError.message);
//       return;
//     }

//         try {
//         // Envoi uniquement du token Stripe
//         await onSubmit({
//              paymentMethodId: paymentMethod.id,
//              amount: Number(amount),
//              currency,
//              merchantId: "merchant_1",
//              description: "Paiement carte",
//         });
//     } catch (e) {
//       setError("Erreur lors du paiement.");
//     }
//   };

//   return (
//     <div>
//       {/* Prévisualisation carte */}
//       <div
//         style={{
//           background: `linear-gradient(135deg, ${theme.surfaceHigh}, #1a2440)`,
//           borderRadius: 10,
//           padding: 20,
//           marginBottom: 24,
//           position: "relative",
//           overflow: "hidden",
//           border: `1px solid ${theme.borderLight}`,
//         }}
//       >
//         <div
//           style={{
//             position: "absolute",
//             top: -20,
//             right: -20,
//             width: 100,
//             height: 100,
//             borderRadius: "50%",
//             background: `${theme.gold}12`,
//           }}
//         />

//         <div
//           style={{
//             display: "flex",
//             justifyContent: "space-between",
//             alignItems: "flex-start",
//             marginBottom: 20,
//           }}
//         >
//           <svg width="36" height="28" viewBox="0 0 36 28" fill="none">
//             <rect width="36" height="28" rx="4" fill={`${theme.gold}22`} />
//             <rect
//               x="4"
//               y="10"
//               width="14"
//               height="8"
//               rx="2"
//               fill={theme.gold}
//               opacity="0.7"
//             />
//           </svg>

//           <span
//             style={{
//               fontSize: 13,
//               fontWeight: 600,
//               color: theme.gold,
//               fontFamily: "JetBrains Mono, monospace",
//             }}
//           >
//             STRIPE
//           </span>
//         </div>

//         <p
//           style={{
//             fontFamily: "JetBrains Mono, monospace",
//             fontSize: 17,
//             letterSpacing: "0.15em",
//             color: theme.text,
//             marginBottom: 12,
//           }}
//         >
//           •••• •••• •••• ••••
//         </p>

//         <div
//           style={{
//             display: "flex",
//             gap: 32,
//             fontSize: 12,
//           }}
//         >
//           <div>
//             <div
//               style={{
//                 color: theme.textDim,
//                 marginBottom: 2,
//                 fontSize: 10,
//                 letterSpacing: "0.08em",
//                 textTransform: "uppercase",
//               }}
//             >
//               Titulaire
//             </div>

//             <div
//               style={{
//                 fontFamily: "JetBrains Mono, monospace",
//                 color: cardHolder
//                   ? theme.text
//                   : theme.textDim,
//               }}
//             >
//               {cardHolder.toUpperCase() || "NOM PRÉNOM"}
//             </div>
//           </div>
//         </div>
//       </div>

//       {/* Champs */}
//       <div
//         style={{
//           display: "flex",
//           flexDirection: "column",
//           gap: 14,
//         }}
//       >
//         {/* Champ Stripe sécurisé */}
//         <div>
//           <label className="bpx-label">
//             Carte bancaire
//           </label>

//           <div
//             style={{
//               border: `1px solid ${theme.border}`,
//               borderRadius: 8,
//               padding: "14px 16px",
//               background: theme.surface,
//             }}
//           >
//             <CardElement
//               options={{
//                 hidePostalCode: true,
//                 disableLink: true,
//                 style: {
//                   base: {
//                     color: theme.text,
//                     fontSize: "15px",
//                     "::placeholder": {
//                       color: theme.textDim,
//                     },
//                   },
//                   invalid: {
//                     color: theme.error,
//                   },
//                 },
//               }}
//             />
//           </div>
//         </div>

//         {/* Titulaire */}
//         <div>
//           <label className="bpx-label">
//             Titulaire
//           </label>

//           <input
//             className="bpx-input"
//             placeholder="Jean Dupont"
//             value={cardHolder}
//             onChange={(e) =>
//               setCardHolder(e.target.value)
//             }
//           />
//         </div>

//         {/* Montant + devise */}
//         <div
//           style={{
//             display: "grid",
//             gridTemplateColumns: "2fr 1fr",
//             gap: 12,
//           }}
//         >
//           <div>
//             <label className="bpx-label">
//               Montant
//             </label>

//             <input
//               className="bpx-input"
//               placeholder="0.00"
//               value={amount}
//               onChange={(e) =>
//                 setAmount(
//                   e.target.value.replace(/[^0-9.]/g, "")
//                 )
//               }
//             />
//           </div>

//           <div>
//             <label className="bpx-label">
//               Devise
//             </label>

//             <select
//               className="bpx-input"
//               value={currency}
//               onChange={(e) =>
//                 setCurrency(e.target.value)
//               }
//               style={{ cursor: "pointer" }}
//             >
//               <option value="EUR">EUR €</option>
//               <option value="USD">USD $</option>
//               <option value="GBP">GBP £</option>
//               <option value="XAF">XAF</option>
//             </select>
//           </div>
//         </div>
//       </div>

//       {/* Erreurs */}
//       {error && (
//         <div
//           style={{
//             background: theme.errorBg,
//             border: `1px solid ${theme.error}33`,
//             borderRadius: 8,
//             padding: "12px 16px",
//             marginTop: 14,
//             fontSize: 13,
//             color: theme.error,
//           }}
//         >
//           {error}
//         </div>
//       )}

//       {/* Bouton paiement */}
//       <button
//         className="bpx-btn-gold"
//         onClick={handleSubmit}
//         disabled={loading || !stripe}
//         style={{
//           width: "100%",
//           fontSize: 15,
//           marginTop: 20,
//         }}
//       >
//         {loading
//           ? "Traitement en cours…"
//           : `Payer ${
//               amount
//                 ? `${amount} ${currency}`
//                 : ""
//             }`}
//       </button>

//       {/* Footer sécurité */}
//       <div
//         style={{
//           display: "flex",
//           alignItems: "center",
//           justifyContent: "center",
//           gap: 8,
//           marginTop: 14,
//         }}
//       >
//         <svg
//           width="13"
//           height="13"
//           viewBox="0 0 24 24"
//           fill="none"
//           stroke={theme.textDim}
//           strokeWidth="2"
//         >
//           <rect
//             x="3"
//             y="11"
//             width="18"
//             height="11"
//             rx="2"
//           />
//           <path d="M7 11V7a5 5 0 0 1 10 0v4" />
//         </svg>

//         <span
//           style={{
//             fontSize: 11,
//             color: theme.textDim,
//           }}
//         >
//           Chiffré TLS · Stripe PCI-DSS
//         </span>
//       </div>
//     </div>
//   );
// } 