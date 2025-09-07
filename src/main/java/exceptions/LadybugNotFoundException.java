package exceptions;

/**
 * Exception thrown when a ladybug with a specific ID cannot be found,
 * or when no ladybugs exist at all.
 * @author ujnaa
 */
public class LadybugNotFoundException extends BehaviorTreeException {

    private final int ladybugId;

    /**
     * Constructs a new ladybug not found exception.
     * @param ladybugId the ID of the ladybug that was not found, or -1 if no ladybugs exist
     */
    public LadybugNotFoundException(int ladybugId) {
        super(ladybugId == -1 ? "Error, no ladybugs found"
                : String.format("Error, ladybug not found", ladybugId));
        this.ladybugId = ladybugId;
    }

    /**
     * Constructs a new ladybug not found exception with custom message.
     * @param ladybugId the ID of the ladybug that was not found
     * @param message custom error message
     */
    public LadybugNotFoundException(int ladybugId, String message) {
        super(message);
        this.ladybugId = ladybugId;
    }

    /**
     * Returns the ID of the ladybug that was not found.
     * @return the ladybug ID, or -1 if no ladybugs exist at all
     */
    public int getLadybugId() {
        return ladybugId;
    }
}