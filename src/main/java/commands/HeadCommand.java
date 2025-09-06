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
 * Command to display the ID of the current node in a ladybug's behavior tree execution.
 *
 * This command shows which node the ladybug's behavior tree is currently pointing to.
 * If the tree has been executed and there's a last executed leaf, it shows that.
 * Otherwise, it shows the current node or falls back to the root node.
 *
 * Usage: head &lt;ladybug&gt;
 *
 * @author ujnaa
 */
public class HeadCommand extends AbstractCommand {

    /**
     * Creates a new HeadCommand.
     *
     * @param state the game state containing board and tree information
     */
    public HeadCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {
        if (args.length != 1) {
            throw new CommandArgumentException(getCommandName(), args, "Usage: head <ladybug>");
        }

        requireLadybugs(); // Prüft ob Board und Marienkäfer existieren

        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args, "Error: invalid ladybug ID");
        }

        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }

        // Prüfe ob TreeExecution für diesen Marienkäfer existiert
        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: no tree loaded for ladybug " + ladybugId);
        }

        var execState = execution.stateOf(ladybug.get());   // <-- public Getter in TreeExecution (siehe unten)
        if (execState != null && execState.getLastExecutedLeaf() != null) {
            System.out.println(execState.getLastExecutedLeaf().getId());
            return;
        }

        // Hole aktuellen Knoten (nutzt das erweiterte System)
        BehaviorTreeNode currentNode = execution.getCurrentNode(ladybug.get());

        if (currentNode == null) {
            // Fallback: verwende Root-Knoten
            BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
            if (tree == null) {
                throw new CommandArgumentException(getCommandName(), args,
                        "Error: no tree found for ladybug " + ladybugId);
            }
            currentNode = tree;
        }

        System.out.println(currentNode.getId());
    }

    @Override
    public String getCommandName() {
        return "head";
    }

    @Override
    public String getUsage() {
        return "head <ladybug>";
    }
}