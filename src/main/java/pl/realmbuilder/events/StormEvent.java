package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Burza",
        probability = 0.20,
        season      = Season.AUTUMN
)
public class StormEvent implements GameEvent {

    @Override
    public String getName() { return "Burza"; }

    @Override
    public String getDescription() {
        return "Gwałtowna burza niszczy część zbiorów i uszkadza budynki. "
                + "Miasto traci złoto i morale.";
    }

    @Override
    public double getProbability() { return 0.20; }

    @Override
    public boolean isNegative() { return true; }

    @Override
    public void apply(City city) {
        double multiplier = city.getDifficulty().getEventMultiplier();
        int goldLoss   = (int)(50 * multiplier);
        int moraleLoss = (int)(15 * multiplier);

        city.subtractResource(ResourceType.GOLD, goldLoss);
        city.subtractResource(ResourceType.MORALE, moraleLoss);

        System.out.printf("🌩️  Burza! Stracono %d złota i %d morale.%n",
                goldLoss, moraleLoss);
    }
}