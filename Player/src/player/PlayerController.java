
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;

/**
 *
 * @author sdtorresl
 */
public class PlayerController implements Initializable {
    /* Global variables */
    
    private static String MEDIA_URL = "/tmp/stream.mp3";
    private final static String STREAM_URL = "http://stream.exeamedia.com/farmatodotest.mp3";
    private final static String BACKUP_URL = "http://a.tumblr.com/tumblr_mpixn84ya21s78phdo1.mp3";
    private File audioFile, rescueFile;
    private static final int DELAY_TIME = 4000;
    
    private static Media media, rescueMedia;
    private MediaPlayer mediaPlayer;
    private Boolean mute, playing, backupPlaying;
    
    private FetchStreamBytes fsb;
    private Thread t;
    
    @FXML
    public Label tittle, artist, album;
    public ProgressBar progress;
    public Button playPauseButton, playBackupButton, backupButton, muteButton;
    public Slider volumeSlider;

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
    

    public ImageView playPauseImageView, muteImageView;
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
        
        int n;
        //Get metadata from media

        //String artistLabel = (String) media.getMetadata().get("artist");
        //String tittleLabel = (String) media.getMetadata().get("title");
        
        
        String md = fsb.getMetadata();
        
        
        System.out.println("+a+a+a+:    " + md);
        
        
        n = md.indexOf('-');
        
        if (md == null || n == -1){
            artistLabel = "Unknown";
            tittleLabel = "Unknown";
        }
        
        else {
            artistLabel = md.substring(13, n-1).trim();
            tittleLabel = md.substring(n+1, md.length()-6).trim();
        }

        //System.out.println(albumCover.toString());
        /*if(artistLabel.equals(""))
            artistLabel = "Desconocido";
        
        if(albumLabel.equals(""))
            albumLabel = "Desconocido";
        
        if(artistLabel.equals(""))  
            albumLabel = "Desconocido";*/

        //String artistLabel = (String) media.getMetadata().get("artist");
        //String tittleLabel = (String) media.getMetadata().get("title");

        
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
            }
            setMetadata();
            mediaPlayer.play();
            //System.out.println(playPauseImageView.getImage());
            playing = true;
            backupPlaying = false;
        }
        else {
            if (!backupPlaying) {
                mediaPlayer.pause();
                //playPauseImage.setImage(new Image("@playing.png"));
                playing = false;
            }
        }
        fade(playPauseButton);
    }
   
    @FXML
    public void playBackupButtonClicked(ActionEvent event) {
        if(!backupPlaying) {
            getFile();
            mediaPlayer.pause(); //Pause stream media player 

            try {
                MEDIA_URL = rescueFile.toURI().toURL().toString(); //Change the media source
                rescueMedia = new Media(MEDIA_URL);
                mediaPlayer = new MediaPlayer(rescueMedia);
            } catch (MalformedURLException e) {
                Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
            }

            setMetadata();
            mediaPlayer.play();
            
            backupPlaying = true; //Flag to indicate that backup music is playing
            playing = false; //Stream music is not playing at this moment
        }
        else {
            mediaPlayer.pause();
            backupPlaying = false;
        }

        fade(playBackupButton);
    }
    
    @FXML
    public void muteButtonClicked(ActionEvent event) {
        if(!mute) {
            mediaPlayer.setMute(true);
            mute = true;
        }
        else {
            mediaPlayer.setMute(false);
            mute = false;
        }
        fade(muteButton);
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
        
            System.out.println("The path to save the rescue file is " + pathToSave);
        
        try {
            saveUrl(pathToSave, BACKUP_URL);
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            audioFile = File.createTempFile("stream.mp3", null);
        } catch (IOException ex) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Create media player

        audioFile = new File(MEDIA_URL);
        audioFile.deleteOnExit();

       	fsb =  new FetchStreamBytes(MEDIA_URL, STREAM_URL, this);
        t = new Thread(fsb);
        t.start();
        media = null;
       
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
    
    public String saveFile(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar respaldo");
        chooser.setInitialFileName("Respaldo.mp3");
              
        try {
            String a = chooser.showSaveDialog(null).getAbsolutePath();
            return a;
        }catch(Exception e) {
            Logger.getLogger(PlayerController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return null;
    }
    
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
    
    /* Open a URL and save it into a file */
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
}
