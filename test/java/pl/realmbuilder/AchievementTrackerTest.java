package pl.realmbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.realmbuilder.engine.AchievementTracker;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

class AchievementTrackerTest {

    private AchievementTracker tracker;
    private City city;

    @BeforeEach
    void setUp() {
        tracker = new AchievementTracker();
        city    = new City("Test", Difficulty.NORMAL);
    }

    @Test
    @DisplayName("Na starcie brak odblokowanych osiągnięć")
    void testNoAchievementsInitially() {
        assertEquals(0, tracker.getUnlockedCount());
    }

    @Test
    @DisplayName("Osiągnięcie Bogacz odblokowane przy 5000 złota")
    void testRichAchievement() {
        // dodaj 5000 złota — maxGoldEverHeld śledzone w addResource
        city.addResource(ResourceType.GOLD, 5000);
        tracker.checkAchievements(city, 8);
        assertTrue(tracker.getUnlockedAchievements()
                        .stream()
                        .anyMatch(a -> a.getId().equals("RICH")),
                "Osiągnięcie RICH powinno być odblokowane");
    }

    @Test
    @DisplayName("Osiągnięcie Metropolia przy 500 populacji")
    void testPopulousAchievement() {
        city.addResource(ResourceType.POPULATION, 450);
        tracker.checkAchievements(city, 8);
        assertTrue(tracker.getUnlockedAchievements()
                .stream()
                .anyMatch(a -> a.getId().equals("POPULOUS")));
    }

    @Test
    @DisplayName("Lista wszystkich osiągnięć ma 7 elementów")
    void testAllAchievementsCount() {
        assertEquals(7, tracker.getAllAchievements().size());
    }
}