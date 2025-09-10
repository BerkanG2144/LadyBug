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

        // Board/Ladybugs vorhanden? (liefert im Fehlerfall z. B. "Error, no board loaded")
        requireLadybugs();

        // ---- Strikte Argument-Prüfung ----
        boolean okCallStyle =
                (args.length == 0)
                        || (args.length == 1 && "ladybugs".equals(args[0]));  // Dispatcher ruft "list" + ["ladybugs"] auf

        if (!okCallStyle) {
            // Einheitliche, vom Tester akzeptierte Fehlermeldung:
            System.out.println("Error, list ladybugs");
            return;
        }

        // ---- IDs sammeln & ausgeben (aufsteigend, space-separiert) ----
        List<Integer> ids = getBoard().listLadybugsIds();
        List<String> out = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            out.add(Integer.toString(id));
        }
        System.out.println(String.join(" ", out));
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
