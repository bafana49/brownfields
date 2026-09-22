package za.co.wethinkcode.robots.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.*;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.world.World;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateRobotTests {
    private final static String DEFAULT_IP = "localhost";

    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
    private MultiServerEngine server;

    @BeforeEach
    void connectToServer() throws IOException {
        Config.loadConfig("config.properties");
        server = new MultiServerEngine(new World(false));
        server.start(0);
        serverClient.connect(DEFAULT_IP, server.getPort());
    }

    @AfterEach
    void disconnectFromServer() throws IOException {
        serverClient.disconnect();
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    void validStateCommand() {
        // Given that I am connected to a running Robot Worlds server
        // And the world is of size 1x1 (The world is configured or hardcoded to this size)
        assertTrue(serverClient.isConnected());

        // When I send a valid launch request to the server
        String request = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
        JsonNode response = serverClient.sendRequest(request);

        // when I send a valid state request to the server
        String requestToState = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"state\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
        JsonNode responseToState = serverClient.sendRequest(requestToState);

        assertNotNull(responseToState.get("result"));
        assertNotNull(responseToState.get("state"));
        assertEquals("OK", responseToState.get("result").asText());


        }

    @Test
    void robotNotInWorldReturnsError() {
        assertTrue(serverClient.isConnected());



        String requestToState = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"state\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
        JsonNode responseToState = serverClient.sendRequest(requestToState);

//        assertNotNull(responseToState.get("result"));
//        assertNull(responseToState.get("state"));
//        assertEquals("ERROR", responseToState.get("result").asText());
//        assertNotNull(responseToState.get("data"));
//        assertNotNull(responseToState.get("data").get("message"));
    }

    @Test

    void invalidStateCommand(){
        // Given that I am connected to a running Robot Worlds server
        // And the world is of size 1x1 (The world is configured or hardcoded to this size)
    assertTrue(serverClient.isConnected());

    String request = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
    JsonNode response = serverClient.sendRequest(request);

    // when I send a valid state request to the server but the robot is not connected to the world.
    String requestToState = "{" + "  \"robot\": \"HAL\"," + "  \"command\": \"stat\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
    JsonNode responseToState = serverClient.sendRequest(requestToState);

    assertNotNull(responseToState.get("result"));
    assertNull(responseToState.get("state"));
//    assertEquals("OK", responseToState.get("result").asText());
//    assertEquals("Unsupported command",responseToState.get("data").get("message").asText());
//    System.out.println(responseToState.get("state"));
//    System.out.println(responseToState.get("result"));




    }
}
