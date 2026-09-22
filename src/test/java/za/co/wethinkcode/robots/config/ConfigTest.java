package za.co.wethinkcode.robots.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @BeforeEach
    void setUp() {
        Config.loadConfig("config.properties");
    }

    @AfterEach
    void restoreConfig() {
        Config.loadConfig("config.properties");
    }

    private static List<String> specs() {
        List<String> result = new ArrayList<>();
        for (Config.CliObstacle obstacle : Config.CLI_OBSTACLES) {
            result.add(obstacle.toSpec());
        }
        return result;
    }

    @Test
    @DisplayName("Omitted flags use spec defaults: port 5000, 1x1, no obstacles")
    void omittedFlagsUseSpecDefaults() {
        Config.applyCommandLineDefaults();
        Config.parseCommandLineArgs(new String[]{});

        assertEquals(1, Config.WIDTH);
        assertEquals(1, Config.HEIGHT);
        assertTrue(Config.CLI_OBSTACLES_SET);
        assertTrue(Config.CLI_OBSTACLES.isEmpty());
        if (System.getenv("PORT") == null || System.getenv("PORT").isEmpty()) {
            assertEquals(5000, Config.PORT);
        }
    }

    @Test
    @DisplayName("config file sets HTTP_PORT for the Web API")
    void loadConfig_setsHttpPort() {
        assertEquals(8080, Config.HTTP_PORT);
        assertNotEquals(Config.PORT, Config.HTTP_PORT);
    }

    @Test
    @DisplayName("CLI -p -s -o override port, square world size, and obstacle")
    void parsePortSizeAndObstacle() {
        Config.parseCommandLineArgs(new String[]{"-p", "5000", "-s", "100", "-o", "10,5"});

        assertEquals(5000, Config.PORT);
        assertEquals(100, Config.WIDTH);
        assertEquals(100, Config.HEIGHT);
        assertEquals(List.of("M-10,5"), specs());
        assertTrue(Config.hasWorldConfigArgs(new String[]{"-p", "5000", "-s", "100", "-o", "10,5"}));
    }

    @Test
    @DisplayName("Repeated -o flags add one obstacle each")
    void parseMultipleObstacleFlags() {
        Config.parseCommandLineArgs(new String[]{"-s", "100", "-o", "10,5", "-o", "20,8", "-o", "1,1"});

        assertEquals(List.of("M-10,5", "M-20,8", "M-1,1"), specs());
    }

    @Test
    @DisplayName("Several x,y values after one -o are all added")
    void parseMultipleObstaclesAfterOneFlag() {
        Config.parseCommandLineArgs(new String[]{"-s", "100", "-o", "10,5", "20,8", "1,1"});

        assertEquals(List.of("M-10,5", "M-20,8", "M-1,1"), specs());
    }

    @Test
    @DisplayName("PowerShell-split obstacle 10 5 is accepted as 10,5")
    void parseObstacleSplitByCommaOperator() {
        Config.parseCommandLineArgs(new String[]{"-o", "10", "5"});

        assertEquals(List.of("M-10,5"), specs());
    }

    @Test
    @DisplayName("-o none means an empty maze, not the config-file obstacles")
    void parseObstacleNone() {
        Config.parseCommandLineArgs(new String[]{"-o", "none"});

        assertTrue(Config.CLI_OBSTACLES_SET);
        assertTrue(Config.CLI_OBSTACLES.isEmpty());
        assertTrue(Config.hasWorldConfigArgs(new String[]{"-o", "none"}));
    }

    @Test
    @DisplayName("L M B prefixes place lake, mountain and pit")
    void parseTypedObstacles() {
        Config.parseCommandLineArgs(new String[]{"-o", "L-2,2", "M-2,3", "B-3,3"});

        assertEquals(3, Config.CLI_OBSTACLES.size());
        assertEquals(ObstacleType.LAKE, Config.CLI_OBSTACLES.get(0).type);
        assertEquals(2, Config.CLI_OBSTACLES.get(0).x);
        assertEquals(2, Config.CLI_OBSTACLES.get(0).y);
        assertEquals(ObstacleType.MOUNTAIN, Config.CLI_OBSTACLES.get(1).type);
        assertEquals(2, Config.CLI_OBSTACLES.get(1).x);
        assertEquals(3, Config.CLI_OBSTACLES.get(1).y);
        assertEquals(ObstacleType.BOTTOMLESS_PIT, Config.CLI_OBSTACLES.get(2).type);
        assertEquals(3, Config.CLI_OBSTACLES.get(2).x);
        assertEquals(3, Config.CLI_OBSTACLES.get(2).y);
    }

    @Test
    @DisplayName("PowerShell-split typed obstacles L-2 2 M-2 3 B-3 3 are accepted")
    void parseTypedObstaclesSplitByCommaOperator() {
        Config.parseCommandLineArgs(new String[]{"-o", "L-2", "2", "M-2", "3", "B-3", "3"});

        assertEquals(List.of("L-2,2", "M-2,3", "B-3,3"), specs());
    }

    @Test
    @DisplayName("A second obstacle on the same cell is skipped")
    void skipDuplicateCell() {
        Config.parseCommandLineArgs(new String[]{"-o", "L-2,2", "M-2,2", "B-3,3"});

        assertEquals(List.of("L-2,2", "B-3,3"), specs());
        assertEquals(ObstacleType.LAKE, Config.CLI_OBSTACLES.get(0).type);
    }

    @Test
    @DisplayName("Obstacles outside the world are dropped with guidance instead of crashing")
    void dropObstaclesOutsideWorld() {
        Config.parseCommandLineArgs(new String[]{"-s", "25", "-o", "L-2,2", "M-25,3", "B-8,8"});

        assertEquals(List.of("L-2,2", "B-8,8"), specs());
    }

    @Test
    @DisplayName("Port-only args do not count as a world-config override")
    void portOnlyIsNotWorldConfig() {
        assertFalse(Config.hasWorldConfigArgs(new String[]{"-p", "5000"}));
        assertFalse(Config.hasWorldConfigArgs(new String[]{}));
    }
}
