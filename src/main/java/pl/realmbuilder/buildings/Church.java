package pl.realmbuilder.buildings;

import pl.realmbuilder.annotations.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

@Building(
        name        = "Kościół",
        cost        = 110,
        produces    = ResourceType.MORALE,
        amount      = 12,
        description = "Podnosi morale mieszkańców przez modlitwy i ceremonie."
)
public class Church implements pl.realmbuilder.interfaces.Building {

    private int level = 1;
    private static final int BASE_PRODUCTION = 12;

    @Override public String getName()                   { return "Kościół"; }
    @Override public int getCost()                      { return 110; }
    @Override public ResourceType getProducedResource() { return ResourceType.MORALE; }
    @Override public int getProductionAmount()          { return BASE_PRODUCTION * level; }
    @Override public int getLevel()                     { return level; }
    @Override public String getDescription() {
        return "Produkuje " + getProductionAmount() + " morale/turę";
    }

    @Override
    public void produce(City city) {
        int amount = (int)(getProductionAmount()
                * city.getDifficulty().getProductionMultiplier());
        city.addResource(ResourceType.MORALE, amount);
    }

    @Override
    public void upgrade() { level++; }
}