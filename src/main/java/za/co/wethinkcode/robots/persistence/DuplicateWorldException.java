package za.co.wethinkcode.robots.persistence;

/**
 * Raised when SAVE is asked to use a world name that is already stored.
 */
public class DuplicateWorldException extends PersistenceException {
    public DuplicateWorldException(String message) {
        super(message);
    }
}
