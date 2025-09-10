package engine;

import bt.*;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.List;

/**
 * Handles the traversal logic for different node types in the behavior tree.
 * This class encapsulates the algorithms for sequence, fallback, and parallel nodes.
 *
 * @author ujnaa
 */
public class NodeTraversalHandler {
    private final ExecutionStateManager stateManager;
    private final TreeExecutionLogger logger;

    /**
     * Creates a new NodeTraversalHandler.
     *
     * @param stateManager the state manager
     * @param logger       the execution logger
     */
    public NodeTraversalHandler(ExecutionStateManager stateManager, TreeExecutionLogger logger) {
        this.stateManager = stateManager;
        this.logger = logger;
    }

    /**
     * Processes a sequence node, executing children until one fails.
     *
     * @param seq          the sequence node
     * @param board        the game board
     * @param agent        the agent
     * @param actionFinder function to find next action in a child
     * @return the next action node or null
     * @throws LadybugException if execution fails
     */
    public BehaviorTreeNode processSequenceNode(
            SequenceNode seq,
            Board board,
            Ladybug agent,
            ActionFinder actionFinder) throws LadybugException {

        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = stateManager.getCachedStatus(agent, child.getId());

            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    // Sequence fails if any child fails
                    logger.logCompositeExit(agent, seq, NodeStatus.FAILURE);
                    stateManager.updateNodeStatus(agent, seq.getId(), NodeStatus.FAILURE);
                    stateManager.resolveCompositeEntry(agent, seq.getId());
                    return null;
                }
                continue; // Skip already successful children
            }

            // Log entry if composite
            if (child instanceof CompositeNode) {
                if (stateManager.recordCompositeEntry(agent, child.getId())) {
                    logger.logCompositeEntry(agent, child);
                }
            }

            // Find next action in child
            BehaviorTreeNode next = actionFinder.findNextAction(child, board, agent);
            if (next != null) {
                return next;
            }

            // Check child result
            NodeStatus childResult = stateManager.getCachedStatus(agent, child.getId());
            if (childResult == null && child instanceof CompositeNode) {
                return null; // Child composite not complete
            }

            if (childResult == NodeStatus.FAILURE) {
                logger.logCompositeExit(agent, seq, NodeStatus.FAILURE);
                stateManager.updateNodeStatus(agent, seq.getId(), NodeStatus.FAILURE);
                stateManager.resolveCompositeEntry(agent, seq.getId());
                return null;
            }
        }

        // All children succeeded
        logger.logCompositeExit(agent, seq, NodeStatus.SUCCESS);
        stateManager.updateNodeStatus(agent, seq.getId(), NodeStatus.SUCCESS);
        return null;
    }

    /**
     * Processes a fallback node, executing children until one succeeds.
     *
     * @param fb           the fallback node
     * @param board        the game board
     * @param agent        the agent
     * @param actionFinder function to find next action in a child
     * @return the next action node or null
     * @throws LadybugException if execution fails
     */
    public BehaviorTreeNode processFallbackNode(
            FallbackNode fb,
            Board board,
            Ladybug agent,
            ActionFinder actionFinder) throws LadybugException {

        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = stateManager.getCachedStatus(agent, child.getId());

            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    // Fallback succeeds if any child succeeds
                    logger.logCompositeExit(agent, fb, NodeStatus.SUCCESS);
                    stateManager.updateNodeStatus(agent, fb.getId(), NodeStatus.SUCCESS);
                    stateManager.resolveCompositeEntry(agent, fb.getId());
                    return null;
                }
                continue; // Skip already failed children
            }

            // Log entry if composite
            if (child instanceof CompositeNode) {
                if (stateManager.recordCompositeEntry(agent, child.getId())) {
                    logger.logCompositeEntry(agent, child);
                }
            }

            // Find next action in child
            BehaviorTreeNode next = actionFinder.findNextAction(child, board, agent);
            if (next != null) {
                return next;
            }

            // Check child result
            NodeStatus res = stateManager.getCachedStatus(agent, child.getId());
            if (res == null) {
                res = NodeStatus.FAILURE; // Default for uncached
            }

            if (res == NodeStatus.SUCCESS) {
                logger.logCompositeExit(agent, fb, NodeStatus.SUCCESS);
                stateManager.updateNodeStatus(agent, fb.getId(), NodeStatus.SUCCESS);
                stateManager.resolveCompositeEntry(agent, fb.getId());
                return null;
            }
        }

        // All children failed
        logger.logCompositeExit(agent, fb, NodeStatus.FAILURE);
        stateManager.updateNodeStatus(agent, fb.getId(), NodeStatus.FAILURE);
        return null;
    }

    /**
     * Processes a parallel node, executing all children.
     *
     * @param par          the parallel node
     * @param board        the game board
     * @param agent        the agent
     * @param actionFinder function to find next action in a child
     * @return the next action node or null
     * @throws LadybugException if execution fails
     */
    public BehaviorTreeNode processParallelNode(
            ParallelNode par,
            Board board,
            Ladybug agent,
            ActionFinder actionFinder) throws LadybugException {

        int successCount = 0;
        int failureCount = 0;
        List<BehaviorTreeNode> children = par.getChildren();
        int totalChildren = children.size();
        int requiredSuccesses = par.getRequiredSuccesses();

        // Execute all children
        for (BehaviorTreeNode child : children) {
            NodeStatus cached = stateManager.getCachedStatus(agent, child.getId());

            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    successCount++;
                } else if (cached == NodeStatus.FAILURE) {
                    failureCount++;
                }
                continue;
            }

            // Find and execute next action in child
            BehaviorTreeNode action = actionFinder.findNextAction(child, board, agent);
            if (action != null) {
                return action;
            }

            // Count child result
            NodeStatus childStatus = stateManager.getCachedStatus(agent, child.getId());
            if (childStatus == NodeStatus.SUCCESS) {
                successCount++;
            } else if (childStatus == NodeStatus.FAILURE) {
                failureCount++;
            }
        }

        // Determine parallel node result
        if (successCount >= requiredSuccesses) {
            logger.logCompositeExit(agent, par, NodeStatus.SUCCESS);
            stateManager.updateNodeStatus(agent, par.getId(), NodeStatus.SUCCESS);
            clearChildStatuses(par, agent);
            stateManager.resolveCompositeEntry(agent, par.getId());
        } else if (failureCount > (totalChildren - requiredSuccesses)) {
            logger.logCompositeExit(agent, par, NodeStatus.FAILURE);
            stateManager.updateNodeStatus(agent, par.getId(), NodeStatus.FAILURE);
            clearChildStatuses(par, agent);
            stateManager.resolveCompositeEntry(agent, par.getId());
        }

        return null;
    }

    /**
     * Clears cached statuses for all children of a node.
     */
    private void clearChildStatuses(BehaviorTreeNode parent, Ladybug agent) {
        ExecuteState state = stateManager.getOrCreateState(agent);
        for (BehaviorTreeNode child : parent.getChildren()) {
            state.getStatusCache().remove(child.getId());
            state.getOpenCompositeEntries().remove(child.getId());
            if (child instanceof CompositeNode) {
                clearChildStatuses(child, agent);
            }
        }
    }

    /**
     * Functional interface for finding the next action in a node.
     */
    @FunctionalInterface
    public interface ActionFinder {
        /**
         * Functional interface for finding the next action in a node.
         *
         * @param agent
         * @param node
         * @param board
         * @return no
         * @throws LadybugException
         */
        BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent)
                throws LadybugException;
    }
}