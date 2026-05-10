package pl.realmbuilder.model;

public enum Difficulty {
    EASY("Łatwy", 1.5, 0.5),
    NORMAL("Normalny", 1.0, 1.0),
    HARD("Trudny", 0.75, 1.5);

    private final String displayName;
    // mnożnik produkcji zasobów (łatwy = więcej zasobów)
    private final double productionMultiplier;
    // mnożnik siły zdarzeń (trudny = silniejsze zdarzenia)
    private final double eventMultiplier;

    Difficulty(String displayName, double productionMultiplier, double eventMultiplier) {
        this.displayName = displayName;
        this.productionMultiplier = productionMultiplier;
        this.eventMultiplier = eventMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getProductionMultiplier() {
        return productionMultiplier;
    }

    public double getEventMultiplier() {
        return eventMultiplier;
    }
}