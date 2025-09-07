package commands;

import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import main.GameState;

import java.util.ArrayList;
import java.util.List;

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


        requireLadybugs(); // Validation aus AbstractCommand

        List<Integer> ids = getBoard().listLadybugsIds();
        List<String> stringIds = new ArrayList<>();
        for (Integer id : ids) {
            stringIds.add(String.valueOf(id));
        }
        String result = String.join(" ", stringIds);
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
