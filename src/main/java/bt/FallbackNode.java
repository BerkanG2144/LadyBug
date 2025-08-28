package bt;

import model.Board;
import model.Ladybug;

public class FallbackNode extends BehaviorTreeNode {
    public FallbackNode(String id) {
        super(id);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board,ladybug);
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
}
