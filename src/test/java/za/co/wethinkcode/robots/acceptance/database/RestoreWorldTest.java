package za.co.wethinkcode.robots.acceptance.database;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.JdbcWorldRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class RestoreWorldTest {
    @Test
    public void shouldRestoreWorldFromSnapshotSuccesfully() throws SQLException{
        // Given a saved world snapshot config exists in the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:robot-world.db");
            JdbcWorldRepository jdbcWorldRepository = new JdbcWorldRepository(connection);
            List<Obstacle> obstacles = List.of(new Obstacle(2, 2, 3, 3, ObstacleType.MOUNTAIN));
            WorldSnapshot worldSnapshot = new WorldSnapshot(20, 20,obstacles);
            jdbcWorldRepository.save(worldSnapshot);
        // When the snapshot is loaded from the database repository and applied
        Optional<WorldSnapshot> loadedSnaphot = jdbcWorldRepository.find();
        assertTrue(loadedSnaphot.isPresent(), "Saved world snapshot must exis in the database");


        // And Fresh live world instance

        World liveWorld = new World(false);
        assertNotEquals(25, liveWorld.getBOTTOM_RIGHT().getX() + 1);

        liveWorld.restore(loadedSnaphot.get());
        //When the restore action is triggered using the snapshot

        liveWorld.restore(worldSnapshot);

        // Then the liveworld dimensions and obstacles match the restored state

        assertEquals(20, liveWorld.getBOTTOM_RIGHT().getX() + 1);
        assertEquals(20, liveWorld.getBOTTOM_RIGHT().getY() + 1);
        assertEquals(1, liveWorld.getObstacles().size());


        assertTrue(liveWorld.getBots().isEmpty(),"REstore world must be clear out live robot states ");
    }
}
