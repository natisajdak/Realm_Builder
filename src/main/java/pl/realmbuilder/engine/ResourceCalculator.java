package pl.realmbuilder.engine;

import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceCalculator {

    // oblicza łączną produkcję danego zasobu przez wszystkie budynki
    public int calculateTotalProduction(List<Building> buildings,
                                        ResourceType type,
                                        double difficultyMultiplier) {
        return buildings.stream()
                .filter(b -> b.getProducedResource() == type)
                .mapToInt(b -> (int)(b.getProductionAmount() * difficultyMultiplier))
                .sum();
    }

    // zwraca mapę: zasób → łączna produkcja
    public Map<ResourceType, Integer> calculateAllProduction(
            List<Building> buildings,
            double difficultyMultiplier) {
        return buildings.stream()
                .collect(Collectors.groupingBy(
                        Building::getProducedResource,
                        Collectors.summingInt(b ->
                                (int)(b.getProductionAmount() * difficultyMultiplier))
                ));
    }

    // mnożnik sezonowy dla danego zasobu i pory roku
    public double getSeasonMultiplier(ResourceType type, Season season) {
        return switch (season) {
            case SPRING -> switch (type) {
                case FOOD   -> 1.2;  // wiosna = lepsze zbiory
                case MORALE -> 1.1;  // wiosna = lepszy nastrój
                default     -> 1.0;
            };
            case SUMMER -> switch (type) {
                case FOOD -> 1.3;    // lato = najlepsze zbiory
                case WOOD -> 1.1;    // lato = łatwiejsze wyręby
                default   -> 1.0;
            };
            case AUTUMN -> switch (type) {
                case FOOD -> 1.1;    // jesień = żniwa
                case GOLD -> 1.2;    // jesień = targi
                default   -> 1.0;
            };
            case WINTER -> switch (type) {
                case FOOD   -> 0.7;  // zima = mniej jedzenia
                case WOOD   -> 0.8;  // zima = trudniejszy wyrąb
                case MORALE -> 0.8;  // zima = gorszy nastrój
                default     -> 1.0;
            };
        };
    }

    // łączna produkcja z uwzględnieniem sezonu
    public int calculateWithSeason(List<Building> buildings,
                                   ResourceType type,
                                   double difficultyMultiplier,
                                   Season season) {
        double seasonMult = getSeasonMultiplier(type, season);
        return buildings.stream()
                .filter(b -> b.getProducedResource() == type)
                .mapToInt(b -> (int)(b.getProductionAmount()
                        * difficultyMultiplier
                        * seasonMult))
                .sum();
    }

    // sprawdza czy miasto ma nadwyżkę żywności
    public boolean hasFoodSurplus(City city) {
        int production = calculateTotalProduction(
                city.getBuildings(),
                ResourceType.FOOD,
                city.getDifficulty().getProductionMultiplier());
        int consumption = Math.max(1,
                city.getResource(ResourceType.POPULATION) / 10);
        return production > consumption;
    }
}