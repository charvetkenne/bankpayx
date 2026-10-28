
// import { useNavigate, useLocation } from "react-router-dom";
// import { useAuth } from "../auth/AuthProvider";
// import theme from "../theme";

// /**
//  * Construit l'affichage du profil à partir de l'utilisateur Supabase.
//  */
// function buildProfile(user) {
//   if (!user) return null;
//   const meta = user.user_metadata || {};
//   const email = user.email || "";
//   const firstName = meta.first_name || email.split("@")[0] || "Utilisateur";
//   const lastName = meta.last_name || "";
//   const initials = `${firstName[0] || "U"}${lastName[0] || ""}`.toUpperCase();
//   return { firstName, email, initials };
// }

// export default function Navbar() {
//   const navigate = useNavigate();
//   const location = useLocation();
//   const auth = useAuth();

//   const profile = buildProfile(auth?.user);
//   const logout = auth?.logout;
//   const isHome = location.pathname === "/";

//   return (
//     <nav
//       style={{
//         borderBottom: `1px solid ${theme.border}`,
//         background: theme.surface,
//         padding: "0 32px",
//         height: 60,
//         display: "flex",
//         alignItems: "center",
//         justifyContent: "space-between",
//         position: "sticky",
//         top: 0,
//         zIndex: 10,
//       }}
//     >
//       {/* Logo */}
//       <button
//         onClick={() => navigate("/")}
//         style={{ 
//           background: "none", 
//           border: "none", 
//           cursor: "pointer", 
//           display: "flex", 
//           alignItems: "center", 
//           gap: 10,
//           padding: 0
//         }}
//       >
//         <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
//           <rect width="22" height="22" rx="6" fill={theme.gold} />
//           <path d="M5 11h12M11 5v12" stroke="#0A0800" strokeWidth="2" strokeLinecap="round" />
//         </svg>
//         <span style={{ fontWeight: 600, fontSize: 15, letterSpacing: "-0.02em", color: theme.text }}>
//           BankPay<span style={{ color: theme.gold }}>X</span>
//         </span>
//       </button>

//       {/* Actions droite */}
//       <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
//         {!isHome && (
//           <button
//             className="bpx-btn-ghost"
//             onClick={() => navigate("/")}
//           >
//             ← Accueil
//           </button>
//         )}

//         {profile ? (
//           <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
//             {/* Avatar */}
//             <div
//               style={{
//                 width: 34,
//                 height: 34,
//                 borderRadius: "50%",
//                 background: `${theme.gold}22`,
//                 border: `1px solid ${theme.gold}44`,
//                 display: "flex",
//                 alignItems: "center",
//                 justifyContent: "center",
//                 fontSize: 12,
//                 fontWeight: 600,
//                 color: theme.gold,
//               }}
//             >
//               {profile.initials}
//             </div>

//             {/* Infos utilisateur */}
//             <div style={{ lineHeight: 1.3 }}>
//               <div style={{ fontSize: 13, fontWeight: 500, color: theme.text }}>{profile.firstName}</div>
//               <div style={{ fontSize: 11, color: theme.textMuted }}>{profile.email}</div>
//             </div>

//             {/* Séparateur */}
//             <div style={{ width: 1, height: 24, background: theme.border, margin: "0 4px" }} />

//             <button
//               className="bpx-btn-ghost"
//               onClick={() => navigate("/history")}
//               style={{
//                 color: location.pathname === "/history" ? theme.gold : undefined,
//                 borderColor: location.pathname === "/history" ? theme.gold : undefined,
//               }}
//             >
//               Historique
//             </button>

//             <button
//               className="bpx-btn-ghost"
//               onClick={logout}
//             >
//               Déconnexion
//             </button>
//           </div>
//         ) : (
//           <button
//             className="bpx-btn-gold"
//             onClick={() => navigate("/login")}
//           >
//             Se connecter
//           </button>
//         )}
//       </div>
//     </nav>
//   );
// }






















import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

/**
 * Construit l'affichage du profil à partir de l'utilisateur Supabase.
 * Lecture synchrone : l'utilisateur vient du contexte AuthProvider,
 * jamais de getCurrentUser() (qui est asynchrone).
 */
function buildProfile(user) {
  if (!user) return null;
  const meta = user.user_metadata || {};
  const email = user.email || "";
  const firstName = meta.first_name || email.split("@")[0] || "Utilisateur";
  const lastName = meta.last_name || "";
  const initials = `${firstName[0] || "U"}${lastName[0] || ""}`.toUpperCase();
  return { firstName, email, initials };
}

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const auth = useAuth();

  const profile = buildProfile(auth?.user);
  const logout = auth?.logout;
  const isHome = location.pathname === "/";

  return (
    <nav
      style={{
        borderBottom: `1px solid ${theme.border}`,
        background: theme.surface,
        padding: "0 32px",
        height: 60,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        position: "sticky",
        top: 0,
        zIndex: 10,
      }}
    >
      {/* Logo */}
      <button
        onClick={() => navigate("/")}
        style={{ background: "none", border: "none", cursor: "pointer", display: "flex", alignItems: "center", gap: 10 }}
      >
        <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
          <rect width="22" height="22" rx="6" fill={theme.gold} />
          <path d="M5 11h12M11 5v12" stroke="#0A0800" strokeWidth="2" strokeLinecap="round" />
        </svg>
        <span style={{ fontWeight: 600, fontSize: 15, letterSpacing: "-0.02em", color: theme.text }}>
          BankPay<span style={{ color: theme.gold }}>X</span>
        </span>
      </button>

      {/* Actions droite */}
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        {!isHome && (
          <button
            className="bpx-btn-ghost"
            onClick={() => navigate("/")}
            style={{ padding: "8px 16px", fontSize: 13 }}
          >
            ← Accueil
          </button>
        )}

        {profile ? (
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            {/* Avatar */}
            <div
              style={{
                width: 34,
                height: 34,
                borderRadius: "50%",
                background: `${theme.gold}22`,
                border: `1px solid ${theme.gold}44`,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 12,
                fontWeight: 600,
                color: theme.gold,
              }}
            >
              {profile.initials}
            </div>

            {/* Infos utilisateur */}
            <div style={{ lineHeight: 1.3 }}>
              <div style={{ fontSize: 13, fontWeight: 500, color: theme.text }}>{profile.firstName}</div>
              <div style={{ fontSize: 11, color: theme.textMuted }}>{profile.email}</div>
            </div>

            {/* Séparateur */}
            <div style={{ width: 1, height: 24, background: theme.border, margin: "0 4px" }} />

            <button
              className="bpx-btn-ghost"
              onClick={() => navigate("/history")}
              style={{
                padding: "6px 14px",
                fontSize: 12,
                color: location.pathname === "/history" ? theme.gold : theme.textMuted,
                borderColor: location.pathname === "/history" ? theme.gold : theme.border,
              }}
            >
              Historique
            </button>

            <button
              onClick={logout}
              style={{
                background: "none",
                border: `1px solid ${theme.border}`,
                borderRadius: 6,
                padding: "5px 10px",
                color: theme.textMuted,
                fontSize: 12,
                cursor: "pointer",
                marginLeft: 4,
              }}
            >
              Déconnexion
            </button>
          </div>
        ) : (
          <button
            className="bpx-btn-ghost"
            onClick={() => navigate("/login")}
            style={{ padding: "8px 16px", fontSize: 13 }}
          >
            Se connecter
          </button>
        )}
      </div>
    </nav>
  );
}