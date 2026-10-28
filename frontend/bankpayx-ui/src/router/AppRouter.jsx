import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import HomePage from "../pages/HomePage";
import PaymentPage from "../pages/PaymentPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import CardPaymentPage from "../pages/CardPaymentPage";
import LoginPage from "../pages/LoginPage";
import PaypalPaymentPage from "../pages/PaypalPaymentPage";
import SuccessPage from "../pages/SuccessPage";
import PaypalSuccessPage from "../pages/PaypalSuccessPage";
import PaypalCancelPage from "../pages/PaypalCancelPage";
import HistoryPage from "../pages/HistoryPage";
import PaymentCallbackPage from "../pages/PaymentCallbackPage";
import StripeReturnPage from "../pages/StripeReturnPage";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/payments" element={<PaymentPage />} />

        {/* /login est la route utilisée par ProtectedRoute, AuthProvider et l'intercepteur Axios.
            /loging est conservée pour les anciens liens (ex. Navbar) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/loging" element={<LoginPage />} />

        
        {/* Définir / réinitialiser le mot de passe (lien reçu par e-mail) */}
        <Route path="/reset-password" element={<ResetPasswordPage />} />


        <Route
          path="/payments/card"
          element={
            <ProtectedRoute requiredRole="payment_user">
              <CardPaymentPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/payments/paypal"
          element={
            <ProtectedRoute requiredRole="payment_user">
              <PaypalPaymentPage />
            </ProtectedRoute>
          }
        />
        <Route path="/payments/success" element={<SuccessPage />} />
        <Route path="/paypal/success" element={<PaypalSuccessPage />} />
        <Route path="/paypal/cancel" element={<PaypalCancelPage />} />

        {/* ATTENTION : même chemin déclaré deux fois — seule la première est utilisée.
            À trancher : PaymentCallbackPage ou StripeReturnPage ? */}
        <Route path="/payment/callback" element={<PaymentCallbackPage />} />
        <Route path="/payment/callback" element={<StripeReturnPage />} />

        <Route
          path="/history"
          element={
            <ProtectedRoute requiredRole="payment_user">
              <HistoryPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}




// import { BrowserRouter, Routes, Route } from "react-router-dom";
// import Navbar from "../components/Navbar";
// import ProtectedRoute from "../components/ProtectedRoute";
// import HomePage from "../pages/HomePage";
// import PaymentPage from "../pages/PaymentPage";
// import CardPaymentPage from "../pages/CardPaymentPage";
// import LoginPage from "../pages/LogingPage";
// import PaypalPaymentPage from "../pages/PaypalPaymentPage";
// import SuccessPage from "../pages/SuccessPage";
// import PaypalSuccessPage from "../pages/PaypalSuccessPage";
// import PaypalCancelPage  from "../pages/PaypalCancelPage";
// import HistoryPage from "../pages/HistoryPage";
// import PaymentCallbackPage from "../pages/PaymentCallbackPage";
// import StripeReturnPage from "../pages/StripeReturnPage";




// export default function AppRouter() {
//   return (
//     <BrowserRouter>
//       <Navbar />
//       <Routes>
//         <Route path="/" element={<HomePage />} />
//         <Route path="/payments" element={<PaymentPage />} />
//         <Route path="/loging" element={<LoginPage/>} />
//         <Route
//           path="/payments/card"
//           element={
//             <ProtectedRoute requiredRole="payment_user">
//               <CardPaymentPage />
//             </ProtectedRoute>
//           }
//         />
//         <Route
//           path="/payments/paypal"
//           element={
//             <ProtectedRoute requiredRole="payment_user">
//               <PaypalPaymentPage />
//             </ProtectedRoute>
//           }
//         />
//         <Route path="/payments/success" element={<SuccessPage />} />
//         <Route path="/paypal/success" element={<PaypalSuccessPage />} />
//         <Route path="/paypal/cancel"  element={<PaypalCancelPage />} />
       
       
//         <Route path="/payment/callback" element={<PaymentCallbackPage />} />
//         <Route path="/payment/callback" element={<StripeReturnPage />} />
//         <Route
//             path="/history"
//             element={
//                 <ProtectedRoute requiredRole="payment_user">
//                     <HistoryPage />
//                 </ProtectedRoute>
//             }
//         />
//       </Routes>
//     </BrowserRouter>
//   );
// }
