package pl.realmbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import pl.realmbuilder.engine.AchievementTracker;
import pl.realmbuilder.engine.RankCalculator;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.io.IOException;
import java.util.stream.Collectors;

public class EndScreenController {

    @FXML private Label            resultLabel;
    @FXML private Label            rankLabel;
    @FXML private Label            rankDescLabel;
    @FXML private Label            scoreLabel;
    @FXML private Label            turnsLabel;
    @FXML private Label            goldLabel;
    @FXML private Label            popLabel;
    @FXML private Label            moraleLabel;
    @FXML private ListView<String> achievementsList;

    public void initEndScreen(City city,
                              AchievementTracker tracker,
                              RankCalculator rankCalc) {
        // wynik gry
        if (city.isPlayerWon()) {
            resultLabel.setText("🏆 WYGRANA!");
            resultLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;"
                    + " -fx-text-fill: #e2b96f;");
        } else {
            resultLabel.setText("💀 PRZEGRANA");
            resultLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;"
                    + " -fx-text-fill: #ff6666;");
        }

        // ranga
        RankCalculator.Rank rank = rankCalc.calculateRank(city);
        rankLabel.setText(rank.getDisplayName());
        rankDescLabel.setText(rank.getDescription());
        scoreLabel.setText("Wynik: " + rankCalc.calculateScore(city) + " pkt");

        // statystyki
        turnsLabel.setText(String.valueOf(city.getCurrentTurn() - 1));
        goldLabel.setText(String.valueOf(city.getResource(ResourceType.GOLD)));
        popLabel.setText(
                String.valueOf(city.getResource(ResourceType.POPULATION)));
        moraleLabel.setText(
                String.valueOf(city.getResource(ResourceType.MORALE)));

        // osiągnięcia
        if (tracker.getUnlockedAchievements().isEmpty()) {
            achievementsList.getItems().add("Brak odblokowanych osiągnięć");
        } else {
            achievementsList.getItems().setAll(
                    tracker.getUnlockedAchievements().stream()
                            .map(a -> "🏅 " + a.getName()
                                    + " — " + a.getDescription())
                            .collect(Collectors.toList())
            );
        }
    }

    @FXML
    public void onBackToMenu() {
        try {
            App.showMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}