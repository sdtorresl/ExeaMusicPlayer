/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exeamusicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

/**
 *
 * @author sdtorresl
 */
public class ExeaMusicPlayerController implements Initializable {
    @FXML
    private Button playButton; // Value will be injected by FXMLLoader
    
    @FXML
    private void playClicked(ActionEvent event) {
        System.out.println("You clicked me!");
        playButton.setText("You clicked me!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
