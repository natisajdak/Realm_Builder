package pl.realmbuilder.engine;

import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.buildings.Farm;
import pl.realmbuilder.buildings.Sawmill;
import pl.realmbuilder.buildings.Houses;

import java.util.List;
import java.util.Scanner;

public class GameEngine {

    private City city;
    private final Scanner scanner = new Scanner(System.in);

    // dostępne budynki do kupienia — w Tygodniu 2 zastąpi to BuildingLoader
    private final List<Building> availableBuildings = List.of(
            new Farm(),
            new Sawmill(),
            new Houses()
    );

    // ======= START GRY =======

    public void start() {
        printWelcome();
        setupGame();
        gameLoop();
        printEndScreen();
    }

    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         REALM BUILDER v0.1           ║");
        System.out.println("║     Zbuduj miasto, przeżyj 80 tur    ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    private void setupGame() {
        System.out.print("Podaj nazwę swojego miasta: ");
        String cityName = scanner.nextLine().trim();
        if (cityName.isEmpty()) cityName = "Nowe Miasto";

        System.out.println();
        System.out.println("Wybierz poziom trudności:");
        System.out.println("  1. Łatwy   (więcej zasobów, słabsze zdarzenia)");
        System.out.println("  2. Normalny");
        System.out.println("  3. Trudny  (mniej zasobów, silniejsze zdarzenia)");
        System.out.print("Twój wybór (1-3): ");

        Difficulty difficulty = switch (readInt(1, 3)) {
            case 1  -> Difficulty.EASY;
            case 3  -> Difficulty.HARD;
            default -> Difficulty.NORMAL;
        };

        city = new City(cityName, difficulty);
        System.out.println();
        System.out.println("Witaj, władco " + cityName + "! Niech rozpocznie się era prosperity!");
        System.out.println();
    }

    // ======= GŁÓWNA PĘTLA =======

    private void gameLoop() {
        while (!city.isGameOver()) {
            printCityStatus();
            handlePlayerAction();
            produceResources();
            city.nextTurn();
        }
    }

    // ======= KROK 1: STATUS MIASTA =======

    private void printCityStatus() {
        System.out.println("─".repeat(50));
        System.out.println(city);
        System.out.println("─".repeat(50));
        System.out.printf("  💰 Złoto:      %d%n",
                city.getResource(ResourceType.GOLD));
        System.out.printf("  🌾 Żywność:    %d%n",
                city.getResource(ResourceType.FOOD));
        System.out.printf("  🪵 Drewno:     %d%n",
                city.getResource(ResourceType.WOOD));
        System.out.printf("  ⚔️  Morale:     %d%n",
                city.getResource(ResourceType.MORALE));
        System.out.printf("  👥 Populacja:  %d%n",
                city.getResource(ResourceType.POPULATION));
        System.out.println();

        if (!city.getBuildings().isEmpty()) {
            System.out.println("🏘️  Twoje budynki:");
            city.getBuildings().forEach(b ->
                    System.out.println("   • " + b.getName()
                            + " (poziom " + b.getLevel() + ") — "
                            + b.getDescription())
            );
            System.out.println();
        }
    }

    // ======= KROK 2: AKCJA GRACZA =======

    private void handlePlayerAction() {
        System.out.println("Co robisz tej tury?");
        System.out.println("  1. Buduj budynek");
        System.out.println("  2. Następna tura");
        System.out.println("  0. Zakończ grę");
        System.out.print("Twój wybór: ");

        switch (readInt(0, 2)) {
            case 1  -> handleBuild();
            case 2  -> System.out.println("➡️  Przechodzisz do następnej tury...");
            case 0  -> confirmQuit();
        }
        System.out.println();
    }

    private void handleBuild() {
        System.out.println();
        System.out.println("Dostępne budynki:");
        for (int i = 0; i < availableBuildings.size(); i++) {
            Building b = availableBuildings.get(i);
            System.out.printf("  %d. %-12s — koszt: %d złota | %s%n",
                    i + 1,
                    b.getName(),
                    b.getCost(),
                    b.getDescription());
        }
        System.out.println("  0. Anuluj");
        System.out.print("Twój wybór: ");

        int choice = readInt(0, availableBuildings.size());
        if (choice == 0) return;

        Building selected = availableBuildings.get(choice - 1);

        if (!city.canAfford(selected.getCost())) {
            System.out.println("❌ Nie masz wystarczająco złota! (potrzebujesz "
                    + selected.getCost() + ", masz "
                    + city.getResource(ResourceType.GOLD) + ")");
            return;
        }

        city.subtractResource(ResourceType.GOLD, selected.getCost());

        // tworzymy nową instancję budynku (nie tę samą co w liście)
        Building newBuilding = createNewInstance(selected);
        city.addBuilding(newBuilding);

        System.out.println("✅ Zbudowano: " + selected.getName()
                + "! Zapłacono " + selected.getCost() + " złota.");
    }

    private void confirmQuit() {
        System.out.print("Czy na pewno chcesz zakończyć grę? (t/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("t")) {
            city.forceGameOver();
        }
    }

    // ======= KROK 3: PRODUKCJA ZASOBÓW =======

    private void produceResources() {
        if (city.getBuildings().isEmpty()) return;

        System.out.println("📦 Produkcja zasobów:");
        city.getBuildings().forEach(building -> {
            int before = city.getResource(building.getProducedResource());
            building.produce(city);
            int after = city.getResource(building.getProducedResource());
            System.out.printf("   %s produkuje +%d %s%n",
                    building.getName(),
                    after - before,
                    building.getProducedResource().getDisplayName());
        });
        System.out.println();
    }

    // ======= EKRAN KOŃCOWY =======

    private void printEndScreen() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        if (city.isPlayerWon()) {
            System.out.println("║          🏆 WYGRANA! 🏆               ║");
        } else {
            System.out.println("║          💀 PRZEGRANA 💀              ║");
        }
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
        System.out.println("Statystyki końcowe miasta: " + city.getName());
        System.out.println("  Przeżyte tury:   " + (city.getCurrentTurn() - 1));
        System.out.println("  Złoto:           " + city.getResource(ResourceType.GOLD));
        System.out.println("  Populacja:       " + city.getResource(ResourceType.POPULATION));
        System.out.println("  Morale:          " + city.getResource(ResourceType.MORALE));
        System.out.println();
        System.out.println("Dziękuję za grę!👋");
    }

    // ======= HELPERS =======

    private int readInt(int min, int max) {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.printf("Podaj liczbę od %d do %d: ", min, max);
                    continue;
                }
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
                System.out.printf("Podaj liczbę od %d do %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Nieprawidłowy wybór. Podaj liczbę od %d do %d: ", min, max);
            }
        }
    }

    // tworzy nową instancję budynku tego samego typu
    private Building createNewInstance(Building template) {
        try {
            return template.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Nie można utworzyć instancji: "
                    + template.getClass().getSimpleName(), e);
        }
    }
}