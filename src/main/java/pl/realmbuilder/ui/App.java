package pl.realmbuilder.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Realm Builder");
        primaryStage.setResizable(false);
        showMenu();
        primaryStage.show();
    }

    public static void showMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/pl/realmbuilder/ui/menu.fxml"));
        Scene scene = new Scene(loader.load(), 600, 450);
        scene.getStylesheets().add(
                App.class.getResource("/pl/realmbuilder/ui/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showGame(String cityName,
                                String difficulty) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/pl/realmbuilder/ui/game.fxml"));
        Scene scene = new Scene(loader.load(), 1120, 720);
        scene.getStylesheets().add(
                App.class.getResource("/pl/realmbuilder/ui/style.css").toExternalForm());
        GameController controller = loader.getController();
        controller.initGame(cityName, difficulty);
        primaryStage.setScene(scene);
    }

    public static void showEndScreen(pl.realmbuilder.model.City city,
                                     pl.realmbuilder.engine.AchievementTracker tracker,
                                     pl.realmbuilder.engine.RankCalculator rank)
            throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/pl/realmbuilder/ui/end_screen.fxml"));
        Scene scene = new Scene(loader.load(), 700, 580);
        scene.getStylesheets().add(
                App.class.getResource("/pl/realmbuilder/ui/style.css").toExternalForm());
        EndScreenController controller = loader.getController();
        controller.initEndScreen(city, tracker, rank);
        primaryStage.setScene(scene);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
