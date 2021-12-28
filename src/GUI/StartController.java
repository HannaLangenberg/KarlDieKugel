package GUI;

import ComputerVision.Vision;
import Game.StartCodeMainWindowPP;
import Tools.GUI_Controller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.opencv.core.Core;

import javax.swing.*;

/**
 * The StartController for the main GUI (start.fxml)
 *
 * @author Aurora Chantal Benegiamo
 * @version 30.12.2020
 * */

public class StartController extends GUI_Controller {
    @FXML
    public TextField namefield;
    @FXML
    private Button neuesFensterButton;
    @FXML
    private Button startButton;

    public static String spieler;

    /**
     * Method triggered by the 'neuesFensterButton' (on the GUI called Highscore) to exchange
     * the current fxml for 'highscore.fxml'.
     * */
    @FXML
    public void highscoreOnAction(ActionEvent actionEvent) throws Exception {
        try
        {
            fensterOeffnen("/Highscore/highscore.fxml", "Highscore");
        }
        catch(Exception e) {    e.printStackTrace();    }
    }


    /**
     * Method triggered by the 'startButton' to load the game in a swing window
     * */
    @FXML
    public void startOnAction(ActionEvent actionEvent) throws Exception  {
        spieler = namefield.getText();
        new Vision();
        Platform.exit();
    }

    public static String getSpieler() {
        return spieler;
    }
}

