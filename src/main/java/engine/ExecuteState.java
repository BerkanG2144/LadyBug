package engine;

import bt.BehaviorTreeNode;
import bt.NodeStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExecuteState {
    private BehaviorTreeNode currentNode;
    private final Map<String, NodeStatus> statusCache = new HashMap<>();
    private BehaviorTreeNode rootNode;

    private final Map<String, Set<String>> parallelNodeProgress = new HashMap<>();

    private final Map<String, Integer> parallelCurrentIndex = new HashMap<>();


    public BehaviorTreeNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(BehaviorTreeNode currentNode) {
        this.currentNode = currentNode;
    }

    public BehaviorTreeNode getRootNode() {
        return rootNode;
    }

    public Map<String, NodeStatus> getStatusCache() {
        return statusCache;
    }

    public void setRootNode(BehaviorTreeNode rootNode) {
        this.rootNode = rootNode;

        if (this.currentNode == null) {
            this.currentNode = rootNode;
        }
    }

    public void reset() {
        currentNode = null;
        statusCache.clear();
    }

    public BehaviorTreeNode findNodeById(String nodeId) {
        if (rootNode == null) {
            return null;
        }
        return findNodeByIdRecursive(rootNode, nodeId);
    }

    public Set<String> getExecutedChildrenForParallel(String parallelNodeId) {
        return parallelNodeProgress.computeIfAbsent(parallelNodeId, k -> new HashSet<>());
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