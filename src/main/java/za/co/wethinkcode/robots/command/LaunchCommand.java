package za.co.wethinkcode.robots.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;
import static za.co.wethinkcode.robots.config.Config.*;
import static za.co.wethinkcode.robots.client.Client.formatState;

/**
 * The Launch class represents a command to launch a robot in the world.
 * It extends the Command class and implements the execute method to perform the launch action.
 * Refactored to use SLF4J Logger instead of System.out.println for better logging control.
 */
public class LaunchCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(LaunchCommand.class);
    private static LaunchCommand instance;

    /**
     * Private constructor to create a Launch command with a name and arguments.
     *
     * @param argument the robot name
     * @param arguments the JsonArray containing robot type
     */
    private LaunchCommand(String argument, JsonArray arguments) {
        super("launch", argument, arguments);
    }

    /**
     * Gets the singleton instance with the given robot name and arguments.
     *
     * @param robotName the name of the robot to launch
     * @param arguments the JsonArray containing robot type
     * @return The singleton instance of LaunchCommand configured with the specified arguments
     */
    public static synchronized LaunchCommand getInstance(String robotName, JsonArray arguments) {
        if (instance == null) {
            instance = new LaunchCommand(robotName, arguments);
        } else {
            setArguments(instance, robotName, arguments);
        }
        return instance;
    }


    /**
     * Helper method to set the arguments for an existing LaunchCommand instance.
     * 
     * @param instance the LaunchCommand instance to update
     * @param robotName the new robot name
     * @param arguments the new JsonArray of arguments
     */
    private static void setArguments(LaunchCommand instance, String robotName, JsonArray arguments) {
        try {
            java.lang.reflect.Field argumentField = Command.class.getDeclaredField("argument");
            java.lang.reflect.Field argumentsField = Command.class.getDeclaredField("arguments");

            argumentField.setAccessible(true);
            argumentsField.setAccessible(true);

            argumentField.set(instance, robotName);
            argumentsField.set(instance, arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a Launch command with the specified name.
     * The name is set to "launch".
     */
    @Override
    public JsonObject execute(World world) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        String robotType = getArguments().get(0).getAsString();
        if (world.getBots().stream().noneMatch(r -> r.getName().equals(getArgument()))) {
            Robot newRobot = new Robot(getArgument(), robotType);
            Position launchPos = findLaunchPosition(world);
            if (launchPos == null) {
                response.addProperty("result", "ERROR");
                data.addProperty("message", "No more space");
            } else {
                newRobot.setPosition(launchPos);
                world.setCurrentRobot(newRobot);
                world.addRobot(newRobot);
                logger.debug("Robot {} launched into Position: {}", newRobot.getName(), formatState(newRobot.state()));
                response.addProperty("result", "OK");
                data.addProperty("message", "Robot '" + getArgument() + "' of type '" + robotType + "' launched.");
                JsonArray position = new JsonArray();
                position.add(launchPos.getX());
                position.add(launchPos.getY());
                data.add("position", position);
            }
        } else {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Robot name already exists");
        }
        response.add("data", data);
        return response;
    }

    /**
     * Finds an open launch cell, preferring the centre of the world first
     * then expanding outward.
     */
    private Position findLaunchPosition(World world) {
        int centreX = WIDTH / 2;
        int centreY = HEIGHT / 2;
        int maxRadius = Math.max(WIDTH, HEIGHT);

        for (int radius = 0; radius < maxRadius; radius++) {
            for (int x = centreX - radius; x <= centreX + radius; x++) {
                for (int y = centreY - radius; y <= centreY + radius; y++) {
                    if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
                        continue;
                    }
                    if (radius > 0
                            && x != centreX - radius && x != centreX + radius
                            && y != centreY - radius && y != centreY + radius) {
                        continue;
                    }
                    Position candidate = new Position(x, y);
                    if (world.isLaunchAllowed(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}
