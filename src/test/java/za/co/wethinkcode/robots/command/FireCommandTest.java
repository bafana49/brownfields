package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static za.co.wethinkcode.robots.Direction.SOUTH;

public class FireCommandTest {
    private World mockWorld;
    private Robot shooter;
    private Robot target;
    private FireCommand fireCommand;
    private Obstacle mountainObstacle;

    /**
     * Prepares mocks and command instance before each test.
     * Sets up the shooter, target, and obstacle.
     */
    @BeforeEach
    public void setUp() {
        mockWorld = mock(World.class);
        shooter = mock(Robot.class);
        target = mock(Robot.class);
        mountainObstacle = mock(Obstacle.class);
        fireCommand = FireCommand.getInstance();

        when(mockWorld.getCurrentRobot()).thenReturn(shooter);
        when(mountainObstacle.getType()).thenReturn(ObstacleType.MOUNTAIN);
    }

    /**
     * Tests the fire command execution when the shooter hits the target.
     * Verifies that the response contains the expected result and data.
     */
    @Test
    @DisplayName("When shooter hits target, execute returns OK with Hit message and target data")
    public void testFireHitsTarget() {
        when(shooter.getName()).thenReturn("ShooterBot");
        when(target.getName()).thenReturn("EnemyBot");
        when(mockWorld.getBots()).thenReturn(Arrays.asList(shooter, target));
        when(shooter.getShots()).thenReturn(5);
        when(shooter.hit(target)).thenReturn(true);
        when(shooter.getPosition()).thenReturn(new Position(0, 0));
        when(shooter.getCurrentDirection()).thenReturn(SOUTH);
        when(target.getPosition()).thenReturn(new Position(0, 1));

        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Hit", data.get("message").getAsString());
        assertEquals("EnemyBot", data.get("robot").getAsString());
        assertTrue(data.has("distance"));
        assertTrue(data.has("state"));
    }

    /**
     * Tests the fire command execution when the shooter misses all targets.
     * Verifies that the response indicates a miss and does not include robot or distance data.
     */
    @Test
    @DisplayName("When shooter misses all targets, execute returns OK with Miss message and no robot or distance data")
    public void testFireMissesAllTargets() {
        when(mockWorld.getBots()).thenReturn(Arrays.asList(shooter, target));
        when(shooter.getShots()).thenReturn(5);
        when(shooter.hit(target)).thenReturn(false);
        when(shooter.getPosition()).thenReturn(new Position(0, 0));
        when(shooter.getCurrentDirection()).thenReturn(SOUTH);
        when(target.getPosition()).thenReturn(new Position(0, 28));
        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Miss", data.get("message").getAsString());
        assertFalse(data.has("robot"));
        assertFalse(data.has("distance"));
    }

    /**
     * Tests the fire command execution when the shooter has no ammo.
     * Verifies that the response indicates out of ammo and decrements the shot count.
     */
    @Test
    @DisplayName("When shooter has no ammo, execute returns OK with Out of ammo message and decrements shot count")
    public void testFireOutOfAmmo() {
        when(mockWorld.getBots()).thenReturn(Arrays.asList(shooter, target));
        when(shooter.getShots()).thenReturn(0);
        when(shooter.getPosition()).thenReturn(new Position(0, 0));
        when(shooter.getCurrentDirection()).thenReturn(SOUTH);
        when(target.getPosition()).thenReturn(new Position(0, 5));

        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Out of ammo.", data.get("message").getAsString());

        verify(shooter).decrementShot();
    }

    /**
     * Tests the fire command execution when the shooter hits an obstacle.
     * Verifies that the response indicates a miss due to an obstacle.
     */
    @Test
    @DisplayName("When fire hits an obstacle, execute returns OK with Miss message and decrements shot count")
    public void testFireHitsObstacle() {
        when(mockWorld.getBots()).thenReturn(Arrays.asList(shooter, target));
        when(shooter.getShots()).thenReturn(5);
        when(shooter.getPosition()).thenReturn(new Position(0, 0));
        when(shooter.getCurrentDirection()).thenReturn(SOUTH);
        when(shooter.getBulletDistance()).thenReturn(5);
        when(target.getPosition()).thenReturn(new Position(0, 10));

        List<Obstacle> obstacles = Collections.singletonList(mountainObstacle);
        when(mockWorld.getObstacles()).thenReturn(obstacles);
        when(mockWorld.isMovementObstructed(any(Position.class))).thenReturn(true);

        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Miss", data.get("message").getAsString());


        verify(shooter).decrementShot();
    }

    /**
     * Tests the fire command execution when there are no obstacles.
     * Verifies that the response indicates a miss and does not include obstacle data.
     */
    @Test
    @DisplayName("When there are no obstacles, execute returns OK with Miss message and decrements shot count")
    public void testFireDecrementsAmmo() {
        when(mockWorld.getBots()).thenReturn(Arrays.asList(shooter, target));
        when(shooter.getShots()).thenReturn(5);
        when(shooter.getPosition()).thenReturn(new Position(0, 0));
        when(shooter.getCurrentDirection()).thenReturn(SOUTH);
        when(target.getPosition()).thenReturn(new Position(0, 20));

        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());

        verify(shooter).decrementShot();
    }

    /**
     * Tests the fire command execution when there is no current robot.
     * Verifies that the response indicates an error due to no active robot context.
     */
    @Test
    @DisplayName("When no current robot exists, execute returns ERROR and no active robot message")
    public void testFireNoCurrentRobot() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);
        JsonObject response = fireCommand.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());

    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        FireCommand instance1 = FireCommand.getInstance();
        FireCommand instance2 = FireCommand.getInstance();
        assertSame(instance1, instance2);
    }
}
