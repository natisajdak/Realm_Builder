package pl.realmbuilder.model;

public enum Specialty {
    COMBAT("Walka"),
    MAGIC("Magia"),
    TRADE("Handel"),
    MEDICINE("Medycyna");

    private final String displayName;

    Specialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}