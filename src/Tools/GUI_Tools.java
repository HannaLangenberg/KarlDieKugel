package Tools;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.net.URL;

/**
 * This class is able to set the Stage's / the JFrame's icon
 *
 * @author Hanna Langenberg
 * @version 30.12.2020
 * */

public class GUI_Tools {

    /**
    * A constant variable to the icon's path
    * */
    private static final String ICON_PATH = "/Tools/KarlDieKugelIcon.png";

    /**
     * Method for setting the JavaFX stage's icon.
     * As we are only exchanging the fxmls this method is only called once in the beginning
     * as the stage remains throughout the game.
     * @param stage The stage that is currently constructed.
     * */
    public static void setStageIcon(Stage stage)
    {
        stage.getIcons().add(new Image(ICON_PATH));
    }

    /**
     * Method for setting the JFrame's icon.
     * @param jframe The JFrame that is currently constructed.
     * */
    public static void setJFrameIcon(JFrame jframe)
    {
        URL url = GUI_Tools.class.getResource(ICON_PATH);
        ImageIcon imgicon = new ImageIcon(url);
        jframe.setIconImage(imgicon.getImage());
    }
}
