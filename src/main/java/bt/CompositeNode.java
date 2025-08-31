package bt;

import java.util.List;

public interface CompositeNode {
    void addChild(BehaviorTreeNode child);
    List<BehaviorTreeNode> getChildren();
}
