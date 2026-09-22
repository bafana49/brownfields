package za.co.wethinkcode.robots.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WebApiHandler}: current world JSON, named restore, and launch.
 */
class WebApiHandlerTest {

    private static final Gson GSON = new Gson();

    private World mockWorld;
    private WorldRepository mockRepository;
    private Context mockContext;
    private WebApiHandler handler;
    private WorldSnapshot currentWorldSnapshot;

    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRepository = mock(WorldRepository.class);
        mockContext = mock(Context.class);
        handler = new WebApiHandler(mockWorld, mockRepository);

        Obstacle mountain = new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN);
        currentWorldSnapshot = new WorldSnapshot(5, 10, List.of(mountain));
        when(mockWorld.getTOP_LEFT()).thenReturn(new Position(0, 0));
        when(mockWorld.getBOTTOM_RIGHT()).thenReturn(new Position(4, 9));
        when(mockWorld.getObstacles()).thenReturn(currentWorldSnapshot.getObstacles());

        when(mockContext.status(anyInt())).thenReturn(mockContext);
        when(mockContext.json(any())).thenReturn(mockContext);
    }

    @AfterEach
    void tearDown() {
        Config.loadConfig("config.properties");
    }

    @Test
    @DisplayName("Constructor should throw NullPointerException for null world")
    void constructor_nullWorld_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new WebApiHandler(null, mockRepository));
    }

    @Test
    @DisplayName("Constructor should throw NullPointerException for null repository")
    void constructor_nullRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new WebApiHandler(mockWorld, null));
    }

    @Test
    @DisplayName("GET /world returns the in-memory world size and map objects")
    void getWorld_returnsCurrentWorldAsJson() {
        handler.getWorld(mockContext);

        verify(mockContext).status(200);
        JsonObject body = capturedJson();
        assertEquals(5, body.get("width").getAsInt());
        assertEquals(10, body.get("height").getAsInt());
        assertEquals(1, body.getAsJsonArray("obstacles").size());
        verify(mockRepository, never()).find();
        verify(mockRepository, never()).find(anyString());
        verify(mockWorld, never()).restore(any());
    }

    @Test
    @DisplayName("GET /world does not include robot positions or status")
    void getWorld_excludesRobots() {
        when(mockWorld.getBots()).thenReturn(List.of(new Robot("HAL", "sniper")));
        when(mockWorld.getRobots()).thenReturn(List.of(new Robot("HAL", "sniper")));

        handler.getWorld(mockContext);

        JsonObject body = capturedJson();
        assertFalse(body.has("robots"));
        assertFalse(body.has("bots"));
    }

    @Test
    @DisplayName("GET /world/{world} restores the named snapshot into the live world")
    void getNamedWorld_restoresPersistedWorld() {
        WorldSnapshot mars = namedMarsSnapshot();
        when(mockContext.pathParam("world")).thenReturn("mars");
        when(mockRepository.find("mars")).thenReturn(Optional.of(mars));

        handler.getNamedWorld(mockContext);

        verify(mockRepository).find("mars");
        verify(mockRepository, never()).find();
        verify(mockWorld).restore(mars);
        verify(mockContext).status(200);
        JsonObject body = capturedJson();
        assertEquals(20, body.get("width").getAsInt());
        assertEquals(15, body.get("height").getAsInt());
        assertEquals(2, body.getAsJsonArray("obstacles").size());
    }

    @Test
    @DisplayName("GET /world/{world} does not restore robot positions or status")
    void getNamedWorld_doesNotRestoreRobots() {
        when(mockContext.pathParam("world")).thenReturn("mars");
        when(mockRepository.find("mars")).thenReturn(Optional.of(namedMarsSnapshot()));

        handler.getNamedWorld(mockContext);

        verify(mockWorld, never()).addRobot(any());
        JsonObject body = capturedJson();
        assertFalse(body.has("robots"));
        assertFalse(body.has("bots"));
    }

    @Test
    @DisplayName("GET /world/{world} returns 404 when the name is unknown")
    void getNamedWorld_missingName_returns404() {
        when(mockContext.pathParam("world")).thenReturn("pluto");
        when(mockRepository.find("pluto")).thenReturn(Optional.empty());

        handler.getNamedWorld(mockContext);

        verify(mockWorld, never()).restore(any());
        verify(mockContext).status(404);
    }

    @Test
    @DisplayName("POST /robot/{name} launches the named robot into the current world")
    void launchRobot_addsRobotToWorld() {
        World world = emptyPlayableWorld();
        WebApiHandler liveHandler = new WebApiHandler(world, mockRepository);
        when(mockContext.pathParam("name")).thenReturn("HAL");
        when(mockContext.body()).thenReturn("{\"make\":\"sniper\"}");

        liveHandler.launchRobot(mockContext);

        assertEquals(1, world.getBots().size());
        assertEquals("HAL", world.getBots().get(0).getName());
        verify(mockContext).status(200);
        JsonObject body = capturedJson();
        assertEquals("OK", body.get("result").getAsString());
        verify(mockRepository, never()).find(anyString());
        verify(mockRepository, never()).find();
    }

    @Test
    @DisplayName("POST /robot/{name} returns an error when the name is already in use")
    void launchRobot_duplicateName_returnsError() {
        World world = emptyPlayableWorld();
        world.addRobot(new Robot("HAL", "sniper"));
        WebApiHandler liveHandler = new WebApiHandler(world, mockRepository);
        when(mockContext.pathParam("name")).thenReturn("HAL");
        when(mockContext.body()).thenReturn("{\"make\":\"tank\"}");

        liveHandler.launchRobot(mockContext);

        assertEquals(1, world.getBots().size());
        verify(mockContext).status(400);
        JsonObject body = capturedJson();
        assertEquals("ERROR", body.get("result").getAsString());
        assertEquals("Robot name already exists", body.getAsJsonObject("data").get("message").getAsString());
    }

    @Test
    @DisplayName("POST /robot/{name} returns an error when the world has no launch space")
    void launchRobot_noSpace_returnsError() {
        Obstacle covering = new Obstacle(0, 0, 0, 0, ObstacleType.MOUNTAIN);
        World world = new World(new WorldSnapshot(1, 1, List.of(covering)), false);
        WebApiHandler liveHandler = new WebApiHandler(world, mockRepository);
        when(mockContext.pathParam("name")).thenReturn("HAL");
        when(mockContext.body()).thenReturn("{\"make\":\"sniper\"}");

        liveHandler.launchRobot(mockContext);

        assertTrue(world.getBots().isEmpty());
        verify(mockContext).status(400);
        JsonObject body = capturedJson();
        assertEquals("ERROR", body.get("result").getAsString());
        assertEquals("No more space", body.getAsJsonObject("data").get("message").getAsString());
    }

    private World emptyPlayableWorld() {
        return new World(new WorldSnapshot(5, 5, List.of()), false);
    }

    private WorldSnapshot namedMarsSnapshot() {
        return new WorldSnapshot(20, 15, List.of(
                new Obstacle(1, 1, 2, 2, ObstacleType.MOUNTAIN),
                new Obstacle(3, 3, 4, 4, ObstacleType.LAKE)
        ));
    }

    private JsonObject capturedJson() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockContext).json(captor.capture());
        Object body = captor.getValue();
        if (body instanceof JsonObject jsonObject) {
            return jsonObject;
        }
        if (body instanceof WorldSnapshot snapshot) {
            JsonObject json = new JsonObject();
            json.addProperty("width", snapshot.getWidth());
            json.addProperty("height", snapshot.getHeight());
            json.add("obstacles", GSON.toJsonTree(snapshot.getObstacles()));
            return json;
        }
        return GSON.toJsonTree(body).getAsJsonObject();
    }
}
