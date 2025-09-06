package exceptions;

/**
 * Exception thrown when a behavior tree node cannot be found.
 * @author u-Kürzel
 */
public class NodeNotFoundException extends BehaviorTreeException {

    private final String nodeId;

    /**
     * Constructs a new node not found exception.
     * @param nodeId the ID of the node that was not found
     */
    public NodeNotFoundException(String nodeId) {
        super(String.format("Node with ID '%s' not found", nodeId));
        this.nodeId = nodeId;
    }

    /**
     * Returns the ID of the node that was not found.
     * @return the node ID
     */
    public String getNodeId() {
        return nodeId;
    }
}