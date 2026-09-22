package za.co.wethinkcode.robots.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.command.look.LookCommand;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LookCommandTest {

    private World mockWorld;
    private Robot currentRobot;
    private Robot otherRobot;
    private LookCommand lookCommand;
    private Obstacle obstacle;

    /**
     * Prepares mocks and command instance before each test.
     * Sets up the world, robots, and obstacles.
//     */


//    test
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        currentRobot = mock(Robot.class);
        otherRobot = mock(Robot.class);
        lookCommand = LookCommand.getInstance();
        obstacle = mock(Obstacle.class);

        Config.VISIBILITY = 10;
        Config.WIDTH = 20;
        Config.HEIGHT = 20;

        when(mockWorld.getCurrentRobot()).thenReturn(currentRobot);
        when(currentRobot.getPosition()).thenReturn(new Position(5, 5));
        when(currentRobot.getName()).thenReturn("CurrentBot");
        when(otherRobot.getName()).thenReturn("OtherBot");
    }

    /**
     * Tests the look command execution when there are no obstacles or other robots.
     * Verifies that the response contains the expected result and data.
     */
    @Test
    @DisplayName("When no obstacles or other robots, execute returns OK with edge objects")
    void testLookWithNoObjects() {
        when(mockWorld.getObstacles()).thenReturn(new ArrayList<>());
        when(mockWorld.getBots()).thenReturn(List.of(currentRobot));

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        assertTrue(objects.size()>0, "Should see at least one edge");
        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            assertEquals("EDGE", obj.get("type").getAsString());
        }
    }

    /**
     * Tests the look command execution with another robot in the world.
     * Verifies that the response contains the expected robot information.
     */
    @Test
    @DisplayName("When another robot is present, execute returns OK with robot data")
    void testLookWithRobot() {
        when(otherRobot.getPosition()).thenReturn(new Position(5, 8));
        when(mockWorld.getObstacles()).thenReturn(new ArrayList<>());
        when(mockWorld.getBots()).thenReturn(Arrays.asList(currentRobot, otherRobot));

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        boolean foundRobot = false;
        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            if (obj.get("type").getAsString().equals("ROBOT")) {
                foundRobot = true;
                assertEquals("SOUTH", obj.get("direction").getAsString());
                assertEquals(3, obj.get("distance").getAsInt());
            }
        }
        assertTrue(foundRobot, "Should have found a robot to the north");
    }

    /**
     * Tests the look command execution with an obstacle in the world.
     * Verifies that the response contains the expected obstacle information.
     */
    @Test
    @DisplayName("When an obstacle is present, execute returns OK with obstacle data")
    void testLookWithObstacle() {
        when(currentRobot.getPosition()).thenReturn(new Position(3, 5));
        when(obstacle.getTopLeft()).thenReturn(new Position(6, 5));
        when(obstacle.getBottomRight()).thenReturn(new Position(10, 10));
        when(mockWorld.getObstacles()).thenReturn(List.of(obstacle));
        when(mockWorld.getBots()).thenReturn(List.of(currentRobot));
        when(obstacle.getType()).thenReturn(ObstacleType.MOUNTAIN);

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        boolean foundObstacle = false;
        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            if (obj.get("type").getAsString().equals("OBSTACLE") &&
                obj.get("direction").getAsString().equals("EAST")) {
                foundObstacle = true;
                assertEquals(3, obj.get("distance").getAsInt());
            }
        }
        assertTrue(foundObstacle, "Should have found an obstacle to the east");
    }

    /**
     * Tests the look command execution with multiple objects in the world.
     * Verifies that the response contains both the robot and obstacle information.
     */
    @Test
    @DisplayName("When multiple objects are present, execute returns OK with both robot and obstacle data")
    void testLookWithMultipleObjects() {
        when(obstacle.blocksPosition(any())).thenReturn(false);
        when(currentRobot.getPosition()).thenReturn(new Position(3, 5));
        when(otherRobot.getPosition()).thenReturn(new Position(5, 5));
        when(mockWorld.getBots()).thenReturn(List.of(currentRobot, otherRobot));
        when(obstacle.getBottomRight()).thenReturn(new Position(10, 10));
        when(obstacle.getType()).thenReturn(ObstacleType.MOUNTAIN);
        when(obstacle.getTopLeft()).thenReturn(new Position(7, 5));
        when(mockWorld.getObstacles()).thenReturn(List.of(obstacle));

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        boolean foundRobot = false;
        boolean foundObstacle = false;
        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            if (obj.get("type").getAsString().equals("ROBOT")) {
                foundRobot = true;
            } else if (obj.get("type").getAsString().equals("OBSTACLE") &&
                       obj.get("direction").getAsString().equals("EAST")) {
                foundObstacle = true;
            }
        }
        assertTrue(foundRobot, "Should have found a robot");
        assertTrue(foundObstacle, "Should have found an obstacle");
    }

    /**
     * Tests the look command execution with an obstacle that blocks the robot's view.
     * Verifies that the response does not include the blocked robot.
     */
    @Test
    @DisplayName("When an obstacle blocks the view, execute does not return the blocked robot")
    void testLookWithNoCurrentRobot() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("No robot context active"));
    }

    /**
     * Tests the look command execution when the robot's position is not set.
     * Verifies that the response indicates an error due to no robot position.
     */
    @Test
    @DisplayName("When robot position is not set, execute returns ERROR with no robot position message")
    void testLookWithNoRobotPosition() {
        when(currentRobot.getPosition()).thenReturn(null);

        JsonObject response = lookCommand.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("Robot has not been positioned"));
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        LookCommand instance1 = LookCommand.getInstance();
        LookCommand instance2 = LookCommand.getInstance();
        assertSame(instance1, instance2);
    }
}
