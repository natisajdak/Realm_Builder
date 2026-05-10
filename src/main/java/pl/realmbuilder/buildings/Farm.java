package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Farma",
        cost        = 100,
        produces    = ResourceType.FOOD,
        amount      = 15,
        description = "Produkuje żywność każdej tury. Niezbędna do wyżywienia populacji."
)
public class Farm implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 15;

    @Override public String getName()                   { return "Farma"; }
    @Override public int getCost()                      { return 100; }
    @Override public ResourceType getProducedResource() { return ResourceType.FOOD; }
    @Override public int getProductionAmount()          { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                     { return level; }
    @Override public String getDescription()            {
        return "Produkuje " + getProductionAmount() + " żywności/turę";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount()
                * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.FOOD, amount);
    }

    @Override
    public void upgrade() {
        level++;
    }
}