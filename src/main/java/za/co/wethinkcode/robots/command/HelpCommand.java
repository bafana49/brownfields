package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;

/**
 * Handles the "help" command.
 * Provides a list of available commands and their basic usage formats
 * back to the client within a JSON response.
 */
public class HelpCommand extends Command {

    // Singleton instance
    private static HelpCommand instance;

    /**
     * Private constructor for HelpCommand.
     * Sets the command name to "help".
     */
    private HelpCommand() {
        super("help");
    }

    /**
     * Gets the singleton instance of HelpCommand.
     * 
     * @return The singleton instance of HelpCommand
     */
    public static synchronized HelpCommand getInstance() {
        if (instance == null) instance = new HelpCommand();
        return instance;
    }

    /**
     * Executes the help command.
     * Generates a JSON response containing a list of available commands.
     *
     * @param world The current World object (not directly used by help, but required by the method signature).
     * @return A JsonObject containing the help message under the "data" field, with a "result" of "OK".
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();

        String helpMessage = """
                I can understand these commands:
                  HELP                - Show this help message.
                  STATE               - Ask for the robot's current state.
                  LAUNCH <type> <name> - Launch a new robot (e.g., LAUNCH sniper HAL).
                  FORWARD <steps>     - Move the robot forward by <steps>.
                  BACK <steps>        - Move the robot backward by <steps>.
                  TURN <left|right>   - Turn the robot left or right.
                  LOOK                - Look around for obstacles and robots.
                  REPAIR              - Start repairing the robot's shields (takes time).
                  RELOAD              - Start reloading the robot's weapon (takes time).
                  FIRE                - Fire the robot's weapon (requires aiming/reload).
                """;

        data.addProperty("message", helpMessage);

        response.addProperty("result", "OK");
        response.add("data", data);
        return response;
    }
}
