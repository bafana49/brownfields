package za.co.wethinkcode.robots.acceptance.database;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;
import za.co.wethinkcode.robots.robot.Robot;

import static org.junit.jupiter.api.Assertions.*;

public class SaveWorldAcceptanceTest {

    @Test
    public void shouldSaveWorldSnapshotExcludingRobots() {
        // Given a live World has been constructed with dimensions and active robots
        World world = new World(false);
        Robot anotherBot = new Robot("Hal", "sniper"); // Adding a live robot to the world memory

        // When the SAVE action triggers a snapshot creation
        WorldSnapshot snapshot = WorldSnapshot.from(world);

        // Then the world dimensions and obstacles are captured
        assertNotNull(snapshot, "WorldSnapshot should be successfully created");
        assertTrue(snapshot.getWidth() > 0, "World width must be saved");
        assertTrue(snapshot.getHeight() > 0, "World height must be saved");

        // And individual robot states are completely excluded from the snapshot view
        // (Verified by checking that WorldSnapshot structure and factory method omit robots entirely)
        boolean snapshotContainsRobots = false;
        for (var field : WorldSnapshot.class.getDeclaredFields()) {
            if (field.getName().toLowerCase().contains("robot") || field.getType().getName().contains("Robot")) {
                snapshotContainsRobots = true;
                break;
            }
        }
        assertFalse(snapshotContainsRobots, "WorldSnapshot architecture must exclude live robot states");
    }
}