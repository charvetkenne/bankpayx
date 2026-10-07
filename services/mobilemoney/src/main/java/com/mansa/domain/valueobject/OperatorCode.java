package com.mansa.domain.valueobject;


public enum OperatorCode {

    MTN("MTN Mobile Money", "mtn"),
    ORANGE("Orange Money", "orange"),
    WAVE("Wave", "wave"),
    CINETPAY("CinetPay (Fallback)", "cinetpay"),
    FEDAPAY("FedaPay", "fedapay"),
    NOTCHPAY("NotchPay", "notchpay");          // ← ligne ajoutée

    private final String displayName;
    private final String identifier;

    OperatorCode(String displayName, String identifier) {
        this.displayName = displayName;
        this.identifier  = identifier;
    }

    public String getDisplayName() { return displayName; }
    public String getIdentifier()  { return identifier; }
    public boolean isFallback()    { return this == CINETPAY; }

    public static OperatorCode fromIdentifier(String identifier) {
        for (OperatorCode code : values()) {
            if (code.identifier.equalsIgnoreCase(identifier)) return code;
        }
        throw new IllegalArgumentException("Unknown operator identifier: " + identifier);
    }

}