package commands;

import bt.BehaviorTreeNode;
import bt.CompositeNode;
import engine.TreeExecution;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugException;
import exceptions.LadybugNotFoundException;
import main.GameState;
import model.Ladybug;

import java.util.List;
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
            throws BoardException, LadybugNotFoundException, CommandArgumentException, LadybugException {
        if (args.length != 1) {
            throw new CommandArgumentException(getCommandName(), args, "Error, head <ladybug>");
        }
        requireLadybugs();
        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args, "Error, invalid ladybug ID");
        }
        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }
        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, no tree loaded for ladybug " + ladybugId);
        }
        var execState = execution.stateOf(ladybug.get());
        if (execState != null && execState.getLastExecutedLeaf() != null) {
            BehaviorTreeNode lastExecuted = execState.getLastExecutedLeaf();
            if (lastExecuted instanceof bt.LeafNode) {
                bt.LeafNode leafNode = (bt.LeafNode) lastExecuted;
                if (leafNode.isAction()) {
                    BehaviorTreeNode parent = findParent(execState.getRootNode(), lastExecuted);
                    if (parent != null && parent instanceof CompositeNode) {
                        List<BehaviorTreeNode> siblings = parent.getChildren();
                        if (!siblings.isEmpty() && siblings.get(siblings.size() - 1) == lastExecuted) {
                            System.out.println(lastExecuted.getId());
                            return;
                        }
                    }
                }
            }
        }
        try {
            BehaviorTreeNode nextAction = execution.findNextActionNode(getBoard(), ladybug.get());
            if (nextAction != null) {
                System.out.println(nextAction.getId());
            } else {
                // Fallback: Wenn kein nächster Action gefunden wurde, verwende Root
                BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
                if (tree == null) {
                    throw new CommandArgumentException(getCommandName(), args,
                            "Error, no tree found for ladybug " + ladybugId);
                }
                System.out.println(tree.getId());
            }
        } catch (LadybugException e) {
            // Bei Fehlern: verwende Root als Fallback
            BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
            if (tree == null) {
                throw new CommandArgumentException(getCommandName(), args,
                        "Error, no tree found for ladybug " + ladybugId);
            }
            System.out.println(tree.getId());
        }
    }
    /**
     * Hilfsmethode: Finde den Elternknoten eines gegebenen Knotens
     */
    private BehaviorTreeNode findParent(BehaviorTreeNode root, BehaviorTreeNode target) {
        for (BehaviorTreeNode child : root.getChildren()) {
            if (child == target) {
                return root;
            }
            BehaviorTreeNode parent = findParent(child, target);
            if (parent != null) {
                return parent;
            }
        }
        return null;
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