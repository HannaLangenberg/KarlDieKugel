package Game.Interactions;

/**
 * The Mathematics - class is responsible for larger calculation tasks.
 *
 * @author Hanna Langenberg
 * @version 30.12.2020
 * */
public class Mathematics {
    private float[] maxResult;
    private float[] minResult;
    private int hitAxis;

    /**
     * This method calculates the distance between the two transferred parameters by vector subtraction.
     * @param minCoord The smaller coordinate
     * @param maxCoord The larger coordinate
     * @return A new float[3] containing the result from the subtraction
     * */
    public float[] calcDist(float[] minCoord, float[] maxCoord) {
//        System.out.println("calcDist used");
        float[] result = new float[3];
        for(int i = 0; i<minCoord.length; i++)
        {
            result[i] = minCoord[i] - maxCoord[i];
        }
        return result;
    }

    /**
     * This method determines the larger value for each position from two float[]
     * {@link Game.Interactions.AABB#intersects(AABB)}
     * @param dist1 Distance 1 that was previously calculated
     * @param dist2 Distance 2 that was previously calculated
     * @return A float[3] containing the largest distances in the three axis directions
     * */
    public float[] calcLargestDistances(float[] dist1, float[] dist2) {
//        System.out.println("calcLargestAxis used");
        float[] largestVals = new float[3];
        for(int i = 0; i<dist1.length; i++)
        {
            largestVals[i] = Math.max(dist1[i], dist2[i]);
        }
        return largestVals;
    }

    /**
     * This method determines which is the largest distance by comparing the distances along all axis with each other.
     * This value is returned and a second value "hitAxis" is set to whichever axis the largest value corresponds to as
     * this could potentially be the axis along which we intersect.
     * @param distance The float[] containing the largest distances for each axis
     * @return The largest distance of all three and the corresponding axis
     * */
    public float calcLargestAxis(float[] distance) {
//        System.out.println("calcLargestVal used");
        float result = distance[0];

        for (int i = 1; i<distance.length; i++)
        {
            result = Math.max(distance[i], result);
        }

        for (int i = 0; i<distance.length; i++)
        {
            if(result == distance[i] && result<0)
            {
                hitAxis = i;
            }
        }
        return result;
    }

    /**
     * This method determines the smallest coordinates from the given float[]
     * @param vertices A float[] containing the wall's vertices
     * */
    public void calcMinCoord(float[] vertices) {
//        System.out.println("calcMinCoord used");
        minResult = new float[3];
        minResult[0] = vertices[0];
        minResult[1] = vertices[1];
        minResult[2] = vertices[2];

        for(int i=3; i<vertices.length-3; i++) {
            minResult[0] = Math.min(vertices[i++], minResult[0]);   // smallest X value
            minResult[1] = Math.min(vertices[i++], minResult[1]);   // smallest Y value
            minResult[2] = Math.min(vertices[i], minResult[2]);     // smallest Z value
        }
    }

    /**
     * This method determines the largest coordinates from the given float[]
     * @param vertices A float[] containing the wall's vertices
     * */
    public void calcMaxCoord(float[] vertices) {
//        System.out.println("calcMaxCoord used");
        maxResult = new float[3];
        maxResult[0] = vertices[0];
        maxResult[1] = vertices[1];
        maxResult[2] = vertices[2];

        for(int i=3; i<vertices.length-3; i++) {
            maxResult[0] = Math.max(vertices[i++], maxResult[0]);   // largest X value
            maxResult[1] = Math.max(vertices[i++], maxResult[1]);   // largest Y value
            maxResult[2] = Math.max(vertices[i], maxResult[2]);     // largest Z value
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public int getHitAxis() {
        return hitAxis;
    }

    public float[] getMinResult() {
        return minResult;
    }
    public void setMinResult(float[] minResult) {
        this.minResult = minResult;
    }

    public float[] getMaxResult() {
        return maxResult;
    }
    public void setMaxResult(float[] maxResult) {
        this.maxResult = maxResult;
    }
}
