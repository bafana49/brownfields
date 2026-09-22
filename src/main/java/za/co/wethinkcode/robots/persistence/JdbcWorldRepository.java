package za.co.wethinkcode.robots.persistence;

import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link WorldRepository}.
 * Uses plain JDBC (no JPA) so storage can later move from SQLite to another database.
 *
 * Schema (created automatically on first use):
 *   worlds    (id INTEGER PK, name TEXT UNIQUE, width INTEGER, height INTEGER)
 *   obstacles (id INTEGER PK AUTOINCREMENT, world_id INTEGER FK,
 *              type TEXT, top_left_x INT, top_left_y INT,
 *              bottom_right_x INT, bottom_right_y INT)
 *
 * Unnamed save() replaces the nameless world row. Named save() inserts a new
 * unique name and refuses to overwrite an existing one.
 */
public class JdbcWorldRepository implements WorldRepository {

    public static final String DEFAULT_URL = "jdbc:sqlite:robot-worlds.db";

    private static final String DUPLICATE_WORLD_MESSAGE =
            "A RobotWorld with that name already exists.";

    private final String jdbcUrl;
    private final Connection connection;

    public JdbcWorldRepository(String jdbcUrl) {
        this.jdbcUrl    = jdbcUrl;
        this.connection = null;
        createSchema(openConnection());
    }

    public JdbcWorldRepository(Connection connection) {
        this.jdbcUrl    = null;
        this.connection = connection;
        createSchema(connection);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Returns the active connection — either injected or opened from the URL. */
    private Connection activeConnection() {
        if (connection != null) return connection;
        return openConnection();
    }

    private Connection openConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new PersistenceException("Cannot open database connection: " + e.getMessage());
        }
    }

    // ── schema ────────────────────────────────────────────────────────────────

    private void createSchema(Connection conn) {
        String worlds = """
                CREATE TABLE IF NOT EXISTS worlds (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    name   TEXT UNIQUE,
                    width  INTEGER NOT NULL,
                    height INTEGER NOT NULL
                )""";
        String obstacles = """
                CREATE TABLE IF NOT EXISTS obstacles (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_id       INTEGER NOT NULL,
                    type           TEXT    NOT NULL,
                    top_left_x     INTEGER NOT NULL,
                    top_left_y     INTEGER NOT NULL,
                    bottom_right_x INTEGER NOT NULL,
                    bottom_right_y INTEGER NOT NULL,
                    FOREIGN KEY (world_id) REFERENCES worlds(id)
                )""";
        try (Statement st = conn.createStatement()) {
            st.execute(worlds);
            st.execute(obstacles);
            try {
                st.execute("ALTER TABLE worlds ADD COLUMN name TEXT");
            } catch (SQLException ignored) {
                // Column already exists on newly created or previously migrated databases.
            }
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_worlds_name ON worlds(name)");
        } catch (SQLException e) {
            throw new PersistenceException("Failed to create schema: " + e.getMessage());
        }
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Override
    public void save(WorldSnapshot snapshot) {
        Connection conn = activeConnection();
        try {
            deleteUnnamed(conn);
            int worldId = insertWorld(conn, null, snapshot);
            insertObstacles(conn, worldId, snapshot.getObstacles());
        } catch (SQLException e) {
            throw new PersistenceException("Failed to save world: " + e.getMessage());
        }
    }

    private void deleteUnnamed(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM obstacles WHERE world_id IN (SELECT id FROM worlds WHERE name IS NULL)");
            st.execute("DELETE FROM worlds WHERE name IS NULL");
        }
    }

    private int insertWorld(Connection conn, String name, WorldSnapshot snapshot) throws SQLException {
        String sql = "INSERT INTO worlds (name, width, height) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (name == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, name);
            }
            ps.setInt(2, snapshot.getWidth());
            ps.setInt(3, snapshot.getHeight());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to obtain world id after insert");
    }

    private void insertObstacles(Connection conn, int worldId, List<Obstacle> obstacles) throws SQLException {
        if (obstacles.isEmpty()) return;
        String sql = """
                INSERT INTO obstacles
                    (world_id, type, top_left_x, top_left_y, bottom_right_x, bottom_right_y)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Obstacle o : obstacles) {
                ps.setInt(1, worldId);
                ps.setString(2, o.getType().name());
                ps.setInt(3, o.getTopLeft().getX());
                ps.setInt(4, o.getTopLeft().getY());
                ps.setInt(5, o.getBottomRight().getX());
                ps.setInt(6, o.getBottomRight().getY());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── find ─────────────────────────────────────────────────────────────────

    @Override
    public Optional<WorldSnapshot> find() {
        Connection conn = activeConnection();
        String sql = "SELECT id, width, height FROM worlds WHERE name IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int worldId = rs.getInt("id");
                int width  = rs.getInt("width");
                int height = rs.getInt("height");
                List<Obstacle> obstacles = loadObstacles(conn, worldId);
                return Optional.of(new WorldSnapshot(width, height, obstacles));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load world: " + e.getMessage());
        }
    }

    private List<Obstacle> loadObstacles(Connection conn, int worldId) throws SQLException {
        String sql = """
                SELECT type, top_left_x, top_left_y, bottom_right_x, bottom_right_y
                FROM   obstacles
                WHERE  world_id = ?""";
        List<Obstacle> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, worldId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ObstacleType type = ObstacleType.valueOf(rs.getString("type"));
                    int x1 = rs.getInt("top_left_x");
                    int y1 = rs.getInt("top_left_y");
                    int x2 = rs.getInt("bottom_right_x");
                    int y2 = rs.getInt("bottom_right_y");
                    result.add(new Obstacle(x1, y1, x2, y2, type));
                }
            }
        }
        return result;
    }

    @Override
    public void save(String name, WorldSnapshot snapshot) {
        Connection conn = activeConnection();
        try {
            if (namedWorldExists(conn, name)) {
                throw new DuplicateWorldException(DUPLICATE_WORLD_MESSAGE);
            }
            int worldId = insertWorld(conn, name, snapshot);
            insertObstacles(conn, worldId, snapshot.getObstacles());
        } catch (DuplicateWorldException e) {
            throw e;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to save world: " + e.getMessage());
        }
    }

    @Override
    public Optional<WorldSnapshot> find(String name) {
        Connection conn = activeConnection();
        String sql = "SELECT id, width, height FROM worlds WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int worldId = rs.getInt("id");
                int width = rs.getInt("width");
                int height = rs.getInt("height");
                return Optional.of(new WorldSnapshot(width, height, loadObstacles(conn, worldId)));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load world: " + e.getMessage());
        }
    }

    private boolean namedWorldExists(Connection conn, String name) throws SQLException {
        String sql = "SELECT 1 FROM worlds WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── original getters (kept for compatibility) ─────────────────────────────

    protected String getJdbcUrl()      { return jdbcUrl; }
    protected Connection getConnection() { return connection; }
}
