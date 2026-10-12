import { createContext, useContext, useEffect, useState } from "react";
import { supabase } from "../services/supabaseClient";
import theme from "../theme";

const AuthContext = createContext(null);

export function useAuth() {
  return useContext(AuthContext);
}

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    // 1. Vérifier la session au chargement initial.
    //    finally garantit que le spinner ne reste jamais bloqué, même en cas d'erreur réseau.
    supabase.auth
      .getSession()
      .then(({ data: { session } }) => {
        if (active) setUser(session?.user ?? null);
      })
      .catch((err) => {
        console.error("[AuthProvider] getSession a échoué :", err);
        if (active) setUser(null);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    // 2. Écouter les changements d'état (login, logout, refresh de token)
    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      if (active) setUser(session?.user ?? null);
    });

    return () => {
      active = false;
      subscription.unsubscribe();
    };
  }, []);

  // Récupère le token JWT à jour pour l'envoyer au backend
  const getAuthToken = async () => {
    const { data: { session } } = await supabase.auth.getSession();
    return session?.access_token || null;
  };

  // AuthProvider est monté au-dessus du Router : pas de useNavigate ici.
  const logout = async () => {
    await supabase.auth.signOut();
    window.location.replace("/login");
  };

  if (loading) {
    return (
      <div style={{ minHeight: "100vh", background: theme.bg, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 24, fontFamily: "Sora, system-ui, sans-serif" }}>
        <div style={{ position: "relative", width: 64, height: 64 }}>
          <div style={{ position: "absolute", inset: 0, border: `2px solid ${theme.border}`, borderTopColor: theme.gold, borderRadius: "50%", animation: "spin 1s linear infinite" }} />
        </div>
        <div style={{ textAlign: "center" }}>
          <p style={{ color: theme.textMuted, fontSize: 14 }}>Chargement de la session...</p>
        </div>
        <style>{`@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ user, loading, getAuthToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// import { createContext, useContext, useEffect, useState } from "react";
// import { useNavigate } from "react-router-dom";
// import { supabase } from "../services/supabaseClient";
// import theme from "../theme";

// const AuthContext = createContext(null);

// export function useAuth() {
//   return useContext(AuthContext);
// }

// export default function AuthProvider({ children }) {
//   const [user, setUser] = useState(null);
//   const [loading, setLoading] = useState(true);
//   const navigate = useNavigate();

//   useEffect(() => {
//     // 1. Vérifier la session au chargement initial
//     supabase.auth.getSession().then(({ data: { session } }) => {
//       setUser(session?.user ?? null);
//       setLoading(false);
//     });

//     // 2. Écouter les changements d'état (login, logout, refresh de token)
//     const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
//       setUser(session?.user ?? null);
//     });

//     return () => subscription.unsubscribe();
//   }, []);

//   // Fonction cruciale : récupère le token JWT à jour pour l'envoyer au backend via Axios
//   const getAuthToken = async () => {
//     const { data: { session } } = await supabase.auth.getSession();
//     return session?.access_token || null;
//   };

//   const logout = async () => {
//     await supabase.auth.signOut();
//     navigate("/login");
//   };

//   if (loading) {
//     return (
//       <div style={{ minHeight: "100vh", background: theme.bg, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 24, fontFamily: "Sora, system-ui, sans-serif" }}>
//         <div style={{ position: "relative", width: 64, height: 64 }}>
//           <div style={{ position: "absolute", inset: 0, border: `2px solid ${theme.border}`, borderTopColor: theme.gold, borderRadius: "50%", animation: "spin 1s linear infinite" }} />
//         </div>
//         <div style={{ textAlign: "center" }}>
//           <p style={{ color: theme.textMuted, fontSize: 14 }}>Chargement de la session...</p>
//         </div>
//         <style>{`@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
//       </div>
//     );
//   }

//   return (
//     <AuthContext.Provider value={{ user, getAuthToken, logout }}>
//       {children}
//     </AuthContext.Provider>
//   );
// }


