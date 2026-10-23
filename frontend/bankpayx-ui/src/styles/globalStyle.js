import theme from "../theme";

export const injectFonts = () => {
  if (document.getElementById("bankpayx-fonts")) return;
  const link = document.createElement("link");
  link.id = "bankpayx-fonts";
  link.rel = "stylesheet";
  link.href = "https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap";
  document.head.appendChild(link);
};

/**
 * NOTE : fichier reconstruit d'après l'usage des classes dans le code
 * (bpx-input, bpx-label, bpx-btn-gold, bpx-btn-ghost, bpx-card, slide-up, pulse).
 * Si l'historique git contient les styles d'origine, comparez-les avec ceux-ci.
 */
const globalStyle = `
  .bpx-root * { box-sizing: border-box; margin: 0; padding: 0; }
  .bpx-root {
    font-family: 'Sora', system-ui, sans-serif;
    background: ${theme.bg};
    min-height: 100vh;
    color: ${theme.text};
  }

  /* Les boutons et champs n'héritent pas de la police par défaut du navigateur */
  .bpx-root button,
  .bpx-root input,
  .bpx-root select,
  .bpx-root textarea {
    font-family: inherit;
  }

  /* ---------- Champs ---------- */
  .bpx-input {
    width: 100%;
    background: ${theme.bg};
    border: 1px solid ${theme.border};
    border-radius: 8px;
    padding: 12px 16px;
    color: ${theme.text};
    font-size: 14px;
    font-family: 'Sora', system-ui, sans-serif;
    outline: none;
    transition: border-color 0.2s;
  }
  .bpx-input:focus {
    border-color: ${theme.gold};
    box-shadow: 0 0 0 3px ${theme.gold}18;
  }
  .bpx-input::placeholder { color: ${theme.textDim}; }

  /* Remplissage automatique du navigateur : garde le thème sombre */
  .bpx-input:-webkit-autofill,
  .bpx-input:-webkit-autofill:hover,
  .bpx-input:-webkit-autofill:focus {
    -webkit-text-fill-color: ${theme.text};
    -webkit-box-shadow: 0 0 0 1000px ${theme.bg} inset;
    caret-color: ${theme.text};
    transition: background-color 9999s ease-in-out 0s;
  }

  /* ---------- Libellés ---------- */
  .bpx-label {
    display: block;
    color: ${theme.textMuted};
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    margin-bottom: 8px;
  }

  /* ---------- Boutons ---------- */
  .bpx-btn-gold {
    background: linear-gradient(135deg, ${theme.gold}, ${theme.goldLight});
    border: none;
    border-radius: 8px;
    padding: 12px 24px;
    font-weight: 600;
    font-size: 14px;
    color: #0A0800;
    cursor: pointer;
    transition: opacity 0.2s, transform 0.1s;
  }
  .bpx-btn-gold:hover:not(:disabled) {
    opacity: 0.9;
    transform: translateY(-1px);
  }
  .bpx-btn-gold:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .bpx-btn-ghost {
    background: transparent;
    border: 1px solid ${theme.border};
    border-radius: 8px;
    padding: 10px 20px;
    font-weight: 500;
    font-size: 14px;
    color: ${theme.textMuted};
    cursor: pointer;
    transition: border-color 0.2s, color 0.2s;
  }
  .bpx-btn-ghost:hover:not(:disabled) {
    border-color: ${theme.gold};
    color: ${theme.gold};
  }
  .bpx-btn-ghost:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  /* Lien discret sous les formulaires */
  .bpx-link {
    background: none;
    border: none;
    padding: 0;
    color: ${theme.gold};
    font-size: 13px;
    cursor: pointer;
    text-decoration: none;
  }
  .bpx-link:hover { text-decoration: underline; }

  /* ---------- Carte ---------- */
  .bpx-card {
    background: ${theme.surface};
    border: 1px solid ${theme.border};
    border-radius: 14px;
    padding: 28px;
  }

  /* ---------- Animations ---------- */
  @keyframes bpx-slide-up {
    from { opacity: 0; transform: translateY(16px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  .slide-up { animation: bpx-slide-up 0.4s ease both; }

  @keyframes bpx-pulse {
    0%, 100% { opacity: 1; }
    50%      { opacity: 0.4; }
  }
  .pulse { animation: bpx-pulse 2s ease-in-out infinite; }
`;

export default globalStyle;


// import theme from "../theme";

// export const injectFonts = () => {
//   if (document.getElementById("bankpayx-fonts")) return;
//   const link = document.createElement("link");
//   link.id = "bankpayx-fonts";
//   link.rel = "stylesheet";
//   link.href = "https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap";
//   document.head.appendChild(link);
// };

// const globalStyle = `
//   .bpx-root * { box-sizing: border-box; margin: 0; padding: 0; }
//   .bpx-root {
//     font-family: 'Sora', system-ui, sans-serif;
//     background: ${theme.bg};
//     min-height: 100vh;
//     color: ${theme.text};
//   }
  
//   /* ... (Garde tous tes styles existants : .bpx-input, .bpx-btn-gold, etc.) ... */

//   .bpx-input {
//     width: 100%;
//     background: ${theme.bg};
//     border: 1px solid ${theme.border};
//     border-radius: 8px;
//     padding: 12px 16px;
//     color: ${theme.text};
//     font-size: 14px;
//     font-family: 'Sora', system-ui, sans-serif;
//     outline: none;
//     transition: border-color 0.2s;
//   }
//   .bpx-input:focus {
//     border-color: ${theme.gold};
//     box-shadow: 0 0 0 3px ${theme.gold}18;
//   }
//   .bpx-input::placeholder { color: ${theme.textDim}; }
  
//   /* ... (Garde .bpx-btn-gold, .bpx-btn-ghost, .bpx-card, .bpx-label, animations) ... */

//   /* ==========================================================================
//      SURCHARGES POUR L'UI D'AUTHENTIFICATION SUPABASE
//      ========================================================================== */
//   .bpx-root .supabase-auth-ui_ui-container {
//     width: 100% !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-button {
//     background: linear-gradient(135deg, ${theme.gold}, ${theme.goldLight}) !important;
//     border: none !important;
//     border-radius: 8px !important;
//     padding: 12px 24px !important;
//     font-weight: 600 !important;
//     font-size: 14px !important;
//     color: #0A0800 !important;
//     cursor: pointer !important;
//     transition: opacity 0.2s, transform 0.1s !important;
//     font-family: 'Sora', system-ui, sans-serif !important;
//     width: 100% !important;
//     margin-top: 8px !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-button:hover {
//     opacity: 0.9 !important;
//     transform: translateY(-1px) !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-button:disabled {
//     opacity: 0.4 !important;
//     cursor: not-allowed !important;
//     transform: none !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-input {
//     background: ${theme.bg} !important;
//     border: 1px solid ${theme.border} !important;
//     border-radius: 8px !important;
//     padding: 12px 16px !important;
//     color: ${theme.text} !important;
//     font-size: 14px !important;
//     font-family: 'Sora', system-ui, sans-serif !important;
//     outline: none !important;
//     transition: border-color 0.2s !important;
//     width: 100% !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-input:focus {
//     border-color: ${theme.gold} !important;
//     box-shadow: 0 0 0 3px ${theme.gold}18 !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-input::placeholder {
//     color: ${theme.textDim} !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-label {
//     color: ${theme.textMuted} !important;
//     font-size: 12px !important;
//     font-weight: 500 !important;
//     letter-spacing: 0.08em !important;
//     text-transform: uppercase !important;
//     margin-bottom: 8px !important;
//     display: block !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-anchor {
//     color: ${theme.gold} !important;
//     text-decoration: none !important;
//     font-size: 13px !important;
//     display: block !important;
//     text-align: center !important;
//     margin-top: 12px !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-anchor:hover {
//     text-decoration: underline !important;
//   }
//   .bpx-root .supabase-auth-ui_ui-message {
//     color: ${theme.textMuted} !important;
//     font-size: 13px !important;
//     margin-top: 8px !important;
//     text-align: center !important;
//   }
//   /* Masquer le lien "Mot de passe oublié" en mode Magic Link (inutile) */
//   .bpx-root a[href*="forgotten_password"] {
//     display: none !important;
//     color: ${theme.gold} !important;
//     text-align: center !important;
//     margin-top: 16px !important;
//     font-size: 13px !important;
//     text-decoration: underline !important;
//   }
// `;

// export default globalStyle;