package za.co.wethinkcode.robots.client;

import com.google.gson.*;

/**
 * ClientTestHelper.java
 * This class contains a helper method to create JSON objects for robot commands.
 * It is used in the ClientTest class to test the functionality of the Client class.
 */
public class ClientTestHelper {
    /**
     * This method creates a JSON object representing a robot command.
     * It takes the command string and the robot name as parameters.
     * The command string is expected to be in the format "command argument".
     *
     * @param command   The command string (e.g., "forward 10").
     * @param robotName The name of the robot (e.g., "TestBot").
     * @return A JsonObject representing the command and its arguments.
     */
    public static JsonObject getJsonObject(String command, String robotName) {
        String[] commandParts = command.split(" ");

        JsonArray argsArray = new JsonArray();
        if (commandParts[0].equals("forward") || commandParts[0].equals("back")) {
            int steps = Integer.parseInt(commandParts[1]);
            argsArray.add(steps);
        } else if (commandParts[0].equals("turn")) {
            String direction = commandParts[1];
            argsArray.add(direction);
        }

        JsonObject request = new JsonObject();
        request.addProperty("robot", robotName);
        request.addProperty("command", commandParts[0]);
        request.add("arguments", argsArray);
        return request;
    }
}