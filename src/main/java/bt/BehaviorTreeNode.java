package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all behavior tree nodes.
 *
 * @author ujnaa
 * @version SS25
 */
public abstract class BehaviorTreeNode {
    private static final String CHILD = "child";
    private final String id;
    private final List<BehaviorTreeNode> children;

    /**
     * Creates a new behavior tree node with the given identifier.
     *
     * @param id the identifier of this node
     */
    protected BehaviorTreeNode(String id) {
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
     * Returns a copy of the list of this node's children.
     *
     * @return a list of child nodes
     */
    public List<BehaviorTreeNode> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * Adds a child node to this node.
     *
     * @param child the child node to add
     */
    public void addChild(BehaviorTreeNode child) {
        children.add(Objects.requireNonNull(child, CHILD));
    }

    /**
     * Index based insertion for subclasses.
     * @param index insert index
     * @param child the child
     */
    protected void insertChild(int index, BehaviorTreeNode child) {
        children.add(index, Objects.requireNonNull(child, CHILD));
    }

    /**
     * Unmodifiable class for iteration.
     * @return children
     * */
    protected List<BehaviorTreeNode> childrenView() {
        return Collections.unmodifiableList(children);
    }
    /**
     * Executes this node's behavior for the given board and ladybug.
     *
     * @param board   the game board
     * @param ladybug the ladybug agent
     * @return the node's execution status (SUCCESS, FAILURE, RUNNING)
     * @throws LadybugException if the ladybug is in an invalid state (e.g. null position/direction)
     *                          or if the action defined by this node cannot be performed
     */
    public abstract NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException;

    /**
     * Returns the type of this node, e.g. "fallback" or "sequence".
     *
     * @return the node type as a string
     */
    public abstract String getType();
}
