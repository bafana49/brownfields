package za.co.wethinkcode.robots.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.persistence.WorldRepository;
import za.co.wethinkcode.robots.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link WebApiServer} lifecycle: start/stop, port binding, and handler injection.
 */
class WebApiServerTest {

    @Test
    @DisplayName("Server should start on the requested port and stop cleanly")
    void start_specifiedPort_bindsThenStops() {
        WebApiServer server = new WebApiServer(mock(WebApiHandler.class));
        server.start(0);

        assertTrue(server.getPort() > 0);
        assertDoesNotThrow(server::stop);
    }

    @Test
    @DisplayName("Server can be started again after stop by creating a new instance")
    void restart_afterStop_newInstanceBinds() {
        WebApiHandler handler = mock(WebApiHandler.class);
        WebApiServer original = new WebApiServer(handler);
        original.start(0);
        original.stop();

        WebApiServer replacement = new WebApiServer(handler);
        replacement.start(0);
        assertTrue(replacement.getPort() > 0);
        replacement.stop();
    }

    @Test
    @DisplayName("Server constructor should throw NullPointerException for null handler")
    void constructor_nullHandler_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new WebApiServer(null));
    }

    @Test
    @DisplayName("Server constructor with world and repository should create a handler")
    void constructor_worldAndRepository_createsHandler() {
        WebApiServer server = new WebApiServer(mock(World.class), mock(WorldRepository.class));
        assertNotNull(server.getHandler());
    }

    @Test
    @DisplayName("Server should expose the handler it was constructed with")
    void getHandler_returnsInjectedHandler() {
        WebApiHandler handler = mock(WebApiHandler.class);
        WebApiServer server = new WebApiServer(handler);
        assertEquals(handler, server.getHandler());
    }
}
