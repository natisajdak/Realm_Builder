package pl.realmbuilder.model;

import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.interfaces.Advisor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class City {

    private final String name;
    private final Difficulty difficulty;

    // zasoby miasta — EnumMap dla wydajności
    private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);

    // posiadane budynki i doradcy
    private final List<Building> buildings = new ArrayList<>();
    private final List<Advisor> advisors = new ArrayList<>();
    private final Map<String, String> buildingPositions = new HashMap<>();

    // stan gry
    private int currentTurn = 1;
    private Season currentSeason = Season.SPRING;
    private boolean gameOver = false;
    private boolean playerWon = false;

    // statystyki do osiągnięć
    private int consecutiveRaidssurvived = 0;
    private int turnsWithHighMorale = 0;
    private int maxGoldEverHeld = 0;

    public static final int MAX_TURNS = 80;
    public static final int WIN_POPULATION = 500; // dla rangi Legenda

    public City(String name, Difficulty difficulty) {
        this.name = name;
        this.difficulty = difficulty;
        initializeResources();
    }

    private void initializeResources() {
        // zasoby startowe zależne od trudności
        int startGold = switch (difficulty) {
            case EASY   -> 300;
            case NORMAL -> 200;
            case HARD   -> 100;
        };

        resources.put(ResourceType.GOLD,       startGold);
        resources.put(ResourceType.FOOD,       100);
        resources.put(ResourceType.WOOD,       50);
        resources.put(ResourceType.MORALE,     50);
        resources.put(ResourceType.POPULATION, 50);
    }

    // ======= ZASOBY =======

    public int getResource(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    public void addResource(ResourceType type, int amount) {
        int current = getResource(type);
        int newValue = Math.max(0, current + amount); // zasoby nie schodzą poniżej 0
        resources.put(type, newValue);
        // śledź maksymalne złoto dla osiągnięcia "Bogacz"
        if (type == ResourceType.GOLD && newValue > maxGoldEverHeld) {
            maxGoldEverHeld = newValue;
        }
    }

    public void subtractResource(ResourceType type, int amount) {
        addResource(type, -amount);
    }

    public boolean canAfford(int goldCost) {
        return getResource(ResourceType.GOLD) >= goldCost;
    }

    // ======= BUDYNKI =======

    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public List<Building> getBuildings() {
        return new ArrayList<>(buildings); // kopia — hermetyczność
    }

    public boolean hasBuilding(Class<?> buildingClass) {
        return buildings.stream()
                .anyMatch(b -> b.getClass().equals(buildingClass));
    }

    public void setBuildingPosition(String buildingClassName, String gridCell) {
        if (buildingClassName == null || gridCell == null) return;
        buildingPositions.put(buildingClassName, gridCell);
    }

    public String getBuildingPosition(String buildingClassName) {
        return buildingPositions.get(buildingClassName);
    }

    public Map<String, String> getBuildingPositions() {
        return new HashMap<>(buildingPositions);
    }

    // ======= DORADCY =======

    public void addAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    public List<Advisor> getAdvisors() {
        return new ArrayList<>(advisors);
    }

    // ======= TURA I SEZON =======

    public void nextTurn() {
        currentTurn++;
        // pora roku zmienia się co 4 tury
        if ((currentTurn - 1) % 4 == 0) {
            currentSeason = currentSeason.next();
        }
        checkGameOver();
    }

    private void checkGameOver() {
        if (getResource(ResourceType.GOLD) <= 0
                || getResource(ResourceType.POPULATION) <= 0) {
            gameOver = true;
            playerWon = false;
        }
        if (currentTurn > MAX_TURNS) {
            gameOver = true;
            playerWon = true;
        }
    }

    // ======= STATYSTYKI =======

    public void incrementConsecutiveRaids() {
        consecutiveRaidssurvived++;
    }

    public void resetConsecutiveRaids() {
        consecutiveRaidssurvived = 0;
    }

    public void incrementHighMoraleTurns() {
        if (getResource(ResourceType.MORALE) > 80) {
            turnsWithHighMorale++;
        } else {
            turnsWithHighMorale = 0; // musi być ciągłe
        }
    }

    // ======= GETTERY =======

    public String getName()                  { return name; }
    public Difficulty getDifficulty()        { return difficulty; }
    public int getCurrentTurn()              { return currentTurn; }
    public Season getCurrentSeason()         { return currentSeason; }
    public boolean isGameOver()              { return gameOver; }
    public boolean isPlayerWon()             { return playerWon; }
    public int getConsecutiveRaidsSurvived() { return consecutiveRaidssurvived; }
    public int getTurnsWithHighMorale()      { return turnsWithHighMorale; }
    public int getMaxGoldEverHeld()          { return maxGoldEverHeld; }

    @Override
    public String toString() {
        return String.format(
                "=== %s === Tura: %d/%d | %s | Złoto: %d | Populacja: %d | Morale: %d",
                name, currentTurn, MAX_TURNS,
                currentSeason.getDisplayName(),
                getResource(ResourceType.GOLD),
                getResource(ResourceType.POPULATION),
                getResource(ResourceType.MORALE)
        );
    }

    public void forceGameOver() {
        this.gameOver = true;
        this.playerWon = false;
    }

    // settery potrzebne do wczytania zapisu
    public void setCurrentTurn(int turn) {
        this.currentTurn = turn;
    }

    public void setCurrentSeason(Season season) {
        this.currentSeason = season;
    }

    public void setMaxGoldEverHeld(int value) {
        this.maxGoldEverHeld = value;
    }

    public void setConsecutiveRaidsSurvived(int value) {
        this.consecutiveRaidssurvived = value;
    }

    public void setTurnsWithHighMorale(int value) {
        this.turnsWithHighMorale = value;
    }

}
