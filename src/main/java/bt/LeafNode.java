package bt;

import model.Board;
import model.Ladybug;

public class LeafNode extends BehaviorTreeNode{

    public enum LeafKind {ACTION, CONDITION}

    private final NodeBehavior behavior;
    private final LeafKind kind;

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
    public NodeStatus tick(Board board, Ladybug ladybug) {
        return behavior.tick(board, ladybug);
    }

    @Override
    public String getType() {
        return "leaf";
    }

    public LeafKind getKind() {
        return kind;
    }

    public NodeBehavior getBehavior() {
        return behavior;
    }

}
