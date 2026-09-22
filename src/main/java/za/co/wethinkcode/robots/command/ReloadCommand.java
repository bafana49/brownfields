package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;

/**
 * The ReloadCommand class represents a command to reload a robot's shots.
 * It extends the Command class and implements the execute method to perform the reload action.
 */
public class ReloadCommand extends Command {

    private static ReloadCommand instance;

    /**
     * Private constructor for ReloadCommand.
     * Sets the command name to "reload".
     */
    private ReloadCommand() {
        super("reload");
    }

    /**
     * Gets the singleton instance of ReloadCommand.
     *
     * @return The singleton instance of ReloadCommand
     */
    public static synchronized ReloadCommand getInstance() {
        if (instance == null) {
            instance = new ReloadCommand();
        }
        return instance;
    }

    /**
     * Executes the reload command in the given world.
     * It reloads the shots and returns the result.
     *
     * @param world the world in which the command is executed
     * @return a JsonObject containing the result of the fire command
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        String result,message;

        if (world.getCurrentRobot().reload()){
            result = "OK";
            message = "Done";
        }else {
            result = "ERROR";
            message = "Reload failed";
        }

        response.addProperty("result", result);
        data.addProperty("message", message);
        response.add("data", data);
        return response;
    }
}
