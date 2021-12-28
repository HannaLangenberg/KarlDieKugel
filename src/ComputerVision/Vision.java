package ComputerVision;

import Game.StartCodeMainWindowPP;
import Tools.GUI_Tools;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * This class is responsible for taking in a videostream from the camera and processing it to calculate the values for
 * rotating the labyrinth as desired by the user.
 *
 * First of all a configuration window is opened in which the user is able to change the upper and lower boundaries of
 * hue, saturation and value that have to be tracked. The result of the changing parameters is displayed in the
 * right window. The default values are set to a strong red as we have been using a red pipecleander to steer the board.
 *
 * Once the user starts the game, the main game is opened and the user can rotate the labyrinth with gestures. The
 * directions are intuitive. Tilt left rotates left, moving up rotates backwards. Right and forward.
 *
 * @author Hanna Langenberg, Julian Riedel
 * @version 07.01.2021
 * */
public class Vision extends JFrame{
    private BufferedImagePanel configHSV_in;
    private BufferedImagePanel configHSV_out;
    private JPanel panel;
    private JSlider min_slider_h;
    private JSlider max_slider_h;
    private JSlider min_slider_s;
    private JSlider max_slider_s;
    private JSlider min_slider_v;
    private JSlider max_slider_v;
    private int h_min = 155;    // 65
    private int s_min = 25;     // 35
    private int v_min = 0;      // 0
    private int h_max = 180;    // 100
    private int s_max = 255;    //
    private int v_max = 255;    //

    private static float angleXaxis = 0f; //up and down
    private final float angleXaxisInc = 0.5f;
    private static float angleZaxis = 0f; //left and right
    private final float angleZaxisInc = 0.5f;

    JButton startGame;
    VideoCapture cap;

    /**
     * Constructor to start the initialization method for the JFrame and open the window thereafter.
     * */
    public Vision() {
        initHSVconfigWindow();
        openWindow();
    }

    /**
     * This methods opens and checks the VideoCapture. While the camera is streaming, it displays the original
     * frame on the left and the processed image on the right.
     * */
    private void openWindow() {

        cap = new VideoCapture(0, Videoio.CAP_ANY);
        Mat frame = new Mat();

        // Check if camera can be opened
        if(!cap.isOpened()) {
            throw new CvException("The Video File or the Camera could not be opened!");
        }

        cap.read(frame);
        System.out.println("  First grabbed frame: " + frame);
        System.out.println("  Matrix columns: " + frame.cols());
        System.out.println("  Matrix rows: " + frame.rows());
        System.out.println("  Matrix channels: " + frame.channels());

        // loop for grabbing frames
        while (cap.read(frame))
        {
            Core.flip(frame, frame, 1);                     // flip frame → match user's desired direction
                                                                    // 1 = along y; -1 = along x
            configHSV_in.setImage(Mat2BufferedImage(frame));        // display "original", flipped frame

            Mat edges = detectEdge(frame);                          // send frame off for edge detection
            configHSV_out.setImage(Mat2BufferedImage(edges));       // Show processed image
            pack();
        }
        cap.release();
    }

    /**
     * This method detects the edge in the camera frame and calculates the angles for the labyrinth.
     * @param original The current frame coming from the camera
     * */
    public Mat detectEdge(Mat original) {
        Mat blurredImg = new Mat();                                             // blurr the image
        Imgproc.GaussianBlur(original, blurredImg, new Size(5,5), 1.5, 1.5);

        Mat hsvImg = new Mat();                                                 // convert to HSV colorspace
        Imgproc.cvtColor(blurredImg, hsvImg, Imgproc.COLOR_BGR2HSV);

        Scalar minVal = new Scalar(h_min, s_min, v_min);                        // determine new Scalars to track the
        Scalar maxVal = new Scalar(h_max, s_max, v_max);                        // color, from sliders or default values

        Mat inRangeImg = new Mat();                                             // track the determined color
        Core.inRange(hsvImg, minVal, maxVal, inRangeImg);

        Imgproc.medianBlur(inRangeImg, blurredImg, 7);                    // more blurring

        Mat cannyImg = new Mat();                                                  // Canny to detect the edge
        Imgproc.Canny(blurredImg, cannyImg, 50,200,3,false);

        Mat resultLines = new Mat();                                            // result Mat that will store the line's
                                                                                // theta and rho values
        Imgproc.HoughLines(cannyImg, resultLines, 1, Math.PI/180, 150,0,0);

        // Every detected line will be drawn, the coordinates and the resulting angles for the labyrinth calculated
        for (int i = 0; i<resultLines.rows(); i++)
        {
            double rho = resultLines.get(i,0)[0],
                   theta = resultLines.get(i,0)[1];
            double a = Math.cos(theta),
                   b = Math.sin(theta);
            double x0 = a*rho,
                   y0 = b*rho;

            /*
            The angles I am checking:
             0°  <   theta <  85° → line tilted to the left
             85° <=  theta <= 95° → line considered as not tilted
             95° <   theta <  180°→ line tilted to the right

             For every angle and direction I limited the rotation to 15f → nice view on the labyrinth and not confusing
             The measured angles are in radians so for the 'stop corridor' the 5° had to be converted to radians.
             ~ the debugging souts are commented out to be able to see the CollisionDetection responses in the console ~
             */

            // left ←
            if (0 < theta && theta < Math.PI/2-Math.toRadians(5.0))
            {
                if (angleZaxis < 15.0f) {
//                    System.out.println("R - Nach Links!");
                    angleZaxis += angleZaxisInc;
                }
            }
            // stop -
            else if (Math.PI/2-Math.toRadians(5.0) <= theta && theta <= Math.PI/2+Math.toRadians(5.0))
            {
//                System.out.println("R - Halt!");
                angleZaxis += 0;
            }
            // right →
            else if (Math.PI/2+Math.toRadians(5.0) < theta && theta < Math.PI)
            {
                if ( -15.0f  < angleZaxis) {
//                    System.out.println("R - Nach Rechts!");
                    angleZaxis -= angleZaxisInc;
                }
            }


            /*
            The areas I am checking:
             upper third → rotate labyrinth backwards
             middle thrid → do not rotate labyrinth
             lower third → rotate labyrinth forward

             As I am only looking at the y coordinates, the x coordinates can be omitted.
             I determine the middle point of the line and use this y - position to calculate the rotation angle.
             ~ the debugging souts are commented out to be able to see the CollisionDetection responses in the console ~
             */
            // two points on the line
            Point left  = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point right = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

            double yPos = left.y+(right.y-left.y)/2;            // calculate the y - position
            if (left.y == right.y)
            {
                yPos = right.y;
            }

            float tm_line = original.height()/3;                // divide the frame's height into the three areas
            float mb_line = tm_line*2;

            if(yPos < tm_line)
            {
                if ( angleXaxis < 15.0f)
                {
//                    System.out.println("M - Nach oben!");
                    angleXaxis += angleXaxisInc;
                }
            }
            else  if (tm_line <= yPos && yPos <= mb_line)
            {
//                System.out.println("M - Halt!");
                angleXaxis +=0;
            }
            else if (mb_line < yPos)
            {
                if ( -15.0f  < angleXaxis)
                {
//                    System.out.println("M - Nach unten!");
                    angleXaxis -= angleXaxisInc;
                }
            }                                                                          // draw lines in original picture
            Imgproc.line(original, left, right, new Scalar(0,0,255), 3,Imgproc.LINE_AA,0);
        }

        return original;
    }

    /**
     * Converts an OpenCV matrix into a BufferedImage.
     *
     * Inspired by
     * http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
     * Fastest code
     *
     * @param imgMat Matrix to be converted must be a one channel (grayscale) or
     * three channel (BGR) matrix, i.e. one or three byte(s) per pixel.
     * @return converted image as BufferedImage
     *
     */
    public BufferedImage Mat2BufferedImage(Mat imgMat){
        int bufferedImageType = 0;
        switch (imgMat.channels()) {
            case 1:
                bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                throw new IllegalArgumentException("Unknown matrix type. Only one byte per pixel (one channel) or three bytes pre pixel (three channels) are allowed.");
        }
        BufferedImage bufferedImage = new BufferedImage(imgMat.cols(), imgMat.rows(), bufferedImageType);
        final byte[] bufferedImageBuffer = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        imgMat.get(0, 0, bufferedImageBuffer);
        return bufferedImage;
    }

    private void initHSVconfigWindow() {
        setTitle("HSV Werte zum Tracken der Edges einstellen");
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        configHSV_in = null;
        configHSV_out = null;

        JLabel lMin_H, lMin_S, lMin_V, lMax_H, lMax_S, lMax_V;

        min_slider_h = constructSliders(0, 179, 30, h_min);
        min_slider_s = constructSliders(0,255,50, s_min);
        min_slider_v = constructSliders(0,255,50, v_min);
        max_slider_h = constructSliders(0, 179, 30, h_max);
        max_slider_s = constructSliders(0,255,50, s_max);
        max_slider_v = constructSliders(0,255,50, v_max);
        min_slider_h.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderH -min: " + min_slider_h.getValue());
                h_min = min_slider_h.getValue();
            }
        });
        max_slider_h.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderH -max: " + max_slider_h.getValue());
                h_max = max_slider_h.getValue();
            }
        });
        min_slider_s.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderS -min: " + min_slider_s.getValue());
                s_min = min_slider_s.getValue();
            }
        });
        max_slider_s.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderS -max: " + max_slider_s.getValue());
                s_max = max_slider_s.getValue();
            }
        });
        min_slider_v.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderV -min: " + min_slider_v.getValue());
                v_min = min_slider_v.getValue();
            }
        });
        max_slider_v.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("SliderV -max: " + max_slider_v.getValue());
                v_max = max_slider_v.getValue();
            }
        });
        lMin_H = new JLabel("Min. Hue");
        lMin_S = new JLabel("Min. Saturation");
        lMin_V = new JLabel("Min. Value");
        lMax_H = new JLabel("Max. Hue");
        lMax_S = new JLabel("Max. Saturation");
        lMax_V = new JLabel("Max. Value");

        startGame = new JButton("Los gehts!");
        startGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                angleXaxis = 0f;
                angleZaxis = 0f;
                setLocation(dim.width - getWidth(),0);
                new StartCodeMainWindowPP();
                System.out.println("Hi");
            }
        });

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        configHSV_in = new BufferedImagePanel();
        contentPane.add(configHSV_in, BorderLayout.LINE_START);
        configHSV_out = new BufferedImagePanel();
        contentPane.add(configHSV_out, BorderLayout.LINE_END);

        panel = new JPanel();

        JPanel grid = new JPanel(new GridLayout(4, 3));
        grid.add(lMin_H);
        grid.add(lMin_S);
        grid.add(lMin_V);
        grid.add(min_slider_h);
        grid.add(min_slider_s);
        grid.add(min_slider_v);
        grid.add(lMax_H);
        grid.add(lMax_S);
        grid.add(lMax_V);
        grid.add(max_slider_h);
        grid.add(max_slider_s);
        grid.add(max_slider_v);
        panel.add(grid);
        panel.add(startGame);

        contentPane.add(panel, BorderLayout.PAGE_END);
        pack();
        setLocation(dim.width/2 - getWidth()/2, dim.height/2 - getHeight()/2);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        GUI_Tools.setJFrameIcon(this);
    }

    public JSlider constructSliders(int min, int max, int increment, int startValue) {
        JSlider slider;
        slider = new JSlider(min, max);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setLabelTable(slider.createStandardLabels(increment));
        slider.setValue(startValue);

        return slider;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTER
    public static float getAngleXaxis() {
        return angleXaxis;
    }

    public static float getAngleZaxis() {
        return angleZaxis;
    }
}
