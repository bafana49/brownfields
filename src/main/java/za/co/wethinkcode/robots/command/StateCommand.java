package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.robot.Robot; // Import Robot

/**
 * Handles the "state" command.
 * This command doesn't modify the world state but signals that the current
 * robot's state information should be included in the response.
 * The actual state data is typically added by the calling handler method.
 */
public class StateCommand extends Command {
    private static StateCommand instance;


    /**
     * Private constructor for StateCommand.
     * Sets the command name to "state".
     */
    public StateCommand() {
        super("state");
    }

    /**
     * Gets the singleton instance of StateCommand.
     * 
     * @return The singleton instance of StateCommand
     */
    public static synchronized StateCommand getInstance() {
        if (instance == null) {
            instance = new StateCommand();
        }
        return instance;
    }

    /**
     * Executes the state command.
     * Checks if a current robot context exists and returns a basic success
     * or error JSON response. The detailed state is added later by the caller.
     *
     * @param world The World object, used here mainly to check for the current robot.
     * @return A JsonObject indicating success or failure (if no robot is active).
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();

        Robot currentRobot = world.getCurrentRobot();

        if (currentRobot == null) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "No active robot. Cannot get state.");
        } else {

            response.addProperty("result", "OK");
            data.addProperty("message", "Robot state follows.");
        }

        response.add("data", data);

        return response;
    }
}
