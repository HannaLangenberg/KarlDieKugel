package ComputerVision;

/**
 * Main class to start the program
 */

import org.opencv.core.Core;

/**
 * @author Karsten Lehn
 * @version 4.9.2020
 *
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Load OpenCV libraries and start program
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        System.loadLibrary( "opencv_videoio_ffmpeg440_64" );

        new Vision();
    }

}
