package pl.realmbuilder.interfaces;

import pl.realmbuilder.model.City;

public interface GameEvent {
    String getName();
    String getDescription();
    double getProbability();
    void apply(City city);        // aplikuje efekt zdarzenia na miasto
    boolean isNegative();         // czy zdarzenie jest negatywne
}