package za.co.wethinkcode.robots.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorldSnapshot}.
 * Verifies that a constructed world is reduced to size and map objects only.
 */
public class WorldSnapshotTest {

    @Test
    @DisplayName("from(world) copies world size from bounds")
    void from_copiesWorldSizeFromBounds() {
        World world = mockWorld(new Position(0, 0), new Position(4, 9), List.of());

        WorldSnapshot snapshot = WorldSnapshot.from(world);

        assertEquals(5, snapshot.getWidth());
        assertEquals(10, snapshot.getHeight());
    }

    @Test
    @DisplayName("from(world) copies obstacles, pits and lakes")
    void from_copiesMapObjects() {
        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        Obstacle pit = new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT);
        Obstacle lake = new Obstacle(5, 5, 6, 6, ObstacleType.LAKE);
        World world = mockWorld(new Position(0, 0), new Position(19, 19),
                List.of(mountain, pit, lake));

        WorldSnapshot snapshot = WorldSnapshot.from(world);

        assertEquals(3, snapshot.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, snapshot.getObstacles().get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, snapshot.getObstacles().get(1).getType());
        assertEquals(ObstacleType.LAKE, snapshot.getObstacles().get(2).getType());
    }

    @Test
    @DisplayName("from(world) does not include robot positions or status")
    void from_doesNotIncludeRobots() {
        World world = mockWorld(new Position(0, 0), new Position(9, 9), List.of());
        when(world.getBots()).thenReturn(List.of(new Robot("HAL", "sniper")));
        when(world.getRobots()).thenReturn(List.of(new Robot("HAL", "sniper")));

        WorldSnapshot snapshot = WorldSnapshot.from(world);

        assertTrue(snapshot.getObstacles().isEmpty());
        verifyNoRobotAccess(world);
        assertThrows(NoSuchMethodException.class, () -> snapshot.getClass().getMethod("getRobots"));
        assertThrows(NoSuchMethodException.class, () -> snapshot.getClass().getMethod("getBots"));
    }

    @Test
    @DisplayName("from(world) with no obstacles still captures size")
    void from_emptyObstacles_stillCapturesSize() {
        World world = mockWorld(new Position(0, 0), new Position(2, 2), List.of());

        WorldSnapshot snapshot = WorldSnapshot.from(world);

        assertEquals(3, snapshot.getWidth());
        assertEquals(3, snapshot.getHeight());
        assertTrue(snapshot.getObstacles().isEmpty());
    }

    private World mockWorld(Position topLeft, Position bottomRight, List<Obstacle> obstacles) {
        World world = mock(World.class);
        when(world.getTOP_LEFT()).thenReturn(topLeft);
        when(world.getBOTTOM_RIGHT()).thenReturn(bottomRight);
        when(world.getObstacles()).thenReturn(obstacles);
        return world;
    }

    private void verifyNoRobotAccess(World world) {
        verify(world, never()).getBots();
        verify(world, never()).getRobots();
    }
}
