package bt;

/**
 * Represents the possible outcomes of executing a behavior tree node.
 *
 * SUCCESS  – the node executed successfully.
 * FAILURE  – the node failed to execute successfully.
 *
 * @author ujnaa
 * @version SS25
 */
public enum NodeStatus {
    /**
     * Indicates that the node executed successfully.
     */
    SUCCESS,

    /**
     * Indicates that the node failed to execute.
     */
    FAILURE;
}
