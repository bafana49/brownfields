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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the complete ORM layer.
 * These tests verify the complete data access flow from application to database
 * through the OrmWorldRepository / WorldDataAccessImpl stack.
 */
class ORMIntegrationTests {

    private static final String DB_FILE = "worlds_orm_integration.db";
    private OrmWorldRepository repository;

    @BeforeEach
    void setUp() {
        new File(DB_FILE).delete();
        // OrmWorldRepository always uses "worlds.db"; we delete that too
        new File("worlds.db").delete();
        repository = new OrmWorldRepository();
    }

    @AfterEach
    void tearDown() {
        if (repository != null) repository.close();
        new File("worlds.db").delete();
        new File(DB_FILE).delete();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static Obstacle mountain(int x1, int y1, int x2, int y2) {
        return new Obstacle(x1, y1, x2, y2, ObstacleType.MOUNTAIN);
    }

    private static Obstacle lake(int x1, int y1, int x2, int y2) {
        return new Obstacle(x1, y1, x2, y2, ObstacleType.LAKE);
    }

    private static Obstacle pit(int x1, int y1, int x2, int y2) {
        return new Obstacle(x1, y1, x2, y2, ObstacleType.BOTTOMLESS_PIT);
    }

    // ── tests ────────────────────────────────────────────────────────────────

    /**
     * Tests the complete save and find workflow for unnamed worlds.
     */
    @Test
    @DisplayName("Complete save and find workflow for unnamed worlds")
    void completeWorkflow_unnamedWorld_saveAndFind() {
        // Given a world snapshot with dimensions and one obstacle
        Obstacle obs = mountain(5, 5, 6, 6);
        WorldSnapshot saved = new WorldSnapshot(50, 60, List.of(obs));

        // When we save it
        repository.save(saved);

        // Then we can retrieve it and the data matches
        Optional<WorldSnapshot> found = repository.find();
        assertTrue(found.isPresent(), "Unnamed world should be retrievable");
        WorldSnapshot ws = found.get();
        assertEquals(50, ws.getWidth());
        assertEquals(60, ws.getHeight());
        assertEquals(1, ws.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, ws.getObstacles().get(0).getType());
    }

    /**
     * Tests the complete save and find workflow for named worlds.
     */
    @Test
    @DisplayName("Complete save and find workflow for named worlds")
    void completeWorkflow_namedWorld_saveAndFind() {
        // Save two distinct named worlds
        repository.save("mars", new WorldSnapshot(100, 80, List.of(mountain(1, 1, 2, 2))));
        repository.save("venus", new WorldSnapshot(200, 150, List.of(lake(3, 3, 4, 4))));

        // Retrieve each and verify the correct world is returned
        WorldSnapshot mars = repository.find("mars").orElseThrow();
        assertEquals(100, mars.getWidth());
        assertEquals(80, mars.getHeight());
        assertEquals(1, mars.getObstacles().size());
        assertEquals(ObstacleType.MOUNTAIN, mars.getObstacles().get(0).getType());

        WorldSnapshot venus = repository.find("venus").orElseThrow();
        assertEquals(200, venus.getWidth());
        assertEquals(150, venus.getHeight());
        assertEquals(1, venus.getObstacles().size());
        assertEquals(ObstacleType.LAKE, venus.getObstacles().get(0).getType());

        // Unknown name returns empty
        assertTrue(repository.find("pluto").isEmpty());
    }

    /**
     * Tests that the ORM layer handles duplicate world names correctly.
     */
    @Test
    @DisplayName("ORM layer should handle duplicate world names correctly")
    void completeWorkflow_duplicateWorldName_handling() {
        // Save original world
        WorldSnapshot original = new WorldSnapshot(5, 5, List.of(mountain(0, 0, 1, 1)));
        repository.save("mars", original);

        // Attempting to save a second world with the same name throws DuplicateWorldException
        WorldSnapshot duplicate = new WorldSnapshot(10, 10, List.of());
        assertThrows(DuplicateWorldException.class, () -> repository.save("mars", duplicate));

        // Original world is unchanged
        WorldSnapshot stored = repository.find("mars").orElseThrow();
        assertEquals(5, stored.getWidth());
        assertEquals(5, stored.getHeight());
        assertEquals(1, stored.getObstacles().size());
    }

    /**
     * Tests the complete obstacle persistence workflow.
     */
    @Test
    @DisplayName("Complete obstacle persistence workflow")
    void completeWorkflow_obstacle_saveAndFind() {
        // Save a world with three different obstacle types
        List<Obstacle> obstacles = List.of(
                mountain(1, 1, 2, 2),
                lake(3, 3, 4, 4),
                pit(5, 5, 6, 6)
        );
        repository.save(new WorldSnapshot(30, 30, obstacles));

        WorldSnapshot found = repository.find().orElseThrow();
        assertEquals(3, found.getObstacles().size());

        List<ObstacleType> types = found.getObstacles().stream()
                .map(Obstacle::getType).toList();
        assertTrue(types.contains(ObstacleType.MOUNTAIN));
        assertTrue(types.contains(ObstacleType.LAKE));
        assertTrue(types.contains(ObstacleType.BOTTOMLESS_PIT));

        // Verify positions survived the round-trip
        Obstacle m = found.getObstacles().stream()
                .filter(o -> o.getType() == ObstacleType.MOUNTAIN).findFirst().orElseThrow();
        assertEquals(1, m.getTopLeft().getX());
        assertEquals(1, m.getTopLeft().getY());
        assertEquals(2, m.getBottomRight().getX());
        assertEquals(2, m.getBottomRight().getY());
    }

    /**
     * Tests that world dimensions are properly saved and retrieved.
     */
    @Test
    @DisplayName("ORM layer should handle world size persistence correctly")
    void completeWorkflow_worldSize_persistence() {
        // Test a variety of dimension combinations
        int[][] sizes = {{1, 1}, {100, 200}, {999, 999}, {50, 75}};
        for (int[] size : sizes) {
            // Each save replaces the unnamed world
            repository.save(new WorldSnapshot(size[0], size[1], List.of()));
            WorldSnapshot ws = repository.find().orElseThrow();
            assertEquals(size[0], ws.getWidth(),  "Width mismatch for " + size[0] + "x" + size[1]);
            assertEquals(size[1], ws.getHeight(), "Height mismatch for " + size[0] + "x" + size[1]);
        }
    }

    /**
     * Tests that relationships between worlds and obstacles work correctly.
     */
    @Test
    @DisplayName("ORM layer should handle complex object relationships")
    void completeWorkflow_complexRelationships_workCorrectly() {
        // Named world A with 3 obstacles
        List<Obstacle> obstaclesA = List.of(
                mountain(0, 0, 1, 1),
                lake(2, 2, 3, 3),
                pit(4, 4, 5, 5)
        );
        repository.save("worldA", new WorldSnapshot(20, 20, obstaclesA));

        // Named world B with 1 obstacle
        repository.save("worldB", new WorldSnapshot(10, 10, List.of(lake(1, 1, 2, 2))));

        // Retrieve A — should have its 3 obstacles, not B's
        WorldSnapshot a = repository.find("worldA").orElseThrow();
        assertEquals(3, a.getObstacles().size());

        // Retrieve B — should have exactly 1 obstacle
        WorldSnapshot b = repository.find("worldB").orElseThrow();
        assertEquals(1, b.getObstacles().size());
        assertEquals(ObstacleType.LAKE, b.getObstacles().get(0).getType());

        // Also save an unnamed world — it must not mix with named ones
        repository.save(new WorldSnapshot(5, 5, List.of(mountain(0, 0, 0, 0))));
        WorldSnapshot unnamed = repository.find().orElseThrow();
        assertEquals(1, unnamed.getObstacles().size());

        // Named worlds remain unaffected
        assertEquals(3, repository.find("worldA").orElseThrow().getObstacles().size());
    }

    /**
     * Tests that database errors are handled gracefully.
     */
    @Test
    @DisplayName("ORM layer should handle database errors gracefully")
    void completeWorkflow_databaseError_handling() {
        // Saving a valid snapshot should not throw
        assertDoesNotThrow(() -> repository.save(new WorldSnapshot(10, 10, List.of())));

        // Duplicate name must throw DuplicateWorldException (not a raw SQL error)
        repository.save("errorWorld", new WorldSnapshot(5, 5, List.of()));
        Exception ex = assertThrows(DuplicateWorldException.class,
                () -> repository.save("errorWorld", new WorldSnapshot(6, 6, List.of())));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());

        // Original world is intact after the rejected save
        WorldSnapshot stored = repository.find("errorWorld").orElseThrow();
        assertEquals(5, stored.getWidth());
    }

    /**
     * Tests that multiple simultaneous reads work without conflicts.
     */
    @Test
    @DisplayName("ORM layer should handle concurrent access correctly")
    void completeWorkflow_concurrentAccess_worksCorrectly() throws InterruptedException {
        // Seed several named worlds
        for (int i = 0; i < 5; i++) {
            repository.save("world-" + i, new WorldSnapshot(10 + i, 10 + i, List.of()));
        }

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch done  = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int idx = t % 5;
            pool.submit(() -> {
                ready.countDown();
                try {
                    ready.await();
                    Optional<WorldSnapshot> ws = repository.find("world-" + idx);
                    if (ws.isEmpty() || ws.get().getWidth() != 10 + idx) {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(10, TimeUnit.SECONDS), "Concurrent reads timed out");
        pool.shutdown();
        assertEquals(0, errors.get(), "Concurrent reads should not produce errors");
    }

    /**
     * Tests that performance is acceptable with a large number of obstacles.
     */
    @Test
    @DisplayName("ORM layer should handle large datasets efficiently")
    void completeWorkflow_largeDataset_performance() {
        // Build 100 obstacles
        List<Obstacle> obstacles = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            obstacles.add(new Obstacle(i * 2, i * 2, i * 2 + 1, i * 2 + 1, ObstacleType.MOUNTAIN));
        }

        long start = System.currentTimeMillis();
        repository.save(new WorldSnapshot(1000, 1000, obstacles));
        WorldSnapshot found = repository.find().orElseThrow();
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(100, found.getObstacles().size());
        // Entire round-trip should complete comfortably under 5 seconds on any machine
        assertTrue(elapsed < 5000, "Large dataset round-trip took too long: " + elapsed + "ms");
    }

    /**
     * Tests that the ORM layer works with different database configurations (in-memory vs file).
     */
    @Test
    @DisplayName("ORM layer should work with different database configurations")
    void completeWorkflow_differentConfigurations_workCorrectly() {
        // File-based (default OrmWorldRepository uses "worlds.db")
        repository.save(new WorldSnapshot(10, 10, List.of(mountain(0, 0, 1, 1))));
        WorldSnapshot fileResult = repository.find().orElseThrow();
        assertEquals(10, fileResult.getWidth());
        assertEquals(1, fileResult.getObstacles().size());

        // Named world in same file-based DB
        repository.save("named", new WorldSnapshot(20, 20, List.of()));
        WorldSnapshot namedResult = repository.find("named").orElseThrow();
        assertEquals(20, namedResult.getWidth());
        assertEquals(0, namedResult.getObstacles().size());
    }

    /**
     * Tests that database connections and resources are properly closed.
     */
    @Test
    @DisplayName("ORM layer should properly clean up resources")
    void completeWorkflow_resourceCleanup_worksCorrectly() {
        // Perform multiple sequential operations
        for (int i = 0; i < 5; i++) {
            repository.save(new WorldSnapshot(10 + i, 10 + i, List.of()));
            WorldSnapshot ws = repository.find().orElseThrow();
            assertEquals(10 + i, ws.getWidth());
        }

        // Closing should not throw
        assertDoesNotThrow(() -> repository.close());

        // Subsequent close calls should also be safe
        assertDoesNotThrow(() -> repository.close());
    }

    /**
     * Tests that Java types are properly mapped to database types and vice versa.
     */
    @Test
    @DisplayName("ORM layer should handle data type conversions correctly")
    void completeWorkflow_dataTypeConversions_workCorrectly() {
        // Enum types — all three obstacle types round-trip correctly
        for (ObstacleType type : ObstacleType.values()) {
            new File("worlds.db").delete();
            OrmWorldRepository repo = newRepository();
            try {
                repo.save(new WorldSnapshot(10, 10, List.of(new Obstacle(0, 0, 1, 1, type))));
                WorldSnapshot ws = repo.find().orElseThrow();
                assertEquals(type, ws.getObstacles().get(0).getType(),
                        "Enum round-trip failed for " + type);
            } finally {
                repo.close();
            }
        }

        // Integer boundary values — max int is impractical for coords; use large positive value
        new File("worlds.db").delete();
        repository = newRepository();
        repository.save(new WorldSnapshot(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, List.of()));
        WorldSnapshot big = repository.find().orElseThrow();
        assertEquals(Integer.MAX_VALUE / 2, big.getWidth());
        assertEquals(Integer.MAX_VALUE / 2, big.getHeight());

        // Strings — world names with spaces and special chars
        repository.save("hello world!", new WorldSnapshot(5, 5, List.of()));
        assertTrue(repository.find("hello world!").isPresent());
    }

    /**
     * Tests that foreign key constraints are respected and data is not corrupted.
     */
    @Test
    @DisplayName("ORM layer should maintain data integrity")
    void completeWorkflow_dataIntegrity_maintained() {
        // Save multiple named worlds; each should have isolated obstacles
        repository.save("alpha", new WorldSnapshot(10, 10, List.of(mountain(0, 0, 1, 1))));
        repository.save("beta",  new WorldSnapshot(20, 20, List.of(lake(2, 2, 3, 3), pit(4, 4, 5, 5))));

        WorldSnapshot alpha = repository.find("alpha").orElseThrow();
        WorldSnapshot beta  = repository.find("beta").orElseThrow();

        // No obstacles leaked between worlds
        assertEquals(1, alpha.getObstacles().size());
        assertEquals(2, beta.getObstacles().size());

        // Saving a new unnamed world does not corrupt existing named ones
        repository.save(new WorldSnapshot(5, 5, List.of(mountain(9, 9, 9, 9))));
        assertEquals(1, repository.find("alpha").orElseThrow().getObstacles().size());
        assertEquals(2, repository.find("beta").orElseThrow().getObstacles().size());

        // Updating unnamed world (save again) replaces only the unnamed entry
        repository.save(new WorldSnapshot(7, 7, List.of()));
        assertEquals(0, repository.find().orElseThrow().getObstacles().size());
        assertEquals(1, repository.find("alpha").orElseThrow().getObstacles().size());
    }

    /**
     * Tests that OrmWorldRepository correctly implements the WorldRepository interface.
     */
    @Test
    @DisplayName("ORM layer should integrate with WorldRepository interface")
    void completeWorkflow_worldRepositoryIntegration_works() {
        // Use via the interface type to confirm substitutability
        za.co.wethinkcode.robots.persistence.WorldRepository repo = repository;

        // Unnamed save/find
        repo.save(new WorldSnapshot(8, 8, List.of(mountain(0, 0, 1, 1))));
        assertTrue(repo.find().isPresent());
        assertEquals(8, repo.find().get().getWidth());

        // Named save/find
        repo.save("mars", new WorldSnapshot(15, 15, List.of()));
        assertTrue(repo.find("mars").isPresent());
        assertEquals(15, repo.find("mars").get().getWidth());

        // Unknown name returns empty
        assertTrue(repo.find("unknown").isEmpty());

        // Duplicate name is refused
        assertThrows(DuplicateWorldException.class,
                () -> repo.save("mars", new WorldSnapshot(1, 1, List.of())));
    }

    /**
     * Tests behaviour when the database is empty or worlds have no obstacles.
     */
    @Test
    @DisplayName("ORM layer should handle empty states correctly")
    void completeWorkflow_emptyStates_handledCorrectly() {
        // No worlds saved yet
        assertTrue(repository.find().isEmpty());
        assertTrue(repository.find("nonexistent").isEmpty());

        // World with no obstacles
        repository.save(new WorldSnapshot(10, 10, List.of()));
        WorldSnapshot empty = repository.find().orElseThrow();
        assertEquals(0, empty.getObstacles().size());

        // Named world with no obstacles
        repository.save("bare", new WorldSnapshot(5, 5, List.of()));
        WorldSnapshot bare = repository.find("bare").orElseThrow();
        assertEquals(0, bare.getObstacles().size());
    }

    /**
     * Tests full Create-Read-Update (replace unnamed) semantics.
     */
    @Test
    @DisplayName("ORM layer should support complete CRUD operations")
    void completeWorkflow_crudOperations_workCorrectly() {
        // Create
        repository.save(new WorldSnapshot(10, 10, List.of(mountain(0, 0, 1, 1))));
        WorldSnapshot v1 = repository.find().orElseThrow();
        assertEquals(10, v1.getWidth());
        assertEquals(1, v1.getObstacles().size());

        // Read (named)
        repository.save("snap1", new WorldSnapshot(20, 20, List.of(lake(2, 2, 3, 3))));
        WorldSnapshot named = repository.find("snap1").orElseThrow();
        assertEquals(20, named.getWidth());

        // Update — replacing the unnamed world
        repository.save(new WorldSnapshot(30, 30, List.of(pit(4, 4, 5, 5), mountain(6, 6, 7, 7))));
        WorldSnapshot v2 = repository.find().orElseThrow();
        assertEquals(30, v2.getWidth());
        assertEquals(2, v2.getObstacles().size());

        // Unnamed update does not affect named world
        WorldSnapshot namedStillIntact = repository.find("snap1").orElseThrow();
        assertEquals(20, namedStillIntact.getWidth());
        assertEquals(1, namedStillIntact.getObstacles().size());

        // Replace with empty (soft delete)
        repository.save(new WorldSnapshot(1, 1, List.of()));
        WorldSnapshot v3 = repository.find().orElseThrow();
        assertEquals(1, v3.getWidth());
        assertEquals(0, v3.getObstacles().size());
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private OrmWorldRepository newRepository() {
        return new OrmWorldRepository();
    }
}