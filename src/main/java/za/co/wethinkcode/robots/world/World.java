package za.co.wethinkcode.robots.world;


import za.co.wethinkcode.robots.OperationalStatus;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.UpdateResponse;
import za.co.wethinkcode.robots.maze.Maze;
import za.co.wethinkcode.robots.obstacle.*;
import za.co.wethinkcode.robots.robot.Robot;
import java.util.ArrayList;
import java.util.List;
import za.co.wethinkcode.robots.config.Config;
import static za.co.wethinkcode.robots.Direction.*;
import static za.co.wethinkcode.robots.config.Config.*;
import static za.co.wethinkcode.robots.UpdateResponse.*;


/**
 * The World class represents the game world where robots and obstacles exist.
 * It contains methods to manage robots, check positions, and update their states.
 */
public class World {


    // Changed from  final  to allow modification during restore operations
    private Position TOP_LEFT;
    private Position BOTTOM_RIGHT;
    private final Maze maze;
    private final List<Robot> robots;
    private Robot currentRobot;
    private final List<Obstacle> obstacleList;
    private WorldGUI gui;
    private final boolean GUI;

    public Position getTOP_LEFT() {
        return TOP_LEFT;
    }

    public Position getBOTTOM_RIGHT() {
        return BOTTOM_RIGHT;
    }

    /**
     * Constructor for the World class.
     * Initializes the maze and sets the boundaries of the world.
     */
    public World(boolean GUI) {
        this.maze = new Maze("");
        this.TOP_LEFT = new Position(0, 0);
        this.BOTTOM_RIGHT = new Position(HEIGHT - 1, WIDTH - 1);
        obstacleList = maze.getObstacles();
        this.GUI = GUI;
        robots = new ArrayList<>();
    }

    /**
     * Restore constructor — creates a world from a persisted snapshot.
     * World dimensions and obstacles come from the snapshot;
     * the Maze is bypassed so no random obstacles are generated.
     *
     * @param snapshot the saved world configuration
     * @param GUI      whether to show the Swing GUI
     */
    public World(WorldSnapshot snapshot, boolean GUI) {
        Config.WIDTH  = snapshot.getWidth();
        Config.HEIGHT = snapshot.getHeight();
        this.maze         = new Maze("none");
        this.TOP_LEFT     = new Position(0, 0);
        this.BOTTOM_RIGHT = new Position(snapshot.getWidth() - 1, snapshot.getHeight() - 1);
        this.obstacleList = new ArrayList<>(snapshot.getObstacles());
        this.robots       = new ArrayList<>();
        this.GUI          = GUI;
    }

    /**
     * Opens the Swing world view after the server is listening so a large
     * grid cannot delay (or look like it blocked) port bind.
     */
    public void openGuiIfEnabled() {
        if (GUI && gui == null) {
            try {
                gui = new WorldGUI(this);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Could not open the world window: an obstacle or robot is outside the map.");
                System.err.println("For a " + WIDTH + "x" + HEIGHT + " world, use positions from 0 to "
                        + (WIDTH - 1) + ".");
            }
        }
    }




    /**
     * Sets the current robot by its name.
     * @param name The name of the robot to set as current.
     */
    public void setCurrentRobotByName(String name) {
        currentRobot = null;
        for (Robot robot : robots) {
            if (robot.getName().equals(name)) {
                currentRobot = robot;
                break;
            }
        }
    }

    /**
     * Sets the current robot directly.
     * @param robot The robot to set as current.
     */
    public void setCurrentRobot(Robot robot) {
        currentRobot = robot;
    }

    /**
     * Returns the list of obstacles in the maze.
     * @return A list of obstacles.
     */
    public List<Obstacle> getObstacles() {
        return maze.getObstacles();
    }

    /**
     * Returns the current robot.
     * @return The current robot.
     */
    public Robot getCurrentRobot() {
        return currentRobot;
    }

    /**
     * Returns the list of robots in the world.
     * @return A list of robots.
     */
    public List<Robot> getBots() {
        return robots;
    }

    /**
     * Adds a robot to the world.
     * @param robot The robot to add.
     */
    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    /**
     * Checks if a new position is allowed based on the current robot's position and obstacles.
     * @param newPosition The new position to check.
     * @return true if the new position is allowed, false otherwise.

     * Refactored to add null safety check for currentRobot to prevent NullPointerException
     * when currentRobot is null but the method is still called.
     */

    public boolean isNewPositionAllowed(Position newPosition) {
        for (Robot robot : robots) {
            if (currentRobot != null && robot.getName().equals(currentRobot.getName())) continue;

            if (robot.getPosition().equals(newPosition)) return false;

            if (currentRobot != null && robot.getPosition().getY() == currentRobot.getPosition().getY()) {
                if (currentRobot.getPosition().getX() > robot.getPosition().getX()) {
                    if (currentRobot.getCurrentDirection() == WEST)
                        return newPosition.getX() > robot.getPosition().getX();
                }
//                Need to address these nested if statements: look at codescene report again.
                else if (currentRobot.getCurrentDirection() == EAST)
                    return newPosition.getX() < robot.getPosition().getX();

            } else if (currentRobot != null && robot.getPosition().getX() == currentRobot.getPosition().getX()) {
                if (currentRobot.getPosition().getY() > robot.getPosition().getY()) {
                    if (currentRobot.getCurrentDirection() == SOUTH)
                        return newPosition.getY() > robot.getPosition().getY();

                } else if (currentRobot.getCurrentDirection() == NORTH)
                    return newPosition.getY() < robot.getPosition().getY();
            }
        }

        for (Obstacle obstacle : obstacleList) {
            if ((newPosition.isIn(obstacle.getTopLeft(), obstacle.getBottomRight())
                    || (currentRobot != null && obstacle.blocksPath(currentRobot.getPosition(), newPosition)))
                    && obstacle.getType()!= ObstacleType.BOTTOMLESS_PIT) return false;
        }
        return true;
    }

    /**
     * Checks if a new position is allowed for launching based on the current robot's positions and obstacles.
     * @param newPosition The new position to check.
     * @return true if the new position is allowed, false otherwise.
     */
    public boolean isLaunchAllowed(Position newPosition) {
        for (Robot robot : robots) {
            if (currentRobot != null && robot.getName().equals(currentRobot.getName())) continue;
            if (robot.getPosition().equals(newPosition)) return false;
        }
        for (Obstacle obstacle : obstacleList) {
            if (newPosition.isIn(obstacle.getTopLeft(), obstacle.getBottomRight())) return false;
        }
        return true;
    }


    /**
     *checks if robot can move from current position to the new without being obstructed
     * @param newPos - new position that the bot is attempting to move to
     * @return boolean that states if bot can move or not.
     */
    public boolean isMovementObstructed(Position newPos){
        for(Obstacle o : obstacleList){
            if (o.getType() == ObstacleType.MOUNTAIN){
                return newPos.isIn(o.getTopLeft(), o.getBottomRight());
            }
        }
        return true;
    }

    /**
     * Updates the position of the current robot based on the number of steps.
     * @param nrSteps The number of steps to move.
     * @return An UpdateResponse indicating the result of the update.
     */
    public UpdateResponse updatePosition(int nrSteps) {

        Position oldPos = getCurrentRobot().getPosition();
        Position pos = oldPos.newPos(currentRobot.getCurrentDirection(), nrSteps);

        if(pos.isIn(TOP_LEFT, BOTTOM_RIGHT) && isNewPositionAllowed(pos)){
            getCurrentRobot().setPosition(pos);
            if (GUI) gui.update();
            for (Obstacle o : getObstacles()){
                if (o.getType() == ObstacleType.BOTTOMLESS_PIT && (o.blocksPosition(pos) || o.blocksPath(oldPos, pos))){
                    getCurrentRobot().setStatus(OperationalStatus.DEAD);

                    return DIED_FELL_IN_PIT;
                }
            }
            return SUCCESS;
        } else if (!pos.isIn(TOP_LEFT, BOTTOM_RIGHT) && isNewPositionAllowed(pos)) {
            // Check which edge the robot is at based on direction
            if (currentRobot.getCurrentDirection() == NORTH) {
                return FAILURE_AT_NORTH_EDGE;
            } else if (currentRobot.getCurrentDirection() == SOUTH) {
                return FAILURE_AT_SOUTH_EDGE;
            } else if (currentRobot.getCurrentDirection() == EAST) {
                return FAILURE_AT_EAST_EDGE;
            } else if (currentRobot.getCurrentDirection() == WEST) {
                return FAILURE_AT_WEST_EDGE;
            }
            getCurrentRobot().setStatus(OperationalStatus.DEAD);
            if (GUI)gui.update();
            return FAILURE_OUT_OF_BOUNDS;
        }
        if (GUI) gui.update();
        return FAILURE_OBSTRUCTED;

    }

    /**
     * deletes robots with a DEAD status
     */
    public void deleteDeadBots(){
        robots.removeIf(r -> r.getStatus() == OperationalStatus.DEAD);
        if (GUI) gui.update();
    }

    /**
     * Returns the maze object.
     * @return The maze object.
     */
    public List<Robot> getRobots(){
        return robots;
    }

    /**
     * Replaces this world's size and map objects from a persisted snapshot.
     * Robots are cleared so the restored world can be replayed with new robots.
     * Database access stays in the repository; this method only applies domain state.
     * Refactored to actually apply the obstacles from the snapshot to the obstacle list.
     * Previously this method only updated the config dimensions but didn't apply the obstacles,
     * which caused test failures expecting the restored obstacles to be present.
     * Also fixed to update the instance boundary fields (TOP_LEFT, BOTTOM_RIGHT) in addition
     * to the static Config values, so the world boundaries are properly restored.
     */
    public void restore(WorldSnapshot snapshot) {
        Config.WIDTH = snapshot.getWidth();
        Config.HEIGHT = snapshot.getHeight();
        
        // Update the instance boundary fields to match the restored dimensions
        this.TOP_LEFT = new Position(0, 0);
        this.BOTTOM_RIGHT = new Position(snapshot.getWidth() - 1, snapshot.getHeight() - 1);

        // replace obstacle list with restored world starts fresh
        robots.clear();
        currentRobot = null;
        obstacleList.clear();
        obstacleList.addAll(snapshot.getObstacles());

        //Refresh GUI if running
        if (GUI) gui.update();
    }

}

