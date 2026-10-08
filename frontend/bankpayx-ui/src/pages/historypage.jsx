import { useState, useEffect, useCallback } from "react";
import { getHistory, getStripeHistory, getPaypalHistory } from "../services/paymentService";
import theme from "../theme";

const TABS = [
    { id: "all",    label: "Tous",   fetcher: getHistory },
    { id: "stripe", label: "Carte",  fetcher: getStripeHistory },
    { id: "paypal", label: "PayPal", fetcher: getPaypalHistory },
];

const STATUS_STYLES = {
    PENDING:          { bg: "#2A2000", color: "#FFB800", label: "En attente" },
    PENDING_APPROVAL: { bg: "#2A2000", color: "#FFB800", label: "En attente" },
    AUTHORIZED:       { bg: "#0A2A1E", color: "#2ECC8A", label: "Validé" },
    APPROVED:         { bg: "#0A2A1E", color: "#2ECC8A", label: "Approuvé"   },
    CAPTURED:         { bg: "#0A2A1E", color: "#2ECC8A", label: "reussi"    },
    FAILED:           { bg: "#2A0E0E", color: "#E05252", label: "Échoué"     },
    REFUNDED:         { bg: "#1A1030", color: "#C9A96E", label: "Remboursé"  },
    CANCELLED:        { bg: "#1A1A1A", color: "#6B7FA3", label: "Annulé"     },
};

const PROVIDER_STYLES = {
    STRIPE: { bg: "#1A1040", color: "#7B68EE", label: "Carte"  },
    PAYPAL: { bg: "#0E1C3A", color: "#4A90D9", label: "PayPal" },
};

function StatusBadge({ status }) {
    const s = STATUS_STYLES[status] || { bg: theme.surface, color: theme.textMuted, label: status };
    return (
        <span style={{
            background: s.bg, color: s.color,
            padding: "3px 10px", borderRadius: 20,
            fontSize: 11, fontWeight: 500,
            border: `1px solid ${s.color}33`,
            whiteSpace: "nowrap",
        }}>
            {s.label}
        </span>
    );
}

function ProviderBadge({ provider }) {
    const p = PROVIDER_STYLES[provider] || { bg: theme.surface, color: theme.textMuted, label: provider };
    return (
        <span style={{
            background: p.bg, color: p.color,
            padding: "2px 8px", borderRadius: 6,
            fontSize: 11, fontWeight: 600,
            letterSpacing: "0.05em",
        }}>
            {p.label}
        </span>
    );
}

function TransactionRow({ tx }) {
    const date = new Date(tx.createdAt).toLocaleString("fr-FR", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit",
    });

    return (
        <div style={{
            display: "grid",
            gridTemplateColumns: "1fr 120px 110px 100px 90px",
            alignItems: "center",
            gap: 12,
            padding: "14px 20px",
            borderBottom: `1px solid ${theme.border}`,
            transition: "background 0.15s",
        }}
            onMouseEnter={(e) => e.currentTarget.style.background = theme.surfaceHigh}
            onMouseLeave={(e) => e.currentTarget.style.background = "transparent"}
        >
            {/* Colonne 1 — ID + date + provider */}
            <div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <ProviderBadge provider={tx.paymentProvider} />
                    {tx.cardType && (
                        <span style={{ fontSize: 11, color: theme.textMuted,
                            background: theme.surfaceHigh, padding: "2px 6px", borderRadius: 4 }}>
                            {tx.cardType}
                        </span>
                    )}
                </div>
                <div style={{ fontFamily: "JetBrains Mono, monospace", fontSize: 12,
                    color: theme.textMuted, marginBottom: 2 }}>
                    {tx.transactionId}
                </div>
                <div style={{ fontSize: 11, color: theme.textDim }}>{date}</div>
            </div>

            {/* Colonne 2 — Carte ou PayPal ref */}
            <div style={{ fontSize: 12, color: theme.textMuted, fontFamily: "JetBrains Mono, monospace" }}>
                {tx.maskedCardNumber || tx.paypalOrderId?.slice(0, 12) || "—"}
            </div>

            {/* Colonne 3 — Montant */}
            <div style={{ textAlign: "right" }}>
                <span style={{ fontSize: 15, fontWeight: 600, color: theme.text }}>
                    {Number(tx.amount).toLocaleString("fr-FR", { minimumFractionDigits: 2 })}
                </span>
                <span style={{ fontSize: 12, color: theme.textMuted, marginLeft: 4 }}>
                    {tx.currency}
                </span>
            </div>

            {/* Colonne 4 — Merchant */}
            <div style={{ fontSize: 12, color: theme.textMuted, textAlign: "center" }}>
                {tx.merchantId}
            </div>

            {/* Colonne 5 — Statut */}
            <div style={{ textAlign: "right" }}>
                <StatusBadge status={tx.status} />
            </div>
        </div>
    );
}

function Pagination({ data, page, onPageChange }) {
    if (!data || data.totalPages <= 1) return null;
    return (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between",
            padding: "16px 20px", borderTop: `1px solid ${theme.border}` }}>
            <span style={{ fontSize: 12, color: theme.textMuted }}>
                {data.totalElements} transaction{data.totalElements > 1 ? "s" : ""} ·
                Page {data.currentPage + 1} / {data.totalPages}
            </span>
            <div style={{ display: "flex", gap: 8 }}>
                <button
                    className="bpx-btn-ghost"
                    disabled={!data.hasPrevious}
                    onClick={() => onPageChange(page - 1)}
                    style={{ padding: "7px 14px", fontSize: 13,
                        opacity: data.hasPrevious ? 1 : 0.3, cursor: data.hasPrevious ? "pointer" : "not-allowed" }}
                >
                    ← Préc.
                </button>
                <button
                    className="bpx-btn-ghost"
                    disabled={!data.hasNext}
                    onClick={() => onPageChange(page + 1)}
                    style={{ padding: "7px 14px", fontSize: 13,
                        opacity: data.hasNext ? 1 : 0.3, cursor: data.hasNext ? "pointer" : "not-allowed" }}
                >
                    Suiv. →
                </button>
            </div>
        </div>
    );
}

export default function HistoryPage() {
    const [activeTab, setActiveTab] = useState("all");
    const [page,      setPage]      = useState(0);
    const [data,      setData]      = useState(null);
    const [loading,   setLoading]   = useState(true);
    const [error,     setError]     = useState("");

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            const tab     = TABS.find((t) => t.id === activeTab);
            const result  = await tab.fetcher(page);
            setData(result);
        } catch {
            setError("Impossible de charger l'historique.");
        } finally {
            setLoading(false);
        }
    }, [activeTab, page]);

    useEffect(() => { fetchData(); }, [fetchData]);

    const switchTab = (id) => {
        setActiveTab(id);
        setPage(0);
        setData(null);
    };

    return (
        <div className="slide-up" style={{ maxWidth: 900, margin: "0 auto", padding: "48px 32px" }}>

            {/* En-tête */}
            <div style={{ display: "flex", alignItems: "flex-end",
                justifyContent: "space-between", marginBottom: 28 }}>
                <div>
                    <p style={{ fontSize: 12, color: theme.textMuted, letterSpacing: "0.1em",
                        textTransform: "uppercase", marginBottom: 6 }}>
                        Paiements
                    </p>
                    <h2 style={{ fontSize: 26, fontWeight: 600, letterSpacing: "-0.02em" }}>
                        Historique
                    </h2>
                </div>
                <button onClick={fetchData}
                    style={{ background: "none", border: `1px solid ${theme.border}`,
                        borderRadius: 8, padding: "8px 14px", color: theme.textMuted,
                        fontSize: 13, cursor: "pointer" }}>
                    ↻ Actualiser
                </button>
            </div>

            {/* Onglets */}
            <div style={{ display: "flex", gap: 4, marginBottom: 20,
                background: theme.surface, borderRadius: 10, padding: 4,
                border: `1px solid ${theme.border}`, width: "fit-content" }}>
                {TABS.map((tab) => (
                    <button
                        key={tab.id}
                        onClick={() => switchTab(tab.id)}
                        style={{
                            padding: "8px 20px", borderRadius: 7, border: "none",
                            fontSize: 13, fontWeight: 500, cursor: "pointer",
                            fontFamily: "Sora, system-ui, sans-serif",
                            background: activeTab === tab.id ? theme.gold : "transparent",
                            color: activeTab === tab.id ? "#0A0800" : theme.textMuted,
                            transition: "all 0.15s",
                        }}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Conteneur principal */}
            <div style={{ background: theme.surface, border: `1px solid ${theme.border}`,
                borderRadius: 14, overflow: "hidden" }}>

                {/* En-tête tableau */}
                <div style={{ display: "grid",
                    gridTemplateColumns: "1fr 120px 110px 100px 90px",
                    gap: 12, padding: "10px 20px",
                    borderBottom: `1px solid ${theme.border}`,
                    background: theme.surfaceHigh }}>
                    {["Transaction", "Référence", "Montant", "Marchand", "Statut"].map((h) => (
                        <div key={h} style={{ fontSize: 11, fontWeight: 500,
                            color: theme.textMuted, letterSpacing: "0.07em",
                            textTransform: "uppercase",
                            textAlign: h === "Montant" || h === "Statut" ? "right" : "left" }}>
                            {h}
                        </div>
                    ))}
                </div>

                {/* États */}
                {loading && (
                    <div style={{ padding: "48px 20px", textAlign: "center" }}>
                        <div style={{ width: 32, height: 32, border: `2px solid ${theme.border}`,
                            borderTopColor: theme.gold, borderRadius: "50%",
                            animation: "spin 1s linear infinite", margin: "0 auto 16px" }} />
                        <p style={{ color: theme.textMuted, fontSize: 13 }}>Chargement…</p>
                    </div>
                )}

                {error && !loading && (
                    <div style={{ padding: "48px 20px", textAlign: "center" }}>
                        <p style={{ color: theme.error, fontSize: 14, marginBottom: 16 }}>{error}</p>
                        <button className="bpx-btn-ghost" onClick={fetchData}>Réessayer</button>
                    </div>
                )}

                {!loading && !error && data?.content?.length === 0 && (
                    <div style={{ padding: "60px 20px", textAlign: "center" }}>
                        <p style={{ color: theme.textMuted, fontSize: 14 }}>
                            Aucune transaction trouvée.
                        </p>
                    </div>
                )}

                {!loading && !error && data?.content?.map((tx) => (
                    <TransactionRow key={tx.transactionId} tx={tx} />
                ))}

                <Pagination data={data} page={page} onPageChange={setPage} />
            </div>
        </div>
    );
}

// AUTHORIZED:       { bg: "#0A1A2A", color: "#4A90D9", label: "Autorisé"   },