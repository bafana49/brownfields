package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;

/**
 * The Turn class represents a command to turn the robot in a specified direction.
 * It extends the Command class and implements the execute method to perform the turn action.
 */
public class TurnCommand extends Command {

    private static TurnCommand instance;

    /**
     * Private constructor to create a Turn command with a direction argument.
     *
     * @param argument the direction to turn ("left" or "right")
     */
    private TurnCommand(String argument) {
        super("turn", argument);
    }

    /**
     * Gets a TurnCommand instance with the given direction.
     * 
     * @param direction the direction to turn ("left" or "right")
     * @return A TurnCommand instance configured for the specified direction
     */
    public static synchronized TurnCommand getInstance(String direction) {
        if (instance == null) instance = new TurnCommand(direction);
        instance.setArgument(direction);
        return instance;
    }

    /**
     * Executes the turn command, which changes the direction of the robot.
     * The direction is determined by the argument passed to the command.
     *
     * @param world the world in which the robot is located
     * @return a JsonObject containing the result of the command execution
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        boolean turn;

        if (getArgument().equals("right") || getArgument().equals("\"right\"")){
            turn = true;
        } else if (getArgument().equals("left") || getArgument().equals("\"left\"")) {
            turn = false;
        }
        else {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Invalid command, please enter 'turn <right/left>'.");
            response.add("data", data);
            return response;
        }
        world.getCurrentRobot().updateDirection(turn);

        response.addProperty("result", "OK");
        data.addProperty("message", "Done");
        response.add("data", data);
        return response;
    }
}
