package main;

/**
 * Entry point for the Ladybug application.
 *
 * <p>Initializes a {@link GameController} and starts the interactive
 * command-line interface for controlling ladybugs and behavior trees.</p>
 *
 * @author ujna
 */
public final class KaraTrees {

    /**
     * Private constructor to prevent instantiation.
     */
    private KaraTrees() {
        // not called
    }

    /**
     * Main entry point of the program.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        GameController controller = new GameController();
        controller.run();
    }
}
