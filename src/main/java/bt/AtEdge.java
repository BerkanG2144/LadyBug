package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether a ladybug
 * is located at the edge of the board.
 *
 * Returns SUCCESS if the ladybug is on the edge,
 * otherwise FAILURE.
 *
 * @author ujnaa
 */
public class AtEdge implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.atEdge(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
