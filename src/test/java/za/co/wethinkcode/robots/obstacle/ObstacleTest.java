package za.co.wethinkcode.robots.obstacle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import za.co.wethinkcode.robots.Position;

public class ObstacleTest {

    /**
     * Tests the constructor with position and size.
     * Verifies that the obstacle is created with the correct name, type, and positions.
     */
    @Test
    @DisplayName("Test Obstacle Constructor with Position and Size")
    void testConstructorWithSizeAndType() {
        Obstacle obstacle = new Obstacle(5, 5, 3, ObstacleType.MOUNTAIN);
        assertEquals("MOUNTAIN 3", obstacle.getName());
        assertEquals(ObstacleType.MOUNTAIN, obstacle.getType());
        assertEquals(new Position(5, 5), obstacle.getTopLeft());
        assertEquals(new Position(8, 8), obstacle.getBottomRight());
    }

    /**
     * Tests the constructor with corner positions.
     * Verifies that the obstacle is created with the correct type and corner positions.
     */
    @Test
    @DisplayName("Test Obstacle Constructor with Corners")
    void testConstructorWithCorners() {
        Obstacle obstacle = new Obstacle(2, 3, 4, ObstacleType.MOUNTAIN);
        assertEquals("MOUNTAIN", obstacle.getType().toString());
        assertEquals(new Position(2, 3), obstacle.getTopLeft());
        assertEquals(new Position(6, 7), obstacle.getBottomRight());
    }

    /**
     * Tests the constructor with a single position.
     * Verifies that the obstacle is created with the correct type and position.
     */
    @Test
    @DisplayName("Test Obstacle Constructor with Single Position")
    void testBlocksPositionInside() {
        Obstacle obstacle = new Obstacle(0, 0, 2, ObstacleType.MOUNTAIN);
        assertTrue(obstacle.blocksPosition(new Position(1, 1)));
        assertTrue(obstacle.blocksPosition(new Position(0, 0)));
        assertTrue(obstacle.blocksPosition(new Position(2, 2)));
    }

    /**
     * Tests the blocksPosition method with positions outside the obstacle.
     * Verifies that the method returns false for positions not within the obstacle's area.
     */
    @Test
    @DisplayName("Test Obstacle Blocks Position Outside")
    void testBlocksPositionOutside() {
        Obstacle obstacle = new Obstacle(0, 0, 2, ObstacleType.MOUNTAIN);
        assertFalse(obstacle.blocksPosition(new Position(3, 3)));
        assertFalse(obstacle.blocksPosition(new Position(-1, 0)));
    }

    /**
     * Tests the blocksPath method with a vertical path.
     * Verifies that the obstacle blocks a vertical path that intersects its area.
     */
    @Test
    @DisplayName("Test Obstacle Blocks Vertical Path")
    void testBlocksVerticalPath() {
        Obstacle obstacle = new Obstacle(5, 5, 7, ObstacleType.MOUNTAIN);
        Position a = new Position(6, 4);
        Position b = new Position(6, 8);
        assertTrue(obstacle.blocksPath(a, b));
    }

    /**
     * Tests the blocksPath method with a horizontal path.
     * Verifies that the obstacle blocks a horizontal path that intersects its area.
     */
    @Test
    @DisplayName("Test Obstacle Blocks Horizontal Path")
    void testBlocksHorizontalPath() {
        Obstacle obstacle = new Obstacle(5, 5, 7, ObstacleType.MOUNTAIN);
        Position a = new Position(4, 6);
        Position b = new Position(8, 6);
        assertTrue(obstacle.blocksPath(a, b));
    }

    /**
     * Tests the blocksPath method with a path that does not intersect the obstacle.
     * Verifies that the obstacle does not block a path that is completely outside its area.
     */
    @Test
    @DisplayName("Test Obstacle Does Not Block Path Outside")
    void testDoesNotBlockDiagonalPath() {
        Obstacle obstacle = new Obstacle(5, 5, 7, ObstacleType.MOUNTAIN);
        Position a = new Position(4, 4);
        Position b = new Position(8, 8);
        assertFalse(obstacle.blocksPath(a, b));  // diagonals are not handled
    }

    /**
     * Tests the blocksPath method with a path that is completely outside the obstacle.
     * Verifies that the obstacle does not block a path that is entirely outside its area.
     */
    @Test
    @DisplayName("Test Obstacle Does Not Block Path Outside Completely")
    void testOverlappingObstaclesTrue() {
        Obstacle obs1 = new Obstacle(0, 0, 3, ObstacleType.BOTTOMLESS_PIT);
        Obstacle obs2 = new Obstacle(2, 2, 3, ObstacleType.LAKE);
        assertTrue(obs1.isOverlapping(obs2));
    }

    /**
     * Tests the isOverlapping method with non-overlapping obstacles.
     * Verifies that the method returns false when the obstacles do not overlap.
     */
    @Test
    @DisplayName("Test Obstacle Does Not Overlap")
    void testOverlappingObstaclesFalse() {
        Obstacle obs1 = new Obstacle(0, 0, 3, ObstacleType.BOTTOMLESS_PIT);
        Obstacle obs2 = new Obstacle(5, 5, 2, ObstacleType.LAKE);
        assertFalse(obs1.isOverlapping(obs2));
    }

    /**
     * Tests the isOverlapping method with partially overlapping obstacles.
     * Verifies that the method returns true when the obstacles partially overlap.
     */
    @Test
    @DisplayName("Test Obstacle Partially Overlaps")
    void testToStringFormat() {
        Obstacle obs = new Obstacle(1, 1, 2, ObstacleType.MOUNTAIN);
        String expected = "MOUNTAIN : { TopLeft-> (x: 1, y: 1) BottomRight-> (x: 3, y: 3) } ";
        assertEquals(expected, obs.toString());
    }
}
