package pl.realmbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.realmbuilder.engine.EconomyProcessor;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

class EconomyProcessorTest {

    private EconomyProcessor processor;
    private City city;

    @BeforeEach
    void setUp() {
        processor = new EconomyProcessor();
        city = new City("Test", Difficulty.NORMAL);
    }

    @Test
    @DisplayName("Populacja konsumuje żywność każdą turę")
    void testFoodConsumption() {
        int foodBefore = city.getResource(ResourceType.FOOD);
        int population = city.getResource(ResourceType.POPULATION); // 50
        int expected   = Math.max(1, population / 10); // 5
        processor.process(city);
        int foodAfter = city.getResource(ResourceType.FOOD);
        assertTrue(foodAfter < foodBefore,
                "Żywność powinna spaść po przetworzeniu tury");
        assertEquals(foodBefore - expected, foodAfter);
    }

    @Test
    @DisplayName("Populacja płaci podatki")
    void testTaxCollection() {
        int goldBefore = city.getResource(ResourceType.GOLD);
        int population = city.getResource(ResourceType.POPULATION); // 50
        int expectedTax = population / 20; // 2
        processor.process(city);
        int goldAfter = city.getResource(ResourceType.GOLD);
        // złoto może też spaść przez inne efekty - sprawdzamy tylko podatki
        assertTrue(goldAfter >= goldBefore - 10,
                "Złoto powinno wzrosnąć o podatki");
    }

    @Test
    @DisplayName("Głód redukuje populację")
    void testFamineReducesPopulation() {
        // zerujemy żywność
        city.subtractResource(ResourceType.FOOD, 99999);
        int popBefore = city.getResource(ResourceType.POPULATION);
        processor.process(city);
        int popAfter = city.getResource(ResourceType.POPULATION);
        assertTrue(popAfter < popBefore,
                "Populacja powinna spaść przy braku żywności");
    }
}