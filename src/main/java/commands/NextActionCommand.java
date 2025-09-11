package commands;

import engine.TreeExecution;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugException;
import exceptions.LadybugNotFoundException;
import main.GameState;
import model.Ladybug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Command to execute the next action for all ladybugs in sequence.
 *
 * This command finds and executes the next action node for each ladybug
 * in order of their IDs. For each ladybug, it traverses the behavior tree
 * until it finds an action to execute, then displays the board state.
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
            throws CommandArgumentException, BoardException, LadybugNotFoundException, LadybugException {
        if (args.length != 1 || !"action".equals(args[0])) {
            throw new CommandArgumentException(getCommandName(), args, "Error, use next action");
        }

        requireLadybugs();

        List<Integer> ladybugIds = new ArrayList<>(getBoard().listLadybugsIds());
        Collections.sort(ladybugIds); // IDs deterministisch aufsteigend

        for (int ladybugId : ladybugIds) {
            TreeExecution execution = gameState.getExecutions().get(ladybugId);
            if (execution == null) {
                throw new CommandArgumentException(getCommandName(), args,
                        "Error, no tree loaded for ladybug " + ladybugId);
            }
            Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
            if (ladybug.isEmpty()) {
                continue;
            }

            execution.tick(getBoard(), ladybug.get());
            getBoard().getRenderer().print();
        }
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