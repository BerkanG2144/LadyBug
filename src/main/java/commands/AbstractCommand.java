package commands;

import main.GameState;
import model.Board;

/**
 * Base class for CLI commands in the Ladybug application.
 * Provides shared utilities (e.g., access to game state, validation helpers)
 * and a template method pattern via {@code execute} delegating to {@code executeInternal}.
 *
 * Implementations should override {@code executeInternal} to perform the actual work.
 *
 * @author ujnaa
 */
public abstract class AbstractCommand implements Command {
    protected final GameState gameState;


    /**
     * Creates a new command bound to the given {@link GameState}.
     *
     * @param gameState the shared game state, must not be {@code null}
     * @throws IllegalArgumentException if {@code gameState} is {@code null}
     */
    protected AbstractCommand(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Validates that a board is loaded.
     * @throws IllegalStateException if no board is loaded
     */
    protected void requireBoard() {
        if (gameState.getBoard() == null) {
            throw new IllegalStateException("Error: no board loaded");
        }
    }

    /**
     * Validates that ladybugs exist on the board.
     * @throws IllegalStateException if no ladybugs found
     */
    protected void requireLadybugs() {
        requireBoard();
        if (getBoard().listLadybugsIds().isEmpty()) {
            throw new IllegalStateException("Error: no ladybugs found");
        }
    }

    /**
     * Returns the currently loaded {@link Board}.
     *
     * @return the board, never {@code null} after calling {@link #requireBoard()}
     */
    protected Board getBoard() {
        return gameState.getBoard();
    }

    /**
     * Executes the specific command logic.
     * Subclasses should throw {@link IllegalArgumentException} or {@link IllegalStateException}
     * in case of invalid input or missing state.
     * @param args command arguments
     * @throws IllegalArgumentException for invalid arguments
     * @throws IllegalStateException for invalid state
     */
    protected abstract void executeInternal(String[] args)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Safe execute method that handles common errors.
     * @param args command arguments
     */
    @Override
    public final void execute(String[] args) {
        try {
            executeInternal(args);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}