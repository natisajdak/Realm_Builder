package pl.realmbuilder.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import pl.realmbuilder.engine.SaveManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML private TextField    cityNameField;
    @FXML private ToggleButton easyBtn;
    @FXML private ToggleButton normalBtn;
    @FXML private ToggleButton hardBtn;
    @FXML private Label        errorLabel;

    private final ToggleGroup  difficultyGroup = new ToggleGroup();
    private final SaveManager  saveManager     = new SaveManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        easyBtn.setToggleGroup(difficultyGroup);
        normalBtn.setToggleGroup(difficultyGroup);
        hardBtn.setToggleGroup(difficultyGroup);
        normalBtn.setSelected(true);
    }

    @FXML
    public void onStartGame() {
        String cityName = cityNameField.getText().trim();
        if (cityName.isEmpty()) {
            errorLabel.setText("⚠ Podaj nazwę miasta!");
            return;
        }
        String difficulty = getDifficulty();
        try {
            App.showGame(cityName, difficulty);
        } catch (Exception e) {
            errorLabel.setText("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onLoadGame() {
        if (!saveManager.saveExists()) {
            errorLabel.setText("⚠ Nie znaleziono pliku zapisu.");
            return;
        }
        try {
            App.showGame(null, null);
        } catch (Exception e) {
            errorLabel.setText("Błąd wczytywania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getDifficulty() {
        if (easyBtn.isSelected())  return "EASY";
        if (hardBtn.isSelected())  return "HARD";
        return "NORMAL";
    }
}