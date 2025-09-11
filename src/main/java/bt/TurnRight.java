package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that turns the ladybug
 * one step to the right.
 *
 * Always returns SUCCESS.
 *
 * @author ujnaa
 * @version SS25
 */
public class TurnRight implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.turnRight(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
