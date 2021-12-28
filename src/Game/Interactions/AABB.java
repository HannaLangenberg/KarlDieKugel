package Game.Interactions;

import java.util.ArrayList;

/**
 * This class was already written when I discovered that OpenGL / Jogl actually provides a function to create AABBs but
 * this way I learned more and had more freedom to adapt the code. Furthermore, I added my methods for intersections and
 * creation of many AABBs in one go.
 *
 * In order to manage collision detection I watched many videos and read many articles.
 * My code is mainly based on the explanation by thebennybox on Youtube.
 * https://www.youtube.com/watch?v=iSIvixNgNq4&ab_channel=thebennybox
 * I had to make some adaptions for Jogl and my current use case.
 *
 * @author Hanna Langenberg
 * @version 30.12.2020
 * */
public class AABB {
    private float[] min;
    private float[] max;
    private int index;
    ArrayList<AABB> aabb = new ArrayList<>();
    Mathematics mathematics = new Mathematics();

    public AABB() {    }

    /**
     * A basic constructor for an Axis Aligned Bounding Box.
     * It holds 2 diagonal corners of the box.
     * @param min The coordinate of the corner with the smallest values in x-, y- and z-direction
     * @param max The coordinate with the largest values
     * Both have the form: float[3]
     * */
    public AABB(float[] min, float[] max) {
        this.min = min;
        this.max = max;
    }

    /**
     * This method tests for the AABB upon which it was called whether it intersects with the AABB transferred as a
     * parameter.
     *
     * //1
     * As the first step {@link Game.Interactions.Mathematics#calcDist(float[], float[])} is called twice and calculates
     * the distance between the two transferred parameters by a simple vector subtraction.
     * The first parameter is always the smaller coordinate, either from Karl or from the AABB we are testing
     * the intersection with. The second one is the larger coordinate. We have to calculate both
     * distances as, if we were to swap their position, we would, with one call, measure the distance like this:
     * CORNER mesh 1 -> through MESH 1 -> through SPACE -> through MESH 2 -> CORNER mesh 2
     * The distance would be incorrect.
     * //2
     * As we are subtracting the larger value from the smaller value in the first step we have to calculate the larger
     * distances from the two previously calculated distances.
     * Therefore we use the method {@link Game.Interactions.Mathematics#calcLargestDistances(float[], float[])} that
     * runs through the arrays, compares their values at corresponding positions and puts the larger value at the
     * current position in a new array.
     * //3
     * Next we need the overall largest value from the three and once this value is less than 0 we are intersecting.
     * This is done using {@link Game.Interactions.Mathematics#calcLargestAxis(float[])} which also determines on which
     * axis we are intersecting.
     *
     * The returned IntersectionData contains whether we are intersecting, on which axis and the min and max coordinates
     * of the intersected wall. (//4)
     *
     * @param karl The AABB that is questioned to be intersecting
     * @return New IntersectionData is returned that holds information about the relationship in which the two tested
     *         AABBs stand.
     * */
    public IntersectionData intersects(AABB karl) {
//        System.out.println(".intersects active");
        float[] dist1;
        float[] dist2;
        float[] distance;

        dist1 = mathematics.calcDist(karl.getMin(), max);               //1
        dist2 = mathematics.calcDist(min, karl.getMax());               //1
        distance = mathematics.calcLargestDistances(dist1, dist2);      //2
        float maxdist = mathematics.calcLargestAxis(distance);          //3
        int axis = mathematics.getHitAxis();                            //4

        return new IntersectionData(maxdist < 0, axis, min, max);
    }

    /**
     * The purpose of this method is to simplify the calling code. It takes in an ArrayList that contains individual
     * float[] containing the vertices of each wall. It then runs through all of the walls, calculates the min and max
     * coordinates and uses them to construct a new AABB which is added to a new ArrayList.
     * @param vecWalls The ArrayList containing the walls
     * @return An ArrayList containing the walls' AABBs
     * */
    public ArrayList<AABB> calcWallAABBs(ArrayList<float[]> vecWalls) {
        for (float[] vecWall : vecWalls) {
            mathematics.calcMinCoord(vecWall);
            mathematics.calcMaxCoord(vecWall);

            aabb.add(new AABB(mathematics.getMinResult(), mathematics.getMaxResult()));
        }
        return aabb;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public float[] getMin() {
        return min;
    }
    public void setMin(float[] min) {
        this.min = min;
    }

    public float[] getMax() {
        return max;
    }
    public void setMax(float[] max) {
        this.max = max;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
