package za.co.wethinkcode.robots.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.world.World;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MultiServerTest.java
 * This class contains unit tests for the Server class.
 * It tests the server's ability to handle client requests and respond correctly.
 */
class MultiServerTest {
    private MultiServerEngine server;
    private int serverPort;

    /**
     * This method sets up the server before each test.
     * It starts the server in a separate thread to allow for concurrent testing.
     */
    @BeforeEach
    void setupServer() throws IOException {
        Config.loadConfig("config.properties");
        server = new MultiServerEngine(new World(false));
        server.start(0);
        serverPort = server.getPort();
    }

    /**
     * This method tears down the server after each test.
     * It properly shuts down the server and interrupts the server thread.
     */
    @AfterEach
    void tearDownServer() throws IOException {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * This test method checks if the server is set up correctly and can respond to a client request.
     * It sends a JSON request to the server and verifies the response.
     */
    @Test
    @DisplayName("Test server setup and response")
    void testServerSetupAndResponse() throws IOException {
        try (Socket clientSocket = new Socket("localhost", serverPort);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            JsonObject launchRequest = new JsonObject();
            launchRequest.addProperty("robot", "TestBot10");
            launchRequest.addProperty("command", "launch");
            JsonArray args = new JsonArray();
            args.add("sniper");
            launchRequest.add("arguments", args);

            out.println(launchRequest);

            String response = in.readLine();
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            assertEquals("OK", jsonResponse.get("result").getAsString());
        }
    }

    @Test
    @DisplayName("World config flags are not treated as Docker background mode")
    void worldConfigArgsAreNotBackgroundMode() {
        assertFalse(MultiServers.isBackgroundMode(new String[]{"-p", "5000", "-s", "100", "-o", "10,5"}));
        assertTrue(MultiServers.isBackgroundMode(new String[]{"background"}));
        assertTrue(MultiServers.isBackgroundMode(new String[]{"-p", "5050", "--headless"}));
        assertFalse(MultiServers.isBackgroundMode(new String[]{}));
    }

    @Test
    @DisplayName("Web API is not started when HTTP_PORT matches the socket port")
    void startWebApi_skipsWhenPortCollidesWithSocket() {
        int originalHttp = Config.HTTP_PORT;
        Config.HTTP_PORT = Config.PORT;
        try {
            MultiServers.startWebApi(new World(false), null);
        } finally {
            Config.HTTP_PORT = originalHttp;
        }
    }
}
