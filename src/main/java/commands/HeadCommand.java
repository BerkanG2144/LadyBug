package commands;

import bt.BehaviorTreeNode;
import bt.CompositeNode;
import engine.ExecuteState;
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
        validateArguments(args);
        requireLadybugs();

        int ladybugId = parseLadybugId(args[0]);
        Optional<Ladybug> ladybug = findLadybug(ladybugId);
        TreeExecution execution = getTreeExecution(ladybugId);
        var execState = execution.stateOf(ladybug.get());

        // Check exception rule: last executed action is the last child of its parent
        if (shouldApplyExceptionRule(execState)) {
            System.out.println(execState.getLastExecutedLeaf().getId());
            return;
        }

        // Normal rule: find next action to execute
        try {
            BehaviorTreeNode nextAction = execution.findNextActionNode(getBoard(), ladybug.get());
            if (nextAction != null) {
                System.out.println(nextAction.getId());
            } else {
                System.out.println(getRootNodeId(ladybugId));
            }
        } catch (LadybugException e) {
            System.out.println(getRootNodeId(ladybugId));
        }
    }
    private void validateArguments(String[] args) throws CommandArgumentException {
        if (args.length != 1) {
            throw new CommandArgumentException(getCommandName(), args, "Error, head <ladybug>");
        }
    }

    private int parseLadybugId(String arg) throws CommandArgumentException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), "Error, invalid ladybug ID");
        }
    }

    private Optional<Ladybug> findLadybug(int ladybugId) throws LadybugNotFoundException {
        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }
        return ladybug;
    }

    private TreeExecution getTreeExecution(int ladybugId) throws CommandArgumentException {
        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new CommandArgumentException(getCommandName(),
                    "Error, no tree loaded for ladybug " + ladybugId);
        }
        return execution;
    }

    private boolean shouldApplyExceptionRule(ExecuteState execState) {
        if (execState == null || execState.getLastExecutedLeaf() == null) {
            return false;
        }
        BehaviorTreeNode lastExecuted = execState.getLastExecutedLeaf();
        if (!(lastExecuted instanceof bt.LeafNode) || !((bt.LeafNode) lastExecuted).isAction()) {
            return false;
        }
        BehaviorTreeNode parent = findParent(execState.getRootNode(), lastExecuted);
        return parent instanceof CompositeNode && isLastChild(parent, lastExecuted);
    }

    private boolean isLastChild(BehaviorTreeNode parent, BehaviorTreeNode child) {
        List<BehaviorTreeNode> siblings = parent.getChildren();
        return !siblings.isEmpty() && siblings.get(siblings.size() - 1) == child;
    }

    private String getRootNodeId(int ladybugId) throws CommandArgumentException {
        BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
        if (tree == null) {
            throw new CommandArgumentException(getCommandName(),
                    "Error, no tree found for ladybug " + ladybugId);
        }
        return tree.getId();
    }

    private BehaviorTreeNode findParent(BehaviorTreeNode root, BehaviorTreeNode target) {
        if (root == null || target == null) {
            return null;
        }
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