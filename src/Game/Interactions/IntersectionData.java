package Game.Interactions;

/**
 * This class constructs new IntersectionData about all probable and all actual collisions.
 *
 * @author Hanna Langenberg
 * @version 02.01.2021
 * */
public class IntersectionData {
    private boolean doesIntersect;
    private int axis;
    private float[] minCoord;
    private float[] maxCoord;

    public IntersectionData() {     }

    /**
     * Constructor for IntersectionData.
     * @param doesIntersect Boolean true if a collision occurred, false if not
     * @param axis The axis on which the collision occurred: 0 ≙ x, 2 ≙ z
     * @param minCoord The smallest coordinate of the wall's AABB
     * @param maxCoord The largest coordinate of the wall's AABB
     * */
    public IntersectionData(boolean doesIntersect, int axis, float[] minCoord, float[] maxCoord)
    {
        this.doesIntersect = doesIntersect;
        this.axis = axis;
        this.minCoord = minCoord;
        this.maxCoord = maxCoord;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public int getAxis() {
        return axis;
    }
    public void setAxis(int axis) {
        this.axis = axis;
    }

    public float[] getMinCoord() {
        return minCoord;
    }
    public void setMinCoord(float[] minCoord) {
        this.minCoord = minCoord;
    }

    public float[] getMaxCoord() {
        return maxCoord;
    }
    public void setMaxCoord(float[] maxCoord) {
        this.maxCoord = maxCoord;
    }

    public boolean getDoesIntersect() {
        return doesIntersect;
    }
    public void setDoesIntersect(boolean doesIntersect) {
        this.doesIntersect = doesIntersect;
    }
}
