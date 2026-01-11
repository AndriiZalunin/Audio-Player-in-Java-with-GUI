package audioplayer.audio_player_with_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try{
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SelectingWin.fxml")));
            Scene scene = new Scene(root);
            stage.setResizable(false);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            stage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("AudioPlayer");

            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }

}