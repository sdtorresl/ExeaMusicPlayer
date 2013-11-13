/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.io.File;
import java.net.MalformedURLException;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
//import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 *
 * @author sdtorresl
 */
public class PlayerController implements Initializable {
    
    private static String MEDIA_URL = 
            System.getProperty("user.home")+"/Music/AC_DC/Hits/08 - T.N.T..mp3";
    private static String RESCUE_URL = "http://stream";
    //private FileOutputStream file;
    private static Media media;
    private MediaPlayer mediaPlayer;
    
    @FXML
    public Label tittle, artist, album;
    public ProgressBar progress;
    public Button playPauseButton, playBackupButton, backupButton;
    public Slider volumeSlider;
    
    /** 
     * Makes an animation that reduces the opacity of an element and 
     * restores it at a specific time.
     * 
     * @param node 
     */
    public void fade(Node node) {
        FadeTransition fadeTransition = 
                new FadeTransition(Duration.seconds(0.3), node);
        fadeTransition.setFromValue(1.0f);
        fadeTransition.setToValue(0.3f);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }
    
    /** 
     * Set the metadata of the current media source in the information 
     * labels of the player.
     */
    public void setMetadata() {
        //Get metadata from media
        String artistLabel = (String) media.getMetadata().get("artist");
        String tittleLabel = (String) media.getMetadata().get("title");
        //System.out.println(albumCover.toString());
        /*if(artistLabel.equals(""))
            artistLabel = "Desconocido";
        
        if(albumLabel.equals(""))
            albumLabel = "Desconocido";
        
        if(artistLabel.equals(""))
            albumLabel = "Desconocido";*/
        
        //Set metadata values
        tittle.setText(tittleLabel);
        artist.setText(artistLabel);
      
        //coverImage.getImage();
    }
    
    @FXML
    public void playPauseButtonClicked(ActionEvent event) {
        setMetadata();
        
        mediaPlayer.play();
        fade(playPauseButton);
    }
   
    @FXML
    public void playBackupButtonClicked(ActionEvent event) {
        setMetadata();
        fade(playBackupButton);
    }
    
    @FXML
    public void muteButtonClicked(ActionEvent event) {
        setMetadata();
        fade(playBackupButton);
    }
    
    @FXML
    public void volumeChanged(ActionEvent event) {
        tittle.setText(Double.toString(volumeSlider.getValue()));
    }
    
    @FXML
    public int downloadBackup(ActionEvent event) {
        /*HttpGet httpGet = new HttpGet(this.remoteUrl);
        HttpResponse response = httpClient.execute(httpGet);*/
        fade(backupButton);
        String pathToSave = saveFile();
        
        if(pathToSave.equals("")) {
            System.out.println("No path to save the file");
            return -1;
        }
        return 0;
    }
        
    @FXML
    public void pauseButtonClicked(ActionEvent event) {
        tittle.setText("");
        artist.setText("");
        album.setText("");
        mediaPlayer.pause();
        fade(pauseButton);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Create media player
        File audioFile = new File(MEDIA_URL);
        media = null;
        
        try {
            MEDIA_URL = audioFile.toURI().toURL().toString();
            media = new Media(MEDIA_URL);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
        }
       
        tittle.setText(Double.toString(volumeSlider.getValue()));
    }    
    
    public String saveFile(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar respaldo");
        //chooser.setInitialDirectory(new File(System.getProperty("user.Home")));
        chooser.setInitialFileName("Respaldo.mp3");
        //select.setFileFilter(new FileNameExtensionFilter("Archivos de música *.mp3"));
        
        try {
            String a = chooser.showSaveDialog(null).getAbsolutePath();
            return a;
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /*
    public FileInputStream getFile(){
        JFileChooser select = new JFileChooser();
        int a = select.showOpenDialog(null);
        File file = select.getSelectedFile();

        if(a == JFileChooser.APPROVE_OPTION){
            try{
                FileInputStream f = new FileInputStream(file);
                return f;
            }
            catch(Exception e){
                System.out.println("File not found!");
            }
        }
        else
            System.out.println("Not input file was choosen");

        return null;
    }*/
}