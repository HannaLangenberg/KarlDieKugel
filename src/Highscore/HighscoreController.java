package Highscore;

import GUI.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import Tools.GUI_Controller;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;

/**
 * The HighscoreController for the highscore GUI (highscore.fxml)
 * There is a code snipplet for a possible connection to a database and the filling
 * of the tableview but it is neither connected nor implemented to date.
 *
 * @author Aurora Chantal Benegiamo , Julian Riedel
 * @version 30.12.2020
 * */

public class HighscoreController extends GUI_Controller {
    @FXML
    public TableColumn<Highscore, String> Name;
    @FXML
    public TableColumn<Highscore, String> Score;
    @FXML
    public TableView<Highscore> Tabelle;
    @FXML
    private Button neuesFensterButton;

    ObservableList dbData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        druckeHighscore("Highscore");
    }

    /**
     * Method triggered by the 'neuesFensterButton' (on the GUI called zurück) to exchange
     * the current fxml for 'start.fxml'.
     * */
    @FXML
    public void backOnAction(ActionEvent actionEvent) throws Exception {
        try
        {
            fensterOeffnen("/GUI/start.fxml", "KarlDieKugel");
        }
        catch(Exception e) {   e.printStackTrace();    }
    }
    public void druckeHighscore(String tabellenname) {
        try {
            ResultSet rs = Main.db.dbAbfrage("SELECT * FROM " + tabellenname+ " ORDER BY Punktzahl DESC");
            while(rs.next()){
                dbData.add(new Highscore(rs.getString("Spieler"), rs.getString("Punktzahl")));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Name.setCellValueFactory(new PropertyValueFactory<>("Spieler"));
        Score.setCellValueFactory(new PropertyValueFactory<>("Punktzahl"));

        Tabelle.setItems(dbData);
    }
}
