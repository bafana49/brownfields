package za.co.wethinkcode.robots.command;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.UpdateResponse;
import za.co.wethinkcode.robots.world.World;
import static za.co.wethinkcode.robots.UpdateResponse.*;

/**
 * The Move class represents a command to move the robot in a specified direction.
 * It extends the Command class and implements the execute method to perform the move action.
 * This class provides a factory method to create instances for forward and back commands.
 */
public class MoveCommand extends Command {


    private static MoveCommand instance;

    /**
     * Private constructor to create a Move command with a name and an argument.
     *
     * @param name the name of the command
     * @param argument the argument for the command
     */
    private MoveCommand(String name, String argument) {
        super(name, argument);
    }

    /**
     * Factory method that creates a new instance of MoveCommand for the specified direction with the given steps.
     * 
     * @param name the direction ("forward", "back", or "backward")
     * @param steps the number of steps to move
     * @return A new instance of MoveCommand configured for the specified direction and steps
     * @throws IllegalArgumentException if the direction is not "forward", "back", or "backward"
     */
    public static synchronized MoveCommand getInstance(String name, String steps) {
        if (instance == null) {
            if ("forward".equalsIgnoreCase(name) || "back".equalsIgnoreCase(name) || "backward".equalsIgnoreCase(name)) {
                setInstance(new MoveCommand(name.toLowerCase(), steps));
            } else {
                throw new IllegalArgumentException("Invalid move direction: " + name);
            }
        }
        instance.setName(name.toLowerCase());
        instance.setArgument(steps);
        return instance;
    }

    public static void setInstance(MoveCommand instance) {
        MoveCommand.instance = instance;
    }

    /**
     * Executes the move command, which changes the position of the robot in the world.
     * The direction and number of steps are determined by the command's name and argument.
     *
     * @param world the world in which the robot is located
     * @return a JsonObject containing the result of the command execution
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        if (getArgument().isEmpty()) setArgument("0");
        int nrSteps = Integer.parseInt(getArgument());


        if (nrSteps <= 0) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "please enter '<forward/back> x' where x is steps to take.");
            response.add("data", data);
            return response;
        }
        int directionMultiplier = 1;
        if(getName().equals("back") || getName().equals("backward")) {
            directionMultiplier = -1;
        }
        UpdateResponse result = world.updatePosition(nrSteps * directionMultiplier);
        String message = "";
        if (result == SUCCESS) {
            message = "Done";
        }else if (result == FAILURE_OBSTRUCTED) {
            message = "Obstructed";
        }else if (result == FAILURE_OUT_OF_BOUNDS) {
            message = "DIED: fell out of bounds";
        } else if (result == DIED_FELL_IN_PIT) {
            message = "DIED : fell in a bottomless pit";
        } else if (result == FAILURE_AT_NORTH_EDGE) {
            message = "At the NORTH edge";
        } else if (result == FAILURE_AT_SOUTH_EDGE) {
            message = "At the SOUTH edge";
        } else if (result == FAILURE_AT_EAST_EDGE) {
            message = "At the EAST edge";
        } else if (result == FAILURE_AT_WEST_EDGE) {
            message = "At the WEST edge";
        }else {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Invalid command, please enter '<forward/back> x' where x is steps to take.");
            response.add("data", data);
            return response;
        }
        response.addProperty("result", "OK");
        data.addProperty("message", message);
        response.add("data", data);
        return response;
    }
}
