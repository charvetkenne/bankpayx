import theme from "../theme";

export const injectFonts = () => {
  if (document.getElementById("bankpayx-fonts")) return;
  const link = document.createElement("link");
  link.id = "bankpayx-fonts";
  link.rel = "stylesheet";
  link.href = "https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap";
  document.head.appendChild(link);
};

const globalStyle = `
  .bpx-root * { box-sizing: border-box; margin: 0; padding: 0; }
  .bpx-root {
    font-family: 'Sora', system-ui, sans-serif;
    background: ${theme.bg};
    min-height: 100vh;
    color: ${theme.text};
  }
  
  /* ... (Garde tous tes styles existants : .bpx-input, .bpx-btn-gold, etc.) ... */

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
  
  /* ... (Garde .bpx-btn-gold, .bpx-btn-ghost, .bpx-card, .bpx-label, animations) ... */

  /* ==========================================================================
     SURCHARGES POUR L'UI D'AUTHENTIFICATION SUPABASE
     ========================================================================== */
  .bpx-root .supabase-auth-ui_ui-container {
    width: 100% !important;
  }
  .bpx-root .supabase-auth-ui_ui-button {
    background: linear-gradient(135deg, ${theme.gold}, ${theme.goldLight}) !important;
    border: none !important;
    border-radius: 8px !important;
    padding: 12px 24px !important;
    font-weight: 600 !important;
    font-size: 14px !important;
    color: #0A0800 !important;
    cursor: pointer !important;
    transition: opacity 0.2s, transform 0.1s !important;
    font-family: 'Sora', system-ui, sans-serif !important;
    width: 100% !important;
    margin-top: 8px !important;
  }
  .bpx-root .supabase-auth-ui_ui-button:hover {
    opacity: 0.9 !important;
    transform: translateY(-1px) !important;
  }
  .bpx-root .supabase-auth-ui_ui-button:disabled {
    opacity: 0.4 !important;
    cursor: not-allowed !important;
    transform: none !important;
  }
  .bpx-root .supabase-auth-ui_ui-input {
    background: ${theme.bg} !important;
    border: 1px solid ${theme.border} !important;
    border-radius: 8px !important;
    padding: 12px 16px !important;
    color: ${theme.text} !important;
    font-size: 14px !important;
    font-family: 'Sora', system-ui, sans-serif !important;
    outline: none !important;
    transition: border-color 0.2s !important;
    width: 100% !important;
  }
  .bpx-root .supabase-auth-ui_ui-input:focus {
    border-color: ${theme.gold} !important;
    box-shadow: 0 0 0 3px ${theme.gold}18 !important;
  }
  .bpx-root .supabase-auth-ui_ui-input::placeholder {
    color: ${theme.textDim} !important;
  }
  .bpx-root .supabase-auth-ui_ui-label {
    color: ${theme.textMuted} !important;
    font-size: 12px !important;
    font-weight: 500 !important;
    letter-spacing: 0.08em !important;
    text-transform: uppercase !important;
    margin-bottom: 8px !important;
    display: block !important;
  }
  .bpx-root .supabase-auth-ui_ui-anchor {
    color: ${theme.gold} !important;
    text-decoration: none !important;
    font-size: 13px !important;
    display: block !important;
    text-align: center !important;
    margin-top: 12px !important;
  }
  .bpx-root .supabase-auth-ui_ui-anchor:hover {
    text-decoration: underline !important;
  }
  .bpx-root .supabase-auth-ui_ui-message {
    color: ${theme.textMuted} !important;
    font-size: 13px !important;
    margin-top: 8px !important;
    text-align: center !important;
  }
  /* Masquer le lien "Mot de passe oublié" en mode Magic Link (inutile) */
  .bpx-root a[href*="forgotten_password"] {
    display: none !important;
  }
`;

export default globalStyle;