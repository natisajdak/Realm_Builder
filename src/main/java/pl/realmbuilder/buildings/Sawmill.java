package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Tartak",
        cost        = 120,
        produces    = ResourceType.WOOD,
        amount      = 20,
        description = "Produkuje drewno potrzebne do budowy innych budynków."
)
public class Sawmill implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 20;

    @Override public String getName()               { return "Tartak"; }
    @Override public int getCost()                  { return 120; }
    @Override public ResourceType getProducedResource() { return ResourceType.WOOD; }
    @Override public int getProductionAmount()      { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                 { return level; }
    @Override public String getDescription()        {
        return "Produkuje " + getProductionAmount() + " drewna/turę";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount() * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.WOOD, amount);
    }

    @Override
    public void upgrade() {
        level++;
    }
}