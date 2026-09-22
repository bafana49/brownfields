package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateCommandTest {

    private World mockWorld;
    private Robot mockRobot;
    private StateCommand stateCommand;

    /**
     * Prepares mocks and command instance before each test.
     * Sets up the world and robot mocks.
     */
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRobot = mock(Robot.class);
        stateCommand = new StateCommand();
    }

    /**
     * Tests the state command execution when the current robot exists.
     * Verifies that the response contains the expected result and data.
     */
    @Test
    @DisplayName("When current robot exists, execute returns OK and state follows message")
    void execute_currentRobotExists_returnsOkResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);

        JsonObject response = stateCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("OK", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Robot state follows.", data.get("message").getAsString());
        assertFalse(response.has("state"));
    }

    /**
     * Tests the state command execution when no current robot exists.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("When no current robot exists, execute returns ERROR and no active robot message")
    void execute_noCurrentRobot_returnsErrorResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);

        JsonObject response = stateCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("ERROR", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("No active robot. Cannot get state.", data.get("message").getAsString());
        assertFalse(response.has("state"));
    }

    /**
     * Tests the state command execution when the current robot is not initialized.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("Command name should be 'state'")
    void getName_returnsCorrectCommandName() {
        assertEquals("state", stateCommand.getName());
    }

    /**
     * Tests the state command execution when the current robot is not initialized.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("getArgument should be null for StateCommand")
    void getArgument_returnsNull() {
        assertNull(stateCommand.getArgument());
    }

    /**
     * Tests the state command execution when the current robot is not initialized.
     * Verifies that the response contains an error result and appropriate message.
     */
    @Test
    @DisplayName("getArguments (JsonArray) should be null for StateCommand")
    void getArguments_JsonArray_returnsNull() {
        assertNull(stateCommand.getArguments());
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        StateCommand instance1 = StateCommand.getInstance();
        StateCommand instance2 = StateCommand.getInstance();
        assertSame(instance1, instance2);
    }
}
