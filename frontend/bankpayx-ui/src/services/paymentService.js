import api from "../api/axios";

/**
 * Paiement par carte bancaire (Stripe)
 * @param {{ cardNumber, expiryMonth, expiryYear, cvc, cardHolder, amount, currency, provider }} payload
 */

export const payByCard = async (payload) => {
    const response = await api.post("/api/v1/payments", payload);
    return response.data;
};

export const getPaymentStatus = async (transactionId) => {
    const response = await api.get(`/api/v1/payments/${transactionId}`);
    return response.data;
};
export const createPaypalOrder = async (payload) => {
    const response = await api.post("/api/v1/paypal/create-order", payload);
    return response.data;
};

export const capturePaypalOrder = async (orderId) => {
    const response = await api.post("/api/v1/paypal/capture-order", { orderId });
    return response.data;
};

export const getHistory = async (page = 0, size = 20) => {
    const response = await api.get(`/api/v1/history?page=${page}&size=${size}`);
    return response.data;
};

export const getStripeHistory = async (page = 0, size = 20) => {
    const response = await api.get(`/api/v1/history/stripe?page=${page}&size=${size}`);
    return response.data;
};

export const getPaypalHistory = async (page = 0, size = 20) => {
    const response = await api.get(`/api/v1/history/paypal?page=${page}&size=${size}`);
    return response.data;
};

/**
 * Paiement PayPal
 * @param {{ amount, currency }} payload
 */
// export const payByPaypal = async (payload) => {
//   const response = await api.post("/api/v1/paypal/pay", payload);
//   return response.data;
// };
