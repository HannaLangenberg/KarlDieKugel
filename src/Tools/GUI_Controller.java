package Tools;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Superior Controller - class for tasks shared by multiple controllers
 *
 * In the FXMLs for the main menu and the highscore there is one button to switch between the two FXMLs.
 * The "Highscore" button, respectively the "Zurück" button. The goal was to avoid opening an entirely new scene
 * and closing the old one. We decided to open the 'new' FXML by retrieveing the current stage and setting
 * the scene with the new FXML.
 *
 * @author Aurora Chantal Benegiamo, Hanna Langenberg
 * @version 30.12.2020
 * */
public class GUI_Controller {
    @FXML
    private Button neuesFensterButton;

    public void fensterOeffnen(String pfad, String title) throws Exception {
        Stage aktuelleStage = (Stage) neuesFensterButton.getScene().getWindow();
        AnchorPane pane = FXMLLoader.load(getClass().getResource(pfad));
        aktuelleStage.setTitle(title);
        aktuelleStage.setScene(new Scene(pane));
    }
}