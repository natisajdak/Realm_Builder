package pl.realmbuilder.engine;

import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GameEngine {

    private City city;
    private final Scanner scanner = new Scanner(System.in);

    // loadery
    private final BuildingLoader    buildingLoader = new BuildingLoader();
    private final EventLoader       eventLoader    = new EventLoader();
    private final AdvisorLoader     advisorLoader  = new AdvisorLoader();

    // procesory
    private final ResourceCalculator resourceCalculator = new ResourceCalculator();
    private final SeasonManager      seasonManager
            = new SeasonManager(resourceCalculator);
    private final EconomyProcessor   economyProcessor   = new EconomyProcessor();
    private final AchievementTracker achievementTracker = new AchievementTracker();
    private final TurnNarrator       narrator           = new TurnNarrator();
    private final SaveManager        saveManager        = new SaveManager();
    private final RankCalculator     rankCalculator     = new RankCalculator();

    private TurnProcessor turnProcessor;

    // dane gry
    private List<Building> availableBuildings;
    private List<Advisor>  availableAdvisors;

    // ======= START =======

    public void start() {
        printWelcome();
        setupGame();
        loadPlugins();
        gameLoop();
        printEndScreen();
    }

    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         REALM BUILDER v0.3           ║");
        System.out.println("║     Zbuduj miasto, przeżyj 80 tur    ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    private void setupGame() {
        // zapytaj o wczytanie zapisu jeśli istnieje
        if (saveManager.saveExists()) {
            System.out.print("Znaleziono zapis gry. Wczytać? (t/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("t")) {
                city = saveManager.load();
                if (city != null) {
                    System.out.println("✅ Wczytano miasto: " + city.getName());
                    return;
                }
            }
        }

        System.out.print("Podaj nazwę swojego miasta: ");
        String cityName = scanner.nextLine().trim();
        if (cityName.isEmpty()) cityName = "Nowe Miasto";

        System.out.println();
        System.out.println("Wybierz poziom trudności:");
        System.out.println("  1. Łatwy   — więcej zasobów, słabsze zdarzenia");
        System.out.println("  2. Normalny");
        System.out.println("  3. Trudny  — mniej zasobów, silniejsze zdarzenia");
        System.out.print("Twój wybór (1-3): ");

        Difficulty difficulty = switch (readInt(1, 3)) {
            case 1  -> Difficulty.EASY;
            case 3  -> Difficulty.HARD;
            default -> Difficulty.NORMAL;
        };

        city = new City(cityName, difficulty);
        System.out.println();
        System.out.println("Witaj, władco " + cityName
                + "! Niech rozpocznie się era prosperity!");
        System.out.println();
        printGoal();
    }

    private void printGoal() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║            CEL GRY                   ║");
        System.out.println("║  Przeżyj 80 tur (20 lat).            ║");
        System.out.println("║  Pilnuj złota i populacji —          ║");
        System.out.println("║  gdy spadną do zera, przegrywasz.    ║");
        System.out.println("║                                      ║");
        System.out.println("║  ZASOBY:                             ║");
        System.out.println("║  💰 Złoto    — buduj i płać podatki  ║");
        System.out.println("║  🌾 Żywność  — karmi populację       ║");
        System.out.println("║  🪵 Drewno   — potrzebne do budowy   ║");
        System.out.println("║  ⚔️  Morale   — szczęście mieszkańców ║");
        System.out.println("║  👥 Populacja — płaci podatki         ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    private void loadPlugins() {
        System.out.println("⚙️  Ładowanie silnika pluginowego...");
        availableBuildings = buildingLoader.loadBuildings();
        availableAdvisors  = advisorLoader.loadAdvisors();
        availableAdvisors.forEach(city::addAdvisor);

        turnProcessor = new TurnProcessor(
                resourceCalculator,
                seasonManager,
                economyProcessor,
                achievementTracker,
                eventLoader,
                narrator
        );

        System.out.println("✅ Załadowano " + availableBuildings.size()
                + " budynków i " + availableAdvisors.size() + " doradców.");
        System.out.println();
    }

    // ======= GŁÓWNA PĘTLA =======

    private void gameLoop() {
        while (!city.isGameOver()) {
            narrator.narrateTurn(city);
            printCityStatus();
            handlePlayerAction();

            // TurnProcessor orkiestruje całą resztę tury
            turnProcessor.processTurn(
                    city,
                    availableBuildings.size(),
                    event -> {
                        printEvent(event);
                        if (event.isNegative()) {
                            offerAdvisorHelp(event);
                        }
                    }
            );
        }
    }

    // ======= STATUS =======

    private void printCityStatus() {
        System.out.println("─".repeat(52));
        System.out.printf("  🏰 %s  |  Tura: %d/80  |  %s%n",
                city.getName(),
                city.getCurrentTurn(),
                city.getCurrentSeason().getDisplayName());
        System.out.println("  "
                + seasonManager.getSeasonDescription(city.getCurrentSeason()));
        System.out.println("─".repeat(52));
        System.out.printf("  💰 Złoto:      %-6d  🌾 Żywność:   %d%n",
                city.getResource(ResourceType.GOLD),
                city.getResource(ResourceType.FOOD));
        System.out.printf("  🪵 Drewno:     %-6d  ⚔️  Morale:    %d%n",
                city.getResource(ResourceType.WOOD),
                city.getResource(ResourceType.MORALE));
        System.out.printf("  👥 Populacja:  %d%n",
                city.getResource(ResourceType.POPULATION));
        System.out.println();

        if (!city.getBuildings().isEmpty()) {
            System.out.println("🏘️  Twoje budynki:");
            city.getBuildings().forEach(b ->
                    System.out.printf("   • %-12s [Poz.%d] — %s%n",
                            b.getName(), b.getLevel(), b.getDescription()));
            System.out.println();
        }

        System.out.println("👥 Rada Miasta:");
        availableAdvisors.forEach(a ->
                System.out.printf("   • %-20s (%s) — koszt: %d złota%n",
                        a.getName(),
                        a.getSpecialty().getDisplayName(),
                        a.getCost()));
        System.out.println();
    }

    // ======= AKCJA GRACZA =======

    private void handlePlayerAction() {
        System.out.println("Co robisz tej tury?");
        System.out.println("  1. Buduj budynek");
        System.out.println("  2. Ulepsz budynek");
        System.out.println("  3. Następna tura");
        System.out.println("  4. Zapisz grę");
        System.out.println("  0. Zakończ grę");
        System.out.print("Twój wybór: ");

        switch (readInt(0, 4)) {
            case 1 -> handleBuild();
            case 2 -> handleUpgrade();
            case 3 -> System.out.println("➡️  Przechodzisz do następnej tury...");
            case 4 -> saveManager.save(city);
            case 0 -> confirmQuit();
        }
        System.out.println();
    }

    private void handleBuild() {
        System.out.println();
        System.out.println("Dostępne budynki:");
        for (int i = 0; i < availableBuildings.size(); i++) {
            Building b = availableBuildings.get(i);
            boolean owned = city.hasBuilding(b.getClass());
            System.out.printf("  %d. %-12s — %d💰  | %s%s%n",
                    i + 1,
                    b.getName(),
                    b.getCost(),
                    b.getDescription(),
                    owned ? "  ✅ POSIADASZ" : "");
        }
        System.out.println("  0. Anuluj");
        System.out.print("Twój wybór: ");

        int choice = readInt(0, availableBuildings.size());
        if (choice == 0) return;

        Building selected = availableBuildings.get(choice - 1);

        if (city.hasBuilding(selected.getClass())) {
            System.out.println("❌ Już posiadasz ten budynek!");
            return;
        }
        if (!city.canAfford(selected.getCost())) {
            System.out.println("❌ Za mało złota! Potrzebujesz "
                    + selected.getCost() + ", masz "
                    + city.getResource(ResourceType.GOLD) + ".");
            return;
        }

        city.subtractResource(ResourceType.GOLD, selected.getCost());
        Building newBuilding = createNewInstance(selected);
        city.addBuilding(newBuilding);
        System.out.println("✅ Zbudowano: " + selected.getName() + "!");
    }

    private void handleUpgrade() {
        List<Building> buildings = city.getBuildings();
        if (buildings.isEmpty()) {
            System.out.println("❌ Brak budynków do ulepszenia.");
            return;
        }

        System.out.println();
        System.out.println("Wybierz budynek (koszt: 80💰 + 30🪵):");
        for (int i = 0; i < buildings.size(); i++) {
            Building b = buildings.get(i);
            System.out.printf("  %d. %-12s [Poz.%d] → %d%n",
                    i + 1, b.getName(), b.getLevel(),
                    b.getProductionAmount() + (b.getProductionAmount()
                            / b.getLevel()));
        }
        System.out.println("  0. Anuluj");
        System.out.print("Twój wybór: ");

        int choice = readInt(0, buildings.size());
        if (choice == 0) return;

        if (!city.canAfford(80)) {
            System.out.println("❌ Za mało złota!");
            return;
        }
        if (city.getResource(ResourceType.WOOD) < 30) {
            System.out.println("❌ Za mało drewna! Potrzebujesz 30🪵.");
            return;
        }

        Building b = buildings.get(choice - 1);
        city.subtractResource(ResourceType.GOLD, 80);
        city.subtractResource(ResourceType.WOOD, 30);
        b.upgrade();
        System.out.println("⬆️  Ulepszono " + b.getName()
                + " do poziomu " + b.getLevel() + "!");
    }

    private void confirmQuit() {
        System.out.print("Na pewno zakończyć? (t/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("t")) {
            city.forceGameOver();
        }
    }

    // ======= ZDARZENIA =======

    private void printEvent(GameEvent event) {
        System.out.println("─".repeat(52));
        System.out.println("⚡ ZDARZENIE: " + event.getName());
        System.out.println("   " + event.getDescription());
        System.out.println("─".repeat(52));
        narrator.narrateEvent(event.getName(), event.isNegative());
        event.apply(city);
        System.out.println();
    }

    private void offerAdvisorHelp(GameEvent event) {
        List<Advisor> helpful = turnProcessor.getHelpfulAdvisors(
                availableAdvisors, event, city);

        if (helpful.isEmpty()) {
            System.out.println("💬 Nikt z Rady nie może pomóc.");
            return;
        }

        System.out.println("👥 Rada Miasta oferuje pomoc:");
        for (int i = 0; i < helpful.size(); i++) {
            Advisor a = helpful.get(i);
            System.out.printf("  %d. %s — %d💰%n",
                    i + 1, a.getName(), a.getCost());
        }
        System.out.println("  0. Odrzuć");
        System.out.print("Twój wybór: ");

        int choice = readInt(0, helpful.size());
        if (choice == 0) return;

        Advisor chosen = helpful.get(choice - 1);
        city.subtractResource(ResourceType.GOLD, chosen.getCost());
        chosen.help(city, event);
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
        System.out.println(rankCalculator.getRankSummary(city));
        System.out.println();
        System.out.println("── Statystyki ──────────────────────────");
        System.out.println("  Miasto:       " + city.getName());
        System.out.println("  Tury:         " + (city.getCurrentTurn() - 1));
        System.out.println("  Złoto:        " + city.getResource(ResourceType.GOLD));
        System.out.println("  Populacja:    "
                + city.getResource(ResourceType.POPULATION));
        System.out.println("  Morale:       " + city.getResource(ResourceType.MORALE));
        System.out.println("  Maks. złoto:  " + city.getMaxGoldEverHeld());
        System.out.println();

        List<?> unlocked = achievementTracker.getUnlockedAchievements();
        if (!unlocked.isEmpty()) {
            System.out.println("── Osiągnięcia ("
                    + achievementTracker.getUnlockedCount() + "/7) ──────────");
            unlocked.forEach(System.out::println);
        } else {
            System.out.println("Brak odblokowanych osiągnięć.");
        }
        System.out.println();
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
                System.out.printf("Nieprawidłowy wybór: ", min, max);
            }
        }
    }

    private Building createNewInstance(Building template) {
        try {
            return (Building) template.getClass()
                    .getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Błąd tworzenia: "
                    + template.getClass().getSimpleName(), e);
        }
    }
}