package za.co.wethinkcode.robots.client;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;
import static za.co.wethinkcode.robots.config.Config.*;

/**
 * Client class for connecting to the server and sending commands.
 * This class handles the client-side logic for interacting with the robot server.
 */
public class Client {

    private static String robotName;


    public static void main(String[] args) {
        loadConfig("config.properties");

        try (
                Socket socket = new Socket(HOST, PORT);
                PrintStream out = new PrintStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to the server. Initializing client robot...");
            System.out.println("Launch a robot using the following: launch <robottype> <robotname>");
            System.out.println("Robot types:\n  Sniper\n  Soldier\n  Hitbot<default>" );
            launchRobot(scanner, out, in);

            // Start server listener thread
            Thread serverListener = new Thread(() -> listenToServer(in));
            serverListener.setDaemon(true);
            serverListener.start();

            // Main command loop
            while (true) {
                String userCommand = scanner.nextLine().trim().toLowerCase();
                if (userCommand.isEmpty()) continue;

                try {
                    JsonObject request = buildJsonCommand(userCommand, robotName);
                    out.println(request);
                    out.flush();

                } catch (Exception e) {
                    System.out.println("Invalid command format. " + e.getMessage());
                    System.out.print(robotName + " > Enter command: ");
                }

            }


        } catch (IOException e) {
            System.out.println("Client failed to connect or communicate. " + e.getMessage());
        }
    }

    /**
     * Launches the robot by sending a launch command to the server.
     * This method prompts the user for the robot type and name, and handles any errors during launch.
     *
     * @param scanner Scanner for user input
     * @param out    PrintStream for sending commands to the server
     * @param in     BufferedReader for receiving responses from the server
     */
    private static void launchRobot(Scanner scanner, PrintStream out, BufferedReader in) {
        while (true) {
            System.out.print("> Enter command: ");
            String commandLine = scanner.nextLine().trim();
            String[] splitCommand = commandLine.split(" ");

            if (splitCommand.length != 3 || !splitCommand[0].equalsIgnoreCase("launch")) {
                System.out.println("Invalid command. Format: launch <robottype> <robotname>");
                continue;
            }

            String robotType = splitCommand[1];
            robotName = splitCommand[2];

            JsonObject launchRequest = new JsonObject();
            launchRequest.addProperty("robot", robotName);
            launchRequest.addProperty("command", "launch");
            JsonArray args = new JsonArray();
            args.add(robotType);
            launchRequest.add("arguments", args);

            out.println(launchRequest);
            out.flush();

            try {
                String rawResponse = in.readLine();
                JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject();
                String result = responseJson.has("result") ? responseJson.get("result").getAsString() : "";

                if ("ERROR".equalsIgnoreCase(result)) {
                    String message = responseJson.getAsJsonObject("data").get("message").getAsString();
                    if (message.toLowerCase().contains("too many of you in this world")) {
                        System.out.println("❌ Name conflict: ");
                        System.out.print(formatResponse(responseJson));
                        continue;
                    } else {
                        System.out.println("❌ Launch error: ");
                        System.out.print(formatResponse(responseJson));
                        continue;
                    }
                }

                System.out.println("✅ Robot launched successfully.");
                System.out.print(formatResponse(responseJson));
                break;

            } catch (IOException | JsonParseException | NullPointerException e) {
                System.out.println("Failed to receive launch response. Retrying... "  + e.getMessage());
            }
        }
    }


    /**
     * Listens to the server for incoming messages and processes them.
     * This method handles the server's responses and formats them for display.
     *
     * @param in BufferedReader for receiving responses from the server
     */
    private static void listenToServer(BufferedReader in) {
        try {
            String rawResponse;
            while ((rawResponse = in.readLine()) != null) {
                if (rawResponse.equalsIgnoreCase("quit")) {
                    System.out.println("\nServer requested shutdown. Exiting...");
                    System.exit(0);
                }

                try {
                    JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject();
                    String formatted = formatResponse(responseJson);
                    System.out.print("\n" + formatted);


                    if (formatted.contains("DEAD")) {
                        System.out.println("Robot destroyed. Exiting...");
                        System.exit(0);
                    }

                    if (formatted.contains("QUIT")){
                        System.out.println("Your robots shields are below 0 and has died from GunFire.");
                        System.out.println("\nRobot Dead, Terminating connection to Server.");
                        System.exit(0);}

                } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
                    System.out.println("\n< Malformed server response:");
                    System.out.println("  Raw: " + rawResponse + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server. " + e.getMessage());
        }
    }

    /**
     * Formats the server response for display.
     * This method extracts relevant information from the JSON response and formats it for better readability.
     *
     * @param responseJson The JSON response from the server
     * @return A formatted string representation of the server response
     */
    private static String formatResponse(JsonObject responseJson) {
        StringBuilder formatted = new StringBuilder("< Server Response:\n");
        String result = responseJson.has("result") ? responseJson.get("result").getAsString() : "N/A";
        formatted.append("  Result: ").append(result).append("\n");

        if (responseJson.has("data") && responseJson.get("data").isJsonObject()) {
            formatted.append(formatData(responseJson.getAsJsonObject("data")));
        }

        if (responseJson.has("state") && responseJson.get("state").isJsonObject()) {
            formatted.append(formatState(responseJson.getAsJsonObject("state")));
            if (responseJson.get("state").toString().contains("REPAIR")) {
                formatted.append("\n ROBOT IS IN REPAIR MODE: COMMANDS CAN NOT BE TYPED OR PROCESSED\n");
                return formatted.toString();

            } else if (responseJson.get("state").toString().contains("RELOAD")) {
                formatted.append("\n ROBOT IS IN RELOAD MODE: COMMANDS CAN NOT BE TYPED OR PROCESSED\n");
                return formatted.toString();

            }
        }

        formatted.append(robotName).append(" > Enter command: ");

        return formatted.toString();
    }

    /**
     * Formats the server response for client display.
     * This method is similar to formatResponse but specifically formats the response for the client side.
     *
     * @param responseJson The JSON response from the server
     * @return A formatted string representation of the server response for the client
     */
    public static String formatServerResponse(JsonObject responseJson) {
        StringBuilder formatted = new StringBuilder("\n< Client Response:\n");

        String result = responseJson.has("result") ? responseJson.get("result").getAsString() : "N/A";
        formatted.append("  Result: ").append(result).append("\n");

        if (responseJson.has("data") && responseJson.get("data").isJsonObject()) {
            formatted.append(formatData(responseJson.getAsJsonObject("data")));
        }

        if (responseJson.has("state") && responseJson.get("state").isJsonObject()) {
            formatted.append(formatState(responseJson.getAsJsonObject("state")));
        }
        formatted.append("Server Command> ");

        return formatted.toString();
    }

    /**
     * Formats the data section of the server response.
     * This method extracts and formats the message and objects seen by the robot.
     *
     * @param data The JSON object containing data from the server
     * @return A formatted string representation of the data
     */
    private static String formatData(JsonObject data) {
        StringBuilder out = new StringBuilder();

        if (data.has("message")) {
            out.append("  Message: ").append(data.get("message").getAsString()).append("\n");
            if (data.get("message").getAsString().contains("Hit")){
                out.append(formatState(data.getAsJsonObject("state"), data.get("robot").getAsString()));
            }

        }

        if (data.has("orientation")) {
            out.append("  Orientation: ").append(data.get("orientation").getAsString()).append("\n");
        }

        if (data.has("objects") && data.get("objects").isJsonArray()) {
            JsonArray objects = data.getAsJsonArray("objects");
            out.append("  Objects Seen:\n");

            if (objects.isEmpty()) {
                out.append("    - Nothing detected.\n");
            } else {
                for (JsonElement el : objects) {
                    JsonObject obj = el.getAsJsonObject();
                    String type = obj.has("type") ? obj.get("type").getAsString() : "?";
                    String dir = obj.has("direction") ? obj.get("direction").getAsString() : "?";
                    int dist = obj.has("distance") ? obj.get("distance").getAsInt() : -1;

                    StringBuilder objectDescription = new StringBuilder();
                    objectDescription.append(type);

                    if (type.equals("OBSTACLE") && obj.has("obstacle_type")) {
                        objectDescription.append(" (").append(obj.get("obstacle_type").getAsString()).append(")");
                    } else if (type.equals("ROBOT") && obj.has("name")) {
                        objectDescription.append(" (").append(obj.get("name").getAsString()).append(")");
                    }

                    out.append("    - ").append(objectDescription)
                            .append(" [Direction: ").append(dir)
                            .append(", Distance: ");
                    if (dist == 0) {
                        out.append(dir).append(" YOU ARE ON THE EDGE BE CAREFUL! ");
                    } else {
                        out.append(dist);
                    }
                            out.append("]\n");
                }
            }
        }
        return out.toString();
    }

    /**
     * Formats the state section of the server response.
     * This method extracts and formats the robot's state information.
     *
     * @param state The JSON object containing the robot's state
     * @return A formatted string representation of the robot's state
     */
    public static String formatState(JsonObject state) {
        StringBuilder stateOut = new StringBuilder("  State:\n");
        if (robotName!= null) {
            stateOut.append("    - Name: ").append(robotName).append("\n");
        }stateOut.append("    - Position: ").append(state.has("position") ? state.get("position").toString() : "?").append("\n")
                .append("    - Make: ").append(state.has("make") ? state.get("make").getAsString() : "?").append("\n")
                .append("    - Direction: ").append(state.has("direction") ? state.get("direction").getAsString() : "?").append("\n")
                .append("    - Shields: ").append(state.has("shields") ? state.get("shields").getAsString() : "?").append("\n")
                .append("    - Shots: ").append(state.has("shots") ? state.get("shots").getAsString() : "?").append("\n")
                .append("    - Status: ").append(state.has("status") ? state.get("status").getAsString() : "?").append("\n");

        return stateOut.toString();
    }

    /**
     * Formats the state section of the server response with a robot name.
     * This method is similar to formatState but includes the robot's name in the output.
     *
     * @param state The JSON object containing the robot's state
     * @param name  The name of the robot
     * @return A formatted string representation of the robot's state with its name
     */
    public static String formatState(JsonObject state, String name) {
        StringBuilder stateOut = new StringBuilder();
        if (name!= null) {
            stateOut.append("    - Robot: ").append(name).append("\n");
        }
        stateOut.append("  State of robot that's hit:\n")
                .append("    - Position: ").append(state.has("position") ? state.get("position").toString() : "?").append("\n")
                .append("    - Make: ").append(state.has("make") ? state.get("make").getAsString() : "?").append("\n")
                .append("    - Direction: ").append(state.has("direction") ? state.get("direction").getAsString() : "?").append("\n")
                .append("    - Shields: ").append(state.has("shields") ? state.get("shields").getAsString() : "?").append("\n")
                .append("    - Shots: ").append(state.has("shots") ? state.get("shots").getAsString() : "?").append("\n");
        if (!Objects.equals(state.get("status").getAsString(), "DEAD")){
            stateOut.append("    - Status: ").append(state.has("status") ? state.get("status").getAsString() : "?").append("\n");
        }

        return stateOut.toString();
    }

    /**
     * Builds a JSON command object based on the user input.
     * This method constructs a JSON object representing the command to be sent to the server.
     *
     * @param command   The command string entered by the user
     * @param robotName The name of the robot
     * @return A JSON object representing the command
     */
    private static JsonObject buildJsonCommand(String command, String robotName) {
        String[] parts = command.split(" ");
        JsonArray args = new JsonArray();

        switch (parts[0]) {
            case "forward":
            case "back":
                if (parts.length == 2) args.add(Integer.parseInt(parts[1]));
                break;
            case "turn":
                if (parts.length == 2) args.add(parts[1]);
                break;
            default:
                break;
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("robot", robotName);
        obj.addProperty("command", parts[0]);
        obj.add("arguments", args);

        return obj;
    }
}
