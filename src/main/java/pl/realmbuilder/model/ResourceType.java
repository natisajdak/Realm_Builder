package pl.realmbuilder.model;

public enum ResourceType {
    GOLD("Złoto", "💰"),
    FOOD("Żywność", "🌾"),
    WOOD("Drewno", "🪵"),
    MORALE("Morale", "⚔️"),
    POPULATION("Populacja", "👥");

    private final String displayName;
    private final String icon;

    ResourceType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}