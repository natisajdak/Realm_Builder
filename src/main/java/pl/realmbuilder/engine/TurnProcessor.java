package pl.realmbuilder.engine;

import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TurnProcessor {

    private final ResourceCalculator resourceCalculator;
    private final SeasonManager      seasonManager;
    private final EconomyProcessor   economyProcessor;
    private final AchievementTracker achievementTracker;
    private final EventLoader        eventLoader;
    private final TurnNarrator       narrator;
    private final Random             random = new Random();

    public TurnProcessor(ResourceCalculator resourceCalculator,
                         SeasonManager seasonManager,
                         EconomyProcessor economyProcessor,
                         AchievementTracker achievementTracker,
                         EventLoader eventLoader,
                         TurnNarrator narrator) {
        this.resourceCalculator = resourceCalculator;
        this.seasonManager      = seasonManager;
        this.economyProcessor   = economyProcessor;
        this.achievementTracker = achievementTracker;
        this.eventLoader        = eventLoader;
        this.narrator           = narrator;
    }

    // orkiestrator jednej tury - wywoływany z GameEngine
    public GameEvent processTurn(City city,
                                 int totalBuildingTypes,
                                 java.util.function.Consumer<GameEvent> onEvent) {
        // krok 1 - efekty sezonu
        seasonManager.applySeasonEffects(city);

        // krok 2 - produkcja przez Stream API z mnożnikiem sezonowym
        produceWithSeasonModifier(city);

        // krok 3 - ekonomia (żywność, podatki, morale)
        economyProcessor.process(city);

        // krok 4 - losowe zdarzenie
        GameEvent event = rollRandomEvent(city);
        if (event != null) {
            onEvent.accept(event);
        }

        // krok 5 - osiągnięcia
        achievementTracker.checkAchievements(city, totalBuildingTypes);

        // krok 6 - następna tura
        city.nextTurn();

        return event;
    }

    private void produceWithSeasonModifier(City city) {
        Season season = city.getCurrentSeason();
        double diffMult = city.getDifficulty().getProductionMultiplier();

        city.getBuildings().forEach(building -> {
            ResourceType type = building.getProducedResource();
            double seasonMult = resourceCalculator.getSeasonMultiplier(type, season);
            int amount = (int)(building.getProductionAmount()
                    * diffMult * seasonMult);
            city.addResource(type, amount);
        });
    }

    public GameEvent rollRandomEvent(City city) {
        if (random.nextDouble() > 0.40) return null;

        List<GameEvent> possible =
                eventLoader.loadEventsForSeason(city.getCurrentSeason());

        return possible.stream()
                .filter(e -> random.nextDouble() < e.getProbability())
                .findFirst()
                .orElse(null);
    }

    public List<Advisor> getHelpfulAdvisors(List<Advisor> all,
                                            GameEvent event,
                                            City city) {
        return all.stream()
                .filter(a -> a.canHelp(event))
                .filter(a -> city.canAfford(a.getCost()))
                .collect(Collectors.toList());
    }

    public AchievementTracker getAchievementTracker() {
        return achievementTracker;
    }
}