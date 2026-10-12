import { supabase } from "../services/supabaseClient";

/**
 * Récupère les informations de l'utilisateur connecté
 */
export const getCurrentUser = async () => {
  const { data: { user } } = await supabase.auth.getUser();
  
  if (!user) return null;

  return {
    id: user.id, // C'est l'UUID que le backend attend comme user_id !
    email: user.email,
    username: user.email?.split('@')[0] || "Utilisateur",
    firstName: user.user_metadata?.first_name || "Utilisateur",
    lastName: user.user_metadata?.last_name || "",
    initials: `${(user.user_metadata?.first_name || "U")[0]}${(user.user_metadata?.last_name || "U")[0]}`.toUpperCase(),
    roles: user.app_metadata?.roles || ["user"]
  };
};

/**
 * Déconnecte l'utilisateur
 */
export const logout = async () => {
  await supabase.auth.signOut();
};

/**
 * Récupère le token JWT (utile si tu dois l'injecter manuellement quelque part)
 */
export const getToken = async () => {
  const { data: { session } } = await supabase.auth.getSession();
  return session?.access_token || null;
};