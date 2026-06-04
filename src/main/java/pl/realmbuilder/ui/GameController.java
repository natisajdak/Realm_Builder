package pl.realmbuilder.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.util.ResourceBundle;

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
  html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:#2c1e16; }
  svg { display:block; width:100vw; height:100vh; object-fit: contain; }
  @keyframes floatRight { 0% { transform: translateX(-150px); } 100% { transform: translateX(950px); } }
  .cloud { animation: floatRight 40s linear infinite; opacity: 0.8; }
  .cloud2 { animation: floatRight 60s linear infinite; animation-delay: -20s; opacity: 0.6; }
  @keyframes ripple { 0%, 100% { transform: scale(1); opacity:0.3; } 50% { transform: scale(1.05); opacity:0.1; } }
  .water-detail { animation: ripple 4s infinite ease-in-out; transform-box: fill-box; transform-origin: center; }
  @keyframes gentleHover { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }
  .pin { animation: gentleHover 4s infinite ease-in-out; transform-box: fill-box; transform-origin: center; }
</style>
</head>
<body>
<svg viewBox="0 0 800 450" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg">
<defs>
  <linearGradient id="grassG" x1="0%" y1="0%" x2="0%" y2="100%">
    <stop offset="0%" stop-color="#7cb342"/>
    <stop offset="100%" stop-color="#558b2f"/>
  </linearGradient>
  <linearGradient id="waterG" x1="0%" y1="0%" x2="100%" y2="0%">
    <stop offset="0%" stop-color="#29b6f6"/>
    <stop offset="50%" stop-color="#0288d1"/>
    <stop offset="100%" stop-color="#29b6f6"/>
  </linearGradient>
  <filter id="shadow"><feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="rgba(0,0,0,0.4)"/></filter>
</defs>

<rect width="800" height="450" fill="url(#grassG)"/>
<polygon points="650,150 720,40 790,150" fill="#78909c" />
<polygon points="720,40 790,150 750,150" fill="#546e7a" />
<polygon points="730,160 800,20 870,160" fill="#607d8b" />
<polygon points="580,160 630,70 680,160" fill="#90a4ae" />

<path d="M200,160 C300,200 350,150 400,160 C500,180 600,140 680,160 M200,380 C300,340 350,390 400,380 C500,360 600,400 680,380 M400,160 L400,380" 
      fill="none" stroke="#d7ccc8" stroke-width="25" stroke-linecap="round" stroke-linejoin="round" opacity="0.6"/>

<path d="M-20,250 C150,220 250,290 400,260 C550,230 650,280 820,250 L820,310 C650,340 550,290 400,320 C250,350 150,280 -20,310 Z" 
      fill="url(#waterG)" filter="url(#shadow)"/>
<path class="water-detail" d="M100,260 Q150,240 200,260" fill="none" stroke="#e1f5fe" stroke-width="3" stroke-linecap="round"/>
<path class="water-detail" d="M500,290 Q550,270 600,290" fill="none" stroke="#e1f5fe" stroke-width="3" stroke-linecap="round"/>

<g transform="translate(370,245) rotate(10)" filter="url(#shadow)">
    <rect x="0" y="0" width="70" height="75" fill="#5d4037" rx="3"/>
    <rect x="5" y="0" width="10" height="75" fill="#4e342e"/>
    <rect x="25" y="0" width="10" height="75" fill="#4e342e"/>
    <rect x="45" y="0" width="10" height="75" fill="#4e342e"/>
</g>

<g filter="url(#shadow)">
    <circle cx="50" cy="50" r="30" fill="#2e7d32"/>
    <circle cx="90" cy="40" r="25" fill="#388e3c"/>
    <circle cx="40" cy="90" r="25" fill="#1b5e20"/>
    <circle cx="750" cy="420" r="35" fill="#2e7d32"/>
    <circle cx="700" cy="450" r="25" fill="#1b5e20"/>
    <circle cx="780" cy="380" r="30" fill="#388e3c"/>
</g>

<g class="cloud" transform="translate(0, 50)">
    <circle cx="50" cy="30" r="20" fill="#ffffff"/>
    <circle cx="80" cy="30" r="25" fill="#ffffff"/>
    <circle cx="110" cy="35" r="15" fill="#ffffff"/>
    <rect x="50" y="30" width="60" height="20" fill="#ffffff"/>
</g>
<g class="cloud2" transform="translate(0, 120) scale(0.7)">
    <circle cx="50" cy="30" r="20" fill="#ffffff"/>
    <circle cx="80" cy="30" r="25" fill="#ffffff"/>
    <circle cx="110" cy="35" r="15" fill="#ffffff"/>
    <rect x="50" y="30" width="60" height="20" fill="#ffffff"/>
</g>
""" + buildBuildingPins() + """
</svg>
</body>
</html>
""";
    }

    private String buildBuildingPins() {
        StringBuilder sb = new StringBuilder();
        Object[][] slots = {
                {"pl.realmbuilder.buildings.Sawmill",  200, 120, "#e67e22", "#d35400", iconSawmill()},
                {"pl.realmbuilder.buildings.Barracks", 400, 100, "#c0392b", "#922b21", iconBarracks()},
                {"pl.realmbuilder.buildings.Church",   650, 120, "#bdc3c7", "#7f8c8d", iconChurch()},
                {"pl.realmbuilder.buildings.Farm",     200, 370, "#27ae60", "#1e8449", iconFarm()},
                {"pl.realmbuilder.buildings.Houses",   400, 370, "#f1c40f", "#b7950b", iconHouses()},
                {"pl.realmbuilder.buildings.Market",   620, 370, "#9b59b6", "#76448a", iconMarket()},
        };

        for (Object[] slot : slots) {
            String className  = (String) slot[0];
            int    x          = (int)    slot[1];
            int    y          = (int)    slot[2];
            String stroke     = (String) slot[3];
            String icon       = (String) slot[5];

            boolean owned = city.getBuildings().stream().anyMatch(b -> b.getClass().getName().equals(className));
            if (owned) {
                sb.append(String.format("""
<g class="pin">
  <circle cx="%d" cy="%d" r="28" fill="#fffdf9" stroke="%s" stroke-width="3" filter="url(#shadow)"/>
  <g transform="translate(%d,%d) scale(1.2)">%s</g>
</g>
""", x, y, stroke, x - 14, y - 14, icon));
            }
        }
        return sb.toString();
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

    @FXML
    public void onNextTurn() {
        if (city.isGameOver()) return;
        turnProcessor.processTurn(city, availableBuildings.size(), event -> Platform.runLater(() -> showEventDialog(event)));
        Platform.runLater(() -> {
            updateUI();
            loadMap();
            checkAchievements();
            if (city.isGameOver()) showEndScreen();
        });
    }

    @FXML
    public void onBuild() {
        if (selectedBuilding == null) { setStatus("⚠ Wybierz budynek z listy 'Dostępne budynki'."); return; }
        if (city.hasBuilding(selectedBuilding.getClass())) { setStatus("❌ Już posiadasz ten budynek!"); return; }
        if (!city.canAfford(selectedBuilding.getCost())) { setStatus("❌ Za mało złota!"); return; }

        city.subtractResource(ResourceType.GOLD, selectedBuilding.getCost());
        try {
            Building nb = (Building) selectedBuilding.getClass().getDeclaredConstructor().newInstance();
            city.addBuilding(nb);
            log("✅ Zbudowano: " + selectedBuilding.getName());
            setStatus("✅ Zbudowano " + selectedBuilding.getName());
        } catch (Exception e) { log("❌ Błąd budowy: " + e.getMessage()); }

        selectedBuilding = null;
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
            VBox item = new VBox(2);
            item.getStyleClass().add("building-item");
            Label name = new Label(b.getName()); name.getStyleClass().add("building-item-name");
            Label cost = new Label(b.getCost() + "💰"); cost.getStyleClass().add("building-item-cost");
            Label desc = new Label(b.getDescription()); desc.getStyleClass().add("building-item-desc"); desc.setWrapText(true);

            item.getChildren().addAll(name, cost, desc);
            item.setOnMouseClicked(e -> {
                clearSelections();
                item.getStyleClass().add("selected");
                selectedBuilding = b;
                setStatus("Wybrano do budowy: " + b.getName());
            });
            availableBuildingsBox.getChildren().add(item);
        });

        ownedBuildingsBox.getChildren().clear();
        city.getBuildings().forEach(b -> {
            VBox item = new VBox(2);
            item.getStyleClass().add("building-item");
            Label name = new Label(b.getName() + " (Poz. " + b.getLevel() + ")"); name.getStyleClass().add("building-item-name");
            Label cost = new Label("Ulepszenie: 80💰 30🌲"); cost.getStyleClass().add("building-item-cost");
            Label desc = new Label(b.getDescription()); desc.getStyleClass().add("building-item-desc"); desc.setWrapText(true);

            item.getChildren().addAll(name, cost, desc);
            item.setOnMouseClicked(e -> {
                clearSelections();
                item.getStyleClass().add("selected-upgrade");
                selectedOwnedBuilding = b;
                setStatus("Wybrano do ulepszenia: " + b.getName());
            });
            ownedBuildingsBox.getChildren().add(item);
        });
    }

    private void clearSelections() {
        availableBuildingsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected"));
        ownedBuildingsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected-upgrade"));
        selectedBuilding = null;
        selectedOwnedBuilding = null;
    }

    private void buildAdvisorCards() {
        advisorsBox.getChildren().clear();
        String[] quotes = { "Rycerz: „Mój miecz gotowy!", "Morgana: „Czuję magię...", "Benedikt: „Złoto to potęga!" };
        int[] qi = {0};
        availableAdvisors.forEach(a -> {
            VBox card = new VBox(2);
            card.getStyleClass().add("advisor-card");

            String portrait = switch (a.getSpecialty()) {
                case COMBAT -> "🗡";
                case MAGIC -> "💼";
                case TRADE -> "🔮";
                default -> "👤";
            };

            Label ico = new Label(portrait); ico.getStyleClass().add("advisor-portrait");
            Label cost = new Label(a.getCost() + "💰"); cost.getStyleClass().add("advisor-cost");

            card.getChildren().addAll(ico, cost);
            int qIdx = qi[0]++;
            card.setOnMouseClicked(e -> {
                advisorsBox.getChildren().forEach(c -> c.getStyleClass().remove("selected"));
                card.getStyleClass().add("selected");
                selectedAdvisor = a;
                advisorQuoteLabel.setText(qIdx < quotes.length ? quotes[qIdx] : "");
            });
            advisorsBox.getChildren().add(card);
        });
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