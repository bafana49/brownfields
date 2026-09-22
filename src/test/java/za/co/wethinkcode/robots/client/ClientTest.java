package za.co.wethinkcode.robots.client;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import java.util.logging.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientTest.java
 * This class contains unit tests for the ClientTestHelper class.
 * It tests the getJsonObject method to ensure it creates the correct JSON object
 * for different robot commands.
 */
class ClientTest {
    private static final Logger logger = Logger.getLogger(ClientTest.class.getName());

    /**
     * Sets up the logger configuration before running the tests.
     */
    @Test
    @DisplayName("getJsonObject should return a forward JSON request")
    void testGetJsonObjectForwardCommand() {
        String command = "forward 10";
        String robotName = "TestBot";

        JsonObject request = ClientTestHelper.getJsonObject(command, robotName);

        assertEquals(robotName, request.get("robot").getAsString());
        assertEquals("forward", request.get("command").getAsString());
        assertEquals(10, request.getAsJsonArray("arguments").get(0).getAsInt());

        logger.info("Created forward JSON request successfully.");
    }

    /**
     * Tests the getJsonObject method for a "turn" command.
     */
    @Test
    void testGetJsonObjectTurnCommand() {
        String command = "turn right";
        String robotName = "TestBot";

        JsonObject request = ClientTestHelper.getJsonObject(command, robotName);

        assertEquals("turn", request.get("command").getAsString());
        assertEquals("right", request.getAsJsonArray("arguments").get(0).getAsString());

        logger.info("Created turn JSON request successfully.");
    }

    @Test
    void testGetJsonObjectBackCommand() {
        String command = "back 5";
        String robotName = "TestBot";
        JsonObject request = ClientTestHelper.getJsonObject(command, robotName);
        assertEquals("back", request.get("command").getAsString());
        assertEquals(5, request.getAsJsonArray("arguments").get(0).getAsInt());
    }

    @Test
    void testGetJsonObjectFireCommand() {
        String command = "fire";
        String robotName = "TestBot";
        JsonObject request = ClientTestHelper.getJsonObject(command, robotName);
        assertEquals("fire", request.get("command").getAsString());
        assertEquals(0, request.getAsJsonArray("arguments").size());
    }

    @Test
    void testGetJsonObjectInvalidCommand() {
        String command = "invalidcommand";
        String robotName = "TestBot";
        JsonObject request = ClientTestHelper.getJsonObject(command, robotName);
        assertEquals("invalidcommand", request.get("command").getAsString());
        assertEquals(0, request.getAsJsonArray("arguments").size());
    }

    @Test
    void testFormatServerResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        JsonObject data = new JsonObject();
        data.addProperty("message", "Test message");
        response.add("data", data);
        String formatted = Client.formatServerResponse(response);
        assertTrue(formatted.contains("Result: OK"));
        assertTrue(formatted.contains("Message: Test message"));
    }

    @Test
    void testFormatState() {
        JsonObject state = new JsonObject();
        state.addProperty("make", "sniper");
        state.addProperty("direction", "NORTH");
        state.addProperty("shields", "5");
        state.addProperty("shots", "3");
        state.addProperty("status", "NORMAL");
        state.add("position", new com.google.gson.JsonArray());
        String formatted = Client.formatState(state);
        assertTrue(formatted.contains("Make: sniper"));
        assertTrue(formatted.contains("Direction: NORTH"));
        assertTrue(formatted.contains("Shields: 5"));
        assertTrue(formatted.contains("Shots: 3"));
        assertTrue(formatted.contains("Status: NORMAL"));
    }

    @Test
    void testBuildJsonCommandForward() throws Exception {
        String command = "forward 10";
        String robotName = "TestBot";
        java.lang.reflect.Method method = Client.class.getDeclaredMethod("buildJsonCommand", String.class, String.class);
        method.setAccessible(true);
        JsonObject request = (JsonObject) method.invoke(null, command, robotName);
        assertEquals(robotName, request.get("robot").getAsString());
        assertEquals("forward", request.get("command").getAsString());
        assertEquals(10, request.getAsJsonArray("arguments").get(0).getAsInt());
    }

    @Test
    void testBuildJsonCommandBack() throws Exception {
        String command = "back 5";
        String robotName = "TestBot";
        java.lang.reflect.Method method = Client.class.getDeclaredMethod("buildJsonCommand", String.class, String.class);
        method.setAccessible(true);
        JsonObject request = (JsonObject) method.invoke(null, command, robotName);
        assertEquals("back", request.get("command").getAsString());
        assertEquals(5, request.getAsJsonArray("arguments").get(0).getAsInt());
    }

    @Test
    void testBuildJsonCommandTurn() throws Exception {
        String command = "turn left";
        String robotName = "TestBot";
        java.lang.reflect.Method method = Client.class.getDeclaredMethod("buildJsonCommand", String.class, String.class);
        method.setAccessible(true);
        JsonObject request = (JsonObject) method.invoke(null, command, robotName);
        assertEquals("turn", request.get("command").getAsString());
        assertEquals("left", request.getAsJsonArray("arguments").get(0).getAsString());
    }

    @Test
    void testBuildJsonCommandFire() throws Exception {
        String command = "fire";
        String robotName = "TestBot";
        java.lang.reflect.Method method = Client.class.getDeclaredMethod("buildJsonCommand", String.class, String.class);
        method.setAccessible(true);
        JsonObject request = (JsonObject) method.invoke(null, command, robotName);
        assertEquals("fire", request.get("command").getAsString());
        assertEquals(robotName, request.get("robot").getAsString());
        assertEquals(0, request.getAsJsonArray("arguments").size());
    }

    @Test
    void testBuildJsonCommandInvalid() throws Exception {
        String command = "foobar";
        String robotName = "TestBot";
        java.lang.reflect.Method method = Client.class.getDeclaredMethod("buildJsonCommand", String.class, String.class);
        method.setAccessible(true);
        JsonObject request = (JsonObject) method.invoke(null, command, robotName);
        assertEquals("foobar", request.get("command").getAsString());
        assertEquals(robotName, request.get("robot").getAsString());
        assertEquals(0, request.getAsJsonArray("arguments").size());
    }
}
