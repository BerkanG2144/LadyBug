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

        String idToken = null;
        if (args == null || args.length == 0) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, print position <ladybug>");
        }
        if ("position".equals(args[0])) {
            if (args.length < 2) {
                throw new CommandArgumentException(getCommandName(), args,
                        "Error, print position <ladybug>");
            }
            idToken = args[1];
        } else {
            idToken = args[0];
        }

        // 2) ID parsen
        final int ladybugId;
        try {
            ladybugId = Integer.parseInt(idToken);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, invalid ladybug ID");
        }

        // 3) Ladybug holen
        var ladybugOpt = getBoard().getLadybugById(ladybugId);
        if (ladybugOpt.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }

        // 4) 1-based coordinates - Position already stores 1-based coordinates
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
