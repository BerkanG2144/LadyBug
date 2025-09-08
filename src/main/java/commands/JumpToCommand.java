package commands;

import bt.BehaviorTreeNode;
import engine.TreeExecution;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import main.GameState;
import model.Ladybug;

import java.util.Optional;

/**
 * Command to manually set the current node for a ladybug in its behavior tree.
 *
 * This command allows jumping to a specific node in the behavior tree,
 * handling skipped siblings according to the parent node type.
 *
 * Usage: jump to &lt;ladybug&gt; &lt;id&gt;
 *
 * @author ujnaa
 */
public class JumpToCommand extends AbstractCommand {

    /**
     * Creates a new JumpToCommand.
     *
     * @param state the game state containing board and tree information
     */
    public JumpToCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {

        if (args.length != 3 || !"to".equals(args[0])) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, jump to <ladybug> <id>");
        }

        requireLadybugs();

        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, invalid ladybug ID");
        }

        String nodeId = args[2];

        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }

        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, no tree loaded for ladybug " + ladybugId);
        }

        boolean success = execution.jumpTo(ladybug.get(), nodeId);
        if (!success) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, node " + nodeId + " not found in tree");
        }
    }

    @Override
    public String getCommandName() {
        return "jump";
    }

    @Override
    public String getUsage() {
        return "jump to <ladybug> <id>";
    }
}