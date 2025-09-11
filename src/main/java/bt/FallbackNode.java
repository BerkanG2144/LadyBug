package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;


/**
 * Behavior tree node of type "fallback".
 *
 * @author ujnaa
 * @version SS25
 */
public class FallbackNode extends BehaviorTreeNode implements CompositeNode {
    private static final String FALLBACK_DESCRIPTION = "fallback";
    /**
     * Constructs a fallback node with the given identifier.
     *
     * @param id the identifier of this node
     */
    public FallbackNode(String id) {
        super(id);
    }

    @Override
    public void addChild(BehaviorTreeNode child) {
        super.addChild(child);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        for (BehaviorTreeNode child : childrenView()) {
            NodeStatus childStatus = child.tick(board, ladybug);
            if (childStatus == NodeStatus.SUCCESS) {
                return NodeStatus.SUCCESS;
            }
        }
        return NodeStatus.FAILURE;
    }

    @Override
    public String getType() {
        return FALLBACK_DESCRIPTION;
    }

    @Override
    public void addChild(int index, BehaviorTreeNode child) {
        insertChild(index, child);
    }
}
