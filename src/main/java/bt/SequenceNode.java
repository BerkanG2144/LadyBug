package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node of type "sequence".
 * Executes its child nodes in order until one fails.
 * <p>
 * Returns FAILURE as soon as one child returns FAILURE,
 * otherwise SUCCESS if all children succeed.
 *
 * @author ujnaa
 */
public class SequenceNode extends BehaviorTreeNode implements CompositeNode {

    /**
     * Constructs a sequence node with the given identifier.
     *
     * @param id the identifier of this node
     */
    public SequenceNode(String id) {
        super(id);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board, ladybug);
            if (childStatus == NodeStatus.FAILURE) {
                return NodeStatus.FAILURE;
            }
        }
        return NodeStatus.SUCCESS;
    }

    @Override
    public String getType() {
        return "sequence";
    }

    @Override
    public void addChild(int index, BehaviorTreeNode child) {
        children.add(index, child);
    }
}
