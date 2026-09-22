package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.persistence.DuplicateWorldException;
import za.co.wethinkcode.robots.persistence.PersistenceException;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

/**
 * Server-console SAVE command.
 * Persists the constructed world (size and map objects) through a repository
 * so that database access stays out of domain classes.
 */
public class SaveCommand extends Command {
    private final WorldRepository repository;

    public SaveCommand(WorldRepository repository) {
        super("save");
        this.repository = repository;
    }

    /**
     * Saves the current world to the database.
     * Robot positions and status are excluded by {@link WorldSnapshot#from(World)}.
     */
    public void save(World world) {
        try {
            repository.save(WorldSnapshot.from(world));
            System.out.println("World saved.");
        } catch (PersistenceException e) {
            System.out.println("Failed to save world: " + e.getMessage());
        }
    }

    /**
     * Saves the current world under a unique name.
     * Robot positions and status are excluded by {@link WorldSnapshot#from(World)}.
     * An existing name is refused rather than overwritten.
     */
    public void save(World world, String worldName) {
        try {
            repository.save(worldName, WorldSnapshot.from(world));
            System.out.println("World saved.");
        } catch (DuplicateWorldException e) {
            System.out.println(e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("Failed to save world: " + e.getMessage());
        }
    }

    @Override
    public JsonObject execute(World world) {
        save(world);
        return new JsonObject();
    }
}
