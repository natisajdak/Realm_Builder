package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Rynek",
        cost        = 150,
        produces    = ResourceType.GOLD,
        amount      = 25,
        description = "Generuje złoto przez handel z kupcami."
)
public class Market implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 25;

    @Override public String getName()                   { return "Rynek"; }
    @Override public int getCost()                      { return 150; }
    @Override public ResourceType getProducedResource() { return ResourceType.GOLD; }
    @Override public int getProductionAmount()          { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                     { return level; }
    @Override public String getDescription() {
        return "Generuje " + getProductionAmount() + " złota/turę";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount()
                * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.GOLD, amount);
    }

    @Override
    public void upgrade() { level++; }
}