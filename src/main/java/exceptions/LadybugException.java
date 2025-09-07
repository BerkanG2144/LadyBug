package exceptions;

/**
 * Thrown when a ladybug-related error occurs (invalid ID, null position/direction, etc.).
 *
 * @author ujna
 */
public class LadybugException extends BehaviorTreeException {

    /**
     * Constructs a new LadybugException with the given message.
     *
     * @param message the detail message
     */
    public LadybugException(String message) {
        super(message);
    }
}
