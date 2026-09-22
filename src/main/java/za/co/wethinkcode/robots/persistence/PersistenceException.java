package za.co.wethinkcode.robots.persistence;

/**
 * Raised when world persistence fails.
 * Domain and command code should depend on this type, not on JDBC exceptions.
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
