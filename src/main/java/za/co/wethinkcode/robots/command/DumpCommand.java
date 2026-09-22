package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.persistence.AsciiWorldFileStore;
import za.co.wethinkcode.robots.world.AsciiWorld;
import za.co.wethinkcode.robots.world.World;

import java.io.IOException;
import java.util.List;

/**
 * The Dump class represents a command to dump the current state of the world.
 * It extends the Command class and implements the execute method to perform the dump action.

 */
public class DumpCommand extends Command {
    // Singleton instance
    private static DumpCommand instance;

    /**
     * Private constructor for DumpCommand.
     * Sets the command name to "dump".
     */
    private DumpCommand() {
        super("dump");
    }

    /**
     * Gets the singleton instance of DumpCommand.
     *
     * @return The singleton instance of DumpCommand
     */
    public static synchronized DumpCommand getInstance() {
        if (instance == null) {
            instance = new DumpCommand();
        }
        return instance;
    }


    /**
     * Prints the current state of the obstacles in the world.
     *
     * @param obstacleList the list of obstacles to be printed
     */
    public void listObstacles(List<Obstacle> obstacleList) {
        System.out.println("Obstacles currently in world:");
        for (int i = 0; i < obstacleList.size(); i++) {
            System.out.println("Obstacle " + (i + 1) + ": " + obstacleList.get(i));
        }
    }


    @Override
    public JsonObject execute(World world) {
        return new JsonObject();
    }


    /**
     * Prints the current state of the world, including the robots and obstacles.
     *
     * @param world the world to be dumped
     */
    public void dump(World world) {
        System.out.println("Dumping world...");
        StringBuilder names = new StringBuilder("[");
        if (!world.getBots().isEmpty()) {
            names.deleteCharAt(names.length() - 1);
        }
        new RobotsCommand().printRobots(world);
        listObstacles(world.getObstacles());
        AsciiWorld txtWorld = new AsciiWorld(world);
        txtWorld.render();
        try {
            new AsciiWorldFileStore().write("target/Ascii-World.txt", txtWorld.getContent().toString());
            System.out.println("\nSuccessfully wrote world to target/Ascii-World.txt");
        } catch (IOException e) {
            System.out.println("\nFailed to write to file: " + e.getMessage());
        }
        System.out.println(txtWorld.getContent().toString());
    }

}
