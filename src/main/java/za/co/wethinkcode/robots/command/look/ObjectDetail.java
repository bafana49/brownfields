package za.co.wethinkcode.robots.command.look;

import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

/**
 * The ObjectDetail class represents the details of an object in the world.
 * It includes the type, distance, direction, and optional name and obstacle type.
 */
public class ObjectDetail {
    private final String type;
    private final int distance;
    private final Direction direction;
    private final String name = null;
    private final ObstacleType obstacleType = null;

    /**
     * Constructor for ObjectDetail.
     *
     * @param type      the type of the object
     * @param distance  the distance to the object
     * @param direction the direction of the object
     */
    ObjectDetail(String type, int distance, Direction direction) {
        this.type = type;
        this.distance = distance;
        this.direction = direction;
    }

    /**
     * Gets the direction of the object.
     * @return direction of the object
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Gets the type of the object.
     *
     * @return the type of the object
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the distance to the object.
     *
     * @return the distance to the object
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Gets the name of the object, if applicable.
     *
     * @return the name of the object, or null if not applicable
     */
    public ObstacleType getObstacleType() {
        return obstacleType;
    }

    /**
     * Gets the name of the object, if applicable.
     *
     * @return the name of the object, or null if not applicable
     */
    public String getName() {
        return name;
    }
}
