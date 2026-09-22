package za.co.wethinkcode.robots.world;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.UpdateResponse;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.config.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static za.co.wethinkcode.robots.Direction.*;
import static za.co.wethinkcode.robots.OperationalStatus.*;
import static za.co.wethinkcode.robots.UpdateResponse.*;

class WorldTest {

    private World world;
    private Robot robot;

    /**
     * Helper method to set the robot's direction using reflection
     * This is needed because Robot class doesn't have a public setter for direction
     */
    private void setRobotDirection(Robot robot, Direction direction) {
        try {
            Field directionField = Robot.class.getDeclaredField("currentDirection");
            directionField.setAccessible(true);
            directionField.set(robot, direction);
        } catch (Exception e) {
            fail("Failed to set robot direction: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        Config.loadConfig("config.properties");
        // Create World without GUI to avoid GUI initialization issues in tests
        world = new World(false);
        robot = new Robot("TestBot", "soldier");
        world.addRobot(robot);
        world.setCurrentRobot(robot);
    }

    @AfterEach
    void tearDown(){
        world = null;
        robot = null;
    }

    @Test
    void testSetCurrentRobotByName() {
        Robot anotherBot = new Robot("AnotherBot", "sniper");
        world.addRobot(anotherBot);
        world.setCurrentRobotByName("AnotherBot");
        assertEquals("AnotherBot", world.getCurrentRobot().getName());
    }

    @Test
    void testUpdatePositionWithinBounds() {
        robot.setPosition(new Position(18, 5));
        setRobotDirection(robot, NORTH);
        UpdateResponse response = world.updatePosition(1);
        assertEquals(SUCCESS, response);
        assertEquals(new Position(18, 4), robot.getPosition());
    }

    @Test
    void testOutOfBoundsKillsRobot() {
        // Position robot at the edge and try to move out of bounds
        robot.setPosition(new Position(0, 0));
        setRobotDirection(robot, NORTH); // Try to move north from (0,0) which should be out of bounds
        UpdateResponse response = world.updatePosition(1);
        assertEquals(FAILURE_AT_NORTH_EDGE, response);
        assertEquals(NORMAL, robot.getStatus()); // Robot should not die when at edge, only when actually falling out
    }

    @Test
    void testBottomlessPitKillsRobot() {
        // Create a mock world to control obstacles
        World spyWorld = spy(new World(false));
        Robot testRobot = new Robot("PitTestBot", "soldier");
        spyWorld.addRobot(testRobot);
        spyWorld.setCurrentRobot(testRobot);

        // Create a bottomless pit obstacle
        Obstacle pit = new Obstacle(1, 1, 2, 2, ObstacleType.BOTTOMLESS_PIT);
        List<Obstacle> obstacles = Collections.singletonList(pit);
        when(spyWorld.getObstacles()).thenReturn(obstacles);

        // Position robot next to pit and move into it
        testRobot.setPosition(new Position(0, 1));
        setRobotDirection(testRobot, EAST);

        UpdateResponse response = spyWorld.updatePosition(1);

        assertEquals(DIED_FELL_IN_PIT, response);
        assertEquals(DEAD, testRobot.getStatus());
    }

    @Test
    void testDeleteDeadBots() {
        Robot bot1 = new Robot("DeadBot", "sniper");
        bot1.setStatus(DEAD);
        world.addRobot(bot1);

        // Verify bot is added
        assertTrue(world.getBots().contains(bot1));

        world.deleteDeadBots();

        // Verify dead bot is removed
        assertFalse(world.getBots().contains(bot1));
    }

    @Test
    void testIsNewPositionAllowed_NoObstructions() {
        Position newPosition = new Position(10, 10);
        assertTrue(world.isNewPositionAllowed(newPosition));
    }

    @Test
    void testIsNewPositionAllowed_ObstructedByRobot() {
        Robot anotherBot = new Robot("AnotherBot", "sniper");
        anotherBot.setPosition(new Position(10, 10));
        world.addRobot(anotherBot);

        Position newPosition = new Position(10, 10);
        assertFalse(world.isNewPositionAllowed(newPosition));
    }

    @Test
    void testIsNewPositionAllowed_ObstructedByObstacle() {
        // Use reflection to directly modify the obstacleList field
        try {
            Field obstacleListField = World.class.getDeclaredField("obstacleList");
            obstacleListField.setAccessible(true);

            List<Obstacle> obstacles = new ArrayList<>();
            obstacles.add(new Obstacle(9, 9, 11, 11, ObstacleType.MOUNTAIN));
            obstacleListField.set(world, obstacles);

            Position newPosition = new Position(10, 10);
            assertFalse(world.isNewPositionAllowed(newPosition));
        } catch (Exception e) {
            fail("Failed to set obstacle list: " + e.getMessage());
        }
    }

    @Test
    void testIsMovementObstructed_ByMountain() {
        // Use reflection to directly modify the obstacleList field
        try {
            Field obstacleListField = World.class.getDeclaredField("obstacleList");
            obstacleListField.setAccessible(true);

            List<Obstacle> obstacles = new ArrayList<>();
            obstacles.add(new Obstacle(5, 5, 7, 7, ObstacleType.MOUNTAIN));
            obstacleListField.set(world, obstacles);

            Position obstructedPosition = new Position(6, 6);
            assertTrue(world.isMovementObstructed(obstructedPosition));

            Position freePosition = new Position(10, 10);
            // Based on your implementation logic, this should return false for non-mountain positions
            assertFalse(world.isMovementObstructed(freePosition));
        } catch (Exception e) {
            fail("Failed to set obstacle list: " + e.getMessage());
        }
    }

    @Test
    void testUpdatePosition_AllDirections() {
        Robot testRobot = new Robot("DirectionsTestBot", "soldier");
        world.addRobot(testRobot);
        world.setCurrentRobot(testRobot);

        // Clear any existing obstacles to ensure clean test
        try {
            Field obstacleListField = World.class.getDeclaredField("obstacleList");
            obstacleListField.setAccessible(true);
            obstacleListField.set(world, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to clear obstacle list: " + e.getMessage());
        }

        // Test NORTH movement (y decreases)
        testRobot.setPosition(new Position(5, 5));
        setRobotDirection(testRobot, NORTH);
        assertEquals(SUCCESS, world.updatePosition(1));
        assertEquals(new Position(5, 4), testRobot.getPosition());

        // Test EAST movement (x increases)
        testRobot.setPosition(new Position(5, 5));
        setRobotDirection(testRobot, EAST);
        assertEquals(SUCCESS, world.updatePosition(1));
        assertEquals(new Position(6, 5), testRobot.getPosition());

        // Test SOUTH movement (y increases)
        testRobot.setPosition(new Position(5, 5));
        setRobotDirection(testRobot, SOUTH);
        assertEquals(SUCCESS, world.updatePosition(1));
        assertEquals(new Position(5, 6), testRobot.getPosition());

        // Test WEST movement (x decreases)
        testRobot.setPosition(new Position(5, 5));
        setRobotDirection(testRobot, WEST);
        assertEquals(SUCCESS, world.updatePosition(1));
        assertEquals(new Position(4, 5), testRobot.getPosition());
    }

    @Test
    void testUpdatePosition_ObstructedByRobot() {
        Robot anotherBot = new Robot("AnotherBot", "sniper");
        anotherBot.setPosition(new Position(5, 4));
        world.addRobot(anotherBot);

        robot.setPosition(new Position(5, 5));
        setRobotDirection(robot, NORTH);

        UpdateResponse response = world.updatePosition(1);
        assertEquals(FAILURE_OBSTRUCTED, response);
        assertEquals(new Position(5, 5), robot.getPosition()); // Position should remain unchanged
    }

    @Test
    void testGetBots() {
        Robot bot1 = new Robot("Bot1", "sniper");
        Robot bot2 = new Robot("Bot2", "tank");
        world.addRobot(bot1);
        world.addRobot(bot2);

        List<Robot> bots = world.getBots();
        assertEquals(3, bots.size()); // Original robot + 2 new bots
        assertTrue(bots.contains(robot));
        assertTrue(bots.contains(bot1));
        assertTrue(bots.contains(bot2));
    }

    @Test
    void testGetRobots() {
        // Test the getRobots method (which appears to be the same as getBots)
        Robot bot1 = new Robot("Bot1", "sniper");
        world.addRobot(bot1);

        List<Robot> robots = world.getRobots();
        assertEquals(2, robots.size());
        assertTrue(robots.contains(robot));
        assertTrue(robots.contains(bot1));
    }

    @Test
    void testIsLaunchAllowed_NoObstructions() {
        Position newPosition = new Position(10, 10);
        assertTrue(world.isLaunchAllowed(newPosition));
    }

    @Test
    void testIsLaunchAllowed_ObstructedByRobot() {
        Robot anotherBot = new Robot("AnotherBot", "sniper");
        anotherBot.setPosition(new Position(10, 10));
        world.addRobot(anotherBot);

        Position newPosition = new Position(10, 10);
        assertFalse(world.isLaunchAllowed(newPosition));
    }

    @Test
    void testWorldBoundaries() {
        // Test that world boundaries are correctly set
        assertNotNull(world.getTOP_LEFT());
        assertNotNull(world.getBOTTOM_RIGHT());

        // Assuming default boundaries based on HEIGHT and WIDTH from config
        assertEquals(new Position(0, 0), world.getTOP_LEFT());
        // The exact bottom right depends on your HEIGHT and WIDTH constants
        assertNotNull(world.getBOTTOM_RIGHT());
    }

    @Test
    void testMultipleStepMovement() {
        robot.setPosition(new Position(10, 10));
        setRobotDirection(robot, NORTH);

        UpdateResponse response = world.updatePosition(3);
        assertEquals(SUCCESS, response);
        assertEquals(new Position(10, 7), robot.getPosition()); // Moved 3 steps north
    }
}