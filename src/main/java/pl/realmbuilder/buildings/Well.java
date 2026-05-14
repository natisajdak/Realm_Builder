package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Studnia",
        cost        = 90,
        produces    = ResourceType.FOOD,
        amount      = 8,
        description = "Zapewnia czystą wodę — redukuje ryzyko suszy i zarazy."
)
public class Well implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 8;

    @Override public String getName()                   { return "Studnia"; }
    @Override public int getCost()                      { return 90; }
    @Override public ResourceType getProducedResource() { return ResourceType.FOOD; }
    @Override public int getProductionAmount()          { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                     { return level; }
    @Override public String getDescription() {
        return "Produkuje " + getProductionAmount() + " żywności/turę + ochrona przed suszą";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount()
                * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.FOOD, amount);
    }

    @Override
    public void upgrade() { level++; }
}