package Game.Interactions;

import java.util.ArrayList;

/**
 * This class checks for collisions and returns new IntersectionData which will later be used to correct Karl's
 * position depending on where we collided with a wall. For the purpose of a clearer arrangement of the code I
 * placed the method here.
 *
 * @author Hanna Langenberg
 * @version 02.01.2021
 * */
public class CollisionDetection {

    /**
     * Checks if a collision between Karl and any of the walls occurred and returns new IntersectionData filled with
     * the retrieved data.
     * @param walls ArrayList containing all wall AABBs
     * @param karl Karl's AABB
     * @return Retrieved IntersectionData
     * */
    public IntersectionData checkDetection(ArrayList<AABB> walls, AABB karl) {
        IntersectionData intersectionData = new IntersectionData();
//        System.out.println("checkDetection active!");
        for (AABB wall : walls) {
            intersectionData = wall.intersects(karl);

            if (intersectionData.getDoesIntersect())
            {
                System.out.println("Intersection detected!");
                return intersectionData;
            }
        }
        return intersectionData;
    }
}
