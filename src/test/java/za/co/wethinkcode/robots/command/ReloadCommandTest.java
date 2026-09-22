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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Tests for the singleton {@link ReloadCommand}.
 */
public class ReloadCommandTest {

    private World mockWorld;
    private Robot mockRobot;
    private ReloadCommand reloadCommand;

    /**
     * Prepares mocks and gets the singleton command instance before each test.
     */
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRobot = mock(Robot.class);
        reloadCommand = ReloadCommand.getInstance();
    }

    /**
     * Verifies successful reload execution when robot can reload.
     */
    @Test
    @DisplayName("When current robot exists and reload returns true, execute returns OK and Done message")
    void execute_currentRobotExistsAndReloadSucceeds_returnsOkResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);
        when(mockRobot.reload()).thenReturn(true);

        JsonObject response = reloadCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("OK", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.has("message"));
        assertEquals("Done", data.get("message").getAsString());

        verify(mockRobot).reload();
        assertFalse(response.has("state"));
    }

    /**
     * Verifies error response when robot cannot reload.
     */
    @Test
    @DisplayName("When current robot exists and reload returns false, execute returns ERROR")
    void execute_currentRobotExistsAndReloadFails_returnsErrorResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);
        when(mockRobot.reload()).thenReturn(false);

        JsonObject response = reloadCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("ERROR", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.has("message"));
        assertEquals("Reload failed", data.get("message").getAsString());

        verify(mockRobot).reload();
        assertFalse(response.has("state"));
    }

    /**
     * Verifies NullPointerException when no current robot is active,
     * reflecting current ReloadCommand behavior without an explicit null check.
     */
    @Test
    @DisplayName("When no current robot exists, execute should throw NullPointerException")
    void execute_noCurrentRobot_throwsNullPointerException() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            reloadCommand.execute(mockWorld);
        });
        verify(mockRobot, never()).reload();
    }


    /**
     * Verifies the command name is "reload".
     */
    @Test
    @DisplayName("Command name should be 'reload'")
    void getName_returnsCorrectCommandName() {
        assertEquals("reload", reloadCommand.getName());
    }

    /**
     * Verifies command has no single string argument.
     */
    @Test
    @DisplayName("getArgument should be null for ReloadCommand")
    void getArgument_returnsNull() {
        assertNull(reloadCommand.getArgument());
    }

    /**
     * Verifies command has no JsonArray arguments.
     */
    @Test
    @DisplayName("getArguments (JsonArray) should be null for ReloadCommand")
    void getArguments_JsonArray_returnsNull() {
        assertNull(reloadCommand.getArguments());
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        ReloadCommand instance1 = ReloadCommand.getInstance();
        ReloadCommand instance2 = ReloadCommand.getInstance();
        assertSame(instance1, instance2);
        assertSame(reloadCommand, instance1);
    }
}