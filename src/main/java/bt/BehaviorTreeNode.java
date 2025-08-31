package bt;

import model.Ladybug;
import model.Board;

import java.util.ArrayList;
import java.util.List;

public abstract class BehaviorTreeNode {
    protected String id;
    protected List<BehaviorTreeNode> children;

    public BehaviorTreeNode(String id) {
        this.id = id;
        this.children = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void addChild(BehaviorTreeNode child) {
        children.add(child);
    }

    public List<BehaviorTreeNode> getChildren() {
        return new ArrayList<>(children);
    }

    public abstract NodeStatus tick(Board board, Ladybug ladybug);
    public abstract String getType(); //"fallback" "sequence"
}
