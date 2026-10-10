import { useNavigate, useLocation } from "react-router-dom";
import { getCurrentUser, logout } from "../services/authService";
import theme from "../theme";

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = getCurrentUser();
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

        {/* Avatar + infos user */}
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{
            width: 34, height: 34, borderRadius: "50%",
            background: `${theme.gold}22`, border: `1px solid ${theme.gold}44`,
            display: "flex", alignItems: "center", justifyContent: "center",
            fontSize: 12, fontWeight: 600, color: theme.gold,
          }}>
                    {/* // dans la section des actions, avant le bloc user */}
        {/* {!isHome && (
            <button className="bpx-btn-ghost" onClick={() => navigate("/")}
                style={{ padding: "8px 16px", fontSize: 13 }}>
                ← Accueil
            </button>
        )} */}

        {/* <button
            className="bpx-btn-ghost"
            onClick={() => navigate("/history")}
            style={{ padding: "8px 16px", fontSize: 13,
                color: location.pathname === "/history" ? theme.gold : theme.textMuted,
                borderColor: location.pathname === "/history" ? theme.gold : theme.border }}
        >
            Historique
        </button> */}
        {/* separateur */}
        <div style={{with: 1, height: 24, background: theme.border, margin:"4px"}}> </div>
            {user.initials}
          </div>
          <div style={{ lineHeight: 1.3 }}>
            <div style={{ fontSize: 13, fontWeight: 500 }}>{user.firstName}</div>
            <div style={{ fontSize: 11, color: theme.textMuted }}>{user.email}</div>
          </div>
        

          <button
              className="bpx-btn-ghost"
              onClick={() => navigate("/history")}
              style={{ padding: "6px 14px", fontSize: 12,
                  color: location.pathname === "/history" ? theme.gold : theme.textMuted,
                  borderColor: location.pathname === "/history" ? theme.gold : theme.border }}
          >
              Historique
          </button>
          <button
            onClick={logout}
            style={{
              background: "none", border: `1px solid ${theme.border}`,
              borderRadius: 6, padding: "5px 10px",
              color: theme.textMuted, fontSize: 12, cursor: "pointer", marginLeft: 4,
            }}
          >
            Déconnexion
          </button>
        </div>
      </div>
    </nav>
  );
}
