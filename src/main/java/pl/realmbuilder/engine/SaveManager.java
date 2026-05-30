package pl.realmbuilder.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.GameSave;
import pl.realmbuilder.model.ResourceType;
import pl.realmbuilder.model.Season;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveManager {

    private static final String SAVE_FILE = "realm_builder_save.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // zapisuje stan gry do pliku JSON
    public boolean save(City city) {
        try {
            GameSave save = cityToSave(city);
            try (FileWriter writer = new FileWriter(SAVE_FILE)) {
                gson.toJson(save, writer);
            }
            System.out.println("💾 Gra zapisana do pliku: " + SAVE_FILE);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Błąd zapisu: " + e.getMessage());
            return false;
        }
    }

    // wczytuje stan gry z pliku JSON
    public City load() {
        try (FileReader reader = new FileReader(SAVE_FILE)) {
            GameSave save = gson.fromJson(reader, GameSave.class);
            City city = saveToCity(save);
            System.out.println("📂 Wczytano zapis z: " + save.getSavedAt());
            return city;
        } catch (IOException e) {
            System.err.println("❌ Nie znaleziono pliku zapisu: " + SAVE_FILE);
            return null;
        }
    }

    public boolean saveExists() {
        return new java.io.File(SAVE_FILE).exists();
    }

    // konwertuje City → GameSave
    private GameSave cityToSave(City city) {
        GameSave save = new GameSave();
        save.setCityName(city.getName());
        save.setDifficulty(city.getDifficulty().name());
        save.setCurrentTurn(city.getCurrentTurn());
        save.setCurrentSeason(city.getCurrentSeason().name());
        save.setMaxGoldEverHeld(city.getMaxGoldEverHeld());
        save.setConsecutiveRaidsSurvived(city.getConsecutiveRaidsSurvived());
        save.setTurnsWithHighMorale(city.getTurnsWithHighMorale());
        save.setSavedAt(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // zasoby jako mapa String → Integer
        Map<String, Integer> resources = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            resources.put(type.name(), city.getResource(type));
        }
        save.setResources(resources);

        // budynki — nazwy klas i poziomy
        List<String> classNames = new ArrayList<>();
        List<Integer> levels    = new ArrayList<>();
        for (Building b : city.getBuildings()) {
            classNames.add(b.getClass().getName());
            levels.add(b.getLevel());
        }
        save.setBuildingClassNames(classNames);
        save.setBuildingLevels(levels);

        return save;
    }

    // konwertuje GameSave → City
    private City saveToCity(GameSave save) {
        Difficulty difficulty = Difficulty.valueOf(save.getDifficulty());
        City city = new City(save.getCityName(), difficulty);

        // przywróć turę i sezon
        city.setCurrentTurn(save.getCurrentTurn());
        city.setCurrentSeason(Season.valueOf(save.getCurrentSeason()));
        city.setMaxGoldEverHeld(save.getMaxGoldEverHeld());
        city.setConsecutiveRaidsSurvived(save.getConsecutiveRaidsSurvived());
        city.setTurnsWithHighMorale(save.getTurnsWithHighMorale());

        // przywróć zasoby
        for (Map.Entry<String, Integer> entry : save.getResources().entrySet()) {
            ResourceType type = ResourceType.valueOf(entry.getKey());
            // wyzeruj i ustaw wartość z zapisu
            int current = city.getResource(type);
            city.subtractResource(type, current);
            city.addResource(type, entry.getValue());
        }

        // przywróć budynki przez refleksję
        List<String> classNames = save.getBuildingClassNames();
        List<Integer> levels    = save.getBuildingLevels();
        for (int i = 0; i < classNames.size(); i++) {
            try {
                Class<?> clazz   = Class.forName(classNames.get(i));
                Building building = (Building)
                        clazz.getDeclaredConstructor().newInstance();
                int targetLevel  = levels.get(i);
                for (int lvl = 1; lvl < targetLevel; lvl++) {
                    building.upgrade();
                }
                city.addBuilding(building);
            } catch (Exception e) {
                System.err.println("❌ Nie można odtworzyć budynku: "
                        + classNames.get(i));
            }
        }

        return city;
    }
}