package bt;

import java.util.List;

/**
 * Interface for composite behavior tree nodes that can
 * contain and manage child nodes.
 *
 * Provides methods to add and retrieve children.
 *
 * @author ujnaa
 * @version SS25
 */
public interface CompositeNode {
    /**
     * Adds a child node to this composite node.
     *
     * @param child the child node to add
     */
    void addChild(BehaviorTreeNode child);

    /**
     * Returns the list of child nodes contained in this composite node.
     *
     * @return list of child nodes
     */
    List<BehaviorTreeNode> getChildren();

    /**
     * Adds a child node at a specific index in this composite node.
     * <p>
     * By default, this operation is not supported and throws
     * {@code UnsupportedOperationException}.
     *
     * @param index the position at which to insert the child
     * @param child the child node to insert
     * @throws UnsupportedOperationException for unsupported position
     */
    default void addChild(int index, BehaviorTreeNode child) {
        throw new UnsupportedOperationException("Error, Insert at position not supported");
    }
}
