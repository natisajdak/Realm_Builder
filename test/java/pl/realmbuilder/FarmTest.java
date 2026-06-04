package pl.realmbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.realmbuilder.buildings.Farm;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

class FarmTest {

    private Farm farm;
    private City city;

    @BeforeEach
    void setUp() {
        farm = new Farm();
        city = new City("Test", Difficulty.NORMAL);
    }

    @Test
    @DisplayName("Farma produkuje poprawną ilość żywności")
    void testProduction() {
        int before = city.getResource(ResourceType.FOOD);
        farm.produce(city);
        int after = city.getResource(ResourceType.FOOD);
        assertEquals(15, after - before); // NORMAL = mnożnik 1.0
    }

    @Test
    @DisplayName("Ulepszenie podwaja produkcję")
    void testUpgrade() {
        assertEquals(15, farm.getProductionAmount());
        farm.upgrade();
        assertEquals(30, farm.getProductionAmount());
        farm.upgrade();
        assertEquals(45, farm.getProductionAmount());
    }

    @Test
    @DisplayName("Na łatwym poziomie produkcja jest większa")
    void testEasyDifficulty() {
        City easyCity = new City("Easy", Difficulty.EASY);
        int before = easyCity.getResource(ResourceType.FOOD);
        farm.produce(easyCity);
        int after = easyCity.getResource(ResourceType.FOOD);
        assertEquals(22, after - before); // 15 * 1.5 = 22
    }

    @Test
    @DisplayName("Farma produkuje żywność a nie złoto")
    void testProducesFood() {
        assertEquals(ResourceType.FOOD, farm.getProducedResource());
    }
}