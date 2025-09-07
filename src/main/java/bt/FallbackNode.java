package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.List;

/**
 * Behavior tree node of type "fallback".
 * It executes its child nodes in order until one succeeds.
 * <p>
 * Returns SUCCESS as soon as one child returns SUCCESS,
 * otherwise FAILURE if all children fail.
 *
 * @author ujnaa
 */
public class FallbackNode extends BehaviorTreeNode implements CompositeNode {
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
        children.add(child);
    }

    /**
     * Returns the list of children of this fallback node.
     *
     * @return list of child nodes
     */
    public List<BehaviorTreeNode> getChildren() {
        return children;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board, ladybug);
            if (childStatus == NodeStatus.SUCCESS) {
                return NodeStatus.SUCCESS;
            }
        }
        return NodeStatus.FAILURE;
    }

    @Override
    public String getType() {
        return "fallback";
    }

    @Override
    public void addChild(int index, BehaviorTreeNode child) {
        children.add(index, child);
    }
}
