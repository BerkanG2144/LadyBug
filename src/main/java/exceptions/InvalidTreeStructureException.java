package exceptions;

/**
 * Exception thrown when a behavior tree has an invalid structure.
 * @author ujnaa
 */
public class InvalidTreeStructureException extends BehaviorTreeException {

    private final String nodeId;
    private final String reason;

    /**
     * Constructs a new invalid tree structure exception.
     * @param nodeId the ID of the problematic node
     * @param reason the reason why the structure is invalid
     */
    public InvalidTreeStructureException(String nodeId, String reason) {
        super(String.format("Invalid tree structure at node '%s': %s", nodeId, reason));
        this.nodeId = nodeId;
        this.reason = reason;
    }

    /**
     * Returns the ID of the problematic node.
     * @return the node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Returns the reason why the structure is invalid.
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
}