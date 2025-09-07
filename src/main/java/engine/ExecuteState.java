package engine;

import bt.BehaviorTreeNode;
import bt.NodeStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the execution state of a behavior tree for a specific agent.
 *
 * This class tracks the current execution state including the current node,
 * cached node statuses, and progress tracking for parallel node execution.
 *
 * @author ujnaa
 */
public class ExecuteState {
    private BehaviorTreeNode currentNode;
    private final Map<String, NodeStatus> statusCache = new HashMap<>();
    private BehaviorTreeNode rootNode;
    private BehaviorTreeNode lastExecutedLeaf;

    private final Set<String> openCompositeEntries = new HashSet<>();


    /**
     * Returns the set of open composite entries.
     *
     * @return the set of open composite entries
     */
    public Set<String> getOpenCompositeEntries() {
        return openCompositeEntries;
    }

    /**
     * Returns the current node in the behavior tree execution.
     *
     * @return the current node
     */
    public BehaviorTreeNode getCurrentNode() {
        return currentNode;
    }

    /**
     * Sets the current node in the behavior tree execution.
     *
     * @param currentNode the node to set as current
     */
    public void setCurrentNode(BehaviorTreeNode currentNode) {
        this.currentNode = currentNode;
    }
    /**
     * Returns the root node of the behavior tree.
     *
     * @return the root node
     */
    public BehaviorTreeNode getRootNode() {
        return rootNode;
    }
    /**
     * Returns the status cache for node execution results.
     *
     * @return the status cache map
     */
    public Map<String, NodeStatus> getStatusCache() {
        return statusCache;
    }
    /**
     * Returns the last executed leaf node.
     *
     * @return the last executed leaf node
     */
    public BehaviorTreeNode getLastExecutedLeaf() {
        return lastExecutedLeaf;
    }
    /**
     * Sets the last executed leaf node.
     *
     * @param lastExecutedLeaf the leaf node to set as last executed
     */
    public void setLastExecutedLeaf(BehaviorTreeNode lastExecutedLeaf) {
        this.lastExecutedLeaf = lastExecutedLeaf;
    }
    /**
     * Sets the root node of the behavior tree.
     *
     * @param rootNode the root node to set
     */
    public void setRootNode(BehaviorTreeNode rootNode) {
        this.rootNode = rootNode;

        if (this.currentNode == null) {
            this.currentNode = rootNode;
        }
    }

    /**
     * Resets the execution state to its initial state.
     */
    public void reset() {
        openCompositeEntries.clear();
        statusCache.clear();
        currentNode = rootNode;
        lastExecutedLeaf = null;
    }

    /**
     * Finds a behavior tree node by its ID.
     *
     * @param nodeId the ID of the node to find
     * @return the node with the given ID, or null if not found
     */
    public BehaviorTreeNode findNodeById(String nodeId) {
        if (rootNode == null) {
            return null;
        }
        return findNodeByIdRecursive(rootNode, nodeId);
    }

    private BehaviorTreeNode findNodeByIdRecursive(BehaviorTreeNode node, String nodeId) {
        if (node.getId().equals(nodeId)) {
            return node;
        }

        for (BehaviorTreeNode child : node.getChildren()) {
            BehaviorTreeNode found = findNodeByIdRecursive(child, nodeId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

}