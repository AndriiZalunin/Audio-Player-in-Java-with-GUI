package audioplayer.audio_player_with_gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class SelectionController implements Initializable {

    private double x = 0, y = 0;
    String choice = null;
    private Stage stage;
    private Scene scene;
    private Parent root;
    public final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/","root","password");
    public final Statement statement = connection.createStatement();

    public SelectionController() throws SQLException {
    }

    @FXML
    ListView<String> playlistListView, musicListView;

    @FXML
    Label messageLabel;

    @FXML
    Button minimizeButton;

    @FXML
    AnchorPane anchorpane;

    public void close(){
        System.exit(0);
    }

    public void minimize(){
        ((Stage) minimizeButton.getScene().getWindow()).setIconified(true);
    }

    public void pressed(MouseEvent event){
        x = event.getSceneX();
        y = event.getSceneY();
    }

    public void dragged(MouseEvent event){
        stage = (Stage) anchorpane.getScene().getWindow();
        stage.setX(event.getScreenX() - x);
        stage.setY(event.getScreenY() - y);
    }

    public void create(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("CreatingWin.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    public void delete() throws SQLException {

        if (choice == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Choose a playlist");
            alert.setHeaderText("Choose a playlist you want to delete.");
            alert.show();
        }
        else {
            statement.execute("DROP TABLE "+choice);
            playlistListView.getItems().clear();
            playlistListView.refresh();
            playlistListView.getItems().addAll(ShowTables());
            musicListView.setVisible(false);
        }
    }

    public void edit(ActionEvent event) throws IOException {

        if (choice == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Choose a playlist");
            alert.setHeaderText("Choose a playlist you want to edit.");
            alert.show();
        }
        else {

            FileWriter writer = new FileWriter("name.txt");
            writer.write(choice);
            writer.close();


            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("EditingWin.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        }
    }

    public void select(ActionEvent event) throws IOException {

        if (choice == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Choose a playlist");
            alert.setHeaderText("Choose a playlist you want to open.");
            alert.show();
        }
        else {

            FileWriter writer = new FileWriter("name.txt");
            writer.write(choice);
            writer.close();


            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainWin.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            playlistListView.getItems().addAll(ShowTables());
            playlistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                    choice = playlistListView.getSelectionModel().getSelectedItem();
                    musicListView.setVisible(true);
                    musicListView.getItems().clear();
                    musicListView.refresh();
                    ArrayList<String> list = new ArrayList<>();
                    try {
                        ResultSet resultSet = statement.executeQuery("SELECT value FROM "+choice);
                        int i = 1;
                        while (resultSet.next()){
                            list.add(i+". "+ new File(resultSet.getString("value")).getName());
                            i++;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    musicListView.getItems().addAll(list);

                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public ArrayList<String> ShowTables() throws SQLException {

        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS Playlists");
        statement.executeUpdate("USE Playlists");


        ResultSet resultSet = statement.executeQuery("SHOW TABLES FROM Playlists");
        ArrayList<String> list = new ArrayList<>();
        if (resultSet.isBeforeFirst()){
            messageLabel.setText("Playlists available: ");
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            return list;
        }
        else {
            messageLabel.setText("You don't have any playlists.");
            return list;
        }
    }
}
