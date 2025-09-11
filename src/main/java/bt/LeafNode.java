package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Behavior tree node representing a leaf.
 * @author ujnaa
 * @version SS25
 */
public class LeafNode extends BehaviorTreeNode {
    private static final String LEAF_NODE_TYPE = "leaf";
    private final NodeBehavior behavior;
    private final LeafKind kind;

    /**
     * Constructs a new leaf node with the given identifier,
     * behavior, and kind.
     *
     * @param id       the identifier of the node
     * @param behavior the behavior this leaf wraps (must not be null)
     * @param kind     the type of leaf, either ACTION or CONDITION (must not be null)
     * @throws IllegalArgumentException if behavior or kind is null
     */
    public LeafNode(String id, NodeBehavior behavior, LeafKind kind) {
        super(id);
        if (behavior == null) {
            throw new IllegalArgumentException("behavior must not be null");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        this.behavior = behavior;
        this.kind = kind;
    }

    /**
     * Returns the output format for logging.
     *
     * @return the correct log
     */
    public String getLogArgsOrEmpty() {
        if (behavior instanceof LogArgsProvider provider) {
            String logName = provider.logArgs();
            return (logName != null && !logName.isEmpty()) ? " " + logName : "";
        }
        return "";
    }
    /**
     * Returns the output format for logging the name.
     *
     * @return the correct log
     */
    public String getLogNameOrDefault() {
        if (behavior instanceof LogNameProvider provider) {
            String logName = provider.logName();
            if (logName != null && !logName.isEmpty()) {
                return logName;
            }
        }
        String className = behavior.getClass().getSimpleName();
        return className.isEmpty() ? className
                : Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        return behavior.tick(board, ladybug);
    }

    @Override
    public String getType() {
        return LEAF_NODE_TYPE;
    }

    /**
     * Returns the wrapped behavior of this leaf.
     *
     * @return the node behavior
     */
    public NodeBehavior getBehavior() {
        return behavior;
    }

    /**
     * Checks if this leaf is an action.
     *
     * @return true if this leaf is an action, false otherwise
     */
    public boolean isAction() {
        return kind == LeafKind.ACTION;
    }

    /**
     * Checks if this leaf is a condition.
     *
     * @return true if this leaf is a condition, false otherwise
     */
    public boolean isCondition() {
        return kind == LeafKind.CONDITION;
    }
}
