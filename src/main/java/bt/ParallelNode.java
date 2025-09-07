package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.List;

/**
 * Behavior tree node of type "parallel".
 * Executes all its child nodes and counts how many succeed.
 * <p>
 * Returns SUCCESS if the number of successful children is at least
 * the configured threshold, otherwise FAILURE.
 *
 * @author ujnaa
 */
public class ParallelNode extends BehaviorTreeNode implements CompositeNode {
    private final int requiredSuccesses;

    /**
     * Constructs a parallel node with the given identifier and
     * required number of successes.
     *
     * @param id                the identifier of this node
     * @param requiredSuccesses the minimum number of children that must succeed
     */
    public ParallelNode(String id, int requiredSuccesses) {
        super(id);
        this.requiredSuccesses = requiredSuccesses;
    }

    @Override
    public void addChild(BehaviorTreeNode child) {
        children.add(child);
    }

    /**
     * Returns the list of child nodes.
     *
     * @return list of child nodes
     */
    public List<BehaviorTreeNode> getChildren() {
        return children;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        int successCount = 0;
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board, ladybug);
            if (childStatus == NodeStatus.SUCCESS) {
                successCount++;
            }
        }
        return (successCount >= requiredSuccesses) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

    @Override
    public String getType() {
        return "parallel";
    }

    @Override
    public void addChild(int index, BehaviorTreeNode child) {
        children.add(index, child);
    }

    /**
     * Returns the required number of child successes.
     *
     * @return the success threshold
     */
    public int getRequiredSuccesses() {
        return requiredSuccesses;
    }
}
