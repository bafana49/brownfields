package za.co.wethinkcode.robots.command.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.command.Command;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;


/**
 * Handles the "look" command.
 * Determines visible objects around the current robot up to the configured visibility range,
 * considering obstacle types for line of sight. Reports all seen objects.
 */
public class LookCommand extends Command {

    private static LookCommand instance;

    /**
     * Constructs a LookCommand, setting the command name to "look".
     */
    public LookCommand() {
        super("look");
    }


    /**
     * Gets the singleton instance of LookCommand.
     *
     * @return The singleton instance of LookCommand
     */
    public static synchronized LookCommand getInstance() {
        if (instance == null) {
            instance = new LookCommand();
        }
        return instance;
    }

    /**
     * Executes the look command for the current robot.
     * Scans surroundings, respecting visibility limits and obstacle types,
     * and returns a JSON response with all seen objects.
     *
     * @param world The game world providing state information.
     * @return A JsonObject detailing seen objects or an error if the look cannot be performed.
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        JsonArray objectsJsonArray;

        Robot currentRobot = world.getCurrentRobot();
        if (currentRobot == null) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Cannot perform 'look': No robot context active.");
            response.add("data", data);
            return response;
        }
        Position currentPos = currentRobot.getPosition();
        if (currentPos == null) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Cannot perform 'look': Robot has not been positioned.");
            response.add("data", data);
            return response;
        }
        DetectedObjectDetails details = new DetectedObjectDetails(currentPos, world);
        objectsJsonArray = details.getSeenObjectDetails();

        response.addProperty("result", "OK");
        data.add("objects", objectsJsonArray);
        response.add("data", data);

        return response;
    }
}
