import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthProvider";

/**
 * Protège une route — si l'utilisateur n'est pas authentifié via Supabase,
 * il est redirigé vers la page de login.
 */
export default function ProtectedRoute({ children, requiredRole }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ padding: 40, textAlign: "center", color: "white" }}>
        Vérification des droits d'accès...
      </div>
    );
  }

  if (!user) {
    // Redirection vers le login si non connecté
    return <Navigate to="/login" replace />;
  }

  // Optionnel : Vérification de rôle (si tu as configuré 'app_metadata.roles' dans Supabase)
  if (requiredRole) {
    const roles = user.app_metadata?.roles || [];
    if (!roles.includes(requiredRole)) {
      return (
        <div style={{ padding: 40, textAlign: "center", color: "#E05252" }}>
          Accès refusé — rôle requis : <strong>{requiredRole}</strong>
        </div>
      );
    }
  }

  return children;
}