import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import HomePage from "../pages/HomePage";
import PaymentPage from "../pages/PaymentPage";
import CardPaymentPage from "../pages/CardPaymentPage";
import PaypalPaymentPage from "../pages/PaypalPaymentPage";
import SuccessPage from "../pages/SuccessPage";
import PaypalSuccessPage from "../pages/PaypalSuccessPage";
import PaypalCancelPage  from "../pages/PaypalCancelPage";
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
        <Route path="/paypal/cancel"  element={<PaypalCancelPage />} />
       
       
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
