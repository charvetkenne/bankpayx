import { useAuth } from "../auth/AuthProvider";

/**
 * Protège une route — si Keycloak n'est pas authentifié,
 * AuthProvider redirige déjà vers le login.
 * Ce composant peut en plus vérifier un rôle spécifique.
 */
export default function ProtectedRoute({ children, requiredRole }) {
  const keycloak = useAuth();

  if (requiredRole) {
    const roles = keycloak?.tokenParsed?.realm_access?.roles || [];
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
