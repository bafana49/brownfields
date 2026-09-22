package za.co.wethinkcode.robots.server;

import com.google.gson.*;
import java.io.*;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.wethinkcode.robots.OperationalStatus;
import za.co.wethinkcode.robots.command.*;
import za.co.wethinkcode.robots.command.look.LookCommand;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldGate;
import static za.co.wethinkcode.robots.client.Client.formatServerResponse;

/**
 * Server class to handle client connections and process commands.
 * Refactored to use SLF4J Logger instead of System.out.println for better logging control.
 */
public class Server implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static World world;
    private String robotName;
    private final Socket socket;
    private final WorldGate worldGate;

    public Server(Socket socket, World worldInstance) throws IOException {
        this(socket, worldInstance, WorldGate.immediate());
    }

    public Server(Socket socket, World worldInstance, WorldGate worldGate) throws IOException {
        String clientMachine = socket.getInetAddress().getHostName();
        logger.info("Connection from {}", clientMachine);
        MultiServers.printServerPrompt();
        world = worldInstance;
        this.socket = socket;
        this.worldGate = worldGate;
    }

    /**
     * The main method that runs the server and handles client requests.
     */
    public void run() {
        try(PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String messageFromClient;
            while((messageFromClient = in.readLine()) != null) {
                JsonObject request = JsonParser.parseString(messageFromClient).getAsJsonObject();
                robotName = request.get("robot").getAsString();
                if (!MultiServers.clientHandlerMap.containsKey(robotName)) {
                    MultiServers.clientHandlerMap.put(robotName, this);
                }
                String commandName = request.get("command").getAsString();
                JsonArray args = request.get("arguments").getAsJsonArray();

                JsonObject response = worldGate.run(() -> {
                    world.setCurrentRobotByName(robotName);
                    JsonObject result = handleCommand(commandName, args);
                    world.deleteDeadBots();
                    return result;
                });
                MultiServers.printServerPrompt();
                logger.debug("Server response: {}", formatServerResponse(response));
                out.println(response);
            }
        } catch (SocketException e){
            logger.info("Client Socket has been closed {}", e.getMessage());
        } catch(IOException ex) {
            logger.error("Error with taking input {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sends a reload message to the client indicating that the reload command was successful.
     * It includes the current state of the robot in the response.
     * Refactored to remove resource leak - previously created new PrintStream on each call
     * without closing the previous one. This method is called from robot threads outside
     * the main request loop, so it needs its own stream management but should close properly.
     */
    public void sendRepairMessage() {
        JsonObject response = worldGate.run(() -> {
            JsonObject payload = new JsonObject();
            JsonObject data = new JsonObject();
            payload.addProperty("result", "OK");
            data.addProperty("message", "Done");
            payload.add("data", data);
            payload.add("state", world.getCurrentRobot().state());
            return payload;
        });

        try (PrintStream out = new PrintStream(socket.getOutputStream())) {
            out.println(response);
        } catch (IOException e) {
            logger.error("Repair message unable to send", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a reload message to the client indicating that the robot has been reloaded.
     * It includes the current state of the robot in the response.
     * Refactored to remove resource leak - previously created new PrintStream on each call
     * without closing the previous one. Using try-with-resources to ensure proper cleanup.
     */
    public void sendReloadMessage() {
        JsonObject response = worldGate.run(() -> {
            JsonObject payload = new JsonObject();
            JsonObject data = new JsonObject();
            payload.addProperty("result", "OK");
            data.addProperty("message", "Done");
            payload.add("data", data);
            payload.add("state", world.getCurrentRobot().state());
            return payload;
        });

        try (PrintStream out = new PrintStream(socket.getOutputStream())) {
            out.println(response);
        } catch (IOException e) {
            logger.error("Reload message unable to send", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a quit message to the client and closes the socket connection.
     * It also removes the robot from the client handler map.
     */
    public void sendQuit() {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        response.addProperty("result", "OK");
        data.addProperty("message", "QUIT");
        response.add("data", data );

        try {
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println(response);
            socket.close();  // Close the socket to terminate client
            MultiServers.clientHandlerMap.remove(robotName); // Clean up map
        } catch (IOException e) {
            logger.error("Quit message unable to send", e);
            throw new RuntimeException(e);
        }
    }



    /**
     * Handles the command received from the client.
     *
     * @param commandName The name of the command to execute.
     * @param args       The arguments for the command.
     * @return A JsonObject containing the response to be sent back to the client.
     */
    private JsonObject handleCommand(String commandName, JsonArray args) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        logger.debug("Name: {}, Command: {}, Arguments: {}", robotName, commandName, args);
        Command command;

        try {
            switch (commandName) {
                case "launch" -> {
                    for (Robot robot: world.getRobots()){
                        if (robot.getName().equals(robotName)){
                            response.addProperty("result", "ERROR");
                            data.addProperty("message", "Robot name already exists");
                            response.add("data", data);
                            return response;
                        }
                    }
                    command = LaunchCommand.getInstance(robotName, args);
                }
                case "forward" -> command = MoveCommand.getInstance("forward", String.valueOf(args.get(0).getAsInt()));
                case "back" -> command = MoveCommand.getInstance("back", String.valueOf(args.get(0).getAsInt()));
                case "turn" -> command = TurnCommand.getInstance(String.valueOf(args.get(0)));
                case "state" -> command = StateCommand.getInstance();
                case "look" -> command = LookCommand.getInstance();
                case "help" -> command = HelpCommand.getInstance();
                case "fire" -> command = FireCommand.getInstance();
                case "reload" -> command = ReloadCommand.getInstance();
                case "repair" -> command = RepairCommand.getInstance();
                case "orientation" -> {
                    response.addProperty("result", "OK");
                    data.addProperty("message", "Done");
                    data.addProperty("orientation", world.getCurrentRobot().getCurrentDirection().toString());
                    response.add("data", data);
                    return response;
                }
                default -> {
                    response.addProperty("result", "ERROR");
                    data.addProperty("message", "Unsupported command");
                    response.add("data", data);
                    return response;
                }
            }

        } catch (Exception e) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Invalid command arguments");
            response.add("data", data);
            return response;
        }
        Robot currentRobot = world.getCurrentRobot();
        if (currentRobot == null && !commandName.equals("launch") && !commandName.equals("help")) {
            response.addProperty("result", "ERROR");
            data.addProperty("message", "Cannot perform '" + commandName + "': No robot context active.");
            response.add("data", data);
            return response;
        }

        response = command.execute(world);

        currentRobot = world.getCurrentRobot();

        // Refactored to remove redundant DEAD status checks and simplify logic
        // Previously had duplicate checks for DEAD status that were unnecessary
        if (response.has("result") && "OK".equals(response.get("result").getAsString()) && currentRobot != null) {
            response.add("state", currentRobot.state());
            if (world.getCurrentRobot().getStatus() != OperationalStatus.DEAD) {
                currentRobot.setStatus(OperationalStatus.NORMAL);
            }
        }

        if (currentRobot != null && !commandName.equals("reload") && !commandName.equals("repair") && currentRobot.getStatus()!= OperationalStatus.DEAD) {
            currentRobot.setStatus(OperationalStatus.NORMAL);
        }

        return response;
    }

}
