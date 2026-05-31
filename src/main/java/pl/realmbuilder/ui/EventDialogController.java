package pl.realmbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import pl.realmbuilder.engine.TurnProcessor;
import pl.realmbuilder.interfaces.Advisor;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.util.List;
import java.util.stream.Collectors;

public class EventDialogController {

    @FXML private Label           eventTitleLabel;
    @FXML private Label           eventDescLabel;
    @FXML private ListView<String> advisorsList;
    @FXML private Label           resultLabel;

    private GameEvent      event;
    private City           city;
    private List<Advisor>  helpfulAdvisors;
    private GameController gameController;

    public void initDialog(GameEvent event,
                           City city,
                           List<Advisor> allAdvisors,
                           TurnProcessor turnProcessor,
                           GameController gameController) {
        this.event          = event;
        this.city           = city;
        this.gameController = gameController;

        eventTitleLabel.setText(
                (event.isNegative() ? "⚡ " : "🎉 ") + event.getName());
        eventDescLabel.setText(event.getDescription());

        helpfulAdvisors = turnProcessor.getHelpfulAdvisors(
                allAdvisors, event, city);

        if (helpfulAdvisors.isEmpty()) {
            advisorsList.getItems().add(
                    "Brak doradców którzy mogą pomóc...");
        } else {
            advisorsList.getItems().setAll(
                    helpfulAdvisors.stream()
                            .map(a -> a.getName()
                                    + "  —  " + a.getCost() + "💰"
                                    + "  |  " + a.getDescription())
                            .collect(Collectors.toList())
            );
        }
    }

    @FXML
    public void onUseAdvisor() {
        int idx = advisorsList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= helpfulAdvisors.size()) {
            resultLabel.setText("⚠ Wybierz doradcę z listy.");
            return;
        }

        Advisor chosen = helpfulAdvisors.get(idx);

        if (!city.canAfford(chosen.getCost())) {
            resultLabel.setText("❌ Za mało złota! Potrzebujesz "
                    + chosen.getCost() + "💰");
            return;
        }

        city.subtractResource(ResourceType.GOLD, chosen.getCost());
        chosen.help(city, event);
        resultLabel.setText("✅ " + chosen.getName() + " pomógł miastu!");
        gameController.log("🤝 " + chosen.getName()
                + " zareagował na zdarzenie: " + event.getName());
        gameController.updateUI();
    }

    @FXML
    public void onDismiss() {
        Stage stage = (Stage) eventTitleLabel.getScene().getWindow();
        stage.close();
    }
}