package za.co.wethinkcode.robots.maze;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

import java.util.List;

class MazeTest {

    @BeforeEach
    void setUp() {
        System.setProperty("OBSTACLE_MODE", "Random");
        System.setProperty("WIDTH", "10");
        Config.loadConfig("config.properties");
    }

    @AfterEach
    void restoreConfig() {
        Config.loadConfig("config.properties");
    }

    /**
     * Tests the Maze constructor with a specific mode.
     * Verifies that the maze is initialized correctly with the expected number of obstacles.
     */
    @Test
    @DisplayName("Test Maze Constructor with Random Mode")
    void testConstructorRandomMode() {
        Maze maze = new Maze("Random");
        List<Obstacle> obstacles = maze.getObstacles();

        // Test that the maze has between 2 and 5 obstacles (as per randomization)
        assertTrue(!obstacles.isEmpty() && obstacles.size() <= 5);
    }


    /**
     * Tests the Maze constructor with an empty mode.
     * Verifies that the maze is initialized correctly with the expected number of obstacles.
     */
    @Test
    @DisplayName("Test Maze Constructor with Empty Mode")
    void testRandomizeMethod() {
        Maze maze = new Maze("Random");
        List<Obstacle> obstacles = maze.getObstacles();

        // Verify that obstacles are randomly generated with valid coordinates and types
        assertFalse(obstacles.isEmpty());
        for (Obstacle obstacle : obstacles) {
            assertNotNull(obstacle);
            assertTrue(obstacle.getTopLeft().getX() >= 1 && obstacle.getTopLeft().getX() < Config.WIDTH);
            assertTrue(obstacle.getTopLeft().getY() >= 1 && obstacle.getTopLeft().getY() < Config.WIDTH);
            assertNotNull(obstacle.getType());
        }
    }

    /**
     * Tests that the maze does not have overlapping obstacles.
     * Verifies that each obstacle is distinct and does not overlap with others.
     */
    @Test
    @DisplayName("Test No Overlapping Obstacles")
    void testNoOverlappingObstacles() {
        Maze maze = new Maze("Random");
        List<Obstacle> obstacles = maze.getObstacles();

        // Verify that no obstacles are overlapping
        for (int i = 0; i < obstacles.size(); i++) {
            for (int j = i + 1; j < obstacles.size(); j++) {
                assertFalse(obstacles.get(i).isOverlapping(obstacles.get(j)));
            }
        }
    }

    @Test
    @DisplayName("CLI size and obstacle are applied and not reset by Maze")
    void commandLineSizeAndObstacleAreHonoured() {
        Config.parseCommandLineArgs(new String[]{"-s", "100", "-o", "10,5"});

        Maze maze = new Maze("");

        assertEquals(100, Config.WIDTH);
        assertEquals(100, Config.HEIGHT);
        assertEquals(1, maze.getObstacles().size());
        Obstacle obstacle = maze.getObstacles().get(0);
        assertEquals(ObstacleType.MOUNTAIN, obstacle.getType());
        assertEquals(10, obstacle.getTopLeft().getX());
        assertEquals(5, obstacle.getTopLeft().getY());
    }

    @Test
    @DisplayName("CLI -o none produces an empty maze without reloading config size")
    void commandLineObstacleNoneProducesEmptyMaze() {
        Config.parseCommandLineArgs(new String[]{"-s", "50", "-o", "none"});

        Maze maze = new Maze("");

        assertEquals(50, Config.WIDTH);
        assertEquals(50, Config.HEIGHT);
        assertTrue(maze.getObstacles().isEmpty());
    }

    @Test
    @DisplayName("Repeated -o flags place a mountain on each cell")
    void commandLineMultipleObstaclesAreHonoured() {
        Config.parseCommandLineArgs(new String[]{"-s", "100", "-o", "10,5", "-o", "20,8"});

        Maze maze = new Maze("");

        assertEquals(2, maze.getObstacles().size());
        assertEquals(10, maze.getObstacles().get(0).getTopLeft().getX());
        assertEquals(5, maze.getObstacles().get(0).getTopLeft().getY());
        assertEquals(20, maze.getObstacles().get(1).getTopLeft().getX());
        assertEquals(8, maze.getObstacles().get(1).getTopLeft().getY());
        assertEquals(ObstacleType.MOUNTAIN, maze.getObstacles().get(0).getType());
        assertEquals(ObstacleType.MOUNTAIN, maze.getObstacles().get(1).getType());
    }

    @Test
    @DisplayName("Typed CLI obstacles place lake, mountain and pit on distinct cells")
    void commandLineTypedObstaclesAreHonoured() {
        Config.parseCommandLineArgs(new String[]{"-s", "10", "-o", "L-2,2", "M-2,3", "B-3,3"});

        Maze maze = new Maze("");

        assertEquals(3, maze.getObstacles().size());
        assertEquals(ObstacleType.LAKE, maze.getObstacles().get(0).getType());
        assertEquals(ObstacleType.MOUNTAIN, maze.getObstacles().get(1).getType());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, maze.getObstacles().get(2).getType());
        assertEquals(2, maze.getObstacles().get(0).getTopLeft().getX());
        assertEquals(2, maze.getObstacles().get(0).getTopLeft().getY());
        assertEquals(2, maze.getObstacles().get(1).getTopLeft().getX());
        assertEquals(3, maze.getObstacles().get(1).getTopLeft().getY());
        assertEquals(3, maze.getObstacles().get(2).getTopLeft().getX());
        assertEquals(3, maze.getObstacles().get(2).getTopLeft().getY());
    }
}
