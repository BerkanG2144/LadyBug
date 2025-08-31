package bt;

import model.Board;
import model.Ladybug;

import java.util.List;

public class ParallelNode extends BehaviorTreeNode implements CompositeNode {
    private final int requiredSuccesses;

    public ParallelNode(String id, int requiredSuccesses) {
        super(id);
        this.requiredSuccesses = requiredSuccesses;
    }

    @Override
    public void addChild(BehaviorTreeNode child) {
        children.add(child);
    }

    public List<BehaviorTreeNode> getChildren() {
        return children;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        int successCount = 0;
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board,ladybug);
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

    public int getRequiredSuccesses() {
        return requiredSuccesses;
    }
}
