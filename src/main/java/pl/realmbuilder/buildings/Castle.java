package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Zamek",
        cost        = 300,
        produces    = ResourceType.MORALE,
        amount      = 20,
        description = "Symbol potęgi i chwały. Maksymalna obrona, ogromne morale."
)
public class Castle implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 20;

    @Override public String getName()                   { return "Zamek"; }
    @Override public int getCost()                      { return 300; }
    @Override public ResourceType getProducedResource() { return ResourceType.MORALE; }
    @Override public int getProductionAmount()          { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                     { return level; }
    @Override public String getDescription() {
        return "Produkuje " + getProductionAmount() + " morale/turę — symbol potęgi";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount()
                * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.MORALE, amount);
        // zamek daje też bonus do złota - podatek od prestiżu
        city.addResource(ResourceType.GOLD, 10 * level);
    }

    @Override
    public void upgrade() { level++; }
}