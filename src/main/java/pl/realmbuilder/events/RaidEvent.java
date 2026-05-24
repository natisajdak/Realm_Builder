package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Najazd",
        probability = 0.15,
        season      = Season.SUMMER
)
public class RaidEvent implements GameEvent {

    @Override
    public String getName() { return "Najazd barbarzyńców"; }

    @Override
    public String getDescription() {
        return "Hordy barbarzyńców atakują miasto! "
                + "Giną mieszkańcy, a skarbiec zostaje ograbiony.";
    }

    @Override
    public double getProbability() { return 0.15; }

    @Override
    public boolean isNegative() { return true; }

    @Override
    public void apply(City city) {
        double multiplier = city.getDifficulty().getEventMultiplier();
        int populationLoss = (int)(20 * multiplier);
        int goldLoss       = (int)(40 * multiplier);

        city.subtractResource(ResourceType.POPULATION, populationLoss);
        city.subtractResource(ResourceType.GOLD, goldLoss);
        city.incrementConsecutiveRaids();

        System.out.printf("⚔️  Najazd! Stracono %d mieszkańców i %d złota.%n",
                populationLoss, goldLoss);
    }
}