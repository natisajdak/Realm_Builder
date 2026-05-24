package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Susza",
        probability = 0.18,
        season      = Season.SUMMER
)
public class DroughtEvent implements GameEvent {

    @Override
    public String getName() { return "Susza"; }

    @Override
    public String getDescription() {
        return "Długotrwała susza niszczy uprawy. "
                + "Pola wyschły, a żywność gwałtownie ubywa.";
    }

    @Override
    public double getProbability() { return 0.18; }

    @Override
    public boolean isNegative() { return true; }

    @Override
    public void apply(City city) {
        double multiplier = city.getDifficulty().getEventMultiplier();
        int foodLoss   = (int)(60 * multiplier);
        int moraleLoss = (int)(10 * multiplier);

        city.subtractResource(ResourceType.FOOD, foodLoss);
        city.subtractResource(ResourceType.MORALE, moraleLoss);

        System.out.printf("☀️  Susza! Stracono %d żywności i %d morale.%n",
                foodLoss, moraleLoss);
    }
}