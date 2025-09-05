package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that moves the ladybug one step forward
 * in its current direction if possible.
 *
 * Returns SUCCESS if the move is valid, otherwise FAILURE.
 *
 * @author ujnaa
 */
public class Move implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.moveForward(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
