package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Behavior tree node of type "parallel".
 * Executes all its child nodes and counts how many succeed.
 * @author ujnaa
 * @version SS25
 */
public class ParallelNode extends BehaviorTreeNode implements CompositeNode {
    private static final String PARALLEL_NODE_TYPE = "parallel";
    private final int requiredSuccesses;

    /**
     * Creates a parallel node with the given identifier and success threshold.
     * @param id the unique identifier of this node (non-null)
     * @param requiredSuccesses the minimum number of child successes required; must be {@code > 0}
     * @throws IllegalArgumentException if {@code requiredSuccesses <= 0}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public ParallelNode(String id, int requiredSuccesses) {
        super(id);
        if (requiredSuccesses <= 0) {
            throw new IllegalArgumentException("requiredSuccesses must be > 0");
        }
        this.requiredSuccesses = requiredSuccesses;
    }

    @Override
    public void addChild(BehaviorTreeNode child) {
        super.addChild(child);
    }

    @Override
    public void addChild(int index, BehaviorTreeNode child) {
        insertChild(index, child);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        int successCount = 0;
        for (BehaviorTreeNode child : childrenView()) {
            if (child.tick(board, ladybug) == NodeStatus.SUCCESS) {
                successCount++;
            }
        }
        return (successCount >= requiredSuccesses) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

    @Override
    public String getType() {
        return PARALLEL_NODE_TYPE;
    }

    /**
     * Required success for the parallel node.
     * @return the success that is required
     */
    public int getRequiredSuccesses() {
        return requiredSuccesses;
    }
}
