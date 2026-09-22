package za.co.wethinkcode.robots.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import za.co.wethinkcode.robots.command.LaunchCommand;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldGate;
import za.co.wethinkcode.robots.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP request/response boundary for the RobotWorld Web API.
 * Holds Domain ({@link World}) and persistence ({@link WorldRepository});
 * does not own Javalin lifecycle. Responses are maps so Javalin's Jackson
 * mapper can serialise them (Gson {@link JsonObject} cannot).
 */
public class WebApiHandler {

    private static final Gson GSON = new Gson();

    private final World world;
    private final WorldRepository worldRepository;
    private final WorldGate worldGate;

    /**
     * @param world           the live Domain world shared with other adapters
     * @param worldRepository persistence abstraction for named-world lookup
     */
    public WebApiHandler(World world, WorldRepository worldRepository) {
        this(world, worldRepository, WorldGate.immediate());
    }

    public WebApiHandler(World world, WorldRepository worldRepository, WorldGate worldGate) {
        this.world = Objects.requireNonNull(world, "world");
        this.worldRepository = Objects.requireNonNull(worldRepository, "worldRepository");
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
    }

    /**
     * GET /world — current in-memory world (size and map objects, not robots).
     */
    public void getWorld(Context context) {
        Map<String, Object> body = worldGate.run(() -> toJson(WorldSnapshot.from(world)));
        context.status(200);
        context.json(body);
    }

    /**
     * GET /world/{world} — look up the named snapshot, apply it with
     * {@link World#restore(WorldSnapshot)}, then return that map (not robots).
     * Unknown names yield 404 and do not change the live world.
     */
    public void getNamedWorld(Context context) {
        String name = context.pathParam("world");
        Optional<WorldSnapshot> found = worldRepository.find(name);
        if (found.isEmpty()) {
            context.status(404);
            return;
        }

        WorldSnapshot snapshot = found.get();
        Map<String, Object> body = worldGate.run(() -> {
            world.restore(snapshot);
            return toJson(snapshot);
        });
        context.status(200);
        context.json(body);
    }

    /**
     * POST /robot/{name} — launch a robot into the current world via {@link LaunchCommand}.
     */
    public void launchRobot(Context context) {
        String name = context.pathParam("name");
        JsonArray arguments = new JsonArray();
        arguments.add(robotMake(context));

        JsonObject result = worldGate.run(
                () -> LaunchCommand.getInstance(name, arguments).execute(world));
        boolean ok = "OK".equals(result.get("result").getAsString());
        context.status(ok ? 200 : 400);
        context.json(jsonMap(result));
    }

    private String robotMake(Context context) {
        JsonObject body = JsonParser.parseString(context.body()).getAsJsonObject();
        return body.get("make").getAsString();
    }

    private Map<String, Object> toJson(WorldSnapshot snapshot) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("width", snapshot.getWidth());
        body.put("height", snapshot.getHeight());

        List<Map<String, Object>> obstacles = new ArrayList<>();
        for (Obstacle obstacle : snapshot.getObstacles()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", obstacle.getType().name());
            item.put("topLeftX", obstacle.getTopLeft().getX());
            item.put("topLeftY", obstacle.getTopLeft().getY());
            item.put("bottomRightX", obstacle.getBottomRight().getX());
            item.put("bottomRightY", obstacle.getBottomRight().getY());
            obstacles.add(item);
        }
        body.put("obstacles", obstacles);
        return body;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonMap(JsonObject json) {
        return GSON.fromJson(json, Map.class);
    }
}
