package bt;

import model.Ladybug;
import model.Board;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all behavior tree nodes.
 * A node has an identifier and can contain child nodes.
 * Subclasses must implement the specific behavior of the node.
 *
 * @author ujnaa
 */
public abstract class BehaviorTreeNode {
    protected String id;
    protected List<BehaviorTreeNode> children;

    /**
     * Creates a new behavior tree node with the given identifier.
     *
     * @param id the identifier of this node
     */
    public BehaviorTreeNode(String id) {
        this.id = id;
        this.children = new ArrayList<>();
    }

    /**
     * Returns the identifier of this node.
     *
     * @return the identifier of the node
     */
    public String getId() {
        return id;
    }

    /**
     * Adds a child node to this node.
     *
     * @param child the child node to add
     */
    public void addChild(BehaviorTreeNode child) {
        children.add(child);
    }

    /**
     * Returns a copy of the list of this node's children.
     *
     * @return a list of child nodes
     */
    public List<BehaviorTreeNode> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * Executes this node's behavior for the given board and ladybug.
     *
     * @param board   the game board
     * @param ladybug the ladybug agent
     * @return the node's execution status (SUCCESS, FAILURE, RUNNING)
     */
    public abstract NodeStatus tick(Board board, Ladybug ladybug);

    /**
     * Returns the type of this node, e.g. "fallback" or "sequence".
     *
     * @return the node type as a string
     */
    public abstract String getType(); //"fallback" "sequence"
}
