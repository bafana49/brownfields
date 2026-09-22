package za.co.wethinkcode.robots.robot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.*;
import za.co.wethinkcode.robots.server.MultiServers;
import za.co.wethinkcode.robots.server.Server;

import java.util.Objects;

import static za.co.wethinkcode.robots.config.Config.*;
import static za.co.wethinkcode.robots.Direction.*;
import static za.co.wethinkcode.robots.OperationalStatus.*;

/**
 * The Robot class represents a robot in the world.
 * It contains information about the robot's position, direction, status, and other attributes.
 */
public class Robot {

    private Position position;
    private Direction currentDirection;
    private OperationalStatus status;
    private String name;
    private int shield;
    private int maxShots = MAX_SHOTS;
    private int maxShields = MAX_SHIELD;
    private int shots;
    private final String type;
    private final int bulletDistance;

    /**
     * Constructor for the Robot class.
     * Initializes the robot's name, type, direction, status, shield, and shots.
     * Refactored to fix the type assignment bug where the else clause assigned to the parameter
     * instead of the instance field. This was causing the robot type to be incorrect when
     * the type wasn't "sniper" or "soldier".
     *
     * @param name The name of the robot.
     * @param type The type of the robot.
     */
    public Robot(String name, String type) {
        super();

        this.position = new Position(0,0);
        this.name = name;
        this.currentDirection = NORTH;
        this.status = NORMAL;

        if (type.equals("sniper")){
            maxShots = 1;
            maxShields = 1;
            this.type = type;

        }
        else if (type.equals("soldier")) {
            maxShots = 3;
            maxShields = 3;
            this.type = type;
        }
        else {
            this.type = "hitbot";
        }

        bulletDistance = 6 - maxShots;
        shots = maxShots;
        shield = maxShields;
    }

    /**
     * Default constructor for the Robot class.
     * Initializes the robot's name, type, direction, status, shield, and shots.
     */
    public Robot(){
        this("Robot","soldier");
    }

    /**
     * Gets the distance that the bullet can travel.
     * @return The distance the bullet can travel.
     */
    public int getBulletDistance() {
        return bulletDistance;
    }

    /**
     * Gets the robot's type.
     * @return The type of the robot.
     */

    public String getType(){
        return type;
    }

    /**
     * Gets the robot's current status.
     *
     * @return The operational status of the robot.
     */
    public OperationalStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the robot's current shots level.
     *
     * @return The shots level of the robot.
     */
    public int getShots() {
        return shots;
    }


    /**
     * Decrements robot's shots by 1
     */
    public void  decrementShot(){
        if (shots > 0) shots--;
    }
    /**
     * Gets the robot's current direction.
     *
     * @return The current direction of the robot.
     */
    public Direction getCurrentDirection() {
        return this.currentDirection;
    }

    /**
     * Updates the robot's direction based on whether it turns right or left.
     *
     * @param turnRight True if the robot turns right, false if it turns left.
     */
    public void updateDirection(boolean turnRight) {
        this.currentDirection = switch (getCurrentDirection()) {
            case NORTH -> turnRight? EAST: WEST;
            case EAST -> turnRight? SOUTH: NORTH;
            case SOUTH -> turnRight? WEST: EAST;
            case WEST -> turnRight? NORTH: SOUTH;
        };
    }


    /**
     * Returns a string representation of the robot's current state.
     * @return A string representation of the robot's position, direction, shields, shots, and status.
     */
    @Override
    public String toString() {
        return "{ 'position':[" + this.position.getX() + ", " + this.position.getY() + "]," +
                "'direction': " + this.currentDirection + ", " + "'shields': " + this.shield
                + ", 'shots': " + this.shots + ", 'status': " + this.status + " }";
    }

    /**
     * Gets the robot's current position.
     * @return The current position of the robot.
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Sets the robot's position.
     * @param status The new operational status of the robot.
     */
    public void setStatus(OperationalStatus status) {
        this.status = status;

    }

    /**
     * Gets the robot's name.
     * @return The name of the robot.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the robot's position.
     * @param pos The new position of the robot.
     */
    public void setPosition(Position pos) {
        this.position = pos;
    }

    /**
     * Checks if robot hits another robot and updates the state.
     * @param targetRobot Another robot instance.
     * @return Boolean whether robot was hit.
     */
    public boolean hit(Robot targetRobot) {
        boolean result ;
        if (shots <= 0) return false;
        result = switch (currentDirection) {
            case NORTH -> targetRobot.gotHit(NORTH, this);
            case EAST -> targetRobot.gotHit(EAST, this);
            case SOUTH -> targetRobot.gotHit(SOUTH, this);
            case WEST -> targetRobot.gotHit(WEST, this);
        };
        return result;
    }

    /**
     * updates state robot was hit.
     * Refactored to remove debug System.out.println that was printing bullet distance.
     * Debug output should not be in production code and was polluting console output.
     */
    private boolean gotHit(Direction direction, Robot bullet) {
        boolean isHit = switch (direction) {
            case NORTH -> (this.position.getY() < bullet.position.getY())&& (this.position.getY() >= bullet.position.getY() - bullet.bulletDistance) && this.position.getX() == bullet.position.getX();
            case SOUTH -> (this.position.getY() > bullet.position.getY())&& (this.position.getY() <= bullet.position.getY() + bullet.bulletDistance) && this.position.getX() == bullet.position.getX();
            case EAST -> (this.position.getX() > bullet.position.getX()) && (this.position.getX() <= bullet.position.getX() + bullet.bulletDistance) && this.position.getY() == bullet.position.getY();
            case WEST -> (this.position.getX() < bullet.position.getX())&& (this.position.getX() >= bullet.position.getX() - bullet.bulletDistance) && this.position.getY() == bullet.position.getY();
        };
//        removed:
//        System.out.println(bullet.bulletDistance); this was an initial bug from the forked repo.

        if (isHit) {
            if (this.shield <= 0) {
                this.status = DEAD;

                Server handler = MultiServers.clientHandlerMap.get(this.name);
                if (handler != null) {
                    handler.sendQuit();
                }

            }
            else shield--;
        }
        return isHit;
    }

    /**
     * Gets the robot's state as a JSON object.
     * @return A JSON object representing the robot's state.
     */
    public JsonObject state() {
        JsonObject state = new JsonObject();
        JsonArray position = new JsonArray();
        position.add(this.position.getX());
        position.add(this.position.getY());
        state.add("position", position);
        state.addProperty("make", type);
        state.addProperty("direction", currentDirection.toString());
        state.addProperty("shields", Integer.parseInt(String.valueOf(shield)));
        state.addProperty("shots", Integer.parseInt(String.valueOf(shots)));
        state.addProperty("status", status.toString());

        return state;
    }

    /**
     * Reloads the robot's shots.
     * It sets the status to RELOAD, sends a reload message to the server,
     * waits for a specified duration, and then resets the status to NORMAL.
     *
     * @return true if the reload was successful.
     */
    public boolean reload(){
        try {
            setStatus(RELOAD);
            Server handler = MultiServers.clientHandlerMap.get(this.name);
            if (handler != null) {
                handler.sendReloadMessage();
            }
            shots = maxShots;
            Thread.sleep(REPAIR_DURATION* 1000L);
            setStatus(NORMAL);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Repairs the robot's shields.
     * It sets the status to REPAIR, sends a repair message to the server,
     * waits for a specified duration, and then resets the status to NORMAL.
     *
     * @return true if the repair was successful.
     */
    public boolean repair(){
        try {
            setStatus(REPAIR);
            Server handler = MultiServers.clientHandlerMap.get(this.name);
            if (handler != null) {
                handler.sendRepairMessage();
            }
            shield = maxShields;
            Thread.sleep(REPAIR_DURATION* 1000L);
            setStatus(NORMAL);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Sets the robot's name.
     * @param name The new name of the robot.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Robot other = (Robot) obj;
        return Objects.equals(this.name, other.getName());
    }

    /**
     * Generates a hash code for the robot based on its name.
     *
     * @return The hash code of the robot.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }



}
