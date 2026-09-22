package za.co.wethinkcode.robots.config;
//pushh
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

/**
 * The Config class is responsible for loading and storing configuration settings for the robot simulation.
 * It reads properties from a configuration file and provides static variables to access these settings.
 */
public class Config {

    public static int HEIGHT;
    public static int WIDTH;
    public static String HOST;
    public static int PORT;
    public static int HTTP_PORT;
    public static int VISIBILITY;
    public static int REPAIR_DURATION ; // seconds
    public static int RELOAD_DURATION; // seconds
    public static int MAX_SHIELD;
    public static int MAX_SHOTS;
    public static String OBSTACLE_MODE;
    /** True when -o was given (including -o none). Maze then ignores OBSTACLE_MODE. */
    public static boolean CLI_OBSTACLES_SET;
    /** Terminal obstacles. Empty means none (the spec default). */
    public static final List<CliObstacle> CLI_OBSTACLES = new ArrayList<>();

    private static final Pattern TYPED_CELL = Pattern.compile("(?i)(L|M|B|BP)-(-?\\d+),(-?\\d+)");
    private static final Pattern TYPED_X = Pattern.compile("(?i)(L|M|B|BP)-(-?\\d+)");

    /**
     * One cell from -o: L-x,y lake, M-x,y mountain, B-x,y pit. Plain x,y is a mountain.
     */
    public static final class CliObstacle {
        public final int x;
        public final int y;
        public final ObstacleType type;

        public CliObstacle(int x, int y, ObstacleType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public String toSpec() {
            String prefix = switch (type) {
                case LAKE -> "L";
                case MOUNTAIN -> "M";
                case BOTTOMLESS_PIT -> "B";
            };
            return prefix + "-" + x + "," + y;
        }
    }

    /**
     * Loads configuration settings from a properties file.
     * The properties file should contain key-value pairs for various settings.
     *
     * @param configFile The path to the configuration file.
     */
    public static void loadConfig(String configFile) {
        Properties properties = new Properties();
        File rawFile = new File(configFile);

        // Try the provided path first
        File file = rawFile;
        
        // If not found, try current directory (for Docker)
        if (!file.exists()) {
            file = new File("config.properties");
        }
        
        // If still not found, try the hardcoded source path (for local development)
        if (!file.exists()) {
            file = new File(rawFile.getAbsolutePath().replace("config.properties", "src/main/java/za/co/wethinkcode/robots/config/config.properties"));
        }

        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            properties.load(fileInputStream);
            HEIGHT = Integer.parseInt(properties.getProperty("HEIGHT"));
            WIDTH = Integer.parseInt(properties.getProperty("WIDTH"));
            HOST = properties.getProperty("HOST");
            PORT = Integer.parseInt(properties.getProperty("PORT"));
            HTTP_PORT = Integer.parseInt(properties.getProperty("HTTP_PORT", "8080"));
            VISIBILITY = Integer.parseInt(properties.getProperty("VISIBILITY"));
            REPAIR_DURATION = Integer.parseInt(properties.getProperty("REPAIR_DURATION"));
            RELOAD_DURATION = Integer.parseInt(properties.getProperty("RELOAD_DURATION"));
            MAX_SHIELD = Integer.parseInt(properties.getProperty("MAX_SHIELD"));
            MAX_SHOTS = Integer.parseInt(properties.getProperty("MAX_SHOTS"));
            OBSTACLE_MODE = properties.getProperty("OBSTACLE_MODE");
            CLI_OBSTACLES_SET = false;
            CLI_OBSTACLES.clear();
        } catch (IOException e) {
            throw new RuntimeException("Could not load config file. Tried: " + rawFile.getAbsolutePath() + ", " + file.getAbsolutePath(), e);
        }

        // Override PORT from environment variable if set
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                PORT = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT environment variable, using config file value");
            }
        }
        String httpPortEnv = System.getenv("HTTP_PORT");
        if (httpPortEnv != null && !httpPortEnv.isEmpty()) {
            try {
                HTTP_PORT = Integer.parseInt(httpPortEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid HTTP_PORT environment variable, using config file value");
            }
        }
    }

    /**
     * Spec defaults when a flag is omitted: port 5000, size 1x1, obstacles none.
     * Call this from server startup before {@link #parseCommandLineArgs(String[])}.
     * Docker may still override the port with the PORT environment variable.
     */
    public static void applyCommandLineDefaults() {
        WIDTH = 1;
        HEIGHT = 1;
        CLI_OBSTACLES_SET = true;
        CLI_OBSTACLES.clear();
        String portEnv = System.getenv("PORT");
        if (portEnv == null || portEnv.isEmpty()) {
            PORT = 5000;
        }
    }

    /**
     * Parses command line arguments and overrides configuration values.
     * Supported arguments:
     * -p port : Server port (default 5000, range 0-9999)
     * -s size : World size (default 1, range 1-9999)
     * -o none | x,y | L-x,y | M-x,y | B-x,y : repeatable; same cell is skipped
     *
     * @param args Command line arguments
     */
    public static void parseCommandLineArgs(String[] args) {
        boolean firstObstacleFlag = true;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p":
                    if (i + 1 < args.length) {
                        try {
                            int port = Integer.parseInt(args[++i]);
                            if (port >= 0 && port <= 9999) {
                                PORT = port;
                            } else {
                                System.err.println("Port must be between 0 and 9999. Using default: " + PORT);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port number. Using default: " + PORT);
                        }
                    }
                    break;
                case "-s":
                    if (i + 1 < args.length) {
                        try {
                            int size = Integer.parseInt(args[++i]);
                            if (size >= 1 && size <= 9999) {
                                WIDTH = size;
                                HEIGHT = size;
                            } else {
                                System.err.println("Size must be between 1 and 9999. Using default: " + WIDTH);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid size number. Using default: " + WIDTH);
                        }
                    }
                    break;
                case "-o":
                    CLI_OBSTACLES_SET = true;
                    if (firstObstacleFlag) {
                        CLI_OBSTACLES.clear();
                        firstObstacleFlag = false;
                    }
                    i = consumeObstacleValues(args, i);
                    break;
            }
        }
        rejectObstaclesOutsideWorld();
    }

    /**
     * True when the user passed -s or -o.
     */
    public static boolean hasWorldConfigArgs(String[] args) {
        for (String arg : args) {
            if ("-s".equals(arg) || "-o".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static int consumeObstacleValues(String[] args, int flagIndex) {
        int i = flagIndex + 1;
        if (i >= args.length) {
            return flagIndex;
        }
        if ("none".equalsIgnoreCase(args[i])) {
            CLI_OBSTACLES.clear();
            return i;
        }
        while (i < args.length && !isReservedArg(args[i])) {
            if ("none".equalsIgnoreCase(args[i])) {
                CLI_OBSTACLES.clear();
                i++;
                continue;
            }
            int consumed = tryParseObstacleToken(args, i);
            if (consumed < 0) {
                System.err.println("Invalid obstacle format. Use 'none', 'x,y', 'L-x,y', 'M-x,y' or 'B-x,y'.");
                i++;
            } else {
                i += consumed;
            }
        }
        return i - 1;
    }

    private static int tryParseObstacleToken(String[] args, int i) {
        String token = args[i];
        Matcher typed = TYPED_CELL.matcher(token);
        if (typed.matches()) {
            addObstacle(Integer.parseInt(typed.group(2)), Integer.parseInt(typed.group(3)), typeFromPrefix(typed.group(1)));
            return 1;
        }
        Matcher typedX = TYPED_X.matcher(token);
        if (typedX.matches() && i + 1 < args.length && args[i + 1].matches("-?\\d+")) {
            addObstacle(Integer.parseInt(typedX.group(2)), Integer.parseInt(args[i + 1]), typeFromPrefix(typedX.group(1)));
            return 2;
        }
        if (token.matches("-?\\d+,-?\\d+")) {
            String[] parts = token.split(",");
            addObstacle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), ObstacleType.MOUNTAIN);
            return 1;
        }
        if (token.matches("-?\\d+") && i + 1 < args.length && args[i + 1].matches("-?\\d+")) {
            addObstacle(Integer.parseInt(token), Integer.parseInt(args[i + 1]), ObstacleType.MOUNTAIN);
            return 2;
        }
        String compact = token.replaceAll("\\s+", "");
        String[] numbers = compact.split(",");
        if (numbers.length >= 4 && numbers.length % 2 == 0 && allIntegers(numbers)) {
            for (int n = 0; n < numbers.length; n += 2) {
                addObstacle(Integer.parseInt(numbers[n]), Integer.parseInt(numbers[n + 1]), ObstacleType.MOUNTAIN);
            }
            return 1;
        }
        return -1;
    }

    private static void addObstacle(int x, int y, ObstacleType type) {
        for (CliObstacle existing : CLI_OBSTACLES) {
            if (existing.x == x && existing.y == y) {
                System.err.println("Skipping obstacle at " + x + "," + y + " - that cell already has an obstacle");
                return;
            }
        }
        CLI_OBSTACLES.add(new CliObstacle(x, y, type));
    }

    /**
     * Drops cells that fall outside 0..size-1 and prints how to fix them.
     * Must run after -s has been applied.
     */
    static void rejectObstaclesOutsideWorld() {
        int maxX = WIDTH - 1;
        int maxY = HEIGHT - 1;
        List<CliObstacle> kept = new ArrayList<>();
        for (CliObstacle obstacle : CLI_OBSTACLES) {
            if (obstacle.x < 0 || obstacle.y < 0 || obstacle.x > maxX || obstacle.y > maxY) {
                System.err.println("Cannot place " + obstacle.toSpec()
                        + ": cell [" + obstacle.x + "," + obstacle.y
                        + "] is outside this " + WIDTH + "x" + HEIGHT + " world.");
                System.err.println("Use x from 0 to " + maxX + " and y from 0 to " + maxY
                        + ". Example: -o \"" + prefixFor(obstacle.type) + "-" + Math.min(Math.max(obstacle.x, 0), maxX)
                        + "," + Math.min(Math.max(obstacle.y, 0), maxY) + "\"");
                continue;
            }
            kept.add(obstacle);
        }
        CLI_OBSTACLES.clear();
        CLI_OBSTACLES.addAll(kept);
    }

    private static String prefixFor(ObstacleType type) {
        return switch (type) {
            case LAKE -> "L";
            case MOUNTAIN -> "M";
            case BOTTOMLESS_PIT -> "B";
        };
    }

    private static ObstacleType typeFromPrefix(String prefix) {
        return switch (prefix.toUpperCase()) {
            case "L" -> ObstacleType.LAKE;
            case "M" -> ObstacleType.MOUNTAIN;
            case "B", "BP" -> ObstacleType.BOTTOMLESS_PIT;
            default -> ObstacleType.MOUNTAIN;
        };
    }

    private static boolean allIntegers(String[] numbers) {
        for (String number : numbers) {
            if (!number.matches("-?\\d+")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isReservedArg(String arg) {
        return "-p".equals(arg) || "-s".equals(arg) || "-o".equals(arg)
                || "--headless".equals(arg) || "background".equalsIgnoreCase(arg);
    }
}
