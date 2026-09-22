package za.co.wethinkcode.robots.web;

import io.javalin.Javalin;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldGate;

import java.util.Objects;

/**
 * Javalin host for the RobotWorld Web API.
 * Creates the HTTP server, registers routes, and starts/stops the listener.
 * Request processing is delegated to {@link WebApiHandler}; this class has no domain logic.
 * Bind a different port from the socket server. {@link za.co.wethinkcode.robots.server.MultiServers}
 * starts this on {@link za.co.wethinkcode.robots.config.Config#HTTP_PORT}.
 */
public class WebApiServer {

    private final Javalin server;
    private final WebApiHandler handler;

    public WebApiServer(World world, WorldRepository worldRepository) {
        this(new WebApiHandler(world, worldRepository));
    }

    public WebApiServer(World world, WorldRepository worldRepository, WorldGate worldGate) {
        this(new WebApiHandler(world, worldRepository, worldGate));
    }

    public WebApiServer(WebApiHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        this.server = Javalin.create(config -> config.showJavalinBanner = false);
        registerRoutes();
    }

    private void registerRoutes() {
        server.get("/world", handler::getWorld);
        server.get("/world/{world}", handler::getNamedWorld);
        server.post("/robot/{name}", handler::launchRobot);
    }

    public void start(int port) {
        server.start(port);
    }

    public void stop() {
        server.stop();
    }

    /**
     * Bound HTTP port after {@link #start(int)}.
     */
    public int getPort() {
        return server.port();
    }

    public WebApiHandler getHandler() {
        return handler;
    }
}
