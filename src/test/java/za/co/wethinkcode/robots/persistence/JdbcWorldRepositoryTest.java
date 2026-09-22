package za.co.wethinkcode.robots.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JDBC world repository.
 * Uses an in-memory SQLite database so the real JDBC adapter is the unit under test,
 * without starting the game server.
 *
 * SAVE writes a snapshot; RESTORE loads it back through {@link WorldRepository#find()}.
 */
public class JdbcWorldRepositoryTest {

    private Connection connection;
    private WorldRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        repository = new JdbcWorldRepository(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("save then find returns the world size")
    void save_thenFind_returnsWorldSize() {
        WorldSnapshot snapshot = new WorldSnapshot(20, 15, List.of());

        repository.save(snapshot);

        WorldSnapshot loaded = repository.find().orElseThrow();
        assertEquals(20, loaded.getWidth());
        assertEquals(15, loaded.getHeight());
    }

    @Test
    @DisplayName("save then find returns obstacles, pits and lakes")
    void save_thenFind_returnsMapObjects() {
        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        Obstacle pit = new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT);
        Obstacle lake = new Obstacle(5, 5, 6, 6, ObstacleType.LAKE);
        WorldSnapshot snapshot = new WorldSnapshot(20, 20, List.of(mountain, pit, lake));

        repository.save(snapshot);

        WorldSnapshot loaded = repository.find().orElseThrow();
        assertEquals(3, loaded.getObstacles().size());
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.MOUNTAIN));
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.BOTTOMLESS_PIT));
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.LAKE));
        assertEquals(1, loaded.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.MOUNTAIN)
                .findFirst().orElseThrow()
                .getTopLeft().getX());
    }

    @Test
    @DisplayName("saved world does not include robot positions or status")
    void save_doesNotStoreRobots() {
        WorldSnapshot snapshot = new WorldSnapshot(10, 10, List.of(
                new Obstacle(0, 0, 0, 0, ObstacleType.MOUNTAIN)
        ));

        repository.save(snapshot);

        WorldSnapshot loaded = repository.find().orElseThrow();
        assertThrows(NoSuchFieldException.class,
                () -> loaded.getClass().getDeclaredField("robots"));
        assertEquals(1, loaded.getObstacles().size());
    }

    @Test
    @DisplayName("save with no obstacles still stores world size")
    void save_emptyObstacles_stillStoresSize() {
        repository.save(new WorldSnapshot(7, 9, List.of()));

        WorldSnapshot loaded = repository.find().orElseThrow();
        assertEquals(7, loaded.getWidth());
        assertEquals(9, loaded.getHeight());
        assertTrue(loaded.getObstacles().isEmpty());
    }

    @Test
    @DisplayName("a later SAVE replaces the previously saved world")
    void save_overwritesPreviousWorld() {
        repository.save(new WorldSnapshot(5, 5, List.of(
                new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN)
        )));
        repository.save(new WorldSnapshot(8, 8, List.of(
                new Obstacle(2, 2, 2, 2, ObstacleType.LAKE)
        )));

        WorldSnapshot loaded = repository.find().orElseThrow();
        assertEquals(8, loaded.getWidth());
        assertEquals(8, loaded.getHeight());
        assertEquals(1, loaded.getObstacles().size());
        assertEquals(ObstacleType.LAKE, loaded.getObstacles().get(0).getType());
    }

    @Test
    @DisplayName("find returns empty when nothing has been saved")
    void find_whenNothingSaved_returnsEmpty() {
        Optional<WorldSnapshot> loaded = repository.find();

        assertTrue(loaded.isEmpty());
    }

    @Test
    @DisplayName("RESTORE load returns the world previously persisted by SAVE")
    void restoreLoad_returnsSavedWorld() {
        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        Obstacle pit = new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT);
        Obstacle lake = new Obstacle(5, 5, 6, 6, ObstacleType.LAKE);
        repository.save(new WorldSnapshot(20, 15, List.of(mountain, pit, lake)));

        WorldSnapshot loaded = repository.find().orElseThrow();

        assertEquals(20, loaded.getWidth());
        assertEquals(15, loaded.getHeight());
        assertEquals(3, loaded.getObstacles().size());
        assertEquals(1, loaded.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.MOUNTAIN)
                .findFirst().orElseThrow()
                .getTopLeft().getX());
        assertEquals(4, loaded.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.BOTTOMLESS_PIT)
                .findFirst().orElseThrow()
                .getBottomRight().getY());
        assertEquals(ObstacleType.LAKE, loaded.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.LAKE)
                .findFirst().orElseThrow()
                .getType());
        assertThrows(NoSuchFieldException.class,
                () -> loaded.getClass().getDeclaredField("robots"));
    }

    @Test
    @DisplayName("RESTORE load returns empty when the database has no saved world")
    void restoreLoad_whenNothingSaved_returnsEmpty() {
        assertTrue(repository.find().isEmpty());
    }

    @Test
    @DisplayName("save then find by name returns the world size")
    void save_thenFindByName_returnsWorldSize() {
        WorldSnapshot snapshot = new WorldSnapshot(20, 15, List.of());

        repository.save("mars", snapshot);

        WorldSnapshot loaded = repository.find("mars").orElseThrow();
        assertEquals(20, loaded.getWidth());
        assertEquals(15, loaded.getHeight());
    }

    @Test
    @DisplayName("save then find by name returns obstacles, pits and lakes")
    void save_thenFindByName_returnsMapObjects() {
        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        Obstacle pit = new Obstacle(3, 3, 4, 4, ObstacleType.BOTTOMLESS_PIT);
        Obstacle lake = new Obstacle(5, 5, 6, 6, ObstacleType.LAKE);

        repository.save("mars", new WorldSnapshot(20, 20, List.of(mountain, pit, lake)));

        WorldSnapshot loaded = repository.find("mars").orElseThrow();
        assertEquals(3, loaded.getObstacles().size());
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.MOUNTAIN));
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.BOTTOMLESS_PIT));
        assertTrue(loaded.getObstacles().stream().anyMatch(o -> o.getType() == ObstacleType.LAKE));
        assertEquals(1, loaded.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.MOUNTAIN)
                .findFirst().orElseThrow()
                .getTopLeft().getX());
    }

    @Test
    @DisplayName("named save does not store robot positions or status")
    void save_byName_doesNotStoreRobots() {
        repository.save("mars", new WorldSnapshot(10, 10, List.of(
                new Obstacle(0, 0, 0, 0, ObstacleType.MOUNTAIN)
        )));

        WorldSnapshot loaded = repository.find("mars").orElseThrow();
        assertThrows(NoSuchFieldException.class,
                () -> loaded.getClass().getDeclaredField("robots"));
        assertEquals(1, loaded.getObstacles().size());
    }

    @Test
    @DisplayName("SAVE with a new name creates a distinct named world")
    void save_newName_createsNamedWorld() {
        repository.save("venus", new WorldSnapshot(7, 9, List.of()));

        WorldSnapshot loaded = repository.find("venus").orElseThrow();
        assertEquals(7, loaded.getWidth());
        assertEquals(9, loaded.getHeight());
        assertTrue(loaded.getObstacles().isEmpty());
        assertTrue(repository.find("mars").isEmpty());
    }

    @Test
    @DisplayName("SAVE with an existing name refuses to overwrite the stored world")
    void save_existingName_throwsDuplicateAndLeavesOriginalUnchanged() {
        Obstacle mountain = new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN);
        repository.save("mars", new WorldSnapshot(5, 5, List.of(mountain)));

        Obstacle lake = new Obstacle(2, 2, 2, 2, ObstacleType.LAKE);
        assertThrows(DuplicateWorldException.class,
                () -> repository.save("mars", new WorldSnapshot(8, 8, List.of(lake))));

        WorldSnapshot loaded = repository.find("mars").orElseThrow();
        assertEquals(5, loaded.getWidth());
        assertEquals(5, loaded.getHeight());
        assertEquals(1, loaded.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, loaded.getObstacles().get(0).getType());
    }

    @Test
    @DisplayName("two named worlds are stored independently")
    void save_twoNames_areStoredIndependently() {
        repository.save("mars", new WorldSnapshot(5, 5, List.of(
                new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN)
        )));
        repository.save("venus", new WorldSnapshot(8, 8, List.of(
                new Obstacle(2, 2, 2, 2, ObstacleType.LAKE)
        )));

        WorldSnapshot mars = repository.find("mars").orElseThrow();
        WorldSnapshot venus = repository.find("venus").orElseThrow();
        assertEquals(5, mars.getWidth());
        assertEquals(ObstacleType.MOUNTAIN, mars.getObstacles().get(0).getType());
        assertEquals(8, venus.getWidth());
        assertEquals(ObstacleType.LAKE, venus.getObstacles().get(0).getType());
    }

    @Test
    @DisplayName("find by name returns empty when that world has not been saved")
    void find_unknownName_returnsEmpty() {
        repository.save("mars", new WorldSnapshot(5, 5, List.of()));

        assertTrue(repository.find("pluto").isEmpty());
    }
}
