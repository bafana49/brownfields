package za.co.wethinkcode.robots.persistence.orm;

/**
 * Data Object for World entity.
 * Represents a world record in the database with dimensions and optional name.
 * Designed to work with ORM frameworks (EoDSQL, Hibernate, etc.) using standard JavaBean patterns.
 */
public class WorldDataDO {

    private int id;
    private String name;
    private int width;
    private int height;

    /**
     * Public no-arg constructor required by EoDSQL.
     * Creates a WorldDataDO with default values.
     */
    public WorldDataDO() {
        this.id = 0;
        this.name = null;
        this.width = 0;
        this.height = 0;
    }

    /**
     * Constructor with all fields.
     */
    public WorldDataDO(int id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor without id (for new records).
     */
    public WorldDataDO(String name, int width, int height) {
        this.id = 0;
        this.name = name;
        this.width = width;
        this.height = height;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorldDataDO that = (WorldDataDO) o;

        if (id != that.id) return false;
        if (width != that.width) return false;
        if (height != that.height) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return "WorldDataDO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
