/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.io.File;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author sdtorresl
 */
public class Player extends Application {
        
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Player.fxml"));
        
        stage.setTitle("Farmatodo radio (beta)");
        stage.setResizable(false);
        //stage.getIcons().add(new Image("file:icon.png"));
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        
        stage.show();
    }

    @Override
    public void stop() {
        Thread t = PlayerController.getThread();
        t.stop();
        System.out.println("Exit");
        
        try {
            File audioFile = PlayerController.getAudioFile();
            audioFile.delete();
        }
        catch(Exception ioe) {
            
        }
        
        Task task = PlayerController.getTask();
        task.cancel();
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}