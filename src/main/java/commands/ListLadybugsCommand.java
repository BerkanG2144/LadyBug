package commands;
import main.GameState;

import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to list all ladybugs on the board by their IDs.
 *
 * This command displays the unique identification numbers of all ladybugs
 * currently present on the board, sorted in ascending order and separated by spaces.
 *
 * Usage: list ladybugs
 *
 * @author ujnaa
 */
public class ListLadybugsCommand extends AbstractCommand {

    /**
     * Creates a new ListLadybugsCommand.
     *
     * @param state the game state containing board and ladybug information
     */
    public ListLadybugsCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {

        // This command doesn't expect any arguments
        if (args.length != 0) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Usage: list ladybugs (no arguments expected)");
        }

        requireLadybugs(); // Validation aus AbstractCommand

        List<Integer> ids = getBoard().listLadybugsIds();
        String result = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
        System.out.println(result);
    }


    @Override
    public String getCommandName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "list ladybugs";
    }
}
