package commands;

import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.TreeParsingException;
import exceptions.QuitException;
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
    public void execute(String[] args)
            throws BoardException, CommandArgumentException, TreeParsingException, QuitException {
        if (args.length != 0) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, " + getUsage());
        }
        // signal: quit program
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

}