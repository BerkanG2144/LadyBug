package commands;

import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.TreeParsingException;
import main.GameState;
import model.Board;
import model.Ladybug;
import model.Position;

import java.util.Optional;

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
public class PrintPositionCommand implements Command {
    private final GameState state;

    /**
     * Creates a new PrintPositionCommand.
     *
     * @param state the game state containing board and ladybug information
     */
    public PrintPositionCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) throws BoardException,
            CommandArgumentException, TreeParsingException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Error, print position <ladybug>");
        }

        Board board = state.getBoard();
        if (board == null) {
            System.out.println("Error, no board loaded");
            return;
        }

        int ladybugId = Integer.parseInt(args[1]);
        Optional<Ladybug> ladybug = board.getLadybugById(ladybugId);

        if (ladybug.isEmpty()) {
            System.out.println("Error, ladybug not found");
            return;
        }

        Position pos = ladybug.get().getPosition();
        System.out.println("(" + pos.x() + ", " + pos.y() + ")");
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
