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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

public class EditController implements Initializable {

    private Statement statement;
    private Stage stage;
    private double x = 0, y = 0;
    String choice;
    String selected_song = null;
    String selected_file = null;
    int selectedId;
    String path = "";
    ArrayList<String> list = new ArrayList<>();

    @FXML
    Label playlistnameLabel;
    @FXML
    ListView<String> playlistListView;
    @FXML
    TreeView<String> TreeViewList;
    @FXML
    TextField pathTextField;
    @FXML
    Button addButton, deleteButton, minimizeButton;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            File file = new File("name.txt");
            Scanner reader = new Scanner(file);
            choice = reader.nextLine();
            reader.close();
            file.delete();

            SelectionController SC = new SelectionController();
            statement = SC.statement;
            playlistListView.getItems().clear();
            playlistListView.refresh();
            ShowList();

        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        creatingTableView();

        playlistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                if (playlistListView.getSelectionModel().getSelectedItem() != null){
                    selected_song = playlistListView.getSelectionModel().getSelectedItem();
                    selectedId = playlistListView.getSelectionModel().getSelectedIndex();
                }
                deleteButton.setDisable(false);
                addButton.setDisable(true);
                //System.out.println(selected_song);
            }
        });

        TreeViewList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observableValue, TreeItem<String> stringTreeItem, TreeItem<String> t1) {

                if (TreeViewList.getSelectionModel().getSelectedItem() != null){

                    selected_file = TreeViewList.getSelectionModel().getSelectedItem().getValue();
                    if (selected_file.lastIndexOf(".") != -1){
                        String format = selected_file.substring(selected_file.lastIndexOf("."));
                        if (format.equals(".mp3") || format.equals(".wav")){

                            addButton.setDisable(false);
                            deleteButton.setDisable(true);
                        }
                    }
                    else {

                        addButton.setDisable(true);
                        deleteButton.setDisable(true);
                    }
                }
                //System.out.println(selected_file);

            }
        });

    }

    public void ShowList() throws SQLException {

        statement.executeUpdate("USE Playlists");

        playlistnameLabel.setText(choice);
        ResultSet resultSet = statement.executeQuery("SELECT value FROM "+choice);
        while (resultSet.next()){
            list.add(new File(resultSet.getString("value")).getName());
        }
        playlistListView.getItems().addAll(list);
    }

    public void creatingTableView(){
        TreeViewList.setRoot(null);

        File directory = new File(path);
        TreeItem<String> root = creatingItems(directory);
        TreeViewList.setRoot(root);
        root.setExpanded(true);
    }

    public TreeItem<String> creatingItems(File file){
        TreeItem<String> treeItems = new TreeItem<>(file.getName());
        if (file.isDirectory()){
            File[] files = file.listFiles();
            if (files != null){
                for (File branch : files){
                    treeItems.getChildren().addAll(creatingItems(branch));
                }
            }
        }
        return treeItems;
    }

    public void next(){

        path = (new File(pathTextField.getText()).getAbsolutePath());
        creatingTableView();

    }

    public void back(){

        path = (new File(pathTextField.getText()).getAbsolutePath());
        //System.out.println(path);
        int last_slash = path.lastIndexOf("\\");
        //System.out.println(last_slash);
        String address = path.substring(0,last_slash);
        if ((new File(address)).isDirectory()){
            pathTextField.setText(address);
            next();
            //System.out.println("next called");
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Directory doesn't exist");
            alert.setHeaderText("You can't move back, such directory doesn't exist.");
            alert.show();
        }

    }

    public void add() throws SQLException {

        File file = new File(path+"/"+selected_file);
        String address = file.getAbsolutePath().replace("\\","/");
        //System.out.println(address);
        if (file.isFile()){
            statement.executeUpdate("INSERT INTO "+choice+" (value) VALUES (\""+address+"\")");
            playlistListView.getItems().add(selected_file);
            playlistListView.refresh();
            //System.out.println("Song is added");
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File not found");
            alert.setHeaderText("The selected file must be in the specified directory.");
            alert.show();
        }
    }

    public void delete() throws SQLException {

        statement.executeUpdate("DELETE FROM "+choice+" WHERE value LIKE \"%"+selected_song+"\"");
        playlistListView.getItems().remove(selectedId);
        playlistListView.refresh();
        playlistListView.getSelectionModel().clearSelection();
        //System.out.println("Song is deleted");
    }

    public void done(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SelectingWin.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    public void mouseClick(MouseEvent event) throws SQLException {
        if (event.getClickCount() == 2){
            selected_file = TreeViewList.getSelectionModel().getSelectedItem().getValue();
            File file = new File(path+"/"+selected_file);
            if (file.isDirectory()){
                pathTextField.setText(file.getAbsolutePath());
                next();
            } else if (file.isFile()) {
                add();
            }
        }
    }
}
