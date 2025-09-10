package engine;

import bt.BehaviorTreeNode;
import bt.NodeStatus;
import model.Ladybug;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages execution states for multiple ladybug agents.
 * This class handles state creation, retrieval, and lifecycle management.
 *
 * @author ujnaa
 */
public class ExecutionStateManager {
    private final Map<Ladybug, ExecuteState> states = new HashMap<>();
    private final BehaviorTreeNode root;

    /**
     * Creates a new ExecutionStateManager for the given behavior tree.
     *
     * @param root the root node of the behavior tree (must not be null)
     * @throws NullPointerException if root is null
     */
    public ExecutionStateManager(BehaviorTreeNode root) {
        this.root = Objects.requireNonNull(root, "Root node cannot be null");
    }

    /**
     * Gets or creates the execution state for an agent.
     *
     * @param agent the agent whose state to obtain
     * @return the ExecuteState for the agent
     */
    public ExecuteState getOrCreateState(Ladybug agent) {
        ExecuteState state = states.computeIfAbsent(agent, k -> new ExecuteState());

        if (state.getRootNode() == null) {
            state.setRootNode(root);
        }
        return state;
    }

    /**
     * Resets the execution state for an agent.
     *
     * @param agent the agent whose state to reset
     */
    public void resetState(Ladybug agent) {
        ExecuteState state = getOrCreateState(agent);
        state.reset();
    }

    /**
     * Updates the status cache for a node.
     *
     * @param agent the agent
     * @param nodeId the node ID
     * @param status the node status
     */
    public void updateNodeStatus(Ladybug agent, String nodeId, NodeStatus status) {
        ExecuteState state = getOrCreateState(agent);
        state.getStatusCache().put(nodeId, status);
    }

    /**
     * Gets the cached status for a node.
     *
     * @param agent the agent
     * @param nodeId the node ID
     * @return the cached status or null if not cached
     */
    public NodeStatus getCachedStatus(Ladybug agent, String nodeId) {
        ExecuteState state = getOrCreateState(agent);
        return state.getStatusCache().get(nodeId);
    }

    /**
     * Clears the status cache for an agent.
     *
     * @param agent the agent whose cache to clear
     */
    public void clearStatusCache(Ladybug agent) {
        ExecuteState state = getOrCreateState(agent);
        state.getStatusCache().clear();
    }

    /**
     * Records that a composite node has been entered.
     *
     * @param agent the agent
     * @param nodeId the composite node ID
     * @return true if this is the first entry, false if already entered
     */
    public boolean recordCompositeEntry(Ladybug agent, String nodeId) {
        ExecuteState state = getOrCreateState(agent);
        return state.getOpenCompositeEntries().add(nodeId);
    }

    /**
     * Removes a composite node from the open entries.
     *
     * @param agent the agent
     * @param nodeId the composite node ID
     */
    public void resolveCompositeEntry(Ladybug agent, String nodeId) {
        ExecuteState state = getOrCreateState(agent);
        state.getOpenCompositeEntries().remove(nodeId);
    }

    /**
     * Gets the root node of the behavior tree.
     *
     * @return the root node
     */
    public BehaviorTreeNode getRoot() {
        return root;
    }
}