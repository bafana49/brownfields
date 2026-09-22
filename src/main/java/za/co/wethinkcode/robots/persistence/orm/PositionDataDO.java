package za.co.wethinkcode.robots.persistence.orm;

/**
 * Data Object for Position entity.
 * Represents coordinate data with x and y values.
 * Designed to work with ORM frameworks (EoDSQL, Hibernate, etc.) using standard JavaBean patterns.
 */
public class PositionDataDO {

    private int x;
    private int y;

    /**
     * Public no-arg constructor required by ORM frameworks.
     * Creates a PositionDataDO with default values.
     */
    public PositionDataDO() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructor with coordinate values.
     */
    public PositionDataDO(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters and Setters

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionDataDO that = (PositionDataDO) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "PositionDataDO{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
