package za.co.wethinkcode.robots.acceptance.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.command.RestoreCommand;
import za.co.wethinkcode.robots.command.SaveCommand;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.JdbcWorldRepository;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story: Save and restore multiple named RobotWorlds
 *
 * As a RobotWorld administrator
 * I want to save and restore multiple RobotWorlds, each identified by a unique name
 * So that I can store and reuse different RobotWorld configurations.
 *
 * These UATs drive the server-console SAVE / RESTORE commands against a live world
 * and a real JDBC repository (in-memory SQLite), without starting the TCP server.
 */
public class NamedRobotWorldAcceptanceTest {

    private Connection connection;
    private WorldRepository repository;
    private SaveCommand saveCommand;
    private RestoreCommand restoreCommand;
    private World liveWorld;
    private int configuredWidth;
    private int configuredHeight;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        Config.loadConfig("config.properties");
        configuredWidth = Config.WIDTH;
        configuredHeight = Config.HEIGHT;

        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        repository = new JdbcWorldRepository(connection);
        saveCommand = new SaveCommand(repository);
        restoreCommand = new RestoreCommand(repository);

        liveWorld = new World(false);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        Config.WIDTH = configuredWidth;
        Config.HEIGHT = configuredHeight;
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Scenario: Save a named RobotWorld
     *
     * Given a RobotWorld exists
     * When the administrator enters SAVE &lt;world-name&gt;
     * Then the RobotWorld is saved to the database under the specified name
     * And its world size and obstacles, pits, and mines are saved
     * And the robots and their current states are not saved.
     */
    @Test
    void saveNamedWorld_persistsSizeAndMapObjectsButNotRobots() {
        // Given a RobotWorld exists
        givenLiveWorld(20, 15, List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN),
                new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT),
                new Obstacle(5, 5, 6, 6, ObstacleType.LAKE)
        ));
        liveWorld.addRobot(new Robot("HAL", "sniper"));
        assertEquals(1, liveWorld.getRobots().size());

        // When the administrator enters SAVE mars-arena
        saveCommand.save(liveWorld, "mars-arena");

        // Then the RobotWorld is saved to the database under the specified name
        assertTrue(console().contains("World saved."));
        WorldSnapshot saved = repository.find("mars-arena")
                .orElseThrow(() -> new AssertionError("Expected world 'mars-arena' in the database"));

        // And its world size and obstacles, pits, and mines are saved
        assertEquals(20, saved.getWidth());
        assertEquals(15, saved.getHeight());
        assertEquals(3, saved.getObstacles().size());
        assertTrue(saved.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.MOUNTAIN));
        assertTrue(saved.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.BOTTOMLESS_PIT));
        assertTrue(saved.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.LAKE));

        // And the robots and their current states are not saved
        assertThrows(NoSuchFieldException.class,
                () -> saved.getClass().getDeclaredField("robots"));
        assertThrows(NoSuchMethodException.class,
                () -> WorldSnapshot.class.getMethod("getRobots"));
        assertEquals(1, liveWorld.getRobots().size(),
                "SAVE must not remove robots from the live world");
    }

    /**
     * Scenario: Restore a named RobotWorld
     *
     * Given a RobotWorld with the specified name exists in the database
     * When the administrator enters RESTORE &lt;world-name&gt;
     * Then the saved RobotWorld is loaded
     * And the live server world is restored to the saved configuration.
     */
    @Test
    void restoreNamedWorld_loadsSavedConfigurationIntoLiveWorld() {
        // Given a RobotWorld with the specified name exists in the database
        WorldSnapshot stored = new WorldSnapshot(20, 15, List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN),
                new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT),
                new Obstacle(5, 5, 6, 6, ObstacleType.LAKE)
        ));
        repository.save("mars-arena", stored);

        givenLiveWorld(8, 8, List.of(
                new Obstacle(0, 0, 0, 0, ObstacleType.LAKE)
        ));
        liveWorld.addRobot(new Robot("R2D2", "soldier"));

        // When the administrator enters RESTORE mars-arena
        restoreCommand.restore(liveWorld, "mars-arena");

        // Then the saved RobotWorld is loaded
        assertTrue(console().contains("World restored."));

        // And the live server world is restored to the saved configuration
        WorldSnapshot live = WorldSnapshot.from(liveWorld);
        assertEquals(20, live.getWidth());
        assertEquals(15, live.getHeight());
        assertEquals(3, liveWorld.getObstacles().size());
        assertTrue(liveWorld.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.MOUNTAIN));
        assertTrue(liveWorld.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.BOTTOMLESS_PIT));
        assertTrue(liveWorld.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.LAKE));
        assertTrue(liveWorld.getRobots().isEmpty(),
                "Restored configuration must not include previously live robots");
    }

    /**
     * Scenario: Restore a world that does not exist
     *
     * Given no RobotWorld with the specified name exists in the database
     * When the administrator enters RESTORE &lt;world-name&gt;
     * Then the server reports that the specified world does not exist
     * And the current RobotWorld remains unchanged.
     */
    @Test
    void restoreMissingName_reportsDoesNotExistAndLeavesLiveWorldUnchanged() {
        // Given no RobotWorld with the specified name exists in the database
        givenLiveWorld(10, 12, List.of(
                new Obstacle(2, 2, 3, 3, ObstacleType.MOUNTAIN)
        ));
        liveWorld.addRobot(new Robot("HAL", "sniper"));
        WorldSnapshot before = WorldSnapshot.from(liveWorld);

        assertTrue(repository.find("pluto").isEmpty());

        // When the administrator enters RESTORE pluto
        restoreCommand.restore(liveWorld, "pluto");

        // Then the server reports that the specified world does not exist
        assertTrue(console().contains("The specified world does not exist."));
        assertFalse(console().contains("World restored."));

        // And the current RobotWorld remains unchanged
        WorldSnapshot after = WorldSnapshot.from(liveWorld);
        assertEquals(before.getWidth(), after.getWidth());
        assertEquals(before.getHeight(), after.getHeight());
        assertEquals(before.getObstacles().size(), after.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, liveWorld.getObstacles().get(0).getType());
        assertEquals(1, liveWorld.getRobots().size());
        assertEquals("HAL", liveWorld.getRobots().get(0).getName());
    }

    /**
     * Scenario: Save a world using an existing name
     *
     * Given a RobotWorld with the specified name already exists in the database
     * When the administrator enters SAVE &lt;world-name&gt;
     * Then the server refuses to overwrite the existing world
     * And reports that a RobotWorld with that name already exists
     * And the current database entry remains unchanged.
     */
    @Test
    void saveExistingName_refusesOverwriteAndLeavesDatabaseUnchanged() {
        // Given a RobotWorld with the specified name already exists in the database
        Obstacle originalMountain = new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN);
        repository.save("mars-arena", new WorldSnapshot(5, 5, List.of(originalMountain)));

        givenLiveWorld(8, 8, List.of(
                new Obstacle(4, 4, 4, 4, ObstacleType.LAKE)
        ));

        // When the administrator enters SAVE mars-arena
        saveCommand.save(liveWorld, "mars-arena");

        // Then the server refuses to overwrite the existing world
        // And reports that a RobotWorld with that name already exists
        assertTrue(console().contains("A RobotWorld with that name already exists."));
        assertFalse(console().contains("World saved."));

        // And the current database entry remains unchanged
        WorldSnapshot stored = repository.find("mars-arena").orElseThrow();
        assertEquals(5, stored.getWidth());
        assertEquals(5, stored.getHeight());
        assertEquals(1, stored.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, stored.getObstacles().get(0).getType());
        assertEquals(1, stored.getObstacles().get(0).getTopLeft().getX());
    }

    /**
     * Scenario: Save a world using a new name
     *
     * Given no RobotWorld with the specified name exists in the database
     * When the administrator enters SAVE &lt;world-name&gt;
     * Then a new RobotWorld is created in the database with that name.
     */
    @Test
    void saveNewName_createsADistinctNamedWorld() {
        // Given another named world already exists, but not the name being saved
        repository.save("mars-arena", new WorldSnapshot(5, 5, List.of(
                new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN)
        )));
        assertTrue(repository.find("venus-basin").isEmpty());

        givenLiveWorld(7, 9, List.of(
                new Obstacle(2, 2, 2, 2, ObstacleType.LAKE)
        ));

        // When the administrator enters SAVE venus-basin
        saveCommand.save(liveWorld, "venus-basin");

        // Then a new RobotWorld is created in the database with that name
        assertTrue(console().contains("World saved."));
        WorldSnapshot venus = repository.find("venus-basin")
                .orElseThrow(() -> new AssertionError("Expected a new world 'venus-basin' in the database"));
        assertEquals(7, venus.getWidth());
        assertEquals(9, venus.getHeight());
        assertEquals(ObstacleType.LAKE, venus.getObstacles().get(0).getType());

        WorldSnapshot mars = repository.find("mars-arena").orElseThrow();
        assertEquals(5, mars.getWidth());
        assertEquals(ObstacleType.MOUNTAIN, mars.getObstacles().get(0).getType());
    }

    private void givenLiveWorld(int width, int height, List<Obstacle> obstacles) {
        liveWorld.restore(new WorldSnapshot(width, height, obstacles));
    }

    private String console() {
        return outContent.toString();
    }
}

