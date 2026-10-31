import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import theme from "../theme";

// Adresse affichée dans la colonne « Contact ».
// Laisser vide ("") pour masquer cette colonne.
const CONTACT_EMAIL = "";

export default function Footer() {
  const auth = useAuth();
  const user = auth?.user ?? null;
  const year = new Date().getFullYear();

  return (
    <footer className="bpx-footer">
      <div className="bpx-footer-inner">
        {/* Marque + copyright */}
        <div className="bpx-footer-col">
          <div className="bpx-footer-brand">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" aria-hidden="true">
              <rect width="22" height="22" rx="6" fill={theme.gold} />
              <path d="M5 11h12M11 5v12" stroke="#0A0800" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <span>
              BankPay<span style={{ color: theme.gold }}>X</span>
            </span>
          </div>
          <p className="bpx-footer-text">© {year} BankPayX. Tous droits réservés.</p>
        </div>

        {/* Navigation */}
        <nav className="bpx-footer-col" aria-label="Navigation du pied de page">
          <span className="bpx-footer-title">Navigation</span>
          <Link to="/">Accueil</Link>
          {user && <Link to="/history">Historique</Link>}
        </nav>

        {/* Paiements */}
        <div className="bpx-footer-col">
          <span className="bpx-footer-title">Paiements</span>
          <p className="bpx-footer-text">Paiements traités par Stripe et PayPal.</p>
        </div>

        {/* Contact (affiché seulement si une adresse est renseignée) */}
        {CONTACT_EMAIL && (
          <div className="bpx-footer-col">
            <span className="bpx-footer-title">Contact</span>
            <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
          </div>
        )}
      </div>
    </footer>
  );
}