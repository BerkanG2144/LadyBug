package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether there is a leaf
 * directly in front of the ladybug.
 *
 * Returns SUCCESS if a leaf is in front, otherwise FAILURE.
 *
 * @author ujnaa
 * @version SS25
 */
public class LeafFront implements NodeBehavior {

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.leafFront(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
