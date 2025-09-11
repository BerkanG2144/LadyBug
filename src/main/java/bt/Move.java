package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that moves the ladybug one step forward
 * in its current direction if possible.
 * @author ujnaa
 * @version SS25
 */
public class Move implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.moveForward(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
