package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that removes a leaf from the field
 * directly in front of the ladybug.
 *
 * Returns SUCCESS if a leaf was taken successfully,
 * otherwise FAILURE.
 *
 * @author ujnaa
 */
public class TakeLeaf implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.takeLeaf(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
