package pl.realmbuilder.advisors;

import pl.realmbuilder.annotations.AdvisorPlugin;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Specialty;

@AdvisorPlugin(
        name        = "Wiedźma Morgana",
        specialty   = Specialty.MAGIC,
        cost        = 40,
        description = "Tajemnicza znachorka. Leczy chorych i odpędza klątwy."
)
public class Witch implements Advisor {

    @Override
    public String getName() { return "Wiedźma Morgana"; }

    @Override
    public Specialty getSpecialty() { return Specialty.MAGIC; }

    @Override
    public int getCost() { return 40; }

    @Override
    public String getDescription() {
        return "Tajemnicza znachorka. Leczy chorych i odpędza klątwy.";
    }

    @Override
    public boolean canHelp(GameEvent event) {
        return event.getClass().getSimpleName().equals("PlagueEvent")
                || event.getClass().getSimpleName().equals("DroughtEvent");
    }

    @Override
    public void help(City city, GameEvent event) {
        city.addResource(ResourceType.POPULATION, 15);
        city.addResource(ResourceType.MORALE, 15);
        city.addResource(ResourceType.FOOD, 20);
        System.out.println("🔮 Wiedźma Morgana: \"Widziałam to w gwiazdach... "
                + "Moje zaklęcia uleczyły chorych i przywróciły urodzaj!\"");
    }
}