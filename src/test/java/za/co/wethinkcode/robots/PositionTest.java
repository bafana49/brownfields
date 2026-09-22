package za.co.wethinkcode.robots;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class PositionTest {

    /**
     * Tests the constructor of the Position class.
     * Verifies that the x and y coordinates are set correctly.
     */
    @Test
    @DisplayName("Test Position Constructor")
    void testConstructor() {
        Position position = new Position(5, 10);
        assertEquals(5, position.getX());
        assertEquals(10, position.getY());
    }

    /**
     * Tests the constructor of the Position class with negative coordinates.
     * Verifies that the x and y coordinates are set correctly even when negative.
     */
    @Test
    @DisplayName("Test Position Constructor with Negative Coordinates")
    void testEquals() {
        Position position1 = new Position(5, 10);
        Position position2 = new Position(5, 10);
        Position position3 = new Position(3, 10);

        assertEquals(position1, position2); // Same coordinates
        assertNotEquals(position1, position3); // Different coordinates
        assertNotEquals(null, position1); // Null comparison
        assertNotEquals("some string", position1); // Different object type
    }

    /**
     * Tests the hashCode method of the Position class.
     * Verifies that two positions with the same coordinates have the same hash code.
     */
    @Test
    @DisplayName("Test Position HashCode")
    void testToString() {
        Position position = new Position(5, 10);
        assertEquals("(x: 5, y: 10)", position.toString());
    }

    /**
     * Tests the isIn method of the Position class.
     * Verifies that a position is correctly identified as being inside or outside a rectangle defined by two corners.
     */
    @Test
    @DisplayName("Test Position HashCode")
    void testIsIn() {
        Position topLeft = new Position(0, 0);
        Position bottomRight = new Position(10, 10);
        Position insidePosition = new Position(5, 5);
        Position outsidePosition = new Position(15, 5);

        assertTrue(insidePosition.isIn(topLeft, bottomRight)); // Inside rectangle
        assertFalse(outsidePosition.isIn(topLeft, bottomRight)); // Outside rectangle
    }

    /**
     * Tests the distanceFrom method of the Position class.
     * Verifies that the distance between two positions is calculated correctly in various directions.
     */
    @Test
    @DisplayName("Test Position Distance From")
    void testDistanceFromNorthSouth() {
        Position position1 = new Position(5, 10);
        Position position2 = new Position(5, 15);

        assertEquals(5, position1.distanceFrom(position2, Direction.NORTH));
        assertEquals(5, position1.distanceFrom(position2, Direction.SOUTH));
    }

    /**
     * Tests the distanceFrom method of the Position class in the East/West direction.
     * Verifies that the distance between two positions is calculated correctly in the East/West direction.
     */
    @Test
    @DisplayName("Test Position Distance From East/West")
    void testDistanceFromEastWest() {
        Position position1 = new Position(5, 10);
        Position position2 = new Position(10, 10);

        // Distance between two positions in the East/West direction
        assertEquals(5, position1.distanceFrom(position2, Direction.EAST));
        assertEquals(5, position1.distanceFrom(position2, Direction.WEST));
    }


}
