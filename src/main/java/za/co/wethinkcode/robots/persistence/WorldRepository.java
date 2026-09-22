package za.co.wethinkcode.robots.persistence;

import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.util.Optional;

/**
 * Repository for persisting a constructed RobotWorld.
 * Implementations own all database access so domain classes stay free of JDBC.
 */
public interface WorldRepository {

    /**
     * Saves world size and map objects (obstacles, pits, mines).
     * Robot positions and status must not be persisted.
     */
    void save(WorldSnapshot snapshot);

    /**
     * Returns the most recently saved world, if any.
     */
    Optional<WorldSnapshot> find();

    /**
     * Saves world size and map objects under a unique name.
     * Must not overwrite a world that already exists with this name.
     */
    void save(String name, WorldSnapshot snapshot);

    /**
     * Returns the world saved under the given name, if any.
     */
    Optional<WorldSnapshot> find(String name);
}
