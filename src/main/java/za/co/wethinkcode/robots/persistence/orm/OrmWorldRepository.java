package za.co.wethinkcode.robots.persistence.orm;

import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ORM-based implementation of WorldRepository using WorldDataAccess.
 * Adapts the ORM data access layer to the WorldRepository interface.
 * Uses SQLite database (same as existing JdbcWorldRepository).
 */
public class OrmWorldRepository implements WorldRepository {

    private static final String DB_URL = "jdbc:sqlite:worlds.db";
    private Connection connection;
    private WorldDataAccess dataAccess;

    public OrmWorldRepository() {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            this.connection.setAutoCommit(false);
            this.dataAccess = new WorldDataAccessImpl(connection);
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize ORM repository", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        // Create tables if they don't exist (same schema as JdbcWorldRepository)
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS worlds (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, " +
                    "width INTEGER NOT NULL, " +
                    "height INTEGER NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS obstacles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "world_id INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "top_left_x INTEGER NOT NULL, " +
                    "top_left_y INTEGER NOT NULL, " +
                    "bottom_right_x INTEGER NOT NULL, " +
                    "bottom_right_y INTEGER NOT NULL, " +
                    "FOREIGN KEY (world_id) REFERENCES worlds(id))");
        }
    }

    @Override
    public void save(WorldSnapshot snapshot) {
        try {
            // Delete existing unnamed world and its obstacles
            dataAccess.deleteUnnamedWorld();
            
            // Delete unnamed world record
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM worlds WHERE name IS NULL")) {
                stmt.executeUpdate();
            }
            
            // Save new unnamed world
            dataAccess.saveUnnamedWorld(snapshot.getWidth(), snapshot.getHeight());
            
            // Get the world ID
            int worldId = dataAccess.getWorldIdByName(null);
            
            // Save obstacles
            for (Obstacle obstacle : snapshot.getObstacles()) {
                Position topLeft = obstacle.getTopLeft();
                Position bottomRight = obstacle.getBottomRight();
                dataAccess.saveObstacle(worldId, obstacle.getType().name(), 
                        topLeft.getX(), topLeft.getY(), 
                        bottomRight.getX(), bottomRight.getY());
            }
            
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to save world snapshot", e);
        }
    }

    @Override
    public Optional<WorldSnapshot> find() {
        WorldDataDO worldDO = dataAccess.findUnnamedWorld();
        if (worldDO == null) {
            return Optional.empty();
        }

        List<ObstacleDataDO> obstacles = dataAccess.findObstaclesByWorldId(worldDO.getId());
        List<Obstacle> obstacleList = new ArrayList<>();
        for (ObstacleDataDO obstacleDO : obstacles) {
            obstacleList.add(new Obstacle(
                    obstacleDO.getTopLeftX(),
                    obstacleDO.getTopLeftY(),
                    obstacleDO.getBottomRightX(),
                    obstacleDO.getBottomRightY(),
                    obstacleDO.getType()
            ));
        }
        return Optional.of(new WorldSnapshot(worldDO.getWidth(), worldDO.getHeight(), obstacleList));
    }

    @Override
    public void save(String name, WorldSnapshot snapshot) {
        try {
            // Check if world already exists
            if (dataAccess.worldExists(name) > 0) {
                throw new za.co.wethinkcode.robots.persistence.DuplicateWorldException("A RobotWorld with that name already exists.");
            }
            
            // Save named world
            dataAccess.saveNamedWorld(name, snapshot.getWidth(), snapshot.getHeight());
            
            // Get the world ID
            int worldId = dataAccess.getWorldIdByName(name);
            
            // Save obstacles
            for (Obstacle obstacle : snapshot.getObstacles()) {
                Position topLeft = obstacle.getTopLeft();
                Position bottomRight = obstacle.getBottomRight();
                dataAccess.saveObstacle(worldId, obstacle.getType().name(), 
                        topLeft.getX(), topLeft.getY(), 
                        bottomRight.getX(), bottomRight.getY());
            }
            
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to save named world snapshot", e);
        }
    }

    @Override
    public Optional<WorldSnapshot> find(String name) {
        WorldDataDO worldDO = dataAccess.findNamedWorld(name);
        if (worldDO == null) {
            return Optional.empty();
        }

        List<ObstacleDataDO> obstacles = dataAccess.findObstaclesByWorldName(name);
        List<Obstacle> obstacleList = new ArrayList<>();
        for (ObstacleDataDO obstacleDO : obstacles) {
            obstacleList.add(new Obstacle(
                    obstacleDO.getTopLeftX(),
                    obstacleDO.getTopLeftY(),
                    obstacleDO.getBottomRightX(),
                    obstacleDO.getBottomRightY(),
                    obstacleDO.getType()
            ));
        }
        return Optional.of(new WorldSnapshot(worldDO.getWidth(), worldDO.getHeight(), obstacleList));
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            // Ignore rollback errors
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Ignore close errors
        }
    }
}
