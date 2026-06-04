package pl.realmbuilder.engine;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

import java.util.function.Consumer;

public class SeasonManager {

    private final ResourceCalculator calculator;
    private Consumer<String> uiLogger;

    public SeasonManager(ResourceCalculator calculator) {
        this.calculator = calculator;
    }

    public void setLogger(Consumer<String> logger) {
        this.uiLogger = logger;
    }

    private void log(String message) {
        if (uiLogger != null) {
            uiLogger.accept(message);
        } else {
            System.out.println(message);
        }
    }

    // wywoływana na początku każdej tury - informuje o efektach sezonu
    public void applySeasonEffects(City city) {
        Season season = city.getCurrentSeason();

        // co 4 tury = nowy sezon - wyświetl komunikat
        if ((city.getCurrentTurn() - 1) % 4 == 0 && city.getCurrentTurn() > 1) {
            printSeasonChange(season);
            applySeasonBonus(city, season);
        }
    }

    private void applySeasonBonus(City city, Season season) {
        switch (season) {
            case SPRING -> {
                city.addResource(ResourceType.MORALE, 5);
                log("🌸 Wiosna przynosi odnowę. +5 morale dla mieszkańców.");
            }
            case SUMMER -> {
                city.addResource(ResourceType.FOOD, 10);
                log("🌞 Słońce sprzyja uprawom. +10 żywności ze słonecznych pól.");
            }
            case AUTUMN -> {
                city.addResource(ResourceType.GOLD, 15);
                log("🍂 Jesienne targi przynoszą zyski. +15 złota ze sprzedaży plonów.");
            }
            case WINTER -> {
                city.subtractResource(ResourceType.FOOD, 20);
                city.subtractResource(ResourceType.MORALE, 5);
                log("⛄ Mroźna zima uszczupla zapasy. -20 żywności, -5 morale.");
            }
        }
    }

    private void printSeasonChange(Season season) {
        String tip = switch (season) {
            case SPRING -> "Dobry czas na budowę i rozbudowę miasta.";
            case SUMMER -> "Farmy produkują więcej — uzupełnij zapasy.";
            case AUTUMN -> "Przygotuj się na zimę — zgromadź żywność.";
            case WINTER -> "Zima jest bezlitosna. Pilnuj zapasów żywności!";
        };
        log("\n📅 Nadchodzi " + season.getDisplayName().toUpperCase() + "\n   💡 " + tip + "\n");
    }

    // zwraca opis wpływu sezonu na produkcję — do wyświetlenia w UI
    public String getSeasonDescription(Season season) {
        return switch (season) {
            case SPRING -> "🌸 Wiosna: +20% żywności, +10% morale";
            case SUMMER -> "🌞 Lato: +30% żywności, +10% drewna";
            case AUTUMN -> "🍂 Jesień: +10% żywności, +20% złota";
            case WINTER -> "⛄ Zima: -30% żywności, -20% drewna, -20% morale";
        };
    }
}