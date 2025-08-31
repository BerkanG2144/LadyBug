package bt;

import model.Board;
import model.Ladybug;

import java.util.List;

public class FallbackNode extends BehaviorTreeNode implements CompositeNode{
    public FallbackNode(String id) {
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
