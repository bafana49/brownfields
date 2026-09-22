package za.co.wethinkcode.robots.acceptance;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.server.MultiServerEngine;
import za.co.wethinkcode.robots.world.World;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story: Launch Robot
 * As a player
 * I want to launch my robot in the online robot world
 * So that I can break the record for the most robot kills
 */
class LaunchRobotTests {
    private static final String DEFAULT_IP = "localhost";

    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
    private MultiServerEngine localServer;
    private int port;
    private int configuredWidth;
    private int configuredHeight;

    @BeforeEach
    void connectToServer() throws Exception {
        Config.loadConfig("config.properties");
        configuredWidth = Config.WIDTH;
        configuredHeight = Config.HEIGHT;
    }

    @AfterEach
    void disconnectFromServer() throws IOException {
        serverClient.disconnect();
        if (localServer != null) {
            localServer.shutdown();
        }
        Config.WIDTH = configuredWidth;
        Config.HEIGHT = configuredHeight;
    }

    // Scenario: Valid launch should succeed (1x1 world)
    @Test
    void validLaunchShouldSucceed() throws Exception {
        setup1x1World();
        assertTrue(serverClient.isConnected());

        // Given that I am connected to a running Robot Worlds server
        // And the world is of size 1x1 (The world is configured or hardcoded to this size)

        // When I send a valid launch request to the server
        String request = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
        JsonNode response = serverClient.sendRequest(request);

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());

        // And the position should be (x:0, y:0)
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("position"));
        assertEquals(0, response.get("data").get("position").get(0).asInt());
        assertEquals(0, response.get("data").get("position").get(1).asInt());

        // And I should also get the state of the robot
        assertNotNull(response.get("state"));
    }

    // Scenario: Invalid launch should fail
    @Test
    void invalidLaunchShouldFail() throws Exception {
        setup1x1World();
        assertTrue(serverClient.isConnected());

        // Given that I am connected to a running Robot Worlds server

        // When I send an invalid launch request with the command "luanch" instead of "launch"
        String request = "{" + "\"robot\": \"HAL\"," + "\"command\": \"luanch\"," + "\"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
        JsonNode response = serverClient.sendRequest(request);

        // Then I should get an error response
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());

        // And the message "Unsupported command"
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("message"));
        assertTrue(response.get("data").get("message").asText().contains("Unsupported command"));
    }

    // Scenario: No more space in world should fail (1x1 world)
    @Test
    void noMoreSpaceInWorldShouldFail() throws Exception {
        setup1x1World();
        assertTrue(serverClient.isConnected());

        // Given that I am connected to a running Robot Worlds server
        // And the world is of size 1x1

        // When I launch a robot successfully
        String launchRequest1 = "{" +
                "\"robot\": \"ROBOT1\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response1 = serverClient.sendRequest(launchRequest1);
        assertEquals("OK", response1.get("result").asText());

        // When I attempt to launch another robot
        String launchRequest2 = "{" +
                "\"robot\": \"ROBOT2\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response2 = serverClient.sendRequest(launchRequest2);

        // Then I should get an error response
        assertNotNull(response2.get("result"));
        assertEquals("ERROR", response2.get("result").asText());

        // And the message should indicate no more space
        assertNotNull(response2.get("data"));
        assertNotNull(response2.get("data").get("message"));
        assertTrue(response2.get("data").get("message").asText().contains("No more space"));
    }

    // Scenario: Can launch another robot (2x2 world)
    @Test
    void canLaunchAnotherRobot() throws Exception {
        setup2x2World();
        assertTrue(serverClient.isConnected());

        // Given a world of size 2x2
        // and robot "HAL" has already been launched into the world
        JsonNode halLaunch = launchRobot("HAL");
        assertEquals("OK", halLaunch.get("result").asText());

        // When I launch robot "R2D2" into the world
        JsonNode r2d2Launch = launchRobot("R2D2");

        // Then the launch should be successful
        assertNotNull(r2d2Launch.get("result"));
        assertEquals("OK", r2d2Launch.get("result").asText());

        // and a randomly allocated position of R2D2 should be returned
        JsonNode position = null;
        if (r2d2Launch.has("data") && r2d2Launch.get("data").has("position")) {
            position = r2d2Launch.get("data").get("position");
        } else if (r2d2Launch.has("state") && r2d2Launch.get("state").has("position")) {
            position = r2d2Launch.get("state").get("position");
        }
        assertNotNull(position, "R2D2 launch response must include a position");
        assertTrue(position.isArray());
        assertEquals(2, position.size());
        assertTrue(position.get(0).isNumber());
        assertTrue(position.get(1).isNumber());
    }

    // Scenario: World without obstacles is full (2x2 world)
    @Test
    void worldWithoutObstaclesIsFull() throws Exception {
        setup2x2World();
        assertTrue(serverClient.isConnected());

        // Given a world of size 2x2
        // and I have successfully launched 9 robots into the world
        for (int i = 1; i <= 9; i++) {
            JsonNode launchResponse = launchRobot("ROBOT" + i);
            assertEquals("OK", launchResponse.get("result").asText(),
                    "Robot " + i + " should launch successfully before testing overflow");
        }

        // When I launch one more robot (10th robot)
        JsonNode overflowResponse = launchRobot("ONE_TOO_MANY");

        // Then I should get an error response back with the message "No more space in this world"
        assertEquals("ERROR", overflowResponse.get("result").asText());
        assertNotNull(overflowResponse.get("data"));
        assertNotNull(overflowResponse.get("data").get("message"));
        assertTrue(overflowResponse.get("data").get("message").asText().contains("No more space"));
    }

    // Scenario: Launch robots into a world with an obstacle (2x2 world)
    @Test
    void launchRobotsIntoWorldWithObstacle() throws Exception {
        setup2x2WorldWithObstacle();
        assertTrue(serverClient.isConnected());

        // Given a 2x2 world (nine coordinates in this project's 0..2 grid)
        // and the world has an obstacle at coordinate [1,1]

        // When I launch 8 robots into the world
        for (int i = 1; i <= 8; i++) {
            JsonNode launchResponse = launchRobot("ROBOT" + i);
            assertEquals("OK", launchResponse.get("result").asText(),
                    "Robot " + i + " should launch successfully");

            // Then each robot cannot be in position [1,1]
            int x = launchResponse.get("state").get("position").get(0).asInt();
            int y = launchResponse.get("state").get("position").get(1).asInt();
            assertFalse(x == 1 && y == 1,
                    "Robot " + i + " must not be placed on the obstacle at [1,1]");
        }
    }

    // Scenario: World with an obstacle is full (2x2 world)
    @Test
    void worldWithObstacleIsFull() throws Exception {
        setup2x2WorldWithObstacle();
        assertTrue(serverClient.isConnected());

        // Given a 2x2 world with an obstacle at coordinate [1,1]
        // and I have successfully launched 8 robots into the world
        for (int i = 1; i <= 8; i++) {
            JsonNode launchResponse = launchRobot("ROBOT" + i);
            assertEquals("OK", launchResponse.get("result").asText(),
                    "Robot " + i + " should launch successfully before testing overflow");
        }

        // When I launch one more (9th) robot into the now-full world
        JsonNode overflowResponse = launchRobot("ONE_TOO_MANY");

        // Then I should get an error response with the specific message
        assertEquals("ERROR", overflowResponse.get("result").asText());
        assertNotNull(overflowResponse.get("data"));
        assertNotNull(overflowResponse.get("data").get("message"));
        assertTrue(overflowResponse.get("data").get("message").asText().contains("No more space"));
    }

    private void setup1x1World() throws Exception {
        World world = new World(false);
        world.getObstacles().clear();
        Config.WIDTH = 1;
        Config.HEIGHT = 1;
        localServer = new MultiServerEngine(world);
        localServer.start(0);
        port = localServer.getPort();
        serverClient.connect(DEFAULT_IP, port);
    }

    private void setup2x2World() throws Exception {
        World world = new World(false);
        world.getObstacles().clear();
        Config.WIDTH = 3;
        Config.HEIGHT = 3;
        localServer = new MultiServerEngine(world);
        localServer.start(0);
        port = localServer.getPort();
        serverClient.connect(DEFAULT_IP, port);
    }

    private void setup2x2WorldWithObstacle() throws Exception {
        World world = new World(false);
        world.getObstacles().clear();
        world.getObstacles().add(new Obstacle(1, 1, 1, 1, ObstacleType.MOUNTAIN));
        Config.WIDTH = 3;
        Config.HEIGHT = 3;
        localServer = new MultiServerEngine(world);
        localServer.start(0);
        port = localServer.getPort();
        serverClient.connect(DEFAULT_IP, port);
    }

    private JsonNode launchRobot(String robotName) {
        String launchRequest = "{"
                + "  \"robot\": \"" + robotName + "\","
                + "  \"command\": \"launch\","
                + "  \"arguments\": [\"shooter\",\"5\",\"5\"]"
                + "}";
        return serverClient.sendRequest(launchRequest);
    }
}
