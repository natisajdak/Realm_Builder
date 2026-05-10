package pl.realmbuilder.interfaces;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Specialty;

public interface Advisor {
    String getName();
    Specialty getSpecialty();
    int getCost();
    boolean canHelp(GameEvent event);   // czy doradca może pomóc przy tym zdarzeniu
    void help(City city, GameEvent event); // redukuje efekt zdarzenia
    String getDescription();
}