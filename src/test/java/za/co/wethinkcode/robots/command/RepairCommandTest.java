package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Tests for the {@link RepairCommand}.
 */
public class RepairCommandTest {

    private World mockWorld;
    private Robot mockRobot;
    private RepairCommand repairCommand;

    /**
     * Prepares mocks and command instance before each test.
     */
    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRobot = mock(Robot.class);
        repairCommand = RepairCommand.getInstance();
        when(mockRobot.getName()).thenReturn("TestBot");
        try {
            Config.loadConfig("config.properties");
            if (Config.REPAIR_DURATION <= 0) {
                Config.REPAIR_DURATION = 5;
            }
        } catch (Exception e) {
            Config.REPAIR_DURATION = 5;
        }
    }

    /**
     * Verifies successful repair execution when robot can repair.
     */
    @Test
    @DisplayName("When current robot exists and repair() returns true, execute returns OK and Done message")
    void execute_currentRobotExistsAndRepairSucceeds_returnsOkResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);
        when(mockRobot.repair()).thenReturn(true);

        JsonObject response = repairCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("OK", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.has("message"));
        assertEquals("Done", data.get("message").getAsString());

        verify(mockRobot).repair();
        assertFalse(response.has("state"));
    }

    /**
     * Verifies error response when robot cannot repair.
     */
    @Test
    @DisplayName("When current robot exists and repair() returns false, execute returns ERROR")
    void execute_currentRobotExistsAndRepairFails_returnsErrorResponse() {
        when(mockWorld.getCurrentRobot()).thenReturn(mockRobot);
        when(mockRobot.repair()).thenReturn(false);

        JsonObject response = repairCommand.execute(mockWorld);

        assertNotNull(response);
        assertEquals("ERROR", response.get("result").getAsString());
        assertTrue(response.has("data"));
        JsonObject data = response.getAsJsonObject("data");
        assertTrue(data.has("message"));
        assertEquals("Repair failed", data.get("message").getAsString());

        verify(mockRobot).repair();
        assertFalse(response.has("state"));
    }

    /**
     * Verifies NullPointerException when no current robot is active.
     */
    @Test
    @DisplayName("When no current robot exists, execute should throw NullPointerException")
    void execute_noCurrentRobot_throwsNullPointerException() {
        when(mockWorld.getCurrentRobot()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            repairCommand.execute(mockWorld);
        });
        verify(mockRobot, never()).repair();
    }


    /**
     * Verifies the command name is "repair".
     */
    @Test
    @DisplayName("Command name should be 'repair'")
    void getName_returnsCorrectCommandName() {
        assertEquals("repair", repairCommand.getName());
    }

    /**
     * Verifies command has no single string argument.
     */
    @Test
    @DisplayName("getArgument should be null for RepairCommand")
    void getArgument_returnsNull() {
        assertNull(repairCommand.getArgument());
    }

    /**
     * Verifies command has no JsonArray arguments.
     */
    @Test
    @DisplayName("getArguments (JsonArray) should be null for RepairCommand")
    void getArguments_JsonArray_returnsNull() {
        assertNull(repairCommand.getArguments());
    }

    /**
     * Verifies singleton behavior of getInstance().
     */
    @Test
    @DisplayName("getInstance should return the same instance (Singleton check)")
    void getInstance_returnsSingletonInstance() {
        RepairCommand instance1 = RepairCommand.getInstance();
        RepairCommand instance2 = RepairCommand.getInstance();
        assertSame(instance1, instance2);
    }
}