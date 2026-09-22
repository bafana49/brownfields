package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TurnCommandTest {

    private World mockWorld;
    private Robot mockRobot;
    private TurnCommand command;

    /**
     * Prepares mocks and command instance before each test.
     * Sets up the world and robot mocks.
     */
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRobot = mock(Robot.class);
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);

        command = TurnCommand.getInstance("\"right\"");
    }

    /**
     * Tests the turn command execution when the robot turns right.
     * Verifies that the response contains the expected result and data.
     */
    @Test
    @DisplayName("When current robot exists and turns right, execute returns OK and Done message")
    void testTurnRight() {
        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());

        verify(mockRobot).updateDirection(true);
    }

    /**
     * Tests the turn command execution when the robot turns left.
     * Verifies that the response contains the expected result and data.
     */
    @Test
    @DisplayName("When current robot exists and turns left, execute returns OK and Done message")
    void testTurnLeft() {
        command = TurnCommand.getInstance("\"left\"");
        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());

        verify(mockRobot).updateDirection(false);
    }

    /**
     * Tests the turn command execution with an invalid direction argument.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("When current robot exists and turns right with no argument, execute returns OK and Done message")
    void testInvalidDirection() {
        command = TurnCommand.getInstance("invalid");
        JsonObject response = command.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("Invalid command"));

        verify(mockRobot, never()).updateDirection(anyBoolean());
    }

    /**
     * Tests the turn command execution when the robot does not exist.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("When current robot does not exist, execute returns ERROR")
    void testEmptyArgument() {
        TurnCommand emptyCommand = TurnCommand.getInstance("");

        JsonObject response = emptyCommand.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("Invalid command"));

        verify(mockRobot, never()).updateDirection(anyBoolean());
    }

    /**
     * Tests the turn command execution when the current robot is null.
     * Verifies that a NullPointerException is thrown.
     */
    @Test
    @DisplayName("When current robot is null, execute throws NullPointerException")
    void testNullRobot() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> command.execute(mockWorld));
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        TurnCommand instance1 = TurnCommand.getInstance("right");
        TurnCommand instance2 = TurnCommand.getInstance("right");
        assertSame(instance1, instance2);
    }

}
