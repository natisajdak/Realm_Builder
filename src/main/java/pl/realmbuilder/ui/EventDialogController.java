package pl.realmbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private Label           advisorsTitleLabel;
    @FXML private ListView<String> advisorsList;
    @FXML private Button          useAdvisorButton;
    @FXML private Button          dismissButton;
    @FXML private Label           resultLabel;

    private GameEvent      event;
    private City           city;
    private List<Advisor>  helpfulAdvisors;
    private GameController gameController;
    private boolean        eventResolved;

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

        applyEventConsequences();
        helpfulAdvisors = turnProcessor.getHelpfulAdvisors(
                allAdvisors, event, city);

        if (helpfulAdvisors.isEmpty()) {
            advisorsTitleLabel.setVisible(false);
            advisorsTitleLabel.setManaged(false);
            advisorsList.setVisible(false);
            advisorsList.setManaged(false);
            useAdvisorButton.setVisible(false);
            useAdvisorButton.setManaged(false);
            dismissButton.setText(event.isNegative()
                    ? "Zaakceptuj konsekwencje"
                    : "Przyjmij rezultat");
            dismissButton.getStyleClass().setAll("btn-primary");
            resultLabel.setText(event.isNegative()
                    ? "Żaden dostępny doradca nie może teraz pomóc."
                    : "To zdarzenie nie wymaga pomocy doradców.");
        } else {
            advisorsList.getItems().setAll(
                    helpfulAdvisors.stream()
                            .map(a -> a.getName()
                                    + "  —  " + a.getCost() + "💰"
                                    + "  |  " + a.getDescription())
                            .collect(Collectors.toList())
            );
            advisorsList.getSelectionModel().selectFirst();
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
        gameController.log("🤝 " + chosen.getName()
                + " zareagował na zdarzenie: " + event.getName());
        gameController.updateUI();
        closeDialog();
    }

    @FXML
    public void onDismiss() {
        gameController.log((event.isNegative()
                ? "⚖ Przyjęto konsekwencje zdarzenia: "
                : "🎉 Przyjęto rezultat zdarzenia: ")
                + event.getName());
        gameController.updateUI();
        closeDialog();
    }

    private void applyEventConsequences() {
        if (eventResolved) return;
        event.apply(city);
        eventResolved = true;
    }

    private void closeDialog() {
        Stage stage = (Stage) eventTitleLabel.getScene().getWindow();
        stage.close();
    }
}
