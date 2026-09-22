package za.co.wethinkcode.robots.persistence.orm;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-based implementation of WorldDataAccess interface.
 * Provides ORM-like functionality using existing JDBC infrastructure.
 * Implements SQL operations for world and obstacle persistence.
 */
public class WorldDataAccessImpl implements WorldDataAccess {

    private final Connection connection;

    public WorldDataAccessImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int deleteUnnamedWorld() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM obstacles WHERE world_id IN (SELECT id FROM worlds WHERE name IS NULL)")) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete unnamed world obstacles", e);
        }
    }

    @Override
    public int saveUnnamedWorld(int width, int height) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO worlds (name, width, height) VALUES (NULL, ?, ?)")) {
            stmt.setInt(1, width);
            stmt.setInt(2, height);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save unnamed world", e);
        }
    }

    @Override
    public int saveNamedWorld(String name, int width, int height) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO worlds (name, width, height) VALUES (?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setInt(2, width);
            stmt.setInt(3, height);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save named world", e);
        }
    }

    @Override
    public WorldDataDO findUnnamedWorld() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, width, height FROM worlds WHERE name IS NULL LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorldDataDO(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find unnamed world", e);
        }
    }

    @Override
    public WorldDataDO findNamedWorld(String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, width, height FROM worlds WHERE name = ?")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorldDataDO(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find named world", e);
        }
    }

    @Override
    public int saveObstacle(int worldId, String type, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO obstacles (world_id, type, top_left_x, top_left_y, bottom_right_x, bottom_right_y) " +
                "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, worldId);
            stmt.setString(2, type);
            stmt.setInt(3, topLeftX);
            stmt.setInt(4, topLeftY);
            stmt.setInt(5, bottomRightX);
            stmt.setInt(6, bottomRightY);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save obstacle", e);
        }
    }

    @Override
    public List<ObstacleDataDO> findObstaclesByWorldId(int worldId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, world_id, type, top_left_x, top_left_y, bottom_right_x, bottom_right_y " +
                "FROM obstacles WHERE world_id = ?")) {
            stmt.setInt(1, worldId);
            ResultSet rs = stmt.executeQuery();
            List<ObstacleDataDO> obstacles = new ArrayList<>();
            while (rs.next()) {
                obstacles.add(mapResultSetToObstacleDataDO(rs));
            }
            return obstacles;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find obstacles by world ID", e);
        }
    }

    @Override
    public List<ObstacleDataDO> findObstaclesByWorldName(String worldName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT o.id, o.world_id, o.type, o.top_left_x, o.top_left_y, o.bottom_right_x, o.bottom_right_y " +
                "FROM obstacles o JOIN worlds w ON o.world_id = w.id WHERE w.name = ?")) {
            stmt.setString(1, worldName);
            ResultSet rs = stmt.executeQuery();
            List<ObstacleDataDO> obstacles = new ArrayList<>();
            while (rs.next()) {
                obstacles.add(mapResultSetToObstacleDataDO(rs));
            }
            return obstacles;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find obstacles by world name", e);
        }
    }

    @Override
    public int deleteObstaclesByWorldId(int worldId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM obstacles WHERE world_id = ?")) {
            stmt.setInt(1, worldId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete obstacles by world ID", e);
        }
    }

    @Override
    public int worldExists(String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM worlds WHERE name = ?")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if world exists", e);
        }
    }

    @Override
    public int getWorldIdByName(String name) {
        String sql = (name == null)
                ? "SELECT id FROM worlds WHERE name IS NULL"
                : "SELECT id FROM worlds WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (name != null) stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get world ID by name", e);
        }
    }

    private WorldDataDO mapResultSetToWorldDataDO(ResultSet rs) throws SQLException {
        WorldDataDO world = new WorldDataDO();
        world.setId(rs.getInt("id"));
        world.setName(rs.getString("name"));
        world.setWidth(rs.getInt("width"));
        world.setHeight(rs.getInt("height"));
        return world;
    }

    private ObstacleDataDO mapResultSetToObstacleDataDO(ResultSet rs) throws SQLException {
        ObstacleDataDO obstacle = new ObstacleDataDO();
        obstacle.setId(rs.getInt("id"));
        obstacle.setWorldId(rs.getInt("world_id"));
        String typeStr = rs.getString("type");
        obstacle.setType(typeStr != null ? za.co.wethinkcode.robots.obstacle.ObstacleType.valueOf(typeStr) : null);
        obstacle.setTopLeftX(rs.getInt("top_left_x"));
        obstacle.setTopLeftY(rs.getInt("top_left_y"));
        obstacle.setBottomRightX(rs.getInt("bottom_right_x"));
        obstacle.setBottomRightY(rs.getInt("bottom_right_y"));
        return obstacle;
    }
}
