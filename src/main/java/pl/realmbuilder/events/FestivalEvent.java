package pl.realmbuilder.events;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

@Event(
        name        = "Festiwal",
        probability = 0.25,
        season      = Season.SPRING
)
public class FestivalEvent implements GameEvent {

    @Override
    public String getName() { return "Festiwal wiosny"; }

    @Override
    public String getDescription() {
        return "Mieszkańcy świętują nadejście wiosny! "
                + "Morale w mieście rośnie, a nowi osadnicy przybywają.";
    }

    @Override
    public double getProbability() { return 0.25; }

    @Override
    public boolean isNegative() { return false; }

    @Override
    public void apply(City city) {
        city.addResource(ResourceType.MORALE, 20);
        city.addResource(ResourceType.POPULATION, 10);
        city.resetConsecutiveRaids();

        System.out.println("🎉 Festiwal wiosny! +20 morale, +10 populacji.");
    }
}