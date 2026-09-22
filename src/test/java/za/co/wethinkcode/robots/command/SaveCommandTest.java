package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.DuplicateWorldException;
import za.co.wethinkcode.robots.persistence.PersistenceException;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the server-console {@link SaveCommand}.
 * Persistence is mocked so these tests cover SAVE behaviour, not JDBC.
 */
public class SaveCommandTest {

    private World mockWorld;
    private WorldRepository mockRepository;
    private SaveCommand saveCommand;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        mockWorld = mock(World.class);
        mockRepository = mock(WorldRepository.class);
        saveCommand = new SaveCommand(mockRepository);

        when(mockWorld.getTOP_LEFT()).thenReturn(new Position(0, 0));
        when(mockWorld.getBOTTOM_RIGHT()).thenReturn(new Position(19, 19));
        when(mockWorld.getObstacles()).thenReturn(List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN),
                new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT),
                new Obstacle(5, 5, 6, 6, ObstacleType.LAKE)
        ));
        when(mockWorld.getBots()).thenReturn(List.of(new Robot("HAL", "sniper")));
        when(mockWorld.getRobots()).thenReturn(List.of(new Robot("HAL", "sniper")));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    @DisplayName("SAVE persists world size")
    void save_persistsWorldSize() {
        saveCommand.save(mockWorld);

        WorldSnapshot snapshot = capturedSnapshot();
        assertEquals(20, snapshot.getWidth());
        assertEquals(20, snapshot.getHeight());
        assertTrue(outContent.toString().contains("World saved."));
    }

    @Test
    @DisplayName("SAVE persists obstacles, pits and lakes")
    void save_persistsMapObjects() {
        saveCommand.save(mockWorld);

        WorldSnapshot snapshot = capturedSnapshot();
        assertEquals(3, snapshot.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, snapshot.getObstacles().get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, snapshot.getObstacles().get(1).getType());
        assertEquals(ObstacleType.LAKE, snapshot.getObstacles().get(2).getType());
    }

    @Test
    @DisplayName("SAVE does not persist robot positions or status")
    void save_doesNotPersistRobots() {
        saveCommand.save(mockWorld);

        WorldSnapshot snapshot = capturedSnapshot();
        assertThrows(NoSuchFieldException.class,
                () -> snapshot.getClass().getDeclaredField("robots"));
        verify(mockWorld, never()).getBots();
        verify(mockWorld, never()).getRobots();
    }

    @Test
    @DisplayName("SAVE with no obstacles still persists world size")
    void save_emptyObstacles_stillPersistsSize() {
        when(mockWorld.getObstacles()).thenReturn(List.of());

        saveCommand.save(mockWorld);

        WorldSnapshot snapshot = capturedSnapshot();
        assertEquals(20, snapshot.getWidth());
        assertEquals(20, snapshot.getHeight());
        assertTrue(snapshot.getObstacles().isEmpty());
    }

    @Test
    @DisplayName("SAVE prints a failure message when the repository cannot persist")
    void save_repositoryFailure_printsError() {
        doThrow(new PersistenceException("World save is not implemented yet"))
                .when(mockRepository).save(any(WorldSnapshot.class));

        saveCommand.save(mockWorld);

        assertTrue(outContent.toString().contains("Failed to save world:"));
        assertFalse(outContent.toString().contains("World saved."));
    }

    @Test
    @DisplayName("command name should be 'save'")
    void getName_returnsSave() {
        assertEquals("save", saveCommand.getName());
    }

    @Test
    @DisplayName("execute saves the world and returns an empty JsonObject")
    void execute_savesAndReturnsEmptyJson() {
        JsonObject result = saveCommand.execute(mockWorld);

        verify(mockRepository).save(any(WorldSnapshot.class));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("SAVE <world-name> persists world size under that name")
    void save_newName_persistsWorldSizeUnderThatName() {
        saveCommand.save(mockWorld, "mars");

        WorldSnapshot snapshot = capturedNamedSnapshot("mars");
        assertEquals(20, snapshot.getWidth());
        assertEquals(20, snapshot.getHeight());
        assertTrue(outContent.toString().contains("World saved."));
    }

    @Test
    @DisplayName("SAVE <world-name> persists obstacles, pits and lakes")
    void save_newName_persistsMapObjects() {
        saveCommand.save(mockWorld, "mars");

        WorldSnapshot snapshot = capturedNamedSnapshot("mars");
        assertEquals(3, snapshot.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, snapshot.getObstacles().get(0).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, snapshot.getObstacles().get(1).getType());
        assertEquals(ObstacleType.LAKE, snapshot.getObstacles().get(2).getType());
    }

    @Test
    @DisplayName("SAVE <world-name> does not persist robot positions or status")
    void save_newName_doesNotPersistRobots() {
        saveCommand.save(mockWorld, "mars");

        WorldSnapshot snapshot = capturedNamedSnapshot("mars");
        assertThrows(NoSuchFieldException.class,
                () -> snapshot.getClass().getDeclaredField("robots"));
        verify(mockWorld, never()).getBots();
        verify(mockWorld, never()).getRobots();
    }

    @Test
    @DisplayName("SAVE <world-name> refuses to overwrite an existing name")
    void save_existingName_reportsAlreadyExistsAndDoesNotOverwrite() {
        doThrow(new DuplicateWorldException("A RobotWorld with that name already exists."))
                .when(mockRepository).save(eq("mars"), any(WorldSnapshot.class));

        saveCommand.save(mockWorld, "mars");

        assertTrue(outContent.toString().contains("A RobotWorld with that name already exists."));
        assertFalse(outContent.toString().contains("World saved."));
    }

    private WorldSnapshot capturedSnapshot() {
        ArgumentCaptor<WorldSnapshot> captor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(mockRepository).save(captor.capture());
        return captor.getValue();
    }

    private WorldSnapshot capturedNamedSnapshot(String worldName) {
        ArgumentCaptor<WorldSnapshot> captor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(mockRepository).save(eq(worldName), captor.capture());
        return captor.getValue();
    }
}
