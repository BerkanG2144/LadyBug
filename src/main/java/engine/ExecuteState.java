package engine;

import bt.BehaviorTreeNode;
import bt.NodeStatus;

import java.util.HashMap;
import java.util.Map;

public class ExecuteState {
    private BehaviorTreeNode currentNode;
    private final Map<String, NodeStatus> statusCache = new HashMap<>();

    public BehaviorTreeNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(BehaviorTreeNode currentNode) {
        this.currentNode = currentNode;
    }

    public Map<String, NodeStatus> getStatusCache() {
        return statusCache;
    }

    public void reset() {
        currentNode = null;
        statusCache.clear();
    }
}
