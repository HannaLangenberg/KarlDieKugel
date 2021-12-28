package GUI;

import DB.Datenbank;
import Tools.GUI_Tools;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

/**
 * Main Class to start the application
 * The global variable for the connection to the database and the database itself are present but not connected to date.
 * As the main GUI of our application is done in JavaFX and the game GUI is done in Swing the stage icon has to be set
 * twice. As the path is the same we outsourced this task to a superior tools class in the GUI_Tools package.
 *
 * @author Aurora Chantal Benegiamo, Hanna Langenberg
 * @version 30.12.2020
 * */

public class Main extends Application {

    public static Datenbank db = new Datenbank("resources\\DB.accdb");

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("start.fxml"));
        primaryStage.setTitle("KarlDieKugel");
        GUI_Tools.setStageIcon(primaryStage);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        System.loadLibrary( "opencv_videoio_ffmpeg440_64" );
        launch(args);
    }
}
