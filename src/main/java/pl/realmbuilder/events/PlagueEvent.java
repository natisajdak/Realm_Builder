package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Zaraza",
        probability = 0.12,
        season      = Season.WINTER,
        anyseason   = false
)
public class PlagueEvent implements GameEvent {

    @Override
    public String getName() { return "Zaraza"; }

    @Override
    public String getDescription() {
        return "Tajemnicza choroba zbiera śmiertelne żniwo. "
                + "Populacja gwałtownie spada, morale w rozsypce.";
    }

    @Override
    public double getProbability() { return 0.12; }

    @Override
    public boolean isNegative() { return true; }

    @Override
    public void apply(City city) {
        double multiplier = city.getDifficulty().getEventMultiplier();
        int populationLoss = (int)(30 * multiplier);
        int moraleLoss     = (int)(20 * multiplier);

        city.subtractResource(ResourceType.POPULATION, populationLoss);
        city.subtractResource(ResourceType.MORALE, moraleLoss);

        System.out.printf("☠️  Zaraza! Stracono %d mieszkańców i %d morale.%n",
                populationLoss, moraleLoss);
    }
}