package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.persistence.PersistenceException;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.util.Optional;

/**
 * Server-console RESTORE command.
 * Loads a previously saved world through a repository so that database
 * access stays out of domain classes, then applies the snapshot to the live world.
 */
public class RestoreCommand extends Command {
    private final WorldRepository repository;

    public RestoreCommand(WorldRepository repository) {
        super("restore");
        this.repository = repository;
    }

    /**
     * Restores the current world from the database.
     * Robot positions and status must not be restored.
     * Refactored to actually load snapshot from repository and apply it to world.
     * This fixes the failing tests that expected the restore method to work.
     */
    public void restore(World world) {
        try {
            Optional<WorldSnapshot> snapshot = repository.find();
            if (snapshot.isPresent()) {
                world.restore(snapshot.get());
                System.out.println("World restored.");
            } else {
                System.out.println("No saved world to restore.");
            }
        } catch (PersistenceException e) {
            System.out.println("Failed to restore world: " + e.getMessage());
        }
    }

    /**
     * Restores the live world from the named database entry.
     * Robot positions and status must not be restored.
     */
    public void restore(World world, String worldName) {
        try {
            Optional<WorldSnapshot> snapshot = repository.find(worldName);
            if (snapshot.isPresent()) {
                world.restore(snapshot.get());
                System.out.println("World restored.");
            } else {
                System.out.println("The specified world does not exist.");
            }
        } catch (PersistenceException e) {
            System.out.println("Failed to restore world: " + e.getMessage());
        }
    }

    @Override
    public JsonObject execute(World world) {
        restore(world);
        return new JsonObject();
    }
}
