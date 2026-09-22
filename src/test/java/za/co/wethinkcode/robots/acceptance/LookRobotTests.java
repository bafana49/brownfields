package za.co.wethinkcode.robots.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.server.MultiServerEngine;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;
import za.co.wethinkcode.robots.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story: Look
 *
 * Now that we can configure a world with objects, we can cater for the scenario wherein a robot can see an obstacle and other robots.
 * Notice that we are not yet dealing with the case of visibility. That will be dealt with in later iterations
 */
public class LookRobotTests {
    private static final String DEFAULT_IP = "localhost";

    private final List<RobotWorldClient> clients = new ArrayList<>();
    private MultiServerEngine localServer;
    private int port;
    private int configuredWidth;
    private int configuredHeight;
    private int configuredVisibility;

    @BeforeEach
    void connectToServer() throws Exception {
        Config.loadConfig("config.properties");
        configuredWidth = Config.WIDTH;
        configuredHeight = Config.HEIGHT;
        configuredVisibility = Config.VISIBILITY;

        // Local 3x3 grid (0..2) has nine cells.
        // Obstacle at [0,1] as specified; centre-first launch places the first robot at [1,1].
        // Maze reloads config.properties in its constructor, so size must be set after World creation.
        World world = new World(false);
        world.getObstacles().clear();
        world.getObstacles().add(new Obstacle(0, 1, 0, 1, ObstacleType.MOUNTAIN));
        Config.WIDTH = 3;
        Config.HEIGHT = 3;
        Config.VISIBILITY = 10;
        localServer = new MultiServerEngine(world);
        localServer.start(0);
        port = localServer.getPort();
    }

    @AfterEach
    void disconnectFromServer() throws IOException {
        for (RobotWorldClient client : clients) {
            client.disconnect();
        }
        clients.clear();
        if (localServer != null) {
            localServer.shutdown();
        }
        Config.WIDTH = configuredWidth;
        Config.HEIGHT = configuredHeight;
        Config.VISIBILITY = configuredVisibility;
    }

    /**
     * Scenario: See an obstacle

     * Given a world of size 2x2

     * and the world has an obstacle at coordinate [0,1]
     *
     *
     * and I have successfully launched a robot into the world
     * When I ask the robot to look
     * Then I should get a response back with an object of type OBSTACLE at a distance of 1 step.
     */
    @Test
    void seeAnObstacle() {
        // Given a world of size 2x2
        // and the world has an obstacle at coordinate [0,1]
        RobotWorldClient client = new RobotWorldJsonClient();
        client.connect(DEFAULT_IP, port);
        clients.add(client);

        // and I have successfully launched a robot into the world
        String launchRequest = "{"
                + "  \"robot\": \"HAL\","
                + "  \"command\": \"launch\","
                + "  \"arguments\": [\"shooter\",\"5\",\"5\"]"
                + "}";
        JsonNode launchResponse = client.sendRequest(launchRequest);
        assertEquals("OK", launchResponse.get("result").asText());

        // When I ask the robot to look
        String lookRequest = "{"
                + "  \"robot\": \"HAL\","
                + "  \"command\": \"look\","
                + "  \"arguments\": []"
                + "}";
        JsonNode lookResponse = client.sendRequest(lookRequest);

        // Then I should get a response back with an object of type OBSTACLE at a distance of 1 step
        assertEquals("OK", lookResponse.get("result").asText());
        assertNotNull(lookResponse.get("data"));
        assertNotNull(lookResponse.get("data").get("objects"));

        boolean foundObstacleAtDistanceOne = false;
        for (JsonNode object : lookResponse.get("data").get("objects")) {
            String type = object.get("type").asText();
            int distance = object.get("distance").asInt();
            if ("OBSTACLE".equals(type) && distance == 1) {
                foundObstacleAtDistanceOne = true;
                break;
            }
        }

        assertTrue(foundObstacleAtDistanceOne, "Expected to find an OBSTACLE at distance 1");
    }

    /**
     * Scenario: See robots and obstacles
     *
     * Given a world of size 2x2
     * and the world has an obstacle at coordinate [0,1]
     * and I have successfully launched 8 robots into the world
     * When I ask the first robot to look
     * Then I should get a response back with
     * one object being an OBSTACLE that is one step away
     * and three objects should be ROBOTs that is one step away
     */
    @Test
    void seeRobotsAndObstacles() {
        // Given a world of size 2x2
        // and the world has an obstacle at coordinate [0,1]
        // and I have successfully launched 8 robots into the world
        RobotWorldClient firstRobotClient = null;
        String firstRobotName = null;

        for (int i = 1; i <= 8; i++) {
            RobotWorldClient client = new RobotWorldJsonClient();
            client.connect(DEFAULT_IP, port);
            clients.add(client);

            String name = "ROBOT" + i;
            JsonNode launchResponse = launch(client, name);
            assertEquals("OK", launchResponse.get("result").asText(),
                    "Robot " + name + " should launch successfully");

            if (i == 1) {
                firstRobotClient = client;
                firstRobotName = name;
            }
        }

        assertNotNull(firstRobotClient, "Expected the first launched robot");

        // When I ask the first robot to look
        JsonNode lookResponse = look(firstRobotClient, firstRobotName);
        assertEquals("OK", lookResponse.get("result").asText());
        assertNotNull(lookResponse.get("data"));
        assertNotNull(lookResponse.get("data").get("objects"));

        // Then I should get a response back with
        // one object being an OBSTACLE that is one step away
        // and three objects should be ROBOTs that is one step away
        int obstaclesAtOne = 0;
        int robotsAtOne = 0;
        for (JsonNode object : lookResponse.get("data").get("objects")) {
            String type = object.get("type").asText();
            int distance = object.get("distance").asInt();
            if ("OBSTACLE".equals(type) && distance == 1) {
                obstaclesAtOne++;
            }
            if ("ROBOT".equals(type) && distance == 1) {
                robotsAtOne++;
            }
        }

        assertEquals(1, obstaclesAtOne, "Expected one OBSTACLE at distance 1");
        assertEquals(3, robotsAtOne, "Expected three ROBOT objects at distance 1");
    }

    private JsonNode launch(RobotWorldClient client, String name) {
        String request = "{"
                + "\"robot\":\"" + name + "\","
                + "\"command\":\"launch\","
                + "\"arguments\":[\"shooter\",\"5\",\"5\"]"
                + "}";
        return client.sendRequest(request);
    }

    private JsonNode look(RobotWorldClient client, String name) {
        String request = "{"
                + "\"robot\":\"" + name + "\","
                + "\"command\":\"look\","
                + "\"arguments\":[]"
                + "}";
        return client.sendRequest(request);
    }

}
