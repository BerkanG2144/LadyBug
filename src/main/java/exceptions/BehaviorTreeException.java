package exceptions;

/**
 * Base exception class for all behavior tree related exceptions.
 * @author ujnaa
 */
public class BehaviorTreeException extends Exception {

    /**
     * Constructs a new behavior tree exception with the specified detail message.
     * @param message the detail message
     */
    public BehaviorTreeException(String message) {
        super(message);
    }

    /**
     * Constructs a new behavior tree exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BehaviorTreeException(String message, Throwable cause) {
        super(message, cause);
    }
}