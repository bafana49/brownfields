package za.co.wethinkcode.robots.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP tests for the Web API: current world, named restore, and launch.
 */
class WebApiIntegrationTest {

    private WebApiServer server;
    private int port;
    private World world;
    private InMemoryWorldRepository repository;

    @BeforeEach
    void startServer() {
        Config.loadConfig("config.properties");
        WorldSnapshot empty = new WorldSnapshot(5, 5, List.of());
        world = new World(empty, false);
        repository = new InMemoryWorldRepository();
        repository.save("mars", new WorldSnapshot(8, 6, List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN)
        )));

        server = new WebApiServer(world, repository);
        server.start(0);
        port = server.getPort();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
        Config.loadConfig("config.properties");
    }

    @Test
    @DisplayName("GET /world returns the current in-memory world as JSON")
    void getWorld_returnsCurrentWorld() throws UnirestException {
        HttpResponse<String> response = Unirest.get(url("/world")).asString();

        assertEquals(200, response.getStatus());
        JsonObject body = parse(response);
        assertEquals(5, body.get("width").getAsInt());
        assertEquals(5, body.get("height").getAsInt());
        assertTrue(body.has("obstacles"));
        assertFalse(body.has("robots"));
        assertFalse(body.has("bots"));
    }

    @Test
    @DisplayName("GET /world/{world} restores and returns the named persisted world")
    void getNamedWorld_restoresAndReturnsNamedWorld() throws UnirestException {
        HttpResponse<String> response = Unirest.get(url("/world/mars")).asString();

        assertEquals(200, response.getStatus());
        JsonObject body = parse(response);
        assertEquals(8, body.get("width").getAsInt());
        assertEquals(6, body.get("height").getAsInt());
        assertEquals(1, body.getAsJsonArray("obstacles").size());
        assertEquals(7, world.getBOTTOM_RIGHT().getX());
        assertEquals(5, world.getBOTTOM_RIGHT().getY());
        assertTrue(world.getBots().isEmpty());
    }

    @Test
    @DisplayName("GET /world/{world} returns 404 when the world is not saved")
    void getNamedWorld_unknownName_returns404() throws UnirestException {
        HttpResponse<String> response = Unirest.get(url("/world/pluto")).asString();

        assertEquals(404, response.getStatus());
        assertEquals(4, world.getBOTTOM_RIGHT().getX());
        assertEquals(4, world.getBOTTOM_RIGHT().getY());
    }

    @Test
    @DisplayName("POST /robot/{name} launches a robot into the current world")
    void launchRobot_placesRobotInWorld() throws UnirestException {
        HttpResponse<String> response = Unirest.post(url("/robot/HAL"))
                .header("Content-Type", "application/json")
                .body("{\"make\":\"sniper\"}")
                .asString();

        assertEquals(200, response.getStatus());
        assertEquals("OK", parse(response).get("result").getAsString());
        assertEquals(1, world.getBots().size());
        assertEquals("HAL", world.getBots().get(0).getName());
    }

    @Test
    @DisplayName("POST /robot/{name} rejects a duplicate robot name")
    void launchRobot_duplicateName_returnsError() throws UnirestException {
        Unirest.post(url("/robot/HAL"))
                .header("Content-Type", "application/json")
                .body("{\"make\":\"sniper\"}")
                .asString();

        HttpResponse<String> response = Unirest.post(url("/robot/HAL"))
                .header("Content-Type", "application/json")
                .body("{\"make\":\"tank\"}")
                .asString();

        assertEquals(400, response.getStatus());
        assertEquals("ERROR", parse(response).get("result").getAsString());
        assertEquals(1, world.getBots().size());
    }

    @Test
    @DisplayName("Unknown routes still return 404")
    void unknownRoute_returns404() throws UnirestException {
        assertEquals(404, Unirest.get(url("/nonexistent")).asString().getStatus());
        assertEquals(404, Unirest.post(url("/nonexistent")).body("{}").asString().getStatus());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private JsonObject parse(HttpResponse<String> response) {
        return JsonParser.parseString(response.getBody()).getAsJsonObject();
    }

    private static final class InMemoryWorldRepository implements WorldRepository {
        private final Map<String, WorldSnapshot> named = new ConcurrentHashMap<>();
        private WorldSnapshot latest;

        @Override
        public void save(WorldSnapshot snapshot) {
            latest = snapshot;
        }

        @Override
        public void save(String name, WorldSnapshot snapshot) {
            named.put(name, snapshot);
            latest = snapshot;
        }

        @Override
        public Optional<WorldSnapshot> find() {
            return Optional.ofNullable(latest);
        }

        @Override
        public Optional<WorldSnapshot> find(String name) {
            return Optional.ofNullable(named.get(name));
        }
    }
}
