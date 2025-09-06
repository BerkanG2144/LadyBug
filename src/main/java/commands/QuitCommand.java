package commands;

import main.GameState;

/**
 * Command to quit the game.
 *
 * @author ujnaa
 */
public class QuitCommand implements Command {

    /**
     * Creates a new PrintPositionCommand.
     *
     * @param gameState the game state containing board and ladybug information
     */
    public QuitCommand(GameState gameState) {
        // GameState not needed for quit, but keeping interface consistent
    }

    @Override
    public void execute(String[] args) throws Exception {
        // No arguments expected for quit
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: quit");
        }

        // Exit the program gracefully
        // Note: We don't use System.exit() as per requirements
        throw new QuitException();
    }

    @Override
    public String getCommandName() {
        return "quit";
    }

    @Override
    public String getUsage() {
        return "quit";
    }

    /**
     * Special exception to signal that the program should quit.
     */
    public static class QuitException extends Exception {
        /**
         * Constructs a new QuitException with a default message.
         */
        public QuitException() {
            super("Program terminated by user");
        }
    }
}