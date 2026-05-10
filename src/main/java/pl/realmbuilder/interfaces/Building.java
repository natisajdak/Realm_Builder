package pl.realmbuilder.interfaces;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

public interface Building {
    String getName();
    int getCost();
    ResourceType getProducedResource();
    int getProductionAmount();
    void produce(City city);      // produkuje zasoby i dodaje do miasta
    void upgrade();               // ulepsza budynek (zwiększa produkcję)
    int getLevel();
    String getDescription();
}