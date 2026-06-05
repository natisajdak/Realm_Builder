package pl.realmbuilder.model;

import java.util.List;
import java.util.Map;

// klasa do serializacji stanu gry do JSON przez Gson
public class GameSave {

    private String cityName;
    private String difficulty;
    private int currentTurn;
    private String currentSeason;
    private Map<String, Integer> resources;
    private List<String> buildingClassNames;
    private List<Integer> buildingLevels;
    private Map<String, String> buildingPositions;
    private int maxGoldEverHeld;
    private int consecutiveRaidsSurvived;
    private int turnsWithHighMorale;
    private String savedAt;

    // konstruktor bezargumentowy wymagany przez Gson
    public GameSave() {}

    // gettery i settery — Gson potrzebuje dostępu do pól
    public String getCityName()                    { return cityName; }
    public void setCityName(String v)              { cityName = v; }

    public String getDifficulty()                  { return difficulty; }
    public void setDifficulty(String v)            { difficulty = v; }

    public int getCurrentTurn()                    { return currentTurn; }
    public void setCurrentTurn(int v)              { currentTurn = v; }

    public String getCurrentSeason()               { return currentSeason; }
    public void setCurrentSeason(String v)         { currentSeason = v; }

    public Map<String, Integer> getResources()     { return resources; }
    public void setResources(Map<String, Integer> v) { resources = v; }

    public List<String> getBuildingClassNames()    { return buildingClassNames; }
    public void setBuildingClassNames(List<String> v) { buildingClassNames = v; }

    public List<Integer> getBuildingLevels()       { return buildingLevels; }
    public void setBuildingLevels(List<Integer> v) { buildingLevels = v; }

    public Map<String, String> getBuildingPositions() { return buildingPositions; }
    public void setBuildingPositions(Map<String, String> v) { buildingPositions = v; }

    public int getMaxGoldEverHeld()                { return maxGoldEverHeld; }
    public void setMaxGoldEverHeld(int v)          { maxGoldEverHeld = v; }

    public int getConsecutiveRaidsSurvived()       { return consecutiveRaidsSurvived; }
    public void setConsecutiveRaidsSurvived(int v) { consecutiveRaidsSurvived = v; }

    public int getTurnsWithHighMorale()            { return turnsWithHighMorale; }
    public void setTurnsWithHighMorale(int v)      { turnsWithHighMorale = v; }

    public String getSavedAt()                     { return savedAt; }
    public void setSavedAt(String v)               { savedAt = v; }
}
