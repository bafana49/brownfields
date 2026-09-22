package za.co.wethinkcode.robots.persistence.orm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.DuplicateWorldException;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrmWorldRepository.
 * Tests the complete ORM workflow from repository interface to database persistence.
 */
class OrmWorldRepositoryTest {

    private OrmWorldRepository repository;
    private static final String DB_FILE = "worlds.db";

    @BeforeEach
    void setUp() {
        new File(DB_FILE).delete();
        repository = new OrmWorldRepository();
    }

    @AfterEach
    void tearDown() {
        if (repository != null) {
            repository.close();
        }
        new File(DB_FILE).delete();
    }

    @Test
    @DisplayName("Should save and find unnamed world")
    void completeWorkflow_unnamedWorld_saveAndFind() {
        Obstacle obstacle = new Obstacle(10, 10, 15, 15, ObstacleType.MOUNTAIN);
        WorldSnapshot snapshot = new WorldSnapshot(100, 100, List.of(obstacle));

        repository.save(snapshot);

        Optional<WorldSnapshot> found = repository.find();
        assertTrue(found.isPresent());
        assertEquals(100, found.get().getWidth());
        assertEquals(100, found.get().getHeight());
        assertEquals(1, found.get().getObstacles().size());
    }

    @Test
    @DisplayName("Should save and find named world")
    void completeWorkflow_namedWorld_saveAndFind() {
        Obstacle obstacle = new Obstacle(20, 20, 25, 25, ObstacleType.LAKE);
        WorldSnapshot snapshot = new WorldSnapshot(200, 200, List.of(obstacle));

        repository.save("mars", snapshot);

        Optional<WorldSnapshot> found = repository.find("mars");
        assertTrue(found.isPresent());
        assertEquals(200, found.get().getWidth());
        assertEquals(200, found.get().getHeight());
        assertEquals(1, found.get().getObstacles().size());
    }

    @Test
    @DisplayName("Should reject duplicate world names")
    void completeWorkflow_duplicateWorldName_handling() {
        WorldSnapshot snapshot1 = new WorldSnapshot(100, 100, List.of());
        repository.save("mars", snapshot1);

        WorldSnapshot snapshot2 = new WorldSnapshot(200, 200, List.of());
        assertThrows(DuplicateWorldException.class, () -> repository.save("mars", snapshot2));
    }

    @Test
    @DisplayName("Should persist world size correctly")
    void completeWorkflow_worldSize_persistence() {
        WorldSnapshot snapshot = new WorldSnapshot(150, 250, List.of());
        repository.save(snapshot);

        Optional<WorldSnapshot> found = repository.find();
        assertTrue(found.isPresent());
        assertEquals(150, found.get().getWidth());
        assertEquals(250, found.get().getHeight());
    }

    @Test
    @DisplayName("Should save and find obstacles")
    void completeWorkflow_obstacle_saveAndFind() {
        Obstacle mountain = new Obstacle(10, 10, 15, 15, ObstacleType.MOUNTAIN);
        Obstacle lake     = new Obstacle(20, 20, 25, 25, ObstacleType.LAKE);
        WorldSnapshot snapshot = new WorldSnapshot(100, 100, List.of(mountain, lake));

        repository.save(snapshot);

        Optional<WorldSnapshot> found = repository.find();
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getObstacles().size());
    }

    @Test
    @DisplayName("Should handle empty world state")
    void completeWorkflow_emptyStates_handledCorrectly() {
        WorldSnapshot snapshot = new WorldSnapshot(100, 100, List.of());
        repository.save(snapshot);

        Optional<WorldSnapshot> found = repository.find();
        assertTrue(found.isPresent());
        assertEquals(0, found.get().getObstacles().size());
    }

    @Test
    @DisplayName("Should support CRUD operations")
    void completeWorkflow_crudOperations_workCorrectly() {
        // Create
        repository.save(new WorldSnapshot(100, 100, List.of()));
        assertTrue(repository.find().isPresent());

        // Update (save over existing unnamed world)
        repository.save(new WorldSnapshot(200, 200, List.of()));
        Optional<WorldSnapshot> updated = repository.find();
        assertTrue(updated.isPresent());
        assertEquals(200, updated.get().getWidth());
        assertEquals(200, updated.get().getHeight());

        // Save again with smaller world
        repository.save(new WorldSnapshot(50, 50, List.of()));
        Optional<WorldSnapshot> small = repository.find();
        assertTrue(small.isPresent());
        assertEquals(50, small.get().getWidth());
        assertEquals(0, small.get().getObstacles().size());
    }

    @Test
    @DisplayName("Should integrate with WorldRepository interface")
    void completeWorkflow_worldRepositoryIntegration_works() {
        WorldSnapshot snapshot = new WorldSnapshot(100, 100, List.of());
        repository.save(snapshot);
        assertTrue(repository.find().isPresent());

        repository.save("test-world", snapshot);
        assertTrue(repository.find("test-world").isPresent());
    }
}
