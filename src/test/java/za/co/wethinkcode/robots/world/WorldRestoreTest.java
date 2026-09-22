package za.co.wethinkcode.robots.world;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.UpdateResponse;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.config.Config;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static za.co.wethinkcode.robots.Direction.EAST;
import static za.co.wethinkcode.robots.UpdateResponse.SUCCESS;

/**
 * Tests that {@link World#restore(WorldSnapshot)} turns a persisted snapshot
 * into a live, playable world. No database access belongs here — the snapshot
 * is already a domain value loaded by the repository.
 */
public class WorldRestoreTest {

    private World world;

    @BeforeEach
    void setUp() {
        world = new World(false);
    }

    @AfterEach
    void tearDown() {
        world = null;
        Config.loadConfig("config.properties");
    }

    @Test
    @DisplayName("restore applies world size from the snapshot")
    void restore_appliesWorldSize() {
        world.restore(new WorldSnapshot(8, 12, List.of()));

        assertEquals(new Position(0, 0), world.getTOP_LEFT());
        assertEquals(new Position(7, 11), world.getBOTTOM_RIGHT());

        WorldSnapshot roundTrip = WorldSnapshot.from(world);
        assertEquals(8, roundTrip.getWidth());
        assertEquals(12, roundTrip.getHeight());
    }

    @Test
    @DisplayName("restore applies obstacles, pits and lakes")
    void restore_appliesMapObjects() {
        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        Obstacle pit = new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT);
        Obstacle lake = new Obstacle(5, 5, 6, 6, ObstacleType.LAKE);

        world.restore(new WorldSnapshot(20, 20, List.of(mountain, pit, lake)));

        List<Obstacle> obstacles = world.getObstacles();
        assertEquals(3, obstacles.size());
        assertEquals(ObstacleType.MOUNTAIN, obstacles.get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, obstacles.get(1).getType());
        assertEquals(ObstacleType.LAKE, obstacles.get(2).getType());
        assertEquals(1, obstacles.get(0).getTopLeft().getX());
        assertEquals(3, obstacles.get(1).getTopLeft().getX());
        assertEquals(5, obstacles.get(2).getTopLeft().getX());
    }

    @Test
    @DisplayName("restore replaces previously generated obstacles")
    void restore_replacesExistingObstacles() {
        world.restore(new WorldSnapshot(10, 10, List.of(
                new Obstacle(2, 2, 3, 3, ObstacleType.LAKE)
        )));

        assertEquals(1, world.getObstacles().size());
        assertEquals(ObstacleType.LAKE, world.getObstacles().get(0).getType());
        assertEquals(new Position(2, 2), world.getObstacles().get(0).getTopLeft());
        assertEquals(new Position(3, 3), world.getObstacles().get(0).getBottomRight());
    }

    @Test
    @DisplayName("restore does not restore robot positions or status")
    void restore_doesNotRestoreRobots() {
        world.addRobot(new Robot("OLD", "sniper"));
        world.setCurrentRobotByName("OLD");

        world.restore(new WorldSnapshot(10, 10, List.of(
                new Obstacle(0, 0, 0, 0, ObstacleType.MOUNTAIN)
        )));

        assertTrue(world.getRobots().isEmpty());
        assertTrue(world.getBots().isEmpty());
        assertNull(world.getCurrentRobot());
        assertThrows(NoSuchMethodException.class,
                () -> WorldSnapshot.class.getMethod("getRobots"));
    }

    @Test
    @DisplayName("restore with no obstacles still applies world size")
    void restore_emptyObstacles_stillAppliesSize() {
        world.restore(new WorldSnapshot(7, 9, List.of()));

        assertEquals(new Position(6, 8), world.getBOTTOM_RIGHT());
        assertTrue(world.getObstacles().isEmpty());
    }

    @Test
    @DisplayName("restored world is playable: a robot can launch and move")
    void restore_worldIsPlayable() {
        Obstacle mountain = new Obstacle(5, 5, 6, 6, ObstacleType.MOUNTAIN);
        world.restore(new WorldSnapshot(20, 20, List.of(mountain)));

        assertFalse(world.isLaunchAllowed(new Position(5, 5)));
        assertTrue(world.isLaunchAllowed(new Position(0, 0)));

        Robot robot = new Robot("HAL", "sniper");
        robot.setPosition(new Position(0, 0));
        world.addRobot(robot);
        world.setCurrentRobot(robot);
        setRobotDirection(robot, EAST);

        UpdateResponse response = world.updatePosition(1);

        assertEquals(SUCCESS, response);
        assertEquals(new Position(1, 0), robot.getPosition());
        assertEquals(1, world.getRobots().size());
    }

    private void setRobotDirection(Robot robot, Direction direction) {
        try {
            Field directionField = Robot.class.getDeclaredField("currentDirection");
            directionField.setAccessible(true);
            directionField.set(robot, direction);
        } catch (Exception e) {
            fail("Failed to set robot direction: " + e.getMessage());
        }
    }
}
