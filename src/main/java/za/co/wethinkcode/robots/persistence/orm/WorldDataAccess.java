package za.co.wethinkcode.robots.persistence.orm;

import java.util.List;

/**
 * Data Access Interface for World and Obstacle entities.
 * Mimics EoDSQL BaseQuery pattern with custom SQL annotations.
 * Designed to work with custom ORM framework or be adapted for EoDSQL/Hibernate.
 * 
 * This interface defines SQL operations for world and obstacle persistence
 * using annotation-based SQL mapping similar to EoDSQL patterns.
 */
public interface WorldDataAccess {

    /**
     * Saves an unnamed world (replaces existing unnamed world).
     * Uses parameter binding for world properties.
     * 
     * @param world The world data object to save
     * @return Number of rows affected
     */
    @Update("DELETE FROM obstacles WHERE world_id IN (SELECT id FROM worlds WHERE name IS NULL)")
    int deleteUnnamedWorld();

    /**
     * Saves an unnamed world (replaces existing unnamed world).
     * Uses parameter binding for world properties.
     * 
     * @param world The world data object to save
     * @return Number of rows affected
     */
    @Update("INSERT INTO worlds (name, width, height) VALUES (NULL, ?{1}, ?{2})")
    int saveUnnamedWorld(int width, int height);

    /**
     * Saves a named world (creates new world with unique name).
     * Uses parameter binding for world properties including name.
     * 
     * @param name The unique name for the world
     * @param world The world data object to save
     * @return Number of rows affected
     */
    @Update("INSERT INTO worlds (name, width, height) VALUES (?{1}, ?{2}, ?{3})")
    int saveNamedWorld(String name, int width, int height);

    /**
     * Finds the most recently saved unnamed world.
     * Returns world data including dimensions.
     * 
     * @return WorldDataDO if found, null otherwise
     */
    @Select("SELECT id, name, width, height FROM worlds WHERE name IS NULL LIMIT 1")
    WorldDataDO findUnnamedWorld();

    /**
     * Finds a world by its unique name.
     * Returns world data including dimensions.
     * 
     * @param name The world name to search for
     * @return WorldDataDO if found, null otherwise
     */
    @Select("SELECT id, name, width, height FROM worlds WHERE name = ?{1}")
    WorldDataDO findNamedWorld(String name);

    /**
     * Saves obstacles for a specific world.
     * Links obstacles to world via foreign key.
     * 
     * @param worldId The ID of the world to link obstacles to
     * @param obstacles List of obstacle data objects to save
     * @return Number of rows affected
     */
    @Update("INSERT INTO obstacles (world_id, type, top_left_x, top_left_y, bottom_right_x, bottom_right_y) " +
           "VALUES (?{1}, ?{2}, ?{3}, ?{4}, ?{5}, ?{6})")
    int saveObstacle(int worldId, String type, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY);

    /**
     * Finds all obstacles for a specific world.
     * Returns obstacle data including type and position.
     * 
     * @param worldId The ID of the world to find obstacles for
     * @return List of ObstacleDataDO objects
     */
    @Select("SELECT id, world_id, type, top_left_x, top_left_y, bottom_right_x, bottom_right_y " +
           "FROM obstacles WHERE world_id = ?{1}")
    List<ObstacleDataDO> findObstaclesByWorldId(int worldId);

    /**
     * Finds all obstacles for a named world.
     * Joins worlds table to get world ID from name.
     * 
     * @param worldName The name of the world to find obstacles for
     * @return List of ObstacleDataDO objects
     */
    @Select("SELECT o.id, o.world_id, o.type, o.top_left_x, o.top_left_y, o.bottom_right_x, o.bottom_right_y " +
           "FROM obstacles o JOIN worlds w ON o.world_id = w.id WHERE w.name = ?{1}")
    List<ObstacleDataDO> findObstaclesByWorldName(String worldName);

    /**
     * Deletes all obstacles for a specific world.
     * Used when updating or replacing a world.
     * 
     * @param worldId The ID of the world to delete obstacles for
     * @return Number of rows affected
     */
    @Update("DELETE FROM obstacles WHERE world_id = ?{1}")
    int deleteObstaclesByWorldId(int worldId);

    /**
     * Checks if a world with the given name already exists.
     * Used for duplicate name validation.
     * 
     * @param name The world name to check
     * @return true if world exists, false otherwise
     */
    @Select("SELECT COUNT(*) FROM worlds WHERE name = ?{1}")
    int worldExists(String name);

    /**
     * Gets the world ID for a named world.
     * Used for linking obstacles to worlds.
     * 
     * @param name The world name to get ID for
     * @return World ID if found, 0 otherwise
     */
    @Select("SELECT id FROM worlds WHERE name = ?{1}")
    int getWorldIdByName(String name);
}
