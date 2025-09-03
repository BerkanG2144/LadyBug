package commands;

import main.GameState;
import model.Board;
import model.BoardGrid;
import model.LadybugManager;
import model.PathFinder;

/**
 * Base class for all commands with common validation and access methods.
 * @author u-Kürzel
 */
public abstract class AbstractCommand implements Command {
    protected final GameState gameState;

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

    // Convenience accessors
    protected Board getBoard() {
        return gameState.getBoard();
    }

    protected BoardGrid getGrid() {
        return gameState.getGrid();
    }

    protected LadybugManager getLadybugManager() {
        return gameState.getLadybugManager();
    }

    protected PathFinder getPathFinder() {
        return gameState.getPathFinder();
    }

    /**
     * Safe execute method that handles common errors.
     * @param args command arguments
     */
    @Override
    public final void execute(String[] args) {
        try {
            executeInternal(args);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Internal execute method to be implemented by subclasses.
     * @param args command arguments
     * @throws Exception if execution fails
     */
    protected abstract void executeInternal(String[] args) throws Exception;
}