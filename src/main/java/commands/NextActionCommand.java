package commands;

import engine.TreeExecution;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import main.GameState;
import model.Ladybug;

import java.util.List;
import java.util.Optional;

/**
 * Command to execute the next action for all ladybugs on the board.
 *
 * This command processes each ladybug in order by ID, executes their next action
 * according to their behavior tree, and displays the updated board state after
 * each action execution.
 *
 * Usage: next action
 *
 * @author ujnaa
 */
public class NextActionCommand extends AbstractCommand {

    /**
     * Creates a new NextActionCommand.
     *
     * @param state the game state containing board and tree information
     */
    public NextActionCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {

        validateArguments(args);
        requireLadybugs();

        List<Integer> ladybugIds = getBoard().listLadybugsIds();

        for (int ladybugId : ladybugIds) {
            processLadybugAction(ladybugId);
        }
    }

    /**
     * Validates the command arguments.
     *
     * @param args the command arguments
     * @throws CommandArgumentException if arguments are invalid
     */
    private void validateArguments(String[] args) throws CommandArgumentException {
        if (args.length != 1 || !"action".equals(args[0])) {
            throw new CommandArgumentException(getCommandName(), args, "Usage: next action");
        }
    }

    /**
     * Processes the next action for a specific ladybug.
     *
     * @param ladybugId the ID of the ladybug to process
     */
    private void processLadybugAction(int ladybugId) {
        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            // Skip this ladybug if not found (shouldn't happen after requireLadybugs())
            return;
        }

        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            System.out.println("Error: no tree loaded for ladybug " + ladybugId);
            return;
        }


        // Display the updated board state
        getBoard().print();
    }

    @Override
    public String getCommandName() {
        return "next";
    }

    @Override
    public String getUsage() {
        return "next action";
    }
}