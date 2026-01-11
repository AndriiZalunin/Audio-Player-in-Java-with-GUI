package audioplayer.audio_player_with_gui;

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
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class CreationController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;
    String path = "";
    String name = "";
    Statement statement;
    ArrayList<String> selected_files = new ArrayList<>();
    private double x = 0, y = 0;

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField pathTextField;
    @FXML
    private TreeView<String> TreeViewList;
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

    public void switchToSelectingWin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SelectingWin.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        creatingTableView();

        try {

            SelectionController SC = new SelectionController();
            statement = SC.statement;
            statement.executeUpdate("USE Playlists");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void next() throws SQLException {

        name = nameTextField.getText();
        path = pathTextField.getText();
        System.out.println(path);
        creatingTableView();

        if (!name.isEmpty()){
            namechecking();
        }

    }

    public void back() throws SQLException {

        String path = (new File(pathTextField.getText()).getAbsolutePath());
        //System.out.println(path);
        int last_slash = path.lastIndexOf("\\");
        //System.out.println(last_slash);
        String address = path.substring(0,last_slash);
        if ((new File(address)).isDirectory()){
            pathTextField.setText(address);
            next();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Directory doesn't exist");
            alert.setHeaderText("You can't move back, such directory doesn't exist.");
            alert.show();
        }

    }

    public void selectItem(MouseEvent event) throws SQLException {

        if (event.getClickCount() == 1){
            TreeItem<String> item = TreeViewList.getSelectionModel().getSelectedItem();
            if (item != null){
                String filename = item.getValue();
                File file = new File(path+"/"+filename);
                if (file.isFile()){
                    String format = filename.substring(filename.lastIndexOf("."));
                    if (format.equals(".mp3") || format.equals(".wav")){
                        selected_files.add(file.getAbsolutePath().replace("\\","/"));
                        //System.out.println(filename+" is a audio file");
                    }
                }
            }
        }
        else if (event.getClickCount() == 2){
            String selected_file = TreeViewList.getSelectionModel().getSelectedItem().getValue();
            File file = new File(path+"/"+selected_file);
            if (file.isDirectory()){
                pathTextField.setText(file.getAbsolutePath());
                next();
            }
        }
    }

    public void create(ActionEvent event) throws SQLException, IOException {

        name = nameTextField.getText();
        if (conditions_checking()){
            //System.out.println("creating...");
            statement.executeUpdate("CREATE TABLE "+name+" (id INT PRIMARY KEY AUTO_INCREMENT, value varchar(100));");
            for (int i = 0; i < selected_files.size();i++){
                statement.executeUpdate("INSERT INTO "+name+" (value) VALUES (\""+selected_files.get(i)+"\")");
            }
            switchToSelectingWin(event);
        }

    }

    public boolean conditions_checking() throws SQLException {

        boolean checked;
        checked = namechecking();

        if ((path.isEmpty()) || (selected_files.isEmpty())){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Empty playlist");
            alert.setHeaderText("Choose a directory with your music and select some files.");
            alert.show();
            checked = false;
        }
        //System.out.println("checked in condition_checking: "+checked);
        //System.out.println("Path is: "+path);
        //System.out.println(selected_files);
        return checked;
    }

    public boolean namechecking() throws SQLException {
        boolean checked =true;
        System.out.println("name is: "+name);
        if (name.isBlank()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Enter a name");
            alert.setHeaderText("Please enter a name for your playlist.");
            alert.show();
            checked = false;
        }
        else {
            ResultSet resultSet = statement.executeQuery("SHOW TABLES FROM Playlists WHERE Tables_in_Playlists = \""+name+"\";");
            if (resultSet.next()){
                if (resultSet.getString(1).equals(name)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Name is taken");
                    alert.setHeaderText("You already have a playlist with the same name. Please choose another one.");
                    alert.show();
                    name = null;
                    nameTextField.clear();
                    checked = false;
                }
            }
        }
        //System.out.println("checked in namechecking: "+checked);
        return checked;
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
}
