package pl.realmbuilder.engine;

import pl.realmbuilder.model.Achievement;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AchievementTracker {

    private final List<Achievement> achievements = new ArrayList<>();

    // Nasz kanał komunikacyjny z oknem UI
    private Consumer<String> uiLogger;

    public void setLogger(Consumer<String> logger) {
        this.uiLogger = logger;
    }

    private void log(String message) {
        if (uiLogger != null) {
            uiLogger.accept(message);
        } else {
            System.out.println(message); // Awaryjnie do konsoli
        }
    }

    public AchievementTracker() {
        initAchievements();
    }

    private void initAchievements() {
        achievements.add(new Achievement(
                "SURVIVOR",
                "Niezniszczalny",
                "Przeżyj 3 najazdy z rzędu"));
        achievements.add(new Achievement(
                "RICH",
                "Bogacz",
                "Zgromadź 5000 złota jednocześnie"));
        achievements.add(new Achievement(
                "BUILDER",
                "Budowniczy",
                "Zbuduj wszystkie dostępne budynki"));
        achievements.add(new Achievement(
                "GOOD_RULER",
                "Dobry władca",
                "Utrzymaj morale powyżej 80 przez 10 kolejnych tur"));
        achievements.add(new Achievement(
                "POPULOUS",
                "Metropolia",
                "Osiągnij populację 500 mieszkańców"));
        achievements.add(new Achievement(
                "VETERAN",
                "Weteran",
                "Ukończ 40 tur"));
        achievements.add(new Achievement(
                "LEGEND",
                "Legenda",
                "Ukończ 80 tur z populacją 500+"));
    }

    // wywoływana każdą turę — sprawdza wszystkie osiągnięcia
    public void checkAchievements(City city, int totalBuildingTypes) {
        checkAndUnlock("SURVIVOR",
                city.getConsecutiveRaidsSurvived() >= 3, city);
        checkAndUnlock("RICH",
                city.getMaxGoldEverHeld() >= 5000, city);
        checkAndUnlock("BUILDER",
                city.getBuildings().size() >= totalBuildingTypes, city);
        checkAndUnlock("GOOD_RULER",
                city.getTurnsWithHighMorale() >= 10, city);
        checkAndUnlock("POPULOUS",
                city.getResource(ResourceType.POPULATION) >= 500, city);
        checkAndUnlock("VETERAN",
                city.getCurrentTurn() >= 40, city);
        checkAndUnlock("LEGEND",
                city.getCurrentTurn() >= 80
                        && city.getResource(ResourceType.POPULATION) >= 500, city);
    }

    private void checkAndUnlock(String id, boolean condition, City city) {
        achievements.stream()
                .filter(a -> a.getId().equals(id))
                .filter(a -> !a.isUnlocked())
                .filter(a -> condition)
                .findFirst()
                .ifPresent(a -> {
                    a.unlock();
                    // Bezpieczne emoji i wysyłka do okna gry
                    log("🏆 OSIĄGNIĘCIE ODBLOKOWANE: "
                            + a.getName() + " — " + a.getDescription());
                });
    }

    // zwraca odblokowane osiągnięcia — Stream API
    public List<Achievement> getUnlockedAchievements() {
        return achievements.stream()
                .filter(Achievement::isUnlocked)
                .collect(Collectors.toList());
    }

    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements);
    }

    public int getUnlockedCount() {
        return (int) achievements.stream()
                .filter(Achievement::isUnlocked)
                .count();
    }
}