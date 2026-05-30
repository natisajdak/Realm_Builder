package pl.realmbuilder.engine;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

public class RankCalculator {

    public enum Rank {
        NOVICE   ("⭐ Nowicjusz",     "Ukończyłeś pierwsze 20 tur."),
        MANAGER  ("⭐⭐ Zarządca",     "Wytrwałeś przez 40 tur."),
        RULER    ("⭐⭐⭐ Władca",      "60 tur za Tobą — jesteś prawdziwym przywódcą."),
        KING     ("⭐⭐⭐⭐ Król",       "Ukończyłeś pełne 80 tur!"),
        LEGEND   ("⭐⭐⭐⭐⭐ Legenda",  "80 tur i populacja 500+ — Twoje imię przejdzie do historii!");

        private final String displayName;
        private final String description;

        Rank(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public Rank calculateRank(City city) {
        int turns      = city.getCurrentTurn() - 1;
        int population = city.getResource(ResourceType.POPULATION);

        if (turns >= 80 && population >= 500) return Rank.LEGEND;
        if (turns >= 80)                       return Rank.KING;
        if (turns >= 60)                       return Rank.RULER;
        if (turns >= 40)                       return Rank.MANAGER;
        return Rank.NOVICE;
    }

    public int calculateScore(City city) {
        int turns      = city.getCurrentTurn() - 1;
        int gold       = city.getResource(ResourceType.GOLD);
        int population = city.getResource(ResourceType.POPULATION);
        int morale     = city.getResource(ResourceType.MORALE);
        int buildings  = city.getBuildings().size();

        // formuła punktacji
        return (turns * 10)
                + (gold / 10)
                + (population * 2)
                + (morale * 3)
                + (buildings * 50);
    }

    public String getRankSummary(City city) {
        Rank rank  = calculateRank(city);
        int  score = calculateScore(city);

        return String.format(
                "%s%n%s%nWynik końcowy: %d punktów",
                rank.getDisplayName(),
                rank.getDescription(),
                score
        );
    }
}