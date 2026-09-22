package za.co.wethinkcode.robots.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.command.DumpCommand;
import za.co.wethinkcode.robots.command.RestoreCommand;
import za.co.wethinkcode.robots.command.RobotsCommand;
import za.co.wethinkcode.robots.command.SaveCommand;
import za.co.wethinkcode.robots.persistence.JdbcWorldRepository;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.web.WebApiServer;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldGate;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Composition root: shared Domain {@link World}, JDBC repository, socket server, and Web API.
 * Starts {@link MultiServerEngine} on {@link Config#PORT} and {@link WebApiServer} on
 * {@link Config#HTTP_PORT}. Both share one {@link WorldGate} so world commands do not overlap.
 */
public class MultiServers {
    private static final Logger logger = LoggerFactory.getLogger(MultiServers.class);
    public static final ConcurrentHashMap<String, Server> clientHandlerMap = new ConcurrentHashMap<>();

    private static MultiServerEngine server;
    private static WebApiServer webApi;
    private static WorldGate worldGate;

    /**
     * Gets the server instance.
     * 
     * @return The server instance
     */
    public static MultiServerEngine getServer() {
        return server;
    }

    /**
     * Main method to start the server and handle commands.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Config.loadConfig("config.properties"); // durations, visibility, etc.
        Config.applyCommandLineDefaults(); // port 5000, 1x1, no obstacles unless flags override
        Config.parseCommandLineArgs(args);

        // Docker starts the jar with a "background" argument. World flags (-p/-s/-o)
        // must not be treated as headless mode — that hid the GUI and console.
        boolean backgroundMode = isBackgroundMode(args);
        boolean showGui = !backgroundMode;

        WorldRepository worldRepository = new JdbcWorldRepository(JdbcWorldRepository.DEFAULT_URL);
        logger.info("Starting world from command-line configuration ({}x{})", Config.WIDTH, Config.HEIGHT);
        World worldInstance = new World(showGui);
        worldGate = WorldGate.serialized();
        server = new MultiServerEngine(worldInstance, worldGate);

        try {
            server.start(Config.PORT);
        } catch (IOException e) {
            System.err.println("Failed to bind to port: " + e.getMessage());
            return;
        }

        startWebApi(worldInstance, worldRepository, worldGate);
        printWorldConfiguration();
        worldInstance.openGuiIfEnabled();

        if (backgroundMode) {
            System.out.println("Server running in background mode. Press Ctrl+C to stop.");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                logger.info("Server interrupted, shutting down...");
                shutdownServers();
            }
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Server Command> ");
            String command = scanner.nextLine().toLowerCase().trim();
            String[] parts = command.split("\\s+", 2);
            String verb = parts[0];
            String argument = parts.length > 1 ? parts[1] : null;

            switch (verb) {
                case "quit":
                case "shutdown":
                    logger.info("Shutting down server...");
                    server.broadcastMessage("quit");
                    shutdownServers();
                    System.exit(0);
                    break;
                case "dump":
                    worldGate.run(() -> DumpCommand.getInstance().dump(worldInstance));
                    break;
                case "save":
                    worldGate.run(() -> {
                        if (argument == null || argument.isBlank()) {
                            new SaveCommand(worldRepository).save(worldInstance);
                        } else {
                            new SaveCommand(worldRepository).save(worldInstance, argument);
                        }
                    });
                    break;
                case "restore":
                    worldGate.run(() -> {
                        if (argument == null || argument.isBlank()) {
                            new RestoreCommand(worldRepository).restore(worldInstance);
                        } else {
                            new RestoreCommand(worldRepository).restore(worldInstance, argument);
                        }
                    });
                    break;
                case "robots":
                    worldGate.run(() -> RobotsCommand.getInstance().printRobots(worldInstance));
                    break;
                default:
                    logger.warn("Unknown command: {}", command);
                    break;
            }
        }
    }

    static void startWebApi(World world, WorldRepository worldRepository) {
        startWebApi(world, worldRepository, WorldGate.immediate());
    }

    static void startWebApi(World world, WorldRepository worldRepository, WorldGate gate) {
        if (Config.HTTP_PORT == Config.PORT) {
            System.err.println("HTTP_PORT (" + Config.HTTP_PORT
                    + ") is the same as the socket PORT. Web API not started.");
            return;
        }
        try {
            webApi = new WebApiServer(world, worldRepository, gate);
            webApi.start(Config.HTTP_PORT);
            System.out.println("Web API listening on http://localhost:" + webApi.getPort());
        } catch (Exception e) {
            System.err.println("Failed to start Web API on port " + Config.HTTP_PORT + ": " + e.getMessage());
            webApi = null;
        }
    }

    static void shutdownServers() {
        if (webApi != null) {
            try {
                webApi.stop();
            } catch (Exception e) {
                logger.error("Error stopping Web API: {}", e.getMessage());
            }
            webApi = null;
        }
        if (server != null) {
            try {
                server.shutdown();
            } catch (IOException e) {
                logger.error("Error shutting down: {}", e.getMessage());
            }
        }
        if (worldGate != null) {
            worldGate.shutdown();
            worldGate = null;
        }
    }

    static boolean isBackgroundMode(String[] args) {
        for (String arg : args) {
            if ("background".equalsIgnoreCase(arg) || "--headless".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printWorldConfiguration() {
        System.out.println("World size: " + Config.WIDTH + "x" + Config.HEIGHT);
        System.out.println("Socket port: " + Config.PORT);
        if (webApi != null) {
            System.out.println("Web API port: " + webApi.getPort());
        }
        if (!Config.CLI_OBSTACLES_SET) {
            return;
        }
        if (Config.CLI_OBSTACLES.isEmpty()) {
            System.out.println("Obstacles: none");
            return;
        }
        StringBuilder listed = new StringBuilder();
        for (int i = 0; i < Config.CLI_OBSTACLES.size(); i++) {
            if (i > 0) {
                listed.append(" ");
            }
            listed.append(Config.CLI_OBSTACLES.get(i).toSpec());
        }
        System.out.println("Obstacles: " + listed);
    }

    /**
     * Prints the server command prompt.
     * Kept as System.out.print since this is interactive user prompt, not logging.
     */
    public static void printServerPrompt() {
        System.out.print("\nServer Command> ");
    }
}
