package za.co.wethinkcode.robots.persistence.orm;

import za.co.wethinkcode.robots.obstacle.ObstacleType;

/**
 * Data Object for Obstacle entity.
 * Represents an obstacle record in the database with type and position information.
 * Designed to work with ORM frameworks (EoDSQL, Hibernate, etc.) using standard JavaBean patterns.
 */
public class ObstacleDataDO {

    private int id;
    private int worldId;
    private ObstacleType type;
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;

    /**
     * Public no-arg constructor required by ORM frameworks.
     * Creates an ObstacleDataDO with default values.
     */
    public ObstacleDataDO() {
        this.id = 0;
        this.worldId = 0;
        this.type = null;
        this.topLeftX = 0;
        this.topLeftY = 0;
        this.bottomRightX = 0;
        this.bottomRightY = 0;
    }

    /**
     * Constructor with all fields.
     */
    public ObstacleDataDO(int id, int worldId, ObstacleType type, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        this.id = id;
        this.worldId = worldId;
        this.type = type;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
    }

    /**
     * Constructor without id (for new records).
     */
    public ObstacleDataDO(int worldId, ObstacleType type, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        this.id = 0;
        this.worldId = worldId;
        this.type = type;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public ObstacleType getType() {
        return type;
    }

    public void setType(ObstacleType type) {
        this.type = type;
    }

    public int getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(int topLeftX) {
        this.topLeftX = topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(int topLeftY) {
        this.topLeftY = topLeftY;
    }

    public int getBottomRightX() {
        return bottomRightX;
    }

    public void setBottomRightX(int bottomRightX) {
        this.bottomRightX = bottomRightX;
    }

    public int getBottomRightY() {
        return bottomRightY;
    }

    public void setBottomRightY(int bottomRightY) {
        this.bottomRightY = bottomRightY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObstacleDataDO that = (ObstacleDataDO) o;

        if (id != that.id) return false;
        if (worldId != that.worldId) return false;
        if (topLeftX != that.topLeftX) return false;
        if (topLeftY != that.topLeftY) return false;
        if (bottomRightX != that.bottomRightX) return false;
        if (bottomRightY != that.bottomRightY) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + worldId;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + topLeftX;
        result = 31 * result + topLeftY;
        result = 31 * result + bottomRightX;
        result = 31 * result + bottomRightY;
        return result;
    }

    @Override
    public String toString() {
        return "ObstacleDataDO{" +
                "id=" + id +
                ", worldId=" + worldId +
                ", type=" + type +
                ", topLeftX=" + topLeftX +
                ", topLeftY=" + topLeftY +
                ", bottomRightX=" + bottomRightX +
                ", bottomRightY=" + bottomRightY +
                '}';
    }
}
