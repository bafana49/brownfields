package za.co.wethinkcode.robots.world;

import za.co.wethinkcode.robots.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persistable view of a constructed RobotWorld.
 * Captures world size and map objects (obstacles, pits, mines).
 * Intentionally excludes robots so a saved world can be reused
 * with different collections of robots.
 */
public final class WorldSnapshot {
    private final int width;
    private final int height;
    private final List<Obstacle> obstacles;

    public WorldSnapshot(int width, int height, List<Obstacle> obstacles) {
        this.width = width;
        this.height = height;
        this.obstacles = List.copyOf(obstacles == null ? List.of() : obstacles);
    }

    /**
     * Builds a snapshot from a live world without copying robot state.
     */
    public static WorldSnapshot from(World world) {
        int width = world.getBOTTOM_RIGHT().getX() - world.getTOP_LEFT().getX() + 1;
        int height = world.getBOTTOM_RIGHT().getY() - world.getTOP_LEFT().getY() + 1;
        List<Obstacle> obstacles = world.getObstacles() == null
                ? new ArrayList<>()
                : new ArrayList<>(world.getObstacles());
        return new WorldSnapshot(width, height, obstacles);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }
}
