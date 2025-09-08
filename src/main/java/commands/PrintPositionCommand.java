package commands;

import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import main.GameState;
/**
 * Command to print the current position of a specific ladybug on the board.
 *
 * This command displays the coordinates of a ladybug in the format (x, y).
 * The coordinates are 1-based, with (1,1) being the top-left corner.
 *
 * Usage: print position &lt;ladybug&gt;
 *
 * @author ujnaa
 */
public class PrintPositionCommand extends AbstractCommand {

    /**
     * Creates a new PrintPositionCommand.
     *
     * @param state the game state containing board and ladybug information
     */
    public PrintPositionCommand(GameState state) {
        super(state);
    }


    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {

        requireLadybugs();

        if (args == null || args.length == 0) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, print position <ladybug>");
        }

        String idToken;
        if ("position".equals(args[0])) {
            if (args.length < 2) {
                throw new CommandArgumentException(getCommandName(), args,
                        "Error, print position <ladybug>");
            }
            idToken = args[1];
        } else {
            idToken = args[0];
        }

        // ID parsen
        final int ladybugId;
        try {
            ladybugId = Integer.parseInt(idToken);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, invalid ladybug ID");
        }

        // Ladybug holen (aktive Ladybugs!)
        var ladybugOpt = getState().getBoard().getLadybugManager().getLadybugById(ladybugId);
        if (ladybugOpt.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }

        var p = ladybugOpt.get().getPosition();
        System.out.println("(" + p.x() + ", " + p.y() + ")");
    }

    @Override
    public String getCommandName() {
        return "print";
    }

    @Override
    public String getUsage() {
        return "print position";
    }
}
