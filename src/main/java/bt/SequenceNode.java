package bt;

import model.Board;
import model.Ladybug;

public class SequenceNode extends BehaviorTreeNode {
    public SequenceNode(String id) {
        super(id);
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        for (BehaviorTreeNode child : children) {
            NodeStatus childStatus = child.tick(board,ladybug);
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
}
