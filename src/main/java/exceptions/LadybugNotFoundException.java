package exceptions;

/**
 * Exception thrown when a ladybug with a specific ID cannot be found.
 * @author u-Kürzel
 */
public class LadybugNotFoundException extends BehaviorTreeException {

    private final int ladybugId;

    /**
     * Constructs a new ladybug not found exception.
     * @param ladybugId the ID of the ladybug that was not found
     */
    public LadybugNotFoundException(int ladybugId) {
        super(String.format("Ladybug with ID %d not found", ladybugId));
        this.ladybugId = ladybugId;
    }

    /**
     * Returns the ID of the ladybug that was not found.
     * @return the ladybug ID
     */
    public int getLadybugId() {
        return ladybugId;
    }
}