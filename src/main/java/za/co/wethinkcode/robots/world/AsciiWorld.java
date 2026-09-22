package za.co.wethinkcode.robots.world;

import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.robot.Robot;
import static za.co.wethinkcode.robots.config.Config.*;

/**
 * The AsciiWorld class is responsible for generating an ASCII representation of the world.
 * It takes a World object and builds text showing obstacles and robots.
 * Persistence of that text is handled outside this class.
 */
public class AsciiWorld {
    private StringBuilder content = new StringBuilder();
    private World world ;


    /**
     * Constructor for AsciiWorld.
     * @param world The World object representing the current state of the world.
     */
    public AsciiWorld(World world) {
        this.world =world;
        content.append("\n------Legend-------\n" +
                       "MOUNTAIN :      '^'\n" +
                       "LAKE :          '~'\n" +
                       "BOTTOMLESS_PIT :'■'\n");
    }

    /**
     * Returns the content of the ASCII representation.
     * @return A StringBuilder containing the ASCII representation of the world.
     */
    public StringBuilder getContent() {
        return content;
    }

    /**
     * Generates an ASCII representation of the world.
     * File writing is handled by the caller so this class stays free of I/O.
     */
    public void render(){
        int width = WIDTH;  // Use WIDTH from Config
        int height = HEIGHT;  // Use HEIGHT from Config

        String[][] grid = new String[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = " ";
            }
        }

        for (Obstacle obstacle : world.getObstacles()) {
            Position topLeft = obstacle.getTopLeft();
            Position bottomRight = obstacle.getBottomRight();
            String chr = switch (obstacle.getType()){
                case MOUNTAIN -> "^";
                case LAKE -> "~";
                case BOTTOMLESS_PIT -> "■";
            };

            for (int y = topLeft.getY(); y <= bottomRight.getY(); y++) {
                for (int x = topLeft.getX(); x <= bottomRight.getX(); x++) {
                    if (isInsideGrid(x, y, width, height)) {
                        grid[y][x] = chr;
                    }
                }
            }
        }

        for (Robot robot : world.getBots()) {
            Position pos = robot.getPosition();
            char firstLetter = Character.toUpperCase(robot.getName().charAt(0));
            grid[pos.getY()][pos.getX()] = String.valueOf(firstLetter);

        }




        content.append(" ");
        content.append("_".repeat(width));
        content.append("\n");

        for (int y = 0; y < height; y++) {
            content.append("|");
            for (int x = 0; x < width; x++) {
                content.append(grid[y][x]);
            }
            content.append("|\n");
        }

        content.append(" ");
        content.append("-".repeat(width));
        content.append("\n");
    }

    /**
     * Checks if the given coordinates are inside the grid boundaries.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param width The width of the grid.
     * @param height The height of the grid.
     * @return true if the coordinates are inside the grid, false otherwise.
     */
    private boolean isInsideGrid(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
