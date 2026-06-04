package pl.realmbuilder.advisors;

import pl.realmbuilder.annotations.AdvisorPlugin;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Specialty;

@AdvisorPlugin(
        name        = "Kupiec Benedikt",
        specialty   = Specialty.TRADE,
        cost        = 35,
        description = "Przebiegły handlarz. Potrafi odbudować skarbiec po każdej katastrofie."
)
public class Merchant implements Advisor {

    @Override
    public String getName() { return "Kupiec Benedikt"; }

    @Override
    public Specialty getSpecialty() { return Specialty.TRADE; }

    @Override
    public int getCost() { return 35; }

    @Override
    public String getDescription() {
        return "Przebiegły handlarz. Potrafi odbudować skarbiec po każdej katastrofie.";
    }

    @Override
    public boolean canHelp(GameEvent event) {
        return event.getClass().getSimpleName().equals("StormEvent")
                || event.getClass().getSimpleName().equals("DroughtEvent")
                || event.getClass().getSimpleName().equals("RaidEvent");
    }

    @Override
    public void help(City city, GameEvent event) {
        city.addResource(ResourceType.GOLD, 60);
        city.addResource(ResourceType.MORALE, 5);
    }
}