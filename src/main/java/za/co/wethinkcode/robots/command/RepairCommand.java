package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;

/**
 * The RepairCommand class represents a command to repair a robot's shields.
 * It extends the Command class and implements the execute method to perform the repair action.
 */
public class RepairCommand extends Command {

    private static RepairCommand instance;

    /**
     * Private constructor for RepairCommand.
     * Sets the command name to "repair".
     */
    private RepairCommand() {
        super("repair");
    }

    /**
     * Gets the singleton instance of RepairCommand.
     * 
     * @return The singleton instance of RepairCommand
     */
    public static synchronized RepairCommand getInstance() {
        if (instance == null) {
            instance = new RepairCommand();
        }
        return instance;
    }

    /**
     * Executes the repair command in the given world.
     * It repairs the robot's shields and returns the result.
     *
     * @param world the world in which the command is executed
     * @return a JsonObject containing the result of the repair command
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        String result,message;

        if (world.getCurrentRobot().repair()){
            result = "OK";
            message = "Done";
        }else {
            result = "ERROR";
            message = "Repair failed";
        }

        response.addProperty("result", result);
        data.addProperty("message", message);
        response.add("data", data);
        return response;
    }
}
