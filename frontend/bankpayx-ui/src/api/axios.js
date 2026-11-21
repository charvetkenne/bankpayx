import axios from 'axios';
import { supabase } from '../services/supabaseClient';

const api = axios.create({
  baseURL: import.meta.env.VITE_CARD_SERVICE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(async (config) => {
  // Récupère le token Supabase à la volée
  const { data: { session } } = await supabase.auth.getSession();
  if (session?.access_token) {
    config.headers.Authorization = `Bearer ${session.access_token}`;
  }
  config.headers['x-correlation-id'] = crypto.randomUUID();
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
       console.warn(" Requête rejetée (401). Le token est peut-être invalide ou le backend a un souci de config JWT.");
      console.warn("Détails:", error.response.data);
      // await supabase.auth.signOut();
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;