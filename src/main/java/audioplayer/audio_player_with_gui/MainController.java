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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

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

public class MainController implements Initializable {

    private double x = 0, y = 0;
    private ArrayList<File> songs = new ArrayList<>();
    private int songNumber;
    private final int[] speeds = {25,50,75,100,125,150,175,200};
    private double speed_rate = 1;
    private int play_pause_index = 0;

    private Media media;
    private Stage stage;
    private MediaPlayer mediaPlayer;
    String choice;

    @FXML
    private Label nameLabel, current_timeLabel, end_timeLabel, playlistNameLabel;
    @FXML
    private Button play_pauseButton, minimizeButton;
    @FXML
    private ComboBox<String> speedComboBox;
    @FXML
    private Slider volumeSlider, song_progressSlider;
    @FXML
    private ListView<String> playlistListView;
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

    public void switchToSelectingPage(ActionEvent event) throws IOException {
        mediaPlayer.stop();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SelectingWin.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    public void play_pause(){

        change_speed(null);

        if (play_pause_index == 0){
            mediaPlayer.play();
            playing();
            play_pauseButton.setText("⏸");
            play_pause_index = 1;
        } else if (play_pause_index == 1) {
            mediaPlayer.pause();
            play_pauseButton.setText("▶");
            play_pause_index = 0;
        }
        double durationInSeconds = media.getDuration().toSeconds();
        double minutes = (durationInSeconds % 3600) / 60;
        double seconds = durationInSeconds % 60;
        if (seconds == 0){
            end_timeLabel.setText((int)minutes+":00");
        } else if (seconds < 10) {
            end_timeLabel.setText((int)minutes+":0"+(int)seconds);
        }
        else {
            end_timeLabel.setText((int)minutes+":"+(int)seconds);
        }
    }

    public void next(){

        if (songNumber < songs.size()-1){
            songNumber++;
            mediaPlayer.stop();
            play_pause_index = 0;

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            nameLabel.setText(songs.get(songNumber).getName());

            change_speed(null);
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            play_pause();

            playlistListView.getSelectionModel().selectNext();
        }
        else {
            songNumber = 0;
            mediaPlayer.stop();
            play_pause_index = 0;

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            nameLabel.setText(songs.get(songNumber).getName());

            change_speed(null);
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            play_pause();

            playlistListView.getSelectionModel().selectFirst();
        }
    }

    public void last(ActionEvent event){

        if (songNumber > 0){
            songNumber--;
            mediaPlayer.stop();
            play_pause_index = 0;

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            nameLabel.setText(songs.get(songNumber).getName());

            change_speed(null);
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            play_pause();

            playlistListView.getSelectionModel().selectPrevious();
        }
        else {
            songNumber = songs.size()-1;
            mediaPlayer.stop();
            play_pause_index = 0;

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            nameLabel.setText(songs.get(songNumber).getName());

            change_speed(null);
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            play_pause();

            playlistListView.getSelectionModel().selectLast();
        }
    }

    public void change_speed(ActionEvent event){

        if (speedComboBox.getValue() == null){
            mediaPlayer.setRate(speed_rate);
        }
        else {
            speed_rate = Integer.parseInt(speedComboBox.getValue().substring(0,speedComboBox.getValue().length()-1))*0.01;
            mediaPlayer.setRate(speed_rate);
        }

    }

    public void progress_slider(){
        mediaPlayer.seek(Duration.seconds(song_progressSlider.getValue()*(media.getDuration().toSeconds()/100)));
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
            Statement statement = SC.statement;

            ArrayList<String> song_names = new ArrayList<>();
            playlistNameLabel.setText(choice);
            statement.executeUpdate("USE Playlists");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM "+choice);
            while (resultSet.next()){
                songs.add(new File(resultSet.getString("value")));
                song_names.add((new File(resultSet.getString("value"))).getName());
            }
            System.out.println(songs);
            playlistListView.getItems().addAll(song_names);

        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        nameLabel.setText(songs.get(songNumber).getName());

        for (int speed : speeds) {
            speedComboBox.getItems().add(speed + "%");
        }

        speedComboBox.setOnAction(this::change_speed);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {

                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);

            }
        });

        playlistListView.getSelectionModel().selectFirst();
        playlistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                String chosen_song = playlistListView.getSelectionModel().getSelectedItem();
                System.out.println(chosen_song);
                for (int i = 0; i <=songs.size(); i++){
                    if (chosen_song.equals(songs.get(i).getName())){
                        songNumber = i;
                        mediaPlayer.stop();
                        play_pause_index = 0;

                        media = new Media(songs.get(i).toURI().toString());
                        mediaPlayer = new MediaPlayer(media);

                        nameLabel.setText(songs.get(i).getName());

                        change_speed(null);
                        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
                        play_pause();
                        break;
                    }
                }

            }
        });

    }

    public void playing() {

        mediaPlayer.setOnReady(() -> {
            double durationInSeconds = media.getDuration().toSeconds();
            double minutes = (durationInSeconds % 3600) / 60;
            double seconds = durationInSeconds % 60;
            if (seconds == 0){
                end_timeLabel.setText((int)minutes+":00");
            } else if (seconds < 10) {
                end_timeLabel.setText((int)minutes+":0"+(int)seconds);
            }
            else {
                end_timeLabel.setText((int)minutes+":"+(int)seconds);
            }
        });
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                double current_time = mediaPlayer.getCurrentTime().toSeconds();
                double minutes = (current_time % 3600) / 60;
                double seconds = current_time % 60;
                if (seconds<10){
                    current_timeLabel.setText((int)minutes+":0"+(int)seconds);
                }
                else {
                    current_timeLabel.setText((int)minutes+":"+(int)seconds);
                }

                song_progressSlider.setValue(mediaPlayer.getCurrentTime().toSeconds()/media.getDuration().toSeconds()*100);
                System.out.println(mediaPlayer.getCurrentTime().toSeconds()/media.getDuration().toSeconds());
            }
        });
        mediaPlayer.setOnEndOfMedia(() -> next());
    }
}
