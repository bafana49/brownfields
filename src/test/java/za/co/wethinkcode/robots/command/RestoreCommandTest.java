package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.PersistenceException;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the server-console {@link RestoreCommand}.
 * Persistence is mocked so these tests cover RESTORE behaviour, not JDBC.
 * The command must load a snapshot through {@link WorldRepository} and apply it
 * to the live world — database access stays out of domain code.
 */
public class RestoreCommandTest {

    private World mockWorld;
    private WorldRepository mockRepository;
    private RestoreCommand restoreCommand;
    private WorldSnapshot savedSnapshot;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        mockWorld = mock(World.class);
        mockRepository = mock(WorldRepository.class);
        restoreCommand = new RestoreCommand(mockRepository);

        savedSnapshot = new WorldSnapshot(20, 15, List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN),
                new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT),
                new Obstacle(5, 5, 6, 6, ObstacleType.LAKE)
        ));
        when(mockRepository.find()).thenReturn(Optional.of(savedSnapshot));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    @DisplayName("RESTORE loads world size from the repository")
    void restore_loadsWorldSize() {
        restoreCommand.restore(mockWorld);

        WorldSnapshot applied = capturedSnapshot();
        assertEquals(20, applied.getWidth());
        assertEquals(15, applied.getHeight());
        assertTrue(outContent.toString().contains("World restored."));
    }

    @Test
    @DisplayName("RESTORE loads obstacles, pits and lakes")
    void restore_loadsMapObjects() {
        restoreCommand.restore(mockWorld);

        WorldSnapshot applied = capturedSnapshot();
        assertEquals(3, applied.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, applied.getObstacles().get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, applied.getObstacles().get(1).getType());
        assertEquals(ObstacleType.LAKE, applied.getObstacles().get(2).getType());
    }

    @Test
    @DisplayName("RESTORE does not load robot positions or status")
    void restore_doesNotLoadRobots() {
        restoreCommand.restore(mockWorld);

        WorldSnapshot applied = capturedSnapshot();
        assertThrows(NoSuchFieldException.class,
                () -> applied.getClass().getDeclaredField("robots"));
        verify(mockWorld, never()).getBots();
        verify(mockWorld, never()).getRobots();
        verify(mockWorld, never()).addRobot(any());
    }

    @Test
    @DisplayName("RESTORE with no obstacles still restores world size")
    void restore_emptyObstacles_stillRestoresSize() {
        WorldSnapshot emptyMap = new WorldSnapshot(7, 9, List.of());
        when(mockRepository.find()).thenReturn(Optional.of(emptyMap));

        restoreCommand.restore(mockWorld);

        WorldSnapshot applied = capturedSnapshot();
        assertEquals(7, applied.getWidth());
        assertEquals(9, applied.getHeight());
        assertTrue(applied.getObstacles().isEmpty());
    }

    @Test
    @DisplayName("RESTORE prints a message when nothing has been saved")
    void restore_whenNothingSaved_printsMessage() {
        when(mockRepository.find()).thenReturn(Optional.empty());

        restoreCommand.restore(mockWorld);

        verify(mockWorld, never()).restore(any(WorldSnapshot.class));
        assertTrue(outContent.toString().contains("No saved world to restore."));
        assertFalse(outContent.toString().contains("World restored."));
    }

    @Test
    @DisplayName("RESTORE prints a failure message when the repository cannot load")
    void restore_repositoryFailure_printsError() {
        when(mockRepository.find())
                .thenThrow(new PersistenceException("World restore is not implemented yet"));

        restoreCommand.restore(mockWorld);

        verify(mockWorld, never()).restore(any(WorldSnapshot.class));
        assertTrue(outContent.toString().contains("Failed to restore world:"));
        assertFalse(outContent.toString().contains("World restored."));
    }

    @Test
    @DisplayName("command name should be 'restore'")
    void getName_returnsRestore() {
        assertEquals("restore", restoreCommand.getName());
    }

    @Test
    @DisplayName("execute restores the world and returns an empty JsonObject")
    void execute_restoresAndReturnsEmptyJson() {
        JsonObject result = restoreCommand.execute(mockWorld);

        verify(mockRepository).find();
        verify(mockWorld).restore(savedSnapshot);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("RESTORE reads through the repository instead of using JDBC in the command")
    void restore_usesRepositoryFindOnly() {
        restoreCommand.restore(mockWorld);

        verify(mockRepository, times(1)).find();
        verify(mockRepository, never()).save(any());
        verify(mockWorld).restore(savedSnapshot);
    }

    @Test
    @DisplayName("RESTORE <world-name> loads world size from the named repository entry")
    void restore_namedWorld_loadsWorldSize() {
        when(mockRepository.find("mars")).thenReturn(Optional.of(savedSnapshot));

        restoreCommand.restore(mockWorld, "mars");

        WorldSnapshot applied = capturedSnapshot();
        assertEquals(20, applied.getWidth());
        assertEquals(15, applied.getHeight());
        assertTrue(outContent.toString().contains("World restored."));
    }

    @Test
    @DisplayName("RESTORE <world-name> loads obstacles, pits and lakes")
    void restore_namedWorld_loadsMapObjects() {
        when(mockRepository.find("mars")).thenReturn(Optional.of(savedSnapshot));

        restoreCommand.restore(mockWorld, "mars");

        WorldSnapshot applied = capturedSnapshot();
        assertEquals(3, applied.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, applied.getObstacles().get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, applied.getObstacles().get(1).getType());
        assertEquals(ObstacleType.LAKE, applied.getObstacles().get(2).getType());
    }

    @Test
    @DisplayName("RESTORE <world-name> applies the named snapshot to the live world")
    void restore_namedWorld_appliesSnapshotToLiveWorld() {
        when(mockRepository.find("mars")).thenReturn(Optional.of(savedSnapshot));

        restoreCommand.restore(mockWorld, "mars");

        verify(mockRepository).find("mars");
        verify(mockWorld).restore(savedSnapshot);
    }

    @Test
    @DisplayName("RESTORE <world-name> does not load robot positions or status")
    void restore_namedWorld_doesNotLoadRobots() {
        when(mockRepository.find("mars")).thenReturn(Optional.of(savedSnapshot));

        restoreCommand.restore(mockWorld, "mars");

        WorldSnapshot applied = capturedSnapshot();
        assertThrows(NoSuchFieldException.class,
                () -> applied.getClass().getDeclaredField("robots"));
        verify(mockWorld, never()).getBots();
        verify(mockWorld, never()).getRobots();
        verify(mockWorld, never()).addRobot(any());
    }

    @Test
    @DisplayName("RESTORE <world-name> reports a missing world and leaves the live world unchanged")
    void restore_missingName_reportsDoesNotExistAndLeavesWorldUnchanged() {
        when(mockRepository.find("pluto")).thenReturn(Optional.empty());

        restoreCommand.restore(mockWorld, "pluto");

        verify(mockWorld, never()).restore(any(WorldSnapshot.class));
        assertTrue(outContent.toString().contains("The specified world does not exist."));
        assertFalse(outContent.toString().contains("World restored."));
    }

    @Test
    @DisplayName("RESTORE <world-name> looks up by name instead of the unnamed find")
    void restore_namedWorld_findsByNameNotUnnamedFind() {
        when(mockRepository.find("mars")).thenReturn(Optional.of(savedSnapshot));

        restoreCommand.restore(mockWorld, "mars");

        verify(mockRepository).find("mars");
        verify(mockRepository, never()).save(anyString(), any());
    }

    private WorldSnapshot capturedSnapshot() {
        ArgumentCaptor<WorldSnapshot> captor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(mockWorld).restore(captor.capture());
        return captor.getValue();
    }
}
