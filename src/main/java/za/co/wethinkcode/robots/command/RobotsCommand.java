package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import static za.co.wethinkcode.robots.client.Client.formatState;

/**
 * The Robots class represents a command to list the robots in the world.
 * It extends the Command class and implements the execute method to perform the listing action.
 */
public class RobotsCommand extends Command {

    private static RobotsCommand instance;

    /**
     * Private constructor for RobotsCommand.
     * Sets the command name to "robots".
     */
    RobotsCommand() {
        super("robots");
    }

    /**
     * Gets the singleton instance of RobotsCommand.
     * 
     * @return The singleton instance of RobotsCommand
     */
    public static synchronized RobotsCommand getInstance() {
        if (instance == null) {
            instance = new RobotsCommand();
        }
        return instance;
    }

    @Override
    public JsonObject execute(World world) {
        return null;
    }

    /**
     * Prints the current state of the robots in the world.
     *
     * @param world the world to be printed
     */
    public void printRobots(World world) {
        if (world.getRobots().isEmpty()){
            System.out.println("There are currently no Robots in the world. ");
        }
        else
        {
            System.out.println("Robots currently in the World: ");
            for (Robot robot : world.getRobots()) {
                System.out.print("name: " + robot.getName() +"\n" +formatState(robot.state()));
                System.out.println();
            }
        }
    }
}
