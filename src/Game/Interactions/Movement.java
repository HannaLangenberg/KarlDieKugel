package Game.Interactions;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * This class currently listenes to keyboard input and uses them to rotate the labyrinth and move Karl
 * accordingly. Furthermore it tries to correct Karl's position if he collided with a wall but sadly the
 * code seems to be incorrect in some parts as Karl glitches around a bit. TODO fix this
 *
 * @author Hanna Langenberg
 * @version 02.01.2021
 * */
public class Movement implements KeyListener {
    // variables for Labyrinth
    private float angleXaxis = 0f;
    private float angleZaxis = 0f;
    private float angleXaxisInc = 1f;
    private float angleZaxisInc = 1f;
    private float xHolePosition;
    private float zHolePosition;
    private final float increment = 0.1f;

    // variables for Karl
    private float xKarl, zKarl;
    private float yKarl = 0f;
    private float yKarlInc = 0.2f;
    float[] centerKarlPos = new float[3];
    float[] minKarlPos = new float[3];
    float[] maxKarlPos = new float[3];
    private final float karlsRadius = (float) 2/2;
    private float deltaX;
    private float deltaZ;

    // variables for collision
    private boolean homerun = false;
    private float[] minWallCoord = new float[3];
    private float[] maxWallCoord = new float[3];
    private int hitAxis;

    public Movement() {     }

    @Override
    public void keyTyped(KeyEvent e) {  }

    /**
     * Implementation of KeyListener's method keyPressed.
     * Events are triggered when the arrow keys are used. This method should be replaced with the angles
     * derived from the CV part.
     * The arrow keys rotate the labyrinth by increments of 1f in the corresponding direction. As we are
     * looking along the x - axis at the labyrinth, we rotate the labyrinth along its z - axis using left
     * and right, and along its x - axis using up and down.
     * Furthermore, the rotation is limited to 15f in each direction, as it is still nice to look at and
     * the angle is not too extreme.
     * @param e KeyEvent
     * */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT: // positive direction
                if ( angleZaxis < 15.0f) {
                    angleZaxis += angleZaxisInc;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if ( -15.0f  < angleZaxis) {
                    angleZaxis -= angleZaxisInc;
                }
                break;
            case  KeyEvent.VK_UP: // positive direction
                if ( angleXaxis < 15.0f) {
                    angleXaxis += angleXaxisInc;
                }
                break;
            case KeyEvent.VK_DOWN:
                if ( -15.0f  < angleXaxis) {
                    angleXaxis -= angleXaxisInc;
                }
                break;
        }
    }

    /**
     * This method is responsible for rolling Karl along the labyrinth.
     * As long is homerun is false, meaning Karl did not fall through the hole, his x - and
     * z - position is calculated based on the labyrinth's angle and the increment. Furthermore,
     * it is checked if Karl is within the boundaries of the labyrinth, which is for all the
     * same: 24 × 24. If Karl tries to move out of the labyrinth he gets pushed back so that he
     * just touches the wall.
     *
     * Next, it is checked if Karl hits the hole. I defined that, if Karl comes closer than
     * 0.3f to the hole, he will fall through. So, homerun is set to true, Karl's y - position
     * is decreased and Karl is pulled the remaining distance right onto the hole (meaning by only
     * the x - and z -  values).
     * Lastly the method detKarlsCoord() is called to determine the new values for Karl's AABB.
     * */
    public void basicRollingAround() {
        if (!homerun)
        {
            xKarl -= angleXaxis * increment;
            zKarl += angleZaxis * increment;

            if(xKarl > 12 - karlsRadius) { xKarl = 12 - karlsRadius; }
            if(xKarl < -12 + karlsRadius) { xKarl = -12 + karlsRadius; }
            if(zKarl > 12 - karlsRadius) { zKarl = 12 - karlsRadius; }
            if(zKarl < -12 + karlsRadius) { zKarl = -12 + karlsRadius; }
        }

        if (Math.pow((xKarl - xHolePosition), 2) < 0.3 && Math.pow((zKarl - zHolePosition), 2) < 0.3)
        {
            homerun = true;
            yKarl -= yKarlInc;
            deltaX = xKarl - xHolePosition;
            deltaZ = zKarl - zHolePosition;

            if(deltaX != 0.0 || deltaZ != 0.0)  {
                xKarl -= deltaX;
                zKarl -= deltaZ;
            }
        }
        detKarlsCood();
    }

    /**
     * This method does almost exactly the same as basicRollingAround() but it tries to correct Karl's
     * position if he collided.
     * Basically, as long as homerun is false Karl is supposed to roll around the labyrinth and if he
     * intersects he is supposed to be pushed out of the corresponding wall just like in basicRollingAround.
     * The idea was to retrieve the axis along which we intersected which would be along x (0) and z (2).
     * Now, I needed the current angle of the labyrinth as we could run against one wall on one axis from
     * two directions and I would risk Karl suddenly saying hello from the other side. So I figured out
     * depending on the rotation which side Karl would hit. And I pushed him out.
     * Theoretically a nice idea, I suppose but the first problem was, as long as the labyrinth is still
     * rotated in that direction, Karl would try to pass the wall over and over again instead of laying still
     * against the wall. The second problem was that once the labyrinth gets rotated in the opposite direction
     * to move Karl away from the wall, he would jump over and and enter a loop again to try and pass the
     * wall from the other direction. So, basically, he is either stuck or gets catapulted away.
     *
     * Probably this problem could be solved by calculating Karl's directional vector and repelling him from the wall
     * or setting his velocity to zero.
     *
     * But hey, that makes is a lot more difficult and probably fun. Try to get to the hole without touching the wall
     * and if you do you will get a hilarious effect. A bit like Dr. Bibber.
     *
     * After that homerun and detKarlsCoord() works just fine.
     *
     * @param intersectionData current IntesectionData containing, if a collision happened, on which axis, and what the
     *                         corresponding values of the wall's AABB are.
     * */
    public void rollingAround(IntersectionData intersectionData) {
        if (!homerun)
        {
            xKarl -= angleXaxis * increment;
            zKarl += angleZaxis * increment;

            if ((angleXaxis > 0) && (intersectionData.getAxis() == 0) && intersectionData.getDoesIntersect())
            {
                    xKarl = intersectionData.getMaxCoord()[0] + karlsRadius;
//                    System.out.println("x: "+angleXaxis);
                    zKarl += angleZaxis * increment;
            }
            else if ((angleXaxis < 0) && (intersectionData.getAxis() == 0) && intersectionData.getDoesIntersect())
            {
                    xKarl = intersectionData.getMinCoord()[0] - karlsRadius;
//                    System.out.println("x: "+angleXaxis);
                    zKarl += angleZaxis * increment;
            }
            if ((angleZaxis > 0) && (intersectionData.getAxis() == 2) && intersectionData.getDoesIntersect())
            {
                    zKarl = intersectionData.getMinCoord()[2] - karlsRadius; // rotation to the left
//                    System.out.println("z: "+angleZaxis);
                    xKarl -= angleXaxis * increment;
            }
            else if ((angleZaxis < 0) && (intersectionData.getAxis() == 2) && intersectionData.getDoesIntersect())
            {
                    zKarl = intersectionData.getMaxCoord()[2] + karlsRadius;
//                    System.out.println("z: "+angleZaxis);
                    xKarl -= angleXaxis * increment;

            }
        }

        if (Math.pow((xKarl - xHolePosition), 2) < 0.3 && Math.pow((zKarl - zHolePosition), 2) < 0.3)
        {
            homerun = true;
            yKarl -= yKarlInc;
            deltaX = xKarl - xHolePosition;
            deltaZ = zKarl - zHolePosition;

            if(deltaX != 0.0 || deltaZ != 0.0) {
                xKarl -= deltaX;
                zKarl -= deltaZ;
            }
        }

        detKarlsCood();

    }


    /**
     * This method calculates the parameters for Karl's AABB
     * */
    public void detKarlsCood() {
        //filling Karl's center position array
        centerKarlPos[0] = xKarl;
        centerKarlPos[1] = yKarl;
        centerKarlPos[2] = zKarl;

        //calculating the min and max values of Karl's bounding box, known: radius alwasys 2
        /*
         * x-1, y-1, z-1 -> left bottom back
         * x+1, y+1, z+1 -> right top front
         * */
        minKarlPos[1] = yKarl-1;
        minKarlPos[2] = zKarl-1;
        minKarlPos[0] = xKarl-1;

        maxKarlPos[1] = yKarl+1;
        maxKarlPos[2] = zKarl+1;
        maxKarlPos[0] = xKarl+1;
    }

    public void setValues(float angleXaxis, float angleZaxis) {
        this.angleXaxis = angleXaxis;
        this.angleZaxis = angleZaxis;
    }

    @Override
    public void keyReleased(KeyEvent e) {    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public int getHitAxis() {
        return hitAxis;
    }
    public void setHitAxis(int hitAxis) {
        this.hitAxis = hitAxis;
    }

    public float[] getMinWallCoord() {
        return minWallCoord;
    }
    public void setMinWallCoord(float[] minWallCoord) {
        this.minWallCoord = minWallCoord;
    }

    public float[] getMaxWallCoord() {
        return maxWallCoord;
    }
    public void setMaxWallCoord(float[] maxWallCoord) {
        this.maxWallCoord = maxWallCoord;
    }

    public float getAngleXaxis() {
        return angleXaxis;
    }
    public void setAngleXaxis(float angleXaxis) {
        this.angleXaxis = angleXaxis;
    }

    public float getAngleZaxis() {
        return angleZaxis;
    }
    public void setAngleZaxis(float angleZaxis) {
        this.angleZaxis = angleZaxis;
    }

    public float getAngleXaxisInc() {
        return angleXaxisInc;
    }
    public void setAngleXaxisInc(float angleXaxisInc) {
        this.angleXaxisInc = angleXaxisInc;
    }

    public float getAngleZaxisInc() {
        return angleZaxisInc;
    }
    public void setAngleZaxisInc(float angleZaxisInc) {
        this.angleZaxisInc = angleZaxisInc;
    }

    public float getxKarl() {
        return xKarl;
    }
    public void setxKarl(float xKarl) {
        this.xKarl = xKarl;
    }

    public float getzKarl() {
        return zKarl;
    }
    public void setzKarl(float zKarl) {
        this.zKarl = zKarl;
    }

    public float getyKarl() {
        return yKarl;
    }
    public void setyKarl(float yKarl) {
        this.yKarl = yKarl;
    }

    public float getyKarlInc() {
        return yKarlInc;
    }
    public void setyKarlInc(float yKarlInc) {
        this.yKarlInc = yKarlInc;
    }

    public boolean isHomerun() {
        return homerun;
    }
    public void setHomerun(boolean homerun) {
        this.homerun = homerun;
    }

    public float getxHolePosition() {
        return xHolePosition;
    }
    public void setxHolePosition(float xHolePosition) {
        this.xHolePosition = xHolePosition;
    }

    public float getzHolePosition() {
        return zHolePosition;
    }
    public void setzHolePosition(float zHolePosition) {
        this.zHolePosition = zHolePosition;
    }

    public float getDeltaX() {
        return deltaX;
    }
    public float getDeltaZ() {
        return deltaZ;
    }

    public float[] getCenterKarlPos() {
        return centerKarlPos;
    }
    public float[] getMinKarlPos() {
        return minKarlPos;
    }
    public float[] getMaxKarlPos() {
        return maxKarlPos;
    }
}