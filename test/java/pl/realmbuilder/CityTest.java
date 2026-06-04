package pl.realmbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

class CityTest {

    private City city;

    @BeforeEach
    void setUp() {
        city = new City("Testowe", Difficulty.NORMAL);
    }

    @Test
    @DisplayName("Zasoby startowe są poprawne dla Normal")
    void testInitialResources() {
        assertEquals(200, city.getResource(ResourceType.GOLD));
        assertEquals(100, city.getResource(ResourceType.FOOD));
        assertEquals(50,  city.getResource(ResourceType.POPULATION));
        assertEquals(50,  city.getResource(ResourceType.MORALE));
    }

    @Test
    @DisplayName("Zasoby nie schodzą poniżej zera")
    void testResourcesNotNegative() {
        city.subtractResource(ResourceType.GOLD, 99999);
        assertEquals(0, city.getResource(ResourceType.GOLD));
    }

    @Test
    @DisplayName("canAfford zwraca false gdy za mało złota")
    void testCanAfford() {
        assertFalse(city.canAfford(99999));
        assertTrue(city.canAfford(100));
    }

    @Test
    @DisplayName("Gra kończy się gdy złoto = 0")
    void testGameOverOnNoGold() {
        city.subtractResource(ResourceType.GOLD, 99999);
        city.nextTurn();
        assertTrue(city.isGameOver());
        assertFalse(city.isPlayerWon());
    }

    @Test
    @DisplayName("Wygrana po 80 turach")
    void testWinAfter80Turns() {
        for (int i = 0; i < 80; i++) {
            city.addResource(ResourceType.GOLD, 1000);
            city.addResource(ResourceType.POPULATION, 1);
            city.nextTurn();
        }
        assertTrue(city.isGameOver());
        assertTrue(city.isPlayerWon());
    }

    @Test
    @DisplayName("Pora roku zmienia się co 4 tury")
    void testSeasonChanges() {
        // tura 1 = SPRING
        assertEquals("Wiosna", city.getCurrentSeason().getDisplayName());
        // przejdź 4 tury
        for (int i = 0; i < 4; i++) {
            city.addResource(ResourceType.GOLD, 100);
            city.addResource(ResourceType.POPULATION, 1);
            city.nextTurn();
        }
        assertEquals("Lato", city.getCurrentSeason().getDisplayName());
    }
}