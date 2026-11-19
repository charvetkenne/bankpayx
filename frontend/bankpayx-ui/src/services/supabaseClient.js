// src/lib/supabaseClient.js
import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

if (!supabaseUrl || !supabaseAnonKey) {
  console.error("❌ ERREUR CRITIQUE : Variables Supabase manquantes !");
  console.error("URL:", supabaseUrl);
  console.error("Key:", supabaseAnonKey);  
  throw new Error('Missing Supabase environment variables. Check your .env file and Vercel settings.');
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    // Garde la session en vie même si on ferme l'onglet
    persistSession: true, 
    // Rafraîchit le token automatiquement avant qu'il n'expire
    autoRefreshToken: true, 
    // Stocke la session dans le localStorage (plus fiable que sessionStorage pour les SPA)
    storage: localStorage, 
    storageKey: 'bankpayx-auth-token', // Clé unique pour éviter les conflits
  },
});