package pl.realmbuilder.model;

public enum Season {
    SPRING("Wiosna"),
    SUMMER("Lato"),
    AUTUMN("Jesień"),
    WINTER("Zima");

    private final String displayName;

    Season(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // zwraca następną porę roku (po Zimie wraca Wiosna)
    public Season next() {
        Season[] values = Season.values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
