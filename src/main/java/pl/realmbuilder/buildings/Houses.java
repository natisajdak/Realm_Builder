package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Domy",
        cost        = 80,
        produces    = ResourceType.POPULATION,
        amount      = 10,
        description = "Przyciąga nowych mieszkańców do miasta każdej tury."
)
public class Houses implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 10;

    @Override public String getName()               { return "Domy"; }
    @Override public int getCost()                  { return 80; }
    @Override public ResourceType getProducedResource() { return ResourceType.POPULATION; }
    @Override public int getProductionAmount()      { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                 { return level; }
    @Override public String getDescription()        {
        return "Przyciąga " + getProductionAmount() + " mieszkańców/turę";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount() * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.POPULATION, amount);
    }

    @Override
    public void upgrade() {
        level++;
    }
}