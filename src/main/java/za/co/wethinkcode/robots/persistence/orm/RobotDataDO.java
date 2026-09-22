package za.co.wethinkcode.robots.persistence.orm;

import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.OperationalStatus;

/**
 * Data Object for Robot entity.
 * Represents robot state information for persistence.
 * Designed to work with ORM frameworks (EoDSQL, Hibernate, etc.) using standard JavaBean patterns.
 */
public class RobotDataDO {

    private int id;
    private String name;
    private String type;
    private int positionX;
    private int positionY;
    private Direction direction;
    private OperationalStatus status;
    private int shield;
    private int shots;
    private int maxShots;
    private int maxShields;
    private int bulletDistance;

    /**
     * Public no-arg constructor required by ORM frameworks.
     * Creates a RobotDataDO with default values.
     */
    public RobotDataDO() {
        this.id = 0;
        this.name = null;
        this.type = null;
        this.positionX = 0;
        this.positionY = 0;
        this.direction = null;
        this.status = null;
        this.shield = 0;
        this.shots = 0;
        this.maxShots = 0;
        this.maxShields = 0;
        this.bulletDistance = 0;
    }

    /**
     * Constructor with all fields.
     */
    public RobotDataDO(int id, String name, String type, int positionX, int positionY, 
                      Direction direction, OperationalStatus status, int shield, int shots, 
                      int maxShots, int maxShields, int bulletDistance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.direction = direction;
        this.status = status;
        this.shield = shield;
        this.shots = shots;
        this.maxShots = maxShots;
        this.maxShields = maxShields;
        this.bulletDistance = bulletDistance;
    }

    /**
     * Constructor without id (for new records).
     */
    public RobotDataDO(String name, String type, int positionX, int positionY, 
                      Direction direction, OperationalStatus status, int shield, int shots, 
                      int maxShots, int maxShields, int bulletDistance) {
        this.id = 0;
        this.name = name;
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.direction = direction;
        this.status = status;
        this.shield = shield;
        this.shots = shots;
        this.maxShots = maxShots;
        this.maxShields = maxShields;
        this.bulletDistance = bulletDistance;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public OperationalStatus getStatus() {
        return status;
    }

    public void setStatus(OperationalStatus status) {
        this.status = status;
    }

    public int getShield() {
        return shield;
    }

    public void setShield(int shield) {
        this.shield = shield;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public void setMaxShots(int maxShots) {
        this.maxShots = maxShots;
    }

    public int getMaxShields() {
        return maxShields;
    }

    public void setMaxShields(int maxShields) {
        this.maxShields = maxShields;
    }

    public int getBulletDistance() {
        return bulletDistance;
    }

    public void setBulletDistance(int bulletDistance) {
        this.bulletDistance = bulletDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotDataDO that = (RobotDataDO) o;

        if (id != that.id) return false;
        if (positionX != that.positionX) return false;
        if (positionY != that.positionY) return false;
        if (shield != that.shield) return false;
        if (shots != that.shots) return false;
        if (maxShots != that.maxShots) return false;
        if (maxShields != that.maxShields) return false;
        if (bulletDistance != that.bulletDistance) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (direction != that.direction) return false;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + positionX;
        result = 31 * result + positionY;
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + shield;
        result = 31 * result + shots;
        result = 31 * result + maxShots;
        result = 31 * result + maxShields;
        result = 31 * result + bulletDistance;
        return result;
    }

    @Override
    public String toString() {
        return "RobotDataDO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", positionX=" + positionX +
                ", positionY=" + positionY +
                ", direction=" + direction +
                ", status=" + status +
                ", shield=" + shield +
                ", shots=" + shots +
                ", maxShots=" + maxShots +
                ", maxShields=" + maxShields +
                ", bulletDistance=" + bulletDistance +
                '}';
    }
}
