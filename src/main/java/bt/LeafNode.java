package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Behavior tree node representing a leaf.
 * A leaf node wraps a {@link NodeBehavior} and can either
 * be an action or a condition, defined by {@link LeafKind}.
 *
 * Returns SUCCESS or FAILURE depending on the wrapped behavior.
 *
 * @author ujnaa
 */
public class LeafNode extends BehaviorTreeNode {

    /**
     * Defines the type of leaf node: either an action or a condition.
     */
    public enum LeafKind { ACTION, CONDITION }

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

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        return behavior.tick(board, ladybug);
    }

    @Override
    public String getType() {
        return "leaf";
    }

    /**
     * Returns the kind of this leaf (ACTION or CONDITION).
     *
     * @return the leaf kind
     */
    public LeafKind getKind() {
        return kind;
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
