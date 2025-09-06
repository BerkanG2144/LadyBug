package commands;

import engine.TreeExecution;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import main.GameState;
import model.Ladybug;

import java.util.Optional;

/**
 * Command to reset the execution state of a ladybug's behavior tree.
 *
 * This command resets the tree execution for a specific ladybug back to the root node,
 * clearing any cached state and allowing the tree to start fresh from the beginning.
 *
 * Usage: reset tree &lt;ladybug&gt;
 *
 * @author ujnaa
 */
public class ResetTreeCommand extends AbstractCommand {

    /**
     * Creates a new ResetTreeCommand.
     *
     * @param state the game state containing board and tree information
     */
    public ResetTreeCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {

        validateArguments(args);
        requireLadybugs();

        int ladybugId = parseLadybugId(args[1]);
        Ladybug ladybug = findLadybug(ladybugId);
        TreeExecution execution = getTreeExecution(ladybugId);

        execution.reset(ladybug);
    }

    /**
     * Validates the command arguments.
     *
     * @param args the command arguments
     * @throws CommandArgumentException if arguments are invalid
     */
    private void validateArguments(String[] args) throws CommandArgumentException {
        if (args.length != 2 || !"tree".equals(args[0])) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Usage: reset tree <ladybug>");
        }
    }

    /**
     * Parses the ladybug ID from a string argument.
     *
     * @param arg the string containing the ladybug ID
     * @return the parsed ladybug ID
     * @throws CommandArgumentException if the ID is not a valid integer
     */
    private int parseLadybugId(String arg) throws CommandArgumentException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(),
                    "Error: invalid ladybug ID '" + arg + "'");
        }
    }

    /**
     * Finds a ladybug by its ID.
     *
     * @param ladybugId the ID of the ladybug to find
     * @return the ladybug
     * @throws LadybugNotFoundException if the ladybug is not found
     */
    private Ladybug findLadybug(int ladybugId) throws LadybugNotFoundException {
        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }
        return ladybug.get();
    }

    /**
     * Gets the tree execution for a specific ladybug.
     *
     * @param ladybugId the ID of the ladybug
     * @return the tree execution
     * @throws CommandArgumentException if no tree is loaded for the ladybug
     */
    private TreeExecution getTreeExecution(int ladybugId) throws CommandArgumentException {
        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new CommandArgumentException(getCommandName(),
                    "Error: no tree loaded for ladybug " + ladybugId);
        }
        return execution;
    }

    @Override
    public String getCommandName() {
        return "reset";
    }

    @Override
    public String getUsage() {
        return "reset tree <ladybug>";
    }
}