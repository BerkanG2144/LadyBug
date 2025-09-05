package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether there is a tree
 * directly in front of the ladybug.
 *
 * Returns SUCCESS if a tree is in front, otherwise FAILURE.
 *
 * @author ujnaa
 */
public class TreeFront implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.treeFront(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
