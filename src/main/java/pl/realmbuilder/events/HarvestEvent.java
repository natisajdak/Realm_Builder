package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Obfite żniwa",
        probability = 0.30,
        season      = Season.AUTUMN
)
public class HarvestEvent implements GameEvent {

    @Override
    public String getName() { return "Obfite żniwa"; }

    @Override
    public String getDescription() {
        return "Wyjątkowo urodzajny rok! Spichlerze pękają w szwach, "
                + "a kupcy płacą krocie za nadwyżki.";
    }

    @Override
    public double getProbability() { return 0.30; }

    @Override
    public boolean isNegative() { return false; }

    @Override
    public void apply(City city) {
        city.addResource(ResourceType.FOOD, 80);
        city.addResource(ResourceType.GOLD, 30);
        city.addResource(ResourceType.MORALE, 10);

        System.out.println("🌾 Obfite żniwa! +80 żywności, +30 złota, +10 morale.");
    }
}