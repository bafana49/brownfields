package za.co.wethinkcode.robots.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.server.MultiServerEngine;
import za.co.wethinkcode.robots.world.World;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story: Move Forward
 * As a player
 * I want to command my robot to move forward a specified number of steps
 * so that I can explore the world and not be a sitting duck in a battle.
 */
public class MoveRobotForwardTests {
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
    private MultiServerEngine server;
    private int configuredWidth;
    private int configuredHeight;


    @BeforeEach
    void connectToServer() throws IOException {
        Config.loadConfig("config.properties");
        configuredWidth = Config.WIDTH;
        configuredHeight = Config.HEIGHT;
        World world = new World(false);
        Config.WIDTH = 1;
        Config.HEIGHT = 1;
        server = new MultiServerEngine(world);
        server.start(0);
        serverClient.connect(DEFAULT_IP, server.getPort());
    }

    @AfterEach
    void disconnectFromServer() throws IOException {
        serverClient.disconnect();
        if (server != null) {
            server.shutdown();
        }
        Config.WIDTH = configuredWidth;
        Config.HEIGHT = configuredHeight;
    }

    // Scenario: Moving at the edge of the world
    @Test
    void movingAtEdgeOfWorldShouldReturnEdgeMessage() {
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // And the world is of size 1x1 with no obstacles or pits
        // (Already configured in @BeforeEach)

        // And a robot called "HAL" is already connected and launched
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertEquals("OK", launchResponse.get("result").asText());

        // When I send a command for "HAL" to move forward by 5 steps
        String moveRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"forward\"," +
                "  \"arguments\": [\"5\"]" +
                "}";
        JsonNode moveResponse = serverClient.sendRequest(moveRequest);

        // Then I should get an "OK" response with the message "At the NORTH edge"
        assertEquals("OK", moveResponse.get("result").asText());
        assertEquals("At the NORTH edge", moveResponse.get("data").get("message").asText());

        // And the position information returned should be at co-ordinates [0,0]
        assertEquals(0, moveResponse.get("state").get("position").get(0).asInt());
        assertEquals(0, moveResponse.get("state").get("position").get(1).asInt());
    }

}
