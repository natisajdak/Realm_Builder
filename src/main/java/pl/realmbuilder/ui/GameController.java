package pl.realmbuilder.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.realmbuilder.engine.*;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.Building;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.Difficulty;
import pl.realmbuilder.model.ResourceType;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class GameController implements Initializable {

    @FXML private Label    cityLabel;
    @FXML private Label    turnLabel;
    @FXML private Label    seasonLabel;
    @FXML private Label    seasonBonusLabel;
    @FXML private Label    warningLabel;
    @FXML private ProgressBar turnBar;

    @FXML private Label       goldLabel;
    @FXML private Label       foodLabel;
    @FXML private Label       woodLabel;
    @FXML private Label       moraleLabel;
    @FXML private Label       populationLabel;

    @FXML private HBox    advisorsBox;
    @FXML private Label   advisorQuoteLabel;
    @FXML private VBox    availableBuildingsBox;
    @FXML private VBox    ownedBuildingsBox;
    @FXML private TextArea eventLog;
    @FXML private Label   statusLabel;

    @FXML private WebView mapView;
    private WebEngine     webEngine;

    private City               city;
    private List<Building>     availableBuildings;
    private List<Advisor>      availableAdvisors;
    private Building           selectedBuilding;
    private Building           selectedOwnedBuilding;
    private Advisor            selectedAdvisor;
    private TurnProcessor      turnProcessor;
    private String             selectedMapCell;

    private static final int MAP_WIDTH = 1200;
    private static final int MAP_HEIGHT = 390;
    private static final int GRID_COLUMNS = 12;
    private static final int GRID_ROWS = 4;
    private static final int GRID_START_X = 126;
    private static final int GRID_START_Y = 72;
    private static final int GRID_CELL_WIDTH = 80;
    private static final int GRID_CELL_HEIGHT = 64;

    private static final Set<String> BLOCKED_GRID_CELLS = Set.of(
            "0:0", "9:0", "10:0", "11:0",
            "0:1", "10:1", "11:1",
            "0:2", "1:2", "2:2", "3:2", "4:2", "5:2",
            "6:2", "7:2", "8:2", "9:2", "10:2", "11:2",
            "5:3", "6:3", "11:3"
    );

    private static final Map<String, String> DEFAULT_BUILDING_CELLS = Map.ofEntries(
            Map.entry("pl.realmbuilder.buildings.Sawmill", "1:0"),
            Map.entry("pl.realmbuilder.buildings.Barracks", "3:0"),
            Map.entry("pl.realmbuilder.buildings.Church", "7:0"),
            Map.entry("pl.realmbuilder.buildings.Well", "5:1"),
            Map.entry("pl.realmbuilder.buildings.Castle", "9:1"),
            Map.entry("pl.realmbuilder.buildings.Farm", "1:3"),
            Map.entry("pl.realmbuilder.buildings.Houses", "4:3"),
            Map.entry("pl.realmbuilder.buildings.Market", "7:3")
    );

    private final BuildingLoader     buildingLoader     = new BuildingLoader();
    private final AdvisorLoader      advisorLoader      = new AdvisorLoader();
    private final ResourceCalculator resourceCalc       = new ResourceCalculator();
    private final SeasonManager      seasonManager      = new SeasonManager(resourceCalc);
    private final EconomyProcessor   economyProcessor   = new EconomyProcessor();
    private final AchievementTracker achievementTracker = new AchievementTracker();
    private final EventLoader        eventLoader        = new EventLoader();
    private final TurnNarrator       narrator           = new TurnNarrator();
    private final SaveManager        saveManager        = new SaveManager();
    private final RankCalculator     rankCalculator     = new RankCalculator();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        webEngine = mapView.getEngine();
        mapView.setContextMenuEnabled(false);
        mapView.setOnMouseClicked(event -> handleMapClick(event.getX(), event.getY()));
    }

    public void initGame(String cityName, String difficulty) {
        if (cityName == null) {
            city = saveManager.load();
            if (city == null) { log("❌ Nie udało się wczytać zapisu."); return; }
        } else {
            city = new City(cityName, Difficulty.valueOf(difficulty));
        }

        availableBuildings = buildingLoader.loadBuildings();
        availableAdvisors  = advisorLoader.loadAdvisors();
        availableAdvisors.forEach(city::addAdvisor);

        narrator.setLogger(this::log);
        seasonManager.setLogger(this::log);
        economyProcessor.setLogger(this::log);
        achievementTracker.setLogger(this::log);
        turnProcessor = new TurnProcessor(
                resourceCalc, seasonManager, economyProcessor,
                achievementTracker, eventLoader, narrator);

        loadMap();
        buildAdvisorCards();
        refreshUILists();
        updateUI();
        log("⚔ Witaj w " + city.getName() + "! Zbuduj potężne miasto.");
    }

    private void loadMap() {
        String html = buildMapHtml();
        webEngine.loadContent(html);
    }

    private String buildMapHtml() {
        return """
<!DOCTYPE html>
<html>
<head>
<style>
  html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:#4f6d38; }
  svg { display:block; width:100vw; height:100vh; }
  @keyframes floatRightA { 0% { transform: translateX(-260px); } 100% { transform: translateX(1360px); } }
  @keyframes floatRightB { 0% { transform: translateX(-340px); } 100% { transform: translateX(1280px); } }
  .cloud { animation: floatRightA 58s linear infinite; opacity: 0.68; }
  .cloud2 { animation: floatRightB 78s linear infinite; animation-delay: -34s; opacity: 0.5; }
  .cloud3 { animation: floatRightA 92s linear infinite; animation-delay: -58s; opacity: 0.42; }
  @keyframes riverFlow { from { stroke-dashoffset: 0; } to { stroke-dashoffset: -96; } }
  @keyframes ripple { 0%, 100% { transform: translateX(0); opacity:0.35; } 50% { transform: translateX(20px); opacity:0.16; } }
  .river-current { stroke-dasharray: 34 26; animation: riverFlow 3.2s linear infinite; }
  .water-detail { animation: ripple 5s infinite ease-in-out; transform-box: fill-box; transform-origin: center; }
  @keyframes gentleHover { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }
  @keyframes treeSway { 0%, 100% { transform: rotate(-1.5deg); } 50% { transform: rotate(2deg); } }
  .pin { animation: gentleHover 4s infinite ease-in-out; transform-box: fill-box; transform-origin: center; }
  .pin-ghost { opacity: 0.68; }
  .tree-crown { animation: treeSway 3.8s ease-in-out infinite; transform-box: fill-box; transform-origin: bottom center; }
  .tree-crown:nth-child(2n) { animation-duration: 4.7s; animation-delay: -1.2s; }
  .tree-leaf { stroke:rgba(20,64,26,0.25); stroke-width:1; }
  .grid-cell { fill:#fff7df; stroke:#f0cf8d; stroke-width:1.2; opacity:0.24; }
  .grid-cell.free { cursor:pointer; }
  .grid-cell.free:hover { opacity:0.58; fill:#edf2d9; stroke:#fff7df; }
  .grid-cell.selected { opacity:0.72; fill:#b7c77c; stroke:#fff7df; stroke-width:3; }
  .grid-cell.occupied { opacity:0.2; fill:#6d4b2c; stroke:#3e2c1d; }
  .grid-cell.blocked { opacity:0.14; fill:#234c4d; stroke:#18393d; }
  .grid-hint { fill:#fff7df; font: bold 16px 'Segoe UI', Arial, sans-serif; filter:url(#textShadow); }
</style>
</head>
<body>
<svg viewBox="0 0 1200 390" preserveAspectRatio="xMidYMid slice" xmlns="http://www.w3.org/2000/svg">
<defs>
  <linearGradient id="grassG" x1="0%" y1="0%" x2="0%" y2="100%">
    <stop offset="0%" stop-color="#78934f"/>
    <stop offset="52%" stop-color="#5f7d3e"/>
    <stop offset="100%" stop-color="#3c5c31"/>
  </linearGradient>
  <linearGradient id="waterG" x1="0%" y1="0%" x2="100%" y2="0%">
    <stop offset="0%" stop-color="#2492aa"/>
    <stop offset="45%" stop-color="#166f8d"/>
    <stop offset="100%" stop-color="#28a1b2"/>
  </linearGradient>
  <linearGradient id="hillG" x1="0%" y1="0%" x2="0%" y2="100%">
    <stop offset="0%" stop-color="#88a75f"/>
    <stop offset="100%" stop-color="#456c36"/>
  </linearGradient>
  <linearGradient id="mountainG" x1="0%" y1="0%" x2="0%" y2="100%">
    <stop offset="0%" stop-color="#9b927f"/>
    <stop offset="100%" stop-color="#5e574d"/>
  </linearGradient>
  <filter id="shadow"><feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="rgba(0,0,0,0.4)"/></filter>
  <filter id="softShadow"><feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="rgba(0,0,0,0.25)"/></filter>
  <filter id="textShadow"><feDropShadow dx="0" dy="1" stdDeviation="1.5" flood-color="rgba(0,0,0,0.7)"/></filter>
</defs>

<rect width="1200" height="390" fill="url(#grassG)"/>
<path d="M0,105 C125,45 270,72 390,112 C530,158 655,84 805,116 C970,151 1058,88 1200,112 L1200,390 L0,390 Z" fill="url(#hillG)" opacity="0.62"/>
<path d="M0,282 C140,232 250,276 384,248 C552,214 650,272 820,235 C974,201 1070,242 1200,214 L1200,390 L0,390 Z" fill="#436d35" opacity="0.44"/>
<path d="M50,176 C168,134 250,164 346,139 C466,108 548,150 640,126 C746,100 830,138 930,118 C1038,97 1115,121 1195,97" fill="none" stroke="#d9c89a" stroke-width="18" stroke-linecap="round" stroke-linejoin="round" opacity="0.65"/>
<path d="M238,324 C366,270 466,326 590,288 C720,250 842,304 1012,263" fill="none" stroke="#d9c89a" stroke-width="18" stroke-linecap="round" stroke-linejoin="round" opacity="0.65"/>
<path d="M590,130 L590,292" fill="none" stroke="#d9c89a" stroke-width="17" stroke-linecap="round" opacity="0.58"/>

<g filter="url(#softShadow)">
  <polygon points="945,150 1012,48 1082,150" fill="url(#mountainG)"/>
  <polygon points="1012,48 1082,150 1038,150" fill="#645d51"/>
  <polygon points="1048,160 1120,28 1192,160" fill="#756e60"/>
  <polygon points="1120,28 1192,160 1144,160" fill="#575147"/>
  <polygon points="850,154 908,74 966,154" fill="#a09884"/>
  <polygon points="908,74 966,154 930,154" fill="#696256"/>
  <polygon points="1002,62 1012,48 1027,71 1009,68" fill="#e8e1cf"/>
  <polygon points="1102,58 1120,28 1143,68 1115,62" fill="#e8e1cf"/>
</g>

<g transform="translate(52,128) scale(0.88)" filter="url(#shadow)" opacity="0.82">
  <rect x="0" y="24" width="42" height="72" fill="#75614b"/>
  <polygon points="-6,24 21,-8 48,24" fill="#4d3a2b"/>
  <rect x="14" y="57" width="14" height="39" fill="#2f241b"/>
  <rect x="9" y="32" width="7" height="11" fill="#f0cf8d"/>
  <rect x="26" y="32" width="7" height="11" fill="#f0cf8d"/>
</g>

<g transform="translate(1126,154) scale(0.72)" filter="url(#shadow)" opacity="0.76">
  <rect x="0" y="22" width="48" height="58" fill="#817261"/>
  <rect x="-8" y="12" width="16" height="68" fill="#6c5d4f"/>
  <rect x="40" y="12" width="16" height="68" fill="#6c5d4f"/>
  <polygon points="-10,12 0,-6 10,12" fill="#483629"/>
  <polygon points="38,12 48,-6 58,12" fill="#483629"/>
  <path d="M18,80 L18,52 Q24,45 30,52 L30,80 Z" fill="#2f241b"/>
</g>

<g filter="url(#shadow)">
  <g transform="translate(106,50)">
    <rect x="10" y="38" width="7" height="32" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="7" cy="29" r="15" fill="#244f2e"/>
      <circle class="tree-leaf" cx="18" cy="23" r="17" fill="#2f6a38"/>
      <circle class="tree-leaf" cx="25" cy="34" r="12" fill="#1f4729"/>
      <circle class="tree-leaf" cx="11" cy="39" r="12" fill="#315f35"/>
    </g>
    <rect x="41" y="45" width="7" height="29" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="37" cy="34" r="14" fill="#32673a"/>
      <circle class="tree-leaf" cx="50" cy="29" r="17" fill="#3d7a43"/>
      <circle class="tree-leaf" cx="57" cy="41" r="12" fill="#285a32"/>
      <circle class="tree-leaf" cx="42" cy="45" r="11" fill="#2d6538"/>
    </g>
    <rect x="68" y="38" width="8" height="36" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="63" cy="29" r="16" fill="#285a32"/>
      <circle class="tree-leaf" cx="76" cy="21" r="20" fill="#196333"/>
      <circle class="tree-leaf" cx="88" cy="33" r="15" fill="#2f743c"/>
      <circle class="tree-leaf" cx="72" cy="43" r="14" fill="#244f2e"/>
    </g>
  </g>
  <g transform="translate(1010,278)">
    <rect x="12" y="25" width="9" height="36" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="7" cy="17" r="17" fill="#1f5a30"/>
      <circle class="tree-leaf" cx="20" cy="8" r="21" fill="#26713a"/>
      <circle class="tree-leaf" cx="34" cy="20" r="15" fill="#1d6b37"/>
      <circle class="tree-leaf" cx="15" cy="31" r="13" fill="#285a32"/>
    </g>
    <rect x="47" y="14" width="8" height="48" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="39" cy="8" r="20" fill="#1d6b37"/>
      <circle class="tree-leaf" cx="55" cy="-3" r="25" fill="#13713b"/>
      <circle class="tree-leaf" cx="72" cy="11" r="18" fill="#238344"/>
      <circle class="tree-leaf" cx="50" cy="25" r="17" fill="#285a32"/>
    </g>
    <rect x="84" y="29" width="8" height="33" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="80" cy="20" r="15" fill="#2f743c"/>
      <circle class="tree-leaf" cx="92" cy="12" r="18" fill="#28803f"/>
      <circle class="tree-leaf" cx="103" cy="24" r="13" fill="#1f5a30"/>
      <circle class="tree-leaf" cx="87" cy="33" r="11" fill="#315f35"/>
    </g>
  </g>
  <g transform="translate(762,34)">
    <rect x="14" y="34" width="7" height="27" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="10" cy="25" r="13" fill="#315f35"/>
      <circle class="tree-leaf" cx="21" cy="18" r="16" fill="#3d7a43"/>
      <circle class="tree-leaf" cx="31" cy="28" r="11" fill="#285a32"/>
      <circle class="tree-leaf" cx="16" cy="36" r="10" fill="#244f2e"/>
    </g>
    <rect x="44" y="38" width="7" height="23" fill="#5b3b23"/>
    <g class="tree-crown">
      <circle class="tree-leaf" cx="40" cy="30" r="12" fill="#3a7040"/>
      <circle class="tree-leaf" cx="51" cy="24" r="15" fill="#2f743c"/>
      <circle class="tree-leaf" cx="59" cy="35" r="10" fill="#285a32"/>
      <circle class="tree-leaf" cx="45" cy="39" r="9" fill="#315f35"/>
    </g>
  </g>
</g>

<path d="M-40,218 C122,192 250,229 404,216 C580,199 720,236 896,217 C1034,202 1122,222 1240,204"
      fill="none" stroke="#2e5532" stroke-width="60" stroke-linecap="round" opacity="0.34"/>
<path d="M-40,224 C126,201 250,235 408,222 C580,208 720,243 898,225 C1032,212 1110,230 1240,214
         L1240,265 C1098,280 1018,257 894,271 C718,292 578,254 406,273 C246,290 132,258 -40,280 Z"
      fill="url(#waterG)" filter="url(#shadow)"/>
<path d="M-35,226 C130,202 252,236 408,223 C580,209 720,244 898,226 C1030,213 1112,231 1235,215"
      fill="none" stroke="#7fd2d4" stroke-width="5" stroke-linecap="round" opacity="0.32"/>
<path class="river-current" d="M-18,246 C142,223 252,252 404,239 C582,224 728,255 900,240 C1030,228 1112,242 1224,230" fill="none" stroke="#a8e2df" stroke-width="3" stroke-linecap="round" opacity="0.52"/>
<path class="water-detail" d="M118,246 Q168,233 220,246" fill="none" stroke="#cde7e5" stroke-width="3" stroke-linecap="round"/>
<path class="water-detail" d="M458,256 Q520,241 586,256" fill="none" stroke="#cde7e5" stroke-width="3" stroke-linecap="round"/>
<path class="water-detail" d="M824,246 Q884,233 948,246" fill="none" stroke="#cde7e5" stroke-width="3" stroke-linecap="round"/>

<g transform="translate(542,214) rotate(8)" filter="url(#shadow)">
    <rect x="0" y="0" width="82" height="54" fill="#604328" rx="3"/>
    <rect x="7" y="0" width="10" height="54" fill="#3e2c1d"/>
    <rect x="29" y="0" width="10" height="54" fill="#3e2c1d"/>
    <rect x="51" y="0" width="10" height="54" fill="#3e2c1d"/>
    <rect x="72" y="0" width="6" height="54" fill="#3e2c1d"/>
</g>

<g transform="translate(284,148)" opacity="0.62">
  <path d="M0,42 C34,8 75,2 118,35 C74,28 36,32 0,42 Z" fill="#8da95e"/>
  <path d="M570,48 C612,10 666,4 728,42 C664,35 614,38 570,48 Z" fill="#8da95e"/>
</g>
""" + buildPlacementGrid() + buildBuildingPins() + """
<g class="cloud" transform="translate(0, 38)">
    <circle cx="55" cy="30" r="20" fill="#f2ead7"/>
    <circle cx="84" cy="27" r="25" fill="#f2ead7"/>
    <circle cx="114" cy="34" r="16" fill="#f2ead7"/>
    <rect x="54" y="30" width="66" height="20" fill="#f2ead7"/>
</g>
<g class="cloud2" transform="translate(0, 92) scale(0.72)">
    <circle cx="52" cy="30" r="20" fill="#f2ead7"/>
    <circle cx="82" cy="30" r="25" fill="#f2ead7"/>
    <circle cx="112" cy="36" r="15" fill="#f2ead7"/>
    <rect x="52" y="30" width="62" height="20" fill="#f2ead7"/>
</g>
<g class="cloud3" transform="translate(0, 20) scale(0.58)">
    <circle cx="50" cy="32" r="18" fill="#f2ead7"/>
    <circle cx="77" cy="28" r="24" fill="#f2ead7"/>
    <circle cx="106" cy="35" r="15" fill="#f2ead7"/>
    <rect x="50" y="30" width="62" height="20" fill="#f2ead7"/>
</g>
</svg>
</body>
</html>
""";
    }

    private String buildPlacementGrid() {
        if (selectedBuilding == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
<g class="placement-grid">
  <text class="grid-hint" x="%d" y="%d">Wybierz parcelę dla: %s</text>
""", GRID_START_X, GRID_START_Y - 14, selectedBuilding.getName()));

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                String cell = cellKey(col, row);
                boolean blocked = !isBuildableCell(cell);
                boolean occupied = isCellOccupied(cell);
                boolean selected = cell.equals(selectedMapCell);
                String css = selected ? "selected" : blocked ? "blocked" : occupied ? "occupied" : "free";
                sb.append(String.format("""
  <rect class="grid-cell %s" x="%d" y="%d" width="%d" height="%d" rx="7"/>
""", css, gridCellX(col), gridCellY(row), GRID_CELL_WIDTH - 8, GRID_CELL_HEIGHT - 8));
            }
        }
        sb.append("</g>\n");
        return sb.toString();
    }

    private String buildBuildingPins() {
        StringBuilder sb = new StringBuilder();
        for (Building building : city.getBuildings()) {
            String className = building.getClass().getName();
            String cell = buildingCell(className);
            if (cell == null) continue;
            int x = cellCenterX(cell);
            int y = cellCenterY(cell);
            String[] style = buildingMapStyle(className, building.getName());
            sb.append(buildBuildingPin(style[0], x, y, style[1], style[2],
                    mapIconForClass(className), "pin", "1", "1.2"));
        }

        if (selectedBuilding != null && selectedMapCell != null && !isCellOccupied(selectedMapCell)) {
            String className = selectedBuilding.getClass().getName();
            int x = cellCenterX(selectedMapCell);
            int y = cellCenterY(selectedMapCell);
            String[] style = buildingMapStyle(className, selectedBuilding.getName());
            sb.append(buildBuildingPin("Plan: " + style[0], x, y, style[1], style[2],
                    mapIconForClass(className), "pin-ghost", "0.58", "1.15"));
        }

        return sb.toString();
    }

    private String buildBuildingPin(String name, int x, int y, String stroke, String fill,
                                    String icon, String cssClass, String opacity, String scale) {
        return String.format("""
<g class="%s" opacity="%s">
  <title>%s</title>
  <circle cx="%d" cy="%d" r="30" fill="%s" stroke="%s" stroke-width="3" filter="url(#shadow)"/>
  <circle cx="%d" cy="%d" r="21" fill="none" stroke="#fff7df" stroke-width="1" opacity="0.86"/>
  <g transform="translate(%d,%d) scale(%s)">%s</g>
</g>
""", cssClass, opacity, name, x, y, fill, stroke, x, y, x - 14, y - 14, scale, icon);
    }

    private String[] buildingMapStyle(String className, String name) {
        return switch (className) {
            case "pl.realmbuilder.buildings.Sawmill" -> new String[] {name, "#9a5b25", "#fff0ca"};
            case "pl.realmbuilder.buildings.Barracks" -> new String[] {name, "#8f2e25", "#f1d1bd"};
            case "pl.realmbuilder.buildings.Church" -> new String[] {name, "#718087", "#edf0e6"};
            case "pl.realmbuilder.buildings.Well" -> new String[] {name, "#2f667b", "#d8ecec"};
            case "pl.realmbuilder.buildings.Castle" -> new String[] {name, "#7d6b59", "#eee5d1"};
            case "pl.realmbuilder.buildings.Farm" -> new String[] {name, "#5f7f43", "#edf2d9"};
            case "pl.realmbuilder.buildings.Houses" -> new String[] {name, "#b8893d", "#fff0ca"};
            case "pl.realmbuilder.buildings.Market" -> new String[] {name, "#744a73", "#f0d9ef"};
            default -> new String[] {name, "#9b6d36", "#fff7df"};
        };
    }

    private String mapIconForClass(String className) {
        return switch (className) {
            case "pl.realmbuilder.buildings.Farm" -> iconFarm();
            case "pl.realmbuilder.buildings.Sawmill" -> iconSawmill();
            case "pl.realmbuilder.buildings.Houses" -> iconHouses();
            case "pl.realmbuilder.buildings.Market" -> iconMarket();
            case "pl.realmbuilder.buildings.Barracks" -> iconBarracks();
            case "pl.realmbuilder.buildings.Church" -> iconChurch();
            case "pl.realmbuilder.buildings.Well" -> iconWell();
            case "pl.realmbuilder.buildings.Castle" -> iconCastle();
            default -> "<circle cx='12' cy='12' r='8' fill='#b8893d'/>";
        };
    }

    private String iconFarm() {
        return "<g>" +
                "<path d='M12,2 C9,8 6,15 12,22 C18,15 15,8 12,2 Z' fill='#8bc34a'/>" +
                "<path d='M12,4 C9,4 8,8 8,14 C8,19 10,21 12,21 C14,21 16,19 16,14 C16,8 15,4 12,4 Z' fill='#ffeb3b'/>" +
                "<line x1='10' y1='5' x2='10' y2='20' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='12' y1='4' x2='12' y2='21' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='14' y1='5' x2='14' y2='20' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='8' y1='8' x2='16' y2='8' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='8' y1='11' x2='16' y2='11' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='8' y1='14' x2='16' y2='14' stroke='#f57f17' stroke-width='0.8'/>" +
                "<line x1='8' y1='17' x2='16' y2='17' stroke='#f57f17' stroke-width='0.8'/>" +
                "<path d='M12,22 C7,18 7,12 8,8 C9,14 11,18 12,22 Z' fill='#4caf50'/>" +
                "<path d='M12,22 C17,18 17,12 16,8 C15,14 13,18 12,22 Z' fill='#388e3c'/>" +
                "</g>";
    }

    private String iconSawmill() {
        return "<g>" +
                "<rect x='2' y='10' width='12' height='12' fill='#8d6e63' rx='1'/>" +
                "<polygon points='0,10 8,4 16,10' fill='#5d4037'/>" +
                "<circle cx='18' cy='17' r='5' fill='#b0bec5' stroke='#37474f' stroke-width='1'/>" +
                "<line x1='13' y1='17' x2='23' y2='17' stroke='#37474f'/>" +
                "<line x1='18' y1='12' x2='18' y2='22' stroke='#37474f'/>" +
                "<rect x='4' y='17' width='4' height='5' fill='#ffcc80' rx='1'/>" +
                "</g>";
    }

    private String iconBarracks() {
        return "<g>" +
                "<rect x='4' y='6' width='16' height='16' fill='#78909c'/>" +
                "<polygon points='2,6 5,6 5,3 8,3 8,6 11,6 11,3 14,3 14,6 17,6 17,3 20,3 20,6 22,6 22,8 2,8' fill='#546e7a'/>" +
                "<rect x='10' y='14' width='4' height='8' fill='#37474f' rx='2'/>" +
                "<circle cx='7' cy='11' r='1.5' fill='#ffff88'/>" +
                "<circle cx='17' cy='11' r='1.5' fill='#ffff88'/>" +
                "</g>";
    }

    private String iconChurch() {
        return "<g>" +
                "<rect x='4' y='10' width='16' height='12' fill='#cfd8dc' rx='1'/>" +
                "<polygon points='2,10 12,2 22,10' fill='#90a4ae'/>" +
                "<rect x='10' y='0' width='4' height='10' fill='#b0bec5'/>" +
                "<polygon points='9,2 12,-4 15,2' fill='#78909c'/>" +
                "<line x1='12' y1='-2' x2='12' y2='-8' stroke='#d4af37' stroke-width='1.5'/>" +
                "<line x1='9' y1='-5' x2='15' y2='-5' stroke='#d4af37' stroke-width='1.5'/>" +
                "<rect x='10' y='15' width='4' height='7' fill='#5d4037' rx='1'/>" +
                "</g>";
    }

    private String iconHouses() {
        return "<g>" +
                "<rect x='1' y='12' width='10' height='10' fill='#bcaaa4' rx='1'/>" +
                "<polygon points='0,12 6,6 12,12' fill='#a1887f'/>" +
                "<rect x='12' y='10' width='11' height='12' fill='#d7ccc8' rx='1'/>" +
                "<polygon points='11,10 17.5,3 24,10' fill='#b71c1c'/>" +
                "<rect x='20' y='1' width='2' height='4' fill='#37474f'/>" +
                "<rect x='4' y='16' width='3' height='6' fill='#5d4037'/>" +
                "<rect x='16' y='15' width='4' height='7' fill='#5d4037'/>" +
                "</g>";
    }

    private String iconMarket() {
        return "<g>" +
                "<rect x='3' y='12' width='18' height='10' fill='#ffcc80'/>" +
                "<path d='M2,6 L22,6 L20,12 L4,12 Z' fill='#e53935'/>" +
                "<path d='M5,6 L9,6 L11,12 L7,12 Z' fill='#ffffff'/>" +
                "<path d='M13,6 L17,6 L19,12 L15,12 Z' fill='#ffffff'/>" +
                "<line x1='4' y1='6' x2='4' y2='22' stroke='#5d4037' stroke-width='1.5'/>" +
                "<line x1='20' y1='6' x2='20' y2='22' stroke='#5d4037' stroke-width='1.5'/>" +
                "<rect x='8' y='16' width='5' height='5' fill='#ffb74d' stroke='#e65100'/>" +
                "</g>";
    }

    private String iconWell() {
        return "<g>" +
                "<ellipse cx='12' cy='19' rx='8' ry='4' fill='#90a4ae' stroke='#455a64' stroke-width='1'/>" +
                "<rect x='4' y='12' width='16' height='8' fill='#78909c' stroke='#455a64' stroke-width='1'/>" +
                "<ellipse cx='12' cy='12' rx='8' ry='4' fill='#cfd8dc' stroke='#455a64' stroke-width='1'/>" +
                "<ellipse cx='12' cy='12' rx='5' ry='2.2' fill='#4e8aa0'/>" +
                "<line x1='6' y1='9' x2='6' y2='4' stroke='#5d4037' stroke-width='1.5'/>" +
                "<line x1='18' y1='9' x2='18' y2='4' stroke='#5d4037' stroke-width='1.5'/>" +
                "<path d='M5,4 Q12,-1 19,4' fill='none' stroke='#5d4037' stroke-width='1.5'/>" +
                "<line x1='12' y1='3' x2='12' y2='9' stroke='#3e2723' stroke-width='1'/>" +
                "<rect x='10' y='8' width='4' height='4' fill='#8d6e63' rx='1'/>" +
                "</g>";
    }

    private String iconCastle() {
        return "<g>" +
                "<rect x='3' y='8' width='18' height='14' fill='#8d8271' stroke='#4e463d' stroke-width='1'/>" +
                "<rect x='1' y='5' width='5' height='17' fill='#6f6659' stroke='#4e463d' stroke-width='1'/>" +
                "<rect x='18' y='5' width='5' height='17' fill='#6f6659' stroke='#4e463d' stroke-width='1'/>" +
                "<rect x='3' y='3' width='3' height='4' fill='#6f6659'/>" +
                "<rect x='10' y='3' width='4' height='5' fill='#6f6659'/>" +
                "<rect x='18' y='3' width='3' height='4' fill='#6f6659'/>" +
                "<path d='M9,22 L9,15 Q12,12 15,15 L15,22 Z' fill='#3e2c1d'/>" +
                "<circle cx='7' cy='12' r='1.3' fill='#f5d27d'/>" +
                "<circle cx='17' cy='12' r='1.3' fill='#f5d27d'/>" +
                "</g>";
    }

    @FXML
    public void onNextTurn() {
        if (city.isGameOver()) return;
        turnProcessor.processTurn(city, availableBuildings.size(), event -> Platform.runLater(() -> showEventDialog(event)));
        Platform.runLater(() -> {
            updateUI();
            checkAchievements();
            if (city.isGameOver()) showEndScreen();
        });
    }

    @FXML
    public void onBuild() {
        if (selectedBuilding == null) { setStatus("⚠ Wybierz budynek z listy 'Dostępne budynki'."); return; }
        if (city.hasBuilding(selectedBuilding.getClass())) { setStatus("❌ Już posiadasz ten budynek!"); return; }
        if (selectedMapCell == null) { setStatus("⚠ Wybierz parcelę na mapie, a potem potwierdź budowę."); loadMap(); return; }
        if (!isBuildableCell(selectedMapCell)) { setStatus("❌ Ta parcela nie nadaje się pod budowę."); loadMap(); return; }
        if (isCellOccupied(selectedMapCell)) { setStatus("❌ Ta parcela jest już zajęta."); selectedMapCell = null; loadMap(); return; }
        if (!city.canAfford(selectedBuilding.getCost())) { setStatus("❌ Za mało złota!"); return; }

        city.subtractResource(ResourceType.GOLD, selectedBuilding.getCost());
        try {
            Building nb = (Building) selectedBuilding.getClass().getDeclaredConstructor().newInstance();
            city.addBuilding(nb);
            city.setBuildingPosition(nb.getClass().getName(), selectedMapCell);
            log("✅ Zbudowano: " + selectedBuilding.getName());
            setStatus("✅ Zbudowano " + selectedBuilding.getName());
        } catch (Exception e) { log("❌ Błąd budowy: " + e.getMessage()); }

        selectedBuilding = null;
        selectedMapCell = null;
        refreshUILists();
        loadMap();
        updateUI();
    }

    @FXML
    public void onUpgrade() {
        if (selectedOwnedBuilding == null) { setStatus("⚠ Wybierz budynek z listy 'Zbudowane', aby go ulepszyć."); return; }
        if (!city.canAfford(80)) { setStatus("❌ Za mało złota! Potrzebujesz 80💰"); return; }
        if (city.getResource(ResourceType.WOOD) < 30) { setStatus("❌ Za mało drewna! Potrzebujesz 30🌲"); return; }

        city.subtractResource(ResourceType.GOLD, 80);
        city.subtractResource(ResourceType.WOOD, 30);
        selectedOwnedBuilding.upgrade();

        log("⬆ Ulepszono " + selectedOwnedBuilding.getName() + " do poziomu " + selectedOwnedBuilding.getLevel());
        setStatus("⬆ Ulepszono " + selectedOwnedBuilding.getName());

        selectedOwnedBuilding = null;
        refreshUILists();
        updateUI();
    }

    @FXML
    public void onUseAdvisor() {
        if (selectedAdvisor == null) { setStatus("⚠ Kliknij kartę doradcy żeby go wybrać."); return; }
        if (!city.canAfford(selectedAdvisor.getCost())) { setStatus("❌ Za mało złota! Potrzebujesz " + selectedAdvisor.getCost() + "💰"); return; }

        city.subtractResource(ResourceType.GOLD, selectedAdvisor.getCost());

        // Dynamicznie dobieramy unikalną kwestię dla każdego doradcy
        String Dialog = "Mój miecz i tarcza są w gotowości, Władco!";

        switch (selectedAdvisor.getSpecialty()) {
            case TRADE ->
                    Dialog = "Interes to interes! Sprzedałem zapasy z magazynów i odzyskaliśmy złoto!";
            case MAGIC ->
                    Dialog = "Czuję przypływ starożytnej magii... Los osady wkrótce się odmieni.";
            case COMBAT ->
                    Dialog = "Odpędziliśmy zagrożenie! Granice Twojego królestwa są bezpieczne.";
        }

        narrator.narrateAdvisorAction(selectedAdvisor.getName(), Dialog);

        setStatus("🤝 " + selectedAdvisor.getName() + " użyty.");
        updateUI();
    }

    @FXML
    public void onSave() {
        boolean ok = saveManager.save(city);
        setStatus(ok ? "💾 Zapisano pomyślnie!" : "❌ Błąd zapisu.");
        log(ok ? "💾 Gra zapisana." : "❌ Nie udało się zapisać.");
    }

    @FXML
    public void onQuit() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Zakończ grę");
        a.setHeaderText("Czy na pewno chcesz zakończyć?");
        a.setContentText("Niezapisany postęp zostanie utracony.");
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { city.forceGameOver(); showEndScreen(); } });
    }

    public void updateUI() {
        cityLabel.setText("🏰 " + city.getName());
        turnLabel.setText("Tura: " + city.getCurrentTurn() + "/80");
        seasonLabel.setText(city.getCurrentSeason().getDisplayName());
        seasonBonusLabel.setText(seasonManager.getSeasonDescription(city.getCurrentSeason()));
        turnBar.setProgress((double) city.getCurrentTurn() / 80.0);

        goldLabel.setText(String.valueOf(city.getResource(ResourceType.GOLD)));
        foodLabel.setText(String.valueOf(city.getResource(ResourceType.FOOD)));
        woodLabel.setText(String.valueOf(city.getResource(ResourceType.WOOD)));
        int morale = city.getResource(ResourceType.MORALE);

        if(moraleLabel != null) moraleLabel.setText(String.valueOf(morale));
        populationLabel.setText(String.valueOf(city.getResource(ResourceType.POPULATION)));

        if (city.getResource(ResourceType.GOLD) < 50) {
            warningLabel.setText("⚠ Mało złota!");
            warningLabel.setVisible(true);
        } else if (morale < 25) {
            warningLabel.setText("⚠ Niskie morale!");
            warningLabel.setVisible(true);
        } else if (city.getResource(ResourceType.FOOD) < 30) {
            warningLabel.setText("⚠ Mało żywności!");
            warningLabel.setVisible(true);
        } else {
            warningLabel.setText("");
            warningLabel.setVisible(false);
        }
    }

    private void refreshUILists() {
        availableBuildingsBox.getChildren().clear();
        availableBuildings.stream().filter(b -> !city.hasBuilding(b.getClass())).forEach(b -> {
            VBox item = createBuildingItem(b, b.getName(), b.getCost() + "💰");
            item.setOnMouseClicked(e -> {
                clearSelections();
                item.getStyleClass().add("selected");
                selectedBuilding = b;
                setStatus("Wybrano " + b.getName() + ". Teraz kliknij parcelę na mapie.");
                loadMap();
            });
            availableBuildingsBox.getChildren().add(item);
        });

        ownedBuildingsBox.getChildren().clear();
        city.getBuildings().forEach(b -> {
            VBox item = createBuildingItem(b, b.getName() + " (Poz. " + b.getLevel() + ")", "80💰 30🌲");
            item.setOnMouseClicked(e -> {
                clearSelections();
                item.getStyleClass().add("selected-upgrade");
                selectedOwnedBuilding = b;
                setStatus("Wybrano do ulepszenia: " + b.getName());
                loadMap();
            });
            ownedBuildingsBox.getChildren().add(item);
        });
    }

    private VBox createBuildingItem(Building building, String title, String costText) {
        VBox item = new VBox(4);
        item.getStyleClass().add("building-item");

        HBox header = new HBox(7);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("building-item-header");

        Label icon = new Label(buildingIcon(building));
        icon.getStyleClass().add("building-item-icon");

        Label name = new Label(title);
        name.getStyleClass().add("building-item-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label cost = new Label(costText);
        cost.getStyleClass().add("building-item-cost");

        Label desc = new Label(building.getDescription());
        desc.getStyleClass().add("building-item-desc");
        desc.setWrapText(true);

        header.getChildren().addAll(icon, name, spacer, cost);
        item.getChildren().addAll(header, desc);
        return item;
    }

    private String buildingIcon(Building building) {
        return switch (building.getClass().getSimpleName()) {
            case "Farm" -> "🌾";
            case "Sawmill" -> "🪓";
            case "Houses" -> "🏘";
            case "Market" -> "⚖";
            case "Barracks" -> "🛡";
            case "Church" -> "⛪";
            case "Well" -> "💧";
            case "Castle" -> "🏰";
            default -> "◆";
        };
    }

    private void handleMapClick(double viewX, double viewY) {
        if (selectedBuilding == null) return;

        double scale = Math.max(mapView.getWidth() / MAP_WIDTH, mapView.getHeight() / MAP_HEIGHT);
        double renderedWidth = MAP_WIDTH * scale;
        double renderedHeight = MAP_HEIGHT * scale;
        double offsetX = (mapView.getWidth() - renderedWidth) / 2.0;
        double offsetY = (mapView.getHeight() - renderedHeight) / 2.0;
        double mapX = (viewX - offsetX) / scale;
        double mapY = (viewY - offsetY) / scale;

        int col = (int) ((mapX - GRID_START_X) / GRID_CELL_WIDTH);
        int row = (int) ((mapY - GRID_START_Y) / GRID_CELL_HEIGHT);
        if (col < 0 || col >= GRID_COLUMNS || row < 0 || row >= GRID_ROWS) {
            setStatus("Kliknij jedną z podświetlonych parcel.");
            return;
        }

        onMapCellSelected(col, row);
    }

    private void onMapCellSelected(int col, int row) {
        if (selectedBuilding == null) {
            setStatus("Najpierw wybierz budynek z listy.");
            return;
        }

        String cell = cellKey(col, row);
        if (!isBuildableCell(cell)) {
            setStatus("Ta parcela jest zablokowana przez teren.");
            return;
        }
        if (isCellOccupied(cell)) {
            setStatus("Ta parcela jest już zajęta.");
            return;
        }

        selectedMapCell = cell;
        setStatus("Parcela wybrana. Kliknij „Buduj wybrany”, aby potwierdzić.");
        loadMap();
    }

    private boolean isBuildableCell(String cell) {
        return !BLOCKED_GRID_CELLS.contains(cell);
    }

    private boolean isCellOccupied(String cell) {
        return city.getBuildings().stream()
                .map(b -> buildingCell(b.getClass().getName()))
                .anyMatch(cell::equals);
    }

    private String buildingCell(String className) {
        String savedCell = city.getBuildingPosition(className);
        if (savedCell != null) return savedCell;
        return DEFAULT_BUILDING_CELLS.get(className);
    }

    private String cellKey(int col, int row) {
        return col + ":" + row;
    }

    private int[] parseCell(String cell) {
        String[] parts = cell.split(":");
        return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private int gridCellX(int col) {
        return GRID_START_X + col * GRID_CELL_WIDTH;
    }

    private int gridCellY(int row) {
        return GRID_START_Y + row * GRID_CELL_HEIGHT;
    }

    private int cellCenterX(String cell) {
        int[] parts = parseCell(cell);
        return gridCellX(parts[0]) + (GRID_CELL_WIDTH - 8) / 2;
    }

    private int cellCenterY(String cell) {
        int[] parts = parseCell(cell);
        return gridCellY(parts[1]) + (GRID_CELL_HEIGHT - 8) / 2;
    }

    private void clearSelections() {
        availableBuildingsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected"));
        ownedBuildingsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected-upgrade"));
        selectedBuilding = null;
        selectedOwnedBuilding = null;
        selectedMapCell = null;
    }

    private void buildAdvisorCards() {
        advisorsBox.getChildren().clear();
        availableAdvisors.forEach(a -> {
            VBox card = new VBox(2);
            card.getStyleClass().add("advisor-card");

            String portrait = switch (a.getSpecialty()) {
                case COMBAT -> "🗡";
                case MAGIC -> "🔮";
                case TRADE -> "💼";
                default -> "👤";
            };

            Label ico = new Label(portrait); ico.getStyleClass().add("advisor-portrait");
            Label cost = new Label(a.getCost() + "💰"); cost.getStyleClass().add("advisor-cost");

            card.getChildren().addAll(ico, cost);
            card.setOnMouseClicked(e -> {
                advisorsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected"));
                card.getStyleClass().add("selected");
                selectedAdvisor = a;
                advisorQuoteLabel.setText(advisorQuote(a));
            });
            advisorsBox.getChildren().add(card);
        });
    }

    private String advisorQuote(Advisor advisor) {
        return switch (advisor.getSpecialty()) {
            case COMBAT -> "Aldric: „Mój miecz jest gotowy.”";
            case MAGIC -> "Morgana: „Czuję zmianę losu.”";
            case TRADE -> "Benedikt: „Złoto lubi porządek.”";
            default -> advisor.getName();
        };
    }

    private void showEventDialog(GameEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("event_dialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(App.getPrimaryStage());
            dialog.setTitle("⚡ " + event.getName());
            Scene scene = new Scene(loader.load(), 450, 360);
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            EventDialogController ctrl = loader.getController();
            ctrl.initDialog(event, city, availableAdvisors, turnProcessor, this);
            dialog.setScene(scene);
            dialog.showAndWait();
            updateUI();
            log("⚡ Zdarzenie: " + event.getName());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void checkAchievements() {
        achievementTracker.getUnlockedAchievements().forEach(a -> log("🏅 OSIĄGNIĘCIE: " + a.getName()));
    }

    private void showEndScreen() {
        try { App.showEndScreen(city, achievementTracker, rankCalculator); } catch (IOException e) { e.printStackTrace(); }
    }

    public void log(String msg) { Platform.runLater(() -> eventLog.appendText("[T" + city.getCurrentTurn() + "] " + msg + "\n")); }
    public void setStatus(String msg) { Platform.runLater(() -> statusLabel.setText(msg)); }
}
