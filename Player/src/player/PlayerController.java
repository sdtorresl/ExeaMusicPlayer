
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.scene.control.Slider;
//import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 *
 * @author sdtorresl
 */
public class PlayerController implements Initializable {
    /* Global variables */
    
    private static String MEDIA_URL = "stream.mp3";
    private final static String STREAM_URL = "http://stream01.exeamedia.com/farmatodotest.mp3";
    private final static String BACKUP_URL = "https://docs.google.com/uc?export=download&id=0B7PnSG02Gn9veTJqUVNuamxLVG8";//http://a.tumblr.com/tumblr_mpixn84ya21s78phdo1.mp3";
    private static File audioFile, rescueFile;
    private static final int DELAY_TIME = 4000;
    
    private static Media media, rescueMedia;
    private MediaPlayer mediaPlayer;
    private Boolean mute, playing, backupPlaying;
    
    private FetchStreamBytes fsb;
    private static Thread fsbThread;
    private static Thread metadataThread;
    
    @FXML
    public Label tittle, artist, album;
    public ProgressBar progress;
    public Button playPauseButton, playBackupButton, backupButton, muteButton;
    public Slider volumeSlider;
    public ImageView playPauseImageView, muteImageView;

    /** 
     * Get the actual media player
     * 
     * @return MediaPlayer
     */
    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
      
    /**
     * Return the main thread for 
     * stream audio
     * 
     * @return Thread
     */
    public static Thread getThread() {
        return fsbThread;
    }
    
    /**
     * Return the setMetadata task
     * 
     * @return Task
     */
    public static Task getTask() {
        return task;
    }
    
    /**
     * Return the main thread for 
     * stream audio
     * 
     * @return File
     */
    public static File getAudioFile() {
        return audioFile;
    }
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
        String artistLabel;
        String tittleLabel;
        
        //Get metadata from media
        //String artistLabel = (String) media.getMetadata().get("artist");
        //String tittleLabel = (String) media.getMetadata().get("title");
                
        String md = fsb.getMetadata();
        int n = md.indexOf('-');
        
        if (md == null || n == -1){
            artistLabel = "Desconocido";
            tittleLabel = "Desconocido";
        }
        else {
            artistLabel = md.substring(13, n-1).trim();
            // Delete a string error on some tittle labels
            tittleLabel = md.substring(n+1, md.length()-6).trim().replaceAll("';", "");
        }
        
        //Set metadata values
        tittle.setText(tittleLabel);
        artist.setText(artistLabel);
    }
    
    @FXML
    public void playPauseButtonClicked(ActionEvent event) {
        if(!playing) {
            if (backupPlaying) {
                mediaPlayer.pause(); //Pause 
                mediaPlayer = new MediaPlayer(media);
                playBackupButton.setStyle("-fx-background-color: #102D5A;");
            }
            setMetadata();
            mediaPlayer.play();
            
            //playPauseImageView.setImage(null);
            playPauseButton.setStyle("-fx-background-color: #00AACC;");
            playing = true;
            backupPlaying = false;
        }
        else {
            if (!backupPlaying) {
                mediaPlayer.pause();
                //playPauseImage.setImage(new Image("@playing.png"));
                playing = false;
                playPauseButton.setStyle("-fx-background-color: #102D5A;");
            }
        }
        fade(playPauseButton);
    }
   
    @FXML
    public void playBackupButtonClicked(ActionEvent event) {
        if(!backupPlaying) {
            getFile();
            if(playing) {
                mediaPlayer.pause(); //Pause stream media player 
                playPauseButton.setStyle("-fx-background-color: #102D5A;");
            }

            try {
                MEDIA_URL = rescueFile.toURI().toURL().toString(); //Change the media source
                rescueMedia = new Media(MEDIA_URL);
                mediaPlayer = new MediaPlayer(rescueMedia);
            } catch (MalformedURLException e) {
                Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
            }

            setMetadata();
            mediaPlayer.play();
            playBackupButton.setStyle("-fx-background-color: #00AACC;");
            
            backupPlaying = true; //Flag to indicate that backup music is playing
            playing = false; //Stream music is not playing at this moment
        }
        else {
            mediaPlayer.pause();
            playBackupButton.setStyle("-fx-background-color: #102D5A;");
            backupPlaying = false;
        }

        fade(playBackupButton);
    }
    
    @FXML
    public void muteButtonClicked(ActionEvent event) {
        if(!mute) {
            mediaPlayer.setMute(true);
            mute = true;
            muteButton.setStyle("-fx-background-color: #00AACC;");
        }
        else {
            mediaPlayer.setMute(false);
            mute = false;
            muteButton.setStyle("-fx-background-color: #102D5A;");
        }
        fade(muteButton);
    }
        
    @FXML
    public int downloadBackup(ActionEvent event) {
        fade(backupButton);
        String pathToSave = saveFile(); // Get the path to save backup file
        
        if(pathToSave.equals("")) {
            System.out.println("No path to save the file");
            return -1;
        }
        
        System.out.println("The path to save the backup file is " + pathToSave);
        
        try {
            saveUrl(pathToSave, BACKUP_URL); //Download backup file in the path
        } catch (IOException ex) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, ex);
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
        fade(playPauseButton);
    }
    
    /**
     * Open a file chooser for select a path to save a file
     * 
     * @return String
     */
    public String saveFile(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar respaldo");
        //chooser.setInitialFileName("Respaldo.mp3");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de audio mp3", "*.mp3"));
        
        try {
            String a = chooser.showSaveDialog(null).getAbsolutePath();
            if (!a.contains(".mp3")) {
                a = a + ".mp3";
            }
            return a;
        }catch(Exception e) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return null;
    }
    
    
    /**
     * Get the backup file that will be open
     */
    public void getFile(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir respaldo");
                      
        try {
            rescueFile = chooser.showOpenDialog(null).getAbsoluteFile();
        }catch(Exception e) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
            rescueFile = null;
        }
    }
    
    /**
     * Open a URL and save it into a file 
     * 
     * @param filename
     * @param urlString
     * @throws java.net.MalformedURLException
     */
    public void saveUrl(String filename, String urlString) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(new File(filename));

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        }
        finally {
            if (in != null) 
                in.close();
            if (fout != null)
                fout.close();
        }
    }
    
    private static final Task<Void> metadataUpdate = new Task<Void>() {
         @Override 
         protected Void call() throws Exception {
             System.out.println("Metadata inicio");
             int i = 0;
             while(i<1) {
                System.out.println("Metadata");
                TimeUnit.SECONDS.sleep(1);
                if(isCancelled())
                    break;
             }
             System.out.println("Metadata fin");
             return null;
         }
     };
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        audioFile = new File(MEDIA_URL);
        
        /*
        try {
            //audioFile = File.createTempFile("stream", "ExeaMusicPlayer_").getAbsolutePath().toString();
            audioFile = new File(File.createTempFile("stream", "ExeaMusicPlayer_").getAbsolutePath().toString());
        } catch (IOException ex) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
                
        // Create media player
        
        audioFile.deleteOnExit();

       	fsb =  new FetchStreamBytes(MEDIA_URL, STREAM_URL, this);
        fsbThread = new Thread(fsb);
        fsbThread.start();
        media = null;
        
        //metadataThread = new Thread(metadataUpdate);
        //metadataThread.start();
       
        try {
            Thread.sleep(DELAY_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            MEDIA_URL = audioFile.toURI().toURL().toString();
            media = new Media(MEDIA_URL);

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
        } catch (MalformedURLException e) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
        }
       
        //tittle.setText(Double.toString(volumeSlider.getValue()));
        mute = false;
        playing = false;
        backupPlaying = false;
        
        //Volume control
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging()) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
    }
}