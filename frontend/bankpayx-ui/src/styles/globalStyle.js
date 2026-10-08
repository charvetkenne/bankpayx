import theme from "../theme";

export const injectFonts = () => {
  if (document.getElementById("bankpayx-fonts")) return;
  const link = document.createElement("link");
  link.id = "bankpayx-fonts";
  link.rel = "stylesheet";
  link.href =
    "https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap";
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
  .bpx-btn-gold {
    background: linear-gradient(135deg, ${theme.gold}, ${theme.goldLight});
    color: #0A0800;
    border: none;
    border-radius: 8px;
    padding: 13px 28px;
    font-size: 14px;
    font-weight: 600;
    font-family: 'Sora', system-ui, sans-serif;
    cursor: pointer;
    transition: opacity 0.2s, transform 0.1s;
    letter-spacing: 0.02em;
  }
  .bpx-btn-gold:hover { opacity: 0.9; transform: translateY(-1px); }
  .bpx-btn-gold:active { transform: translateY(0); }
  .bpx-btn-gold:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
  .bpx-btn-ghost {
    background: transparent;
    border: 1px solid ${theme.border};
    border-radius: 8px;
    padding: 11px 24px;
    font-size: 14px;
    color: ${theme.textMuted};
    cursor: pointer;
    font-family: 'Sora', system-ui, sans-serif;
    transition: all 0.2s;
  }
  .bpx-btn-ghost:hover { border-color: ${theme.borderLight}; color: ${theme.text}; }
  .bpx-card {
    background: ${theme.surface};
    border: 1px solid ${theme.border};
    border-radius: 14px;
    padding: 28px;
  }
  .bpx-label {
    font-size: 12px;
    font-weight: 500;
    color: ${theme.textMuted};
    letter-spacing: 0.08em;
    text-transform: uppercase;
    margin-bottom: 8px;
    display: block;
  }
  .fade-in { animation: fadeIn 0.35s ease forwards; }
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  .slide-up { animation: slideUp 0.4s cubic-bezier(0.22, 1, 0.36, 1) forwards; }
  @keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  .pulse { animation: pulse 2s infinite; }
  @keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
  .spin { animation: spin 1s linear infinite; }
  @keyframes spin {
    from { transform: rotate(0deg); }
    to   { transform: rotate(360deg); }
  }
`;

export default globalStyle;
