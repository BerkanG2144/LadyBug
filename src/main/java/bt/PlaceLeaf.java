package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that places a leaf on the field
 * directly in front of the ladybug.
 * Returns SUCCESS if the leaf was placed successfully,
 * otherwise FAILURE.
 *
 * @author ujnaa
 * @version SS25
 *
 */
public class PlaceLeaf implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.placeLeaf(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
