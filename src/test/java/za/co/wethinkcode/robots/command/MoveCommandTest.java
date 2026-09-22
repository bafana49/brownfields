package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static za.co.wethinkcode.robots.UpdateResponse.*;

public class MoveCommandTest {

    private World mockWorld;
    private MoveCommand command;

    /**
     * Prepares mocks and command instance before each test.
     * Sets up the world and command with initial parameters.
     */
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        command = MoveCommand.getInstance("forward", "5");
    }

    /**
     * Tests the execution of the MoveCommand with a valid forward move.
     * Verifies that the response is successful and contains the expected data.
     */
    @Test
    @DisplayName("When moving forward, execute returns OK and Done message")
    void testForwardMoveSuccess() {
        when(mockWorld.updatePosition(5)).thenReturn(SUCCESS);

        System.out.println("[DEBUG_LOG] Forward command argument: " + command.getArgument());
        System.out.println("[DEBUG_LOG] Forward command name: " + command.getName());

        JsonObject response = command.execute(mockWorld);

        System.out.println("[DEBUG_LOG] Response: " + response.toString());

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());

        verify(mockWorld).updatePosition(5);
    }

    /**
     * Tests the execution of the MoveCommand with a valid backward move.
     * Verifies that the response is successful and contains the expected data.
     */
    @Test
    @DisplayName("When moving backward, execute returns OK and Done message")
    void testBackwardMoveSuccess() {
        when(mockWorld.updatePosition(-3)).thenReturn(SUCCESS);
        command = MoveCommand.getInstance("backward", "3");

        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());

        verify(mockWorld).updatePosition(-3);
    }

    /**
     * Tests the execution of the MoveCommand with a valid left turn.
     * Verifies that the response is successful and contains the expected data.
     */
    @Test
    @DisplayName("When turning left, execute returns OK and Done message")
    void testMoveObstructed() {
        when(mockWorld.updatePosition(5)).thenReturn(FAILURE_OBSTRUCTED);

        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Obstructed", data.get("message").getAsString());
    }

    /**
     * Tests the execution of the MoveCommand when moving out of bounds.
     * Verifies that the response indicates failure due to out of bounds.
     */
    @Test
    @DisplayName("When moving out of bounds, execute returns OK and DIED message")
    void testMoveOutOfBounds() {
        when(mockWorld.updatePosition(5)).thenReturn(FAILURE_OUT_OF_BOUNDS);

        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("DIED: fell out of bounds", data.get("message").getAsString());
    }

    /**
     * Tests the execution of the MoveCommand when the robot falls into a pit.
     * Verifies that the response indicates death due to falling in a pit.
     */
    @Test
    @DisplayName("When falling in a pit, execute returns OK and DIED message")
    void testMoveFellInPit() {
        when(mockWorld.updatePosition(5)).thenReturn(DIED_FELL_IN_PIT);

        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("DIED : fell in a bottomless pit", data.get("message").getAsString());
    }

    /**
     * Tests the MoveCommand execution with an invalid command name.
     * Verifies that an IllegalArgumentException is thrown.
     */
    @Test
    @DisplayName("When command name is invalid, execute throws IllegalArgumentException")
    void testInvalidSteps() {
        command = MoveCommand.getInstance("forward", "");

        JsonObject response = command.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("please enter"));

        verify(mockWorld, never()).updatePosition(anyInt());
    }

    /**
     * Tests the MoveCommand execution with negative steps.
     * Verifies that the response indicates an error due to invalid steps.
     */
    @Test
    @DisplayName("When command name is invalid, execute throws IllegalArgumentException")
    void testNegativeSteps() {
        MoveCommand negativeCommand = MoveCommand.getInstance("forward", "-5");

        JsonObject response = negativeCommand.execute(mockWorld);

        assertEquals("ERROR", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.get("message").getAsString().contains("please enter"));

        verify(mockWorld, never()).updatePosition(anyInt());
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        MoveCommand instance1 = MoveCommand.getInstance("forward", "1");
        MoveCommand instance2 = MoveCommand.getInstance("forward", "1");
        assertSame(instance1, instance2);
    }

    /**
     * Tests the MoveCommand execution when the robot is obstructed by another robot.
     * Verifies that the response indicates obstruction.
     */
    @Test
    @DisplayName("When robot is obstructed by another robot, execute returns OK with Obstructed message")
    void testMoveObstructedByRobot() {
        // Mock the behavior where the robot is obstructed by another robot
        when(mockWorld.updatePosition(5)).thenReturn(FAILURE_OBSTRUCTED);

        JsonObject response = command.execute(mockWorld);

        assertEquals("OK", response.get("result").getAsString());
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Obstructed", data.get("message").getAsString());
    }

    /**
     * Tests the robot obstruction scenario with a forward move and backward retreat.
     * First attempt is obstructed, then the robot retreats successfully.
     */
    @Test
    @DisplayName("Robot obstructed when moving forward but can retreat backward")
    void testRobotObstructedThenRetreats() {
        // Mock first movement (forward) to be obstructed
        when(mockWorld.updatePosition(5)).thenReturn(FAILURE_OBSTRUCTED);

        JsonObject forwardResponse = command.execute(mockWorld);

        assertEquals("OK", forwardResponse.get("result").getAsString());
        JsonObject forwardData = forwardResponse.getAsJsonObject("data");
        assertEquals("Obstructed", forwardData.get("message").getAsString());

        // Mock retreat (backward) to be successful
        when(mockWorld.updatePosition(-3)).thenReturn(SUCCESS);
        command = MoveCommand.getInstance("backward", "3");

        JsonObject backwardResponse = command.execute(mockWorld);

        assertEquals("OK", backwardResponse.get("result").getAsString());
        JsonObject backwardData = backwardResponse.getAsJsonObject("data");
        assertEquals("Done", backwardData.get("message").getAsString());

        // Verify both movements were attempted
        verify(mockWorld).updatePosition(5);
        verify(mockWorld).updatePosition(-3);
    }
}
