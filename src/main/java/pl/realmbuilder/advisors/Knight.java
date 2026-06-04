package pl.realmbuilder.advisors;

import pl.realmbuilder.annotations.AdvisorPlugin;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Specialty;

@AdvisorPlugin(
        name        = "Rycerz Aldric",
        specialty   = Specialty.COMBAT,
        cost        = 30,
        description = "Weteran wielu bitew. Specjalizuje się w obronie przed najazdami."
)
public class Knight implements Advisor {

    @Override
    public String getName() { return "Rycerz Aldric"; }

    @Override
    public Specialty getSpecialty() { return Specialty.COMBAT; }

    @Override
    public int getCost() { return 30; }

    @Override
    public String getDescription() {
        return "Weteran wielu bitew. Specjalizuje się w obronie przed najazdami.";
    }

    @Override
    public boolean canHelp(GameEvent event) {
        // Rycerz pomaga przy najazdach i burzach
        return event.getClass().getSimpleName().equals("RaidEvent")
                || event.getClass().getSimpleName().equals("StormEvent");
    }

    @Override
    public void help(City city, GameEvent event) {
        // redukuje straty o 50% - najpierw aplikuj zdarzenie,
        // potem rycerz odbudowuje połowę strat
        city.addResource(ResourceType.POPULATION, 10);
        city.addResource(ResourceType.GOLD, 20);
        city.addResource(ResourceType.MORALE, 10);
    }
}