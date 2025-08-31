package bt;

import model.Board;
import model.Ladybug;

import java.util.ArrayList;
import java.util.List;

public class SequenceNode extends BehaviorTreeNode implements CompositeNode {
    private final List<BehaviorTreeNode> children = new ArrayList<>();

    public SequenceNode(String id) {
        super(id);
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
